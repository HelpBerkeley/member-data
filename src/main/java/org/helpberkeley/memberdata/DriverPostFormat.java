/*
 * Copyright (c) 2020. helpberkeley.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package org.helpberkeley.memberdata;

import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverPostFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormat.class);

    private final ApiClient apiClient;
    private final List<Section> driverPostSections = new ArrayList<>();
    private final List<Section> groupInstructionSections = new ArrayList<>();
    private List<Driver> drivers;
    private final Pattern compositeVariableRE;
    private Map<String, Restaurant> restaurants;

    DriverPostFormat(ApiClient apiClient, final String routedDeliveries)
            throws IOException, InterruptedException, CsvValidationException {
        this.apiClient = apiClient;
        compositeVariableRE = Pattern.compile("\\$\\{[A-Z_]+\\.[A-Z_]+\\}");
        loadRestaurantTemplate();
        loadDriverPostFormat();
        loadGroupPostFormat();
        loadRoutedDeliveries(routedDeliveries);
        processSplitRestaurants();
    }

    List<Driver> getDrivers() {
        return drivers;
    }

    Map<String, Restaurant> getRestaurants() {
        return restaurants;
    }

    List<String> generateDriverPosts() {

        List<String> driverPosts = new ArrayList<>();
        for (Driver driver : drivers) {
            StringBuilder post = new StringBuilder();

            for (Section section : driverPostSections) {

                // FIX THIS, DS: remove when handled
                if (section.name.equals("Split Restaurant")) {
                    post.append(generateSplitRestaurants(section, driver));
                    continue;
                }

                if (section.hasConditional) {
                    if (! evaluateCondition(driver, section.conditionalVariableName)) {
                        continue;
                    }
                }

                for (String line : section.contents.split("\n", -1)) {
                    Matcher matcher = compositeVariableRE.matcher(line);

                    if (matcher.find()) {
                        post.append(processCompositeLine(driver, line));
                    } else {
                        post.append(processLine(driver, line));
                    }
                }
            }

            driverPosts.add(post.toString());
        }

        return driverPosts;
    }

    String generateGroupInstructionsPost() {

        StringBuilder post = new StringBuilder();

        for (Section section : groupInstructionSections) {
            if (section.name.equals("thread title for reply")) {
                // FIX THIS, DS: implement
            }
            // FIX THIS, DS: need to extend macro language to handle this section.
            //               currently hardwired here.
            else if (section.name.equals("header")) {
                post.append(generateGroupInstructionHeader());
            } else {
                post.append(section.contents);
            }
        }

        return post.toString();
    }

    private String generateGroupInstructionHeader() {

        StringBuilder header = new StringBuilder();

        header.append("Hi all!\n");
        header.append("\n");

        for (Driver driver : drivers) {
            String firstRestaurant = driver.getFirstRestaurantName();
            Restaurant restaurant = restaurants.get(firstRestaurant);
            assert restaurant != null : firstRestaurant + " was not found the in restaurant template post";
            String startTime = restaurant.getStartTime();

            header.append("**@");
            header.append(driver.getUserName());
            header.append( " your run starts at ");
            header.append(firstRestaurant);
            header.append(" at ");
            header.append(startTime);
            header.append("**\n");
        }

        return header.toString();
    }

    private void loadRestaurantTemplate() throws IOException, InterruptedException {
        String rawPost = Parser.postBody(apiClient.getPost(Main.RESTAURANT_TEMPLATE_POST_ID));
        RestaurantTemplatePost restaurantTemplatePost = Parser.restaurantTemplatePost(rawPost);
        String restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.fileName);
        RestaurantTemplateParser parser = new RestaurantTemplateParser(restaurantTemplate);
        restaurants = parser.restaurants();
    }

    private void loadDriverPostFormat() throws IOException, InterruptedException {
        String json = apiClient.runQuery(Constants.QUERY_GET_DRIVERS_POST_FORMAT);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 2 : columns.length;

            // Normalize EOL
            String raw = ((String)columns[1]).replaceAll("\\r\\n?", "\n");

            if (raw.startsWith("Control post")) {
                continue;
            }

            // Look for section name of the form:
            // [This is the section name]\n
            String pattern = "^\\[([A-Za-z0-9 ]+)\\]";

            Pattern re = Pattern.compile(pattern);
            Matcher match = re.matcher(raw);

            if (! match.find()) {
                LOGGER.warn("Cannot find section name in {}", raw);
                continue;
            }

            String sectionName = match.group(1);
            String sectionContents = raw.substring(match.end());
            driverPostSections.add(new Section(raw, sectionName, sectionContents));
        }
    }

    private void loadGroupPostFormat() throws IOException, InterruptedException {
        String json = apiClient.runQuery(Constants.QUERY_GET_GROUP_INSTRUCTIONS_FORMAT);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 2 : columns.length;

            // Normalize EOL
            String raw = ((String)columns[1]).replaceAll("\\r\\n?", "\n");

            // Look for section name of the form:
            // [This is the section name]\n
            String pattern = "^\\[([A-Za-z0-9 ]+)\\]";

            Pattern re = Pattern.compile(pattern);
            Matcher match = re.matcher(raw);

            if (! match.find()) {
                LOGGER.warn("Cannot find section name in {}", raw);
                continue;
            }

            String sectionName = match.group(1);
            String sectionContents = raw.substring(match.end());
            groupInstructionSections.add(new Section(raw, sectionName, sectionContents));
        }
    }

    private void loadRoutedDeliveries(final String routedDeliveries) throws IOException, CsvValidationException {
        RoutedDeliveriesParser parser = new RoutedDeliveriesParser(routedDeliveries);
        drivers = parser.drivers();
    }

    private boolean evaluateCondition(final Driver driver, final String variableName) {
        if (variableName.equals("ANY_CONDO")) {
            return driver.hasCondo();
        }

        throw new Error("Unsupported conditional " + variableName);
    }

    private String processLine(final Driver driver, final String line) {

        final String firstRestaurant = driver.getFirstRestaurantName();
        final Restaurant restaurant = restaurants.get(firstRestaurant);
        assert restaurant != null : firstRestaurant + " was not found the in restaurant template post";
        final String startTime = restaurant.getStartTime();

        return line.replaceAll("\\$\\{DRIVER\\}", driver.getUserName())
            .replaceAll("\\$\\{FIRST_RESTAURANT\\}", firstRestaurant)
            .replaceAll("\\$\\{RESTAURANT_START_TIME\\}", startTime)
            .replaceAll("\\$\\{GMAP_URL\\}", driver.getgMapURL())
            + '\n';
    }

    private String processCompositeLine(final Driver driver, final String line) {

        String processedLine;

        if (line.contains("${C.")) {
            processedLine = processConsumerLine(driver, line);
        } else if (line.contains("${RESTAURANT.")) {
            processedLine = processRestaurantLine(driver, line);
        } else {
            throw new Error("Unrecognized composite variable in " + line);
        }

        return processedLine;
    }

    private String processConsumerLine(final Driver driver, final String line) {
        String processedLine = "";

        for (Delivery delivery : driver.getDeliveries()) {

            processedLine += line.replaceAll("\\$\\{C.NAME\\}", delivery.getName())
                .replaceAll("\\$\\{C.USER_NAME\\}", delivery.getUserName())
                .replaceAll("\\$\\{C.PHONE\\}", delivery.getPhone())
                .replaceAll("\\$\\{C.ALT_PHONE\\}", delivery.getAltPhone())
                .replaceAll("\\$\\{C.CITY\\}", delivery.getCity())
                .replaceAll("\\$\\{C.ADDRESS\\}", delivery.getAddress())
                .replaceAll("\\$\\{C.CONDO\\}", String.valueOf(delivery.isCondo()))
                .replaceAll("\\$\\{C.DETAILS\\}", delivery.getDetails())
                .replaceAll("\\$\\{C.RESTAURANT\\}", delivery.getRestaurant())
                .replaceAll("\\$\\{C.NORMAL\\}", delivery.getNormalRations())
                .replaceAll("\\$\\{C.VEGGIE\\}", delivery.getVeggieRations())
                + '\n';
        }

        return processedLine;
    }

    private String processRestaurantLine(final Driver driver, final String line) {
        StringBuilder processedLine = new StringBuilder();

        for (Restaurant restaurant : driver.getPickups()) {
            processedLine.append(
                line.replaceAll("\\$\\{RESTAURANT.NAME\\}", restaurant.getName())
                .replaceAll("\\$\\{RESTAURANT.ADDRESS\\}", restaurant.getAddress())
                .replaceAll("\\$\\{RESTAURANT.DETAILS\\}", restaurant.getDetails())
                .replaceAll("\\$\\{RESTAURANT.ORDERS\\}",
                String.valueOf(restaurant.getOrders()))).append('\n');
        }

        return processedLine.toString();
    }

    private void processSplitRestaurants() {

        // Add all the individual driver pickups to the global restaurants,
        // so the we can detect split restaurants.
        for (Driver driver : drivers) {
            for (Restaurant pickup : driver.getPickups()) {
                Restaurant restaurant = restaurants.get(pickup.getName());
                assert restaurant != null : "restaurant " + pickup.getName() + " not found in template";
                restaurant.addDriver(driver);
                restaurant.addOrders(pickup.getOrders());
            }
        }

        assignPrimarySecondaryDrivers();
    }

    private void assignPrimarySecondaryDrivers() {

        for (Restaurant restaurant : restaurants.values()) {
            Collection<Driver> drivers = restaurant.getDrivers().values();

            // FIX THIS, DS: should this be an assertion?
            if (drivers.size() == 0) {
                LOGGER.warn("Restaurant {} has no drivers", restaurant.getName());
                continue;
            }

            // Not a split restaurant
            if (drivers.size() == 1) {
                continue;
            }
            ArrayList<Driver> order = new ArrayList<>();

            // The driver who arrives first (in order of restaurants in their list) at the split restaurant is primary

            for (Driver driver : drivers) {
                if (driver.getFirstRestaurantName().equals(restaurant.getName())) {
                    order.add(driver);
                }
            }

            if (order.size() == 1) {
                LOGGER.info("Rule 1: Assigned {} as primary for {} because they are the only "
                        + "driver with {} as their first pickup",
                        order.get(0).getUserName(), restaurant.getName(), restaurant.getName());
                restaurant.setPrimaryDriver(order.get(0));
                continue;
            }

            // in case of ties, the driver with the smallest number of stops (restaurants + deliveries) is primary

            List<Driver> driversList = new ArrayList<>(drivers);
            driversList.sort(Comparator.comparing(Driver::getNumStops));

            order.clear();
            Driver firstDriver = driversList.get(0);
            order.add(firstDriver);

            for (int index = 1; index < driversList.size(); index++) {
                Driver driver = driversList.get(index);
                if (driver.getNumStops() == firstDriver.getNumStops()) {
                    order.add(driver);
                } else {
                    break;
                }
            }

            if (order.size() == 1) {
                LOGGER.info("Rule 2: Assigned {} as primary for {} because they have the fewest stops",
                        order.get(0).getUserName(), restaurant.getName());
                restaurant.setPrimaryDriver(order.get(0));
                continue;
            }

            // in case of ties, the driver with the smallest number of restaurants is primary

            driversList.sort(Comparator.comparing(Driver::getNumPickups));

            order.clear();
            firstDriver = driversList.get(0);
            order.add(firstDriver);

            for (int index = 1; index < driversList.size(); index++) {
                Driver driver = driversList.get(index);
                if (driver.getNumPickups() == firstDriver.getNumPickups()) {
                    order.add(driver);
                } else {
                    break;
                }
            }

            if (order.size() == 1) {
                LOGGER.info("Rule 3: Assigned {} as primary for {} because they have the fewest pickups",
                        order.get(0).getUserName(), restaurant.getName());
                restaurant.setPrimaryDriver(order.get(0));
                continue;
            }

            // in case of ties, the driver that comes first in username alphabetical order is primary

            driversList.sort(Comparator.comparing(Driver::getUserName));
            restaurant.setPrimaryDriver(driversList.get(0));

            LOGGER.info("Rule 4: Assigned {} as primary for {} based on alphabetical sort",
                    order.get(0).getUserName(), restaurant.getName());
        }
    }

    private String generateSplitRestaurants(Section section, Driver driver) {

        StringBuilder output = new StringBuilder();
        boolean hasSplit = false;

        for (Restaurant pickup : driver.getPickups()) {

            Restaurant restaurant = restaurants.get(pickup.getName());
            assert restaurant != null : "Cannot find restaurant " + pickup.getName();

            Collection<Driver> drivers = restaurant.getDrivers().values();
            assert drivers.size() != 0;

            if (drivers.size() == 1) {
                continue;
            }

            hasSplit = true;
            boolean isPrimary = restaurant.getPrimaryDriver().getUserName().equals(driver.getUserName());

            LOGGER.debug("{} Driver {} has split restaurant {}",
                    isPrimary ? "Primary" : "Secondary", driver.getUserName(), restaurant.getName());

            if (output.length() == 0) {
                output.append("\nWe are running an experiment:\n\n");
            }

            output.append("* you are one of ");
            output.append(drivers.size());
            output.append(" drivers going to pick up orders at **");
            output.append(restaurant.getName());
            output.append("** [all drivers: ");
            for (Driver allDriver : drivers) {
                output.append("@");
                output.append(allDriver.getUserName());
                output.append(" at ");
                output.append(allDriver.getPhoneNumber());
                output.append("; ");
            }
            output.append("]. The total number of orders is ");
            output.append(restaurant.getOrders());
            output.append(": you are picking up ");
            output.append(pickup.getOrders());
            output.append(" of them.\n");

            output.append("* you are ");
            output.append(isPrimary ? "primary" : "secondary");
            output.append(" driver for this restaurant, which means that **you ");
            output.append(isPrimary ? "need" : "do not need");
            output.append(" to take pics** of the delivery form.\n");
        }

        if (hasSplit) {
            output.append("* because several drivers are sharing restaurants, ");
            output.append("you need to be careful about what orders you are picking up.\n");
            output.append("* if you are not the last driver picking up orders, ");
            output.append("please make sure you are picking up YOUR orders, and tell the ");
            output.append("restaurant that other drivers are coming.\n");
            output.append("* while you pick up your orders, please post on the thread to ");
            output.append("let other drivers know (so that the last driver knows she is last)\n");
            output.append("* if you are the last driver picking up orders, ");
            output.append("please make sure there are no orders left --- otherwise call the dispatcher.\n");
        }

        return output.toString();
    }

    private static class Section {
        final String raw;
        final String name;
        final String contents;
        boolean hasConditional;
        String conditionalVariableName;

        Section(final String raw, final String name, final String contents) {
            this.raw = raw;
            this.name = name;
            checkForConditional(contents.trim());

            if (hasConditional) {

                int index = contents.indexOf("THEN");
                assert index != -1 : "bad conditional: " + contents;
                index = contents.indexOf('{', index);
                assert index != -1 : "bad conditional: " + contents;

                int endIndex = contents.lastIndexOf('}');
                assert endIndex != -1 : "bad conditional: " + contents;
                this.contents = contents.substring(index + 1, endIndex);
            } else {
                this.contents = contents;
            }
        }

        private void checkForConditional(final String contents) {

            if (! contents.startsWith("IF ${")) {
                hasConditional = false;
                return;
            }

            hasConditional = true;
            int endIndex = contents.indexOf("}");
            assert (endIndex != -1) : "malformed conditional in " + contents;
            conditionalVariableName = contents.substring(5, endIndex);
        }
    }
}

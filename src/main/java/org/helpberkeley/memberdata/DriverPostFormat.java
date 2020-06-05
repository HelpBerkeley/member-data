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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverPostFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormat.class);

    private final ApiClient apiClient;
    private final List<Block> blocks = new ArrayList<>();
    private List<Driver> drivers;
    private final Pattern compositeVariableRE;
    private Map<String, Restaurant> restaurants;

    DriverPostFormat(ApiClient apiClient, final String routedDeliveries)
            throws IOException, InterruptedException, CsvValidationException {
        this.apiClient = apiClient;
        compositeVariableRE = Pattern.compile("\\$\\{[A-Z_]+\\.[A-Z_]+\\}");
        loadRestaurantTemplate();
        loadFormatTopic();
        loadRoutedDeliveries(routedDeliveries);
    }

    void generate() {

        for (Driver driver : drivers) {
            StringBuilder post = new StringBuilder();

            for (Block block : blocks) {

                if (block.hasConditional) {
                    if (! evaluateCondition(driver, block.conditionalVariableName)) {
                        continue;
                    }
                }

                for (String line : block.contents.split("\n", -1)) {
                    Matcher matcher = compositeVariableRE.matcher(line);

                    if (matcher.find()) {
                        post.append(processCompositeLine(driver, line));
                    } else {
                        post.append(processLine(driver, line));
                    }
                }
            }

            System.out.println(post.toString());
        }
    }

    private void loadRestaurantTemplate() throws IOException, InterruptedException, CsvValidationException {
        String rawPost = Parser.postBody(apiClient.getPost(Main.RESTAURANT_TEMPLATE_POST_ID));
        RestaurantTemplatePost restaurantTemplatePost = Parser.restaurantTemplatePost(rawPost);
        String restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.fileName);
        RestaurantTemplateParser parser = new RestaurantTemplateParser(restaurantTemplate);
        restaurants = parser.restaurants();
    }

    private void loadFormatTopic() throws IOException, InterruptedException {
        String json = apiClient.runQuery(Constants.QUERY_GET_DRIVERS_POST_FORMAT);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 2 : columns.length;

            Long id = ((Long)columns[0]);
            // Normalize EOL
            String raw = ((String)columns[1]).replaceAll("\\r\\n?", "\n");

            // Look for block name of the form:
            // [This is the block name]\n
            String pattern = "^\\[([A-Za-z0-9 ]+)\\]";

            Pattern re = Pattern.compile(pattern);
            Matcher match = re.matcher(raw);

            if (! match.find()) {
                LOGGER.warn("Cannot find block name in {}", raw);
                continue;
            }

            String blockName = match.group(1);
            String blockContents = raw.substring(match.end());
            blocks.add(new Block(raw, blockName, blockContents));
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

        throw new Error("Unsupport connditional " + variableName);
    }

    private String processLine(final Driver driver, final String line) {

        final String firstRestaurant = driver.getFirstRestaurantName();
        final Restaurant restaurant = restaurants.get(firstRestaurant);
        assert restaurant != null : restaurant + " was not found the in restaurant template post";
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
        String processedLine = "";

        for (Restaurant restaurant : driver.getPickups()) {

            processedLine += line.replaceAll("\\$\\{RESTAURANT.NAME\\}", restaurant.getName())
                    .replaceAll("\\$\\{RESTAURANT.ADDRESS\\}", restaurant.getAddress())
                    .replaceAll("\\$\\{RESTAURANT.DETAILS\\}", restaurant.getDetails())
                    .replaceAll("\\$\\{RESTAURANT.ORDERS\\}", restaurant.getOrders())
                    + '\n';
        }

        return processedLine;
    }

    private static class Block {
        final String raw;
        final String name;
        final String contents;
        boolean hasConditional;
        String conditionalVariableName;

        Block(final String raw, final String name, final String contents) {
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

/*
 * Copyright (c) 2020-2021. helpberkeley.org
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
package org.helpberkeley.memberdata.route;

import org.helpberkeley.memberdata.Delivery;
import org.helpberkeley.memberdata.Driver;
import org.helpberkeley.memberdata.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Google Maps routing
 */
public class Route {

    private static final Logger LOGGER = LoggerFactory.getLogger(Route.class);
    private final GMapApiClient gMapClient = new GMapApiClient();
    private long totalSeconds;

    public void route(Driver driver) {

        List<Restaurant> pickups = driver.getPickups();
        List<Delivery> deliveries = new ArrayList<>(driver.getDeliveries());
        totalSeconds = 0;

        // Get the last pickup as our starting point for routing.
        assert ! pickups.isEmpty() : "0 length pickups for driver " + driver.getUserName();
        Restaurant lastPickup = pickups.get(pickups.size() - 1);
        // Restaurants have the city already at the end of the address field.
        String address = lastPickup.getAddress() + ", CA";
        PickupLocation lastPickupLocation = new PickupLocation(gMapClient.getLocation(address), lastPickup);

        // Get the driver's home address as the fixed endpoint for the routing.
        Location driversHomeLocation = gMapClient.getLocation(driver.getFullAddress());

        // FIX THIS, DS: remove duplicate locations

        List<DeliveryLocation> deliveryLocations = getLocations(deliveries);

        Location[] route = new Location[deliveryLocations.size() + 2];
        route[0] = lastPickupLocation;
        route[route.length - 1] = driversHomeLocation;

        if (route.length == 2) {
            return;
        }

        dualBoundNearestNeighbor(deliveryLocations, route);

        if (! route[0].equals(lastPickupLocation)) {
            route = reverse(route);
        }
        assert route[0].equals(lastPickupLocation);
        assert route[route.length - 1].equals(driversHomeLocation);

        deliveries.clear();

        // Update deliveries

        // Skip starting restaurant and driver home location
        for (int i = 1; i < (route.length - 1); i++) {
            assert route[i] instanceof DeliveryLocation : i + ": " + route[i];
            DeliveryLocation deliveryLocation = (DeliveryLocation)route[i];
            deliveryLocation.delivery.setLocation(deliveryLocation);
            deliveries.add(deliveryLocation.delivery);
        }

        driver.setDeliveries(deliveries, totalSeconds);
    }

    public void shutdown() {
        LOGGER.info("Shutting down GMapClient");
        gMapClient.shutdown();
        LOGGER.info("Done shutting down GMapClient");
    }

    public long getLocationCalls() {
        return gMapClient.getLocationCalls();
    }

    public long getDirectionsCalls() {
        return gMapClient.getDirectionsCalls();
    }

    private void dualBoundNearestNeighbor(List<DeliveryLocation> deliveryLocations, Location[] route) {

        if (deliveryLocations.size() == 0) {
            return;
        }

        // Find starting point

        int start;

        for (start = 0; start < route.length; start++) {
            if (route[start] == null) {
                break;
            }
        }

        assert start > 0;
        Location closest = findClosest(route[start - 1], deliveryLocations);
        route[start] = closest;
        deliveryLocations.remove(closest);

        route = reverse(route);
        dualBoundNearestNeighbor(deliveryLocations, route);
    }

    private Location findClosest(Location location, List<DeliveryLocation> deliveryLocations) {

        long minimumTime = Long.MAX_VALUE;
        Location closestDelivery = null;

        assert deliveryLocations.size() > 0;

        for (Location deliveryLocation : deliveryLocations) {
            long seconds = gMapClient.getTravelTime(location, deliveryLocation);
            if (seconds < minimumTime) {
                minimumTime = seconds;
                closestDelivery = deliveryLocation;
            }
        }

        totalSeconds += minimumTime;
        return closestDelivery;
    }

    private List<DeliveryLocation> getLocations(List<Delivery> deliveries) {

        List<DeliveryLocation> locations = new ArrayList<>();

        for (Delivery delivery : deliveries) {
            String address = delivery.getAddress() + ", " + delivery.getCity() + ", CA";
            Location location = gMapClient.getLocation(address);
            locations.add(new DeliveryLocation(address, location, delivery));
        }

        return locations;
    }

    private Location[] reverse(Location[] reversi) {
        List<Location> reversed = Arrays.asList(reversi);
        Collections.reverse(reversed);

        return reversed.toArray(reversi);
    }

    static class PickupLocation extends Location {
        final Restaurant restaurant;

        PickupLocation(Location location, Restaurant restaurant) {
            super(restaurant.getAddress(), location);
            this.restaurant = restaurant;
        }
    }

    static class DeliveryLocation extends Location {

        final Delivery delivery;

        DeliveryLocation(String address, Location location, Delivery delivery) {
            super(address, location);
            this.delivery = delivery;
        }
    }
}

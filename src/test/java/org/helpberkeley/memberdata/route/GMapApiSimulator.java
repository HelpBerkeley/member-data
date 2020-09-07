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
package org.helpberkeley.memberdata.route;

import com.google.maps.model.*;
import org.helpberkeley.memberdata.MemberDataException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GMapApiSimulator implements GMapApi {

    @Override
    public GeocodingResult[] geocode(String address) {

        double latitude;
        double longitude;

        switch (address) {
            case "1158 Solano Ave, Albany, CA":
                latitude = 37.8902244;
                longitude = -122.2989933;
                break;
            case "1823 Solano Ave, Berkeley, CA":
                latitude = 37.891605;
                longitude = -122.2814284;
                break;
            case "1525 bonita avenue, Berkeley, CA":
                latitude = 37.8789233;
                longitude = -122.2746055;
                break;
            case "3004 Acton Street, Berkeley, CA":
                latitude = 37.8529318;
                longitude = -122.2837765;
                break;
            case "2920 Fulton St., Berkeley, CA":
                latitude = 37.8562838;
                longitude = -122.2660097;
                break;
            case "245 Purdue Ave, Kensington, CA":
                latitude = 37.9082515;
                longitude = -122.2745612;
                break;
            case "60 Alamo Ave., Berkeley, CA":
                latitude = 37.8990125;
                longitude = -122.2713268;
                break;
            case "2550 Dana St. #9H, Berkeley, CA":
                latitude = 37.8632015;
                longitude = -122.2628637;
                break;
            case "2032 Del Norte Street, Berkeley, CA":
                latitude = 37.889501;
                longitude = -122.273232;
                break;
            default:
                throw new MemberDataException("Address " + address + " not known to simulator");
        }

        LatLng latLng = new LatLng(latitude, longitude);
        Geometry geometry = new Geometry();
        geometry.location = latLng;
        GeocodingResult result = new GeocodingResult();
        result.geometry = geometry;
        return new GeocodingResult[] { result };
    }

    @Override
    public DirectionsResult route(Location start, Location end) {

        long seconds = TravelTime.get(start, end);

        DirectionsResult result = new DirectionsResult();

        DirectionsLeg leg = new DirectionsLeg();
        Duration duration = new Duration();
        duration.inSeconds = seconds;
        leg.duration = duration;

        DirectionsLeg[] legs = { leg };

        DirectionsRoute route = new DirectionsRoute();
        route.legs = legs;

        result.routes = new DirectionsRoute[]{ route };

        return result;
    }

    @Override
    public void shutdown() {
        
    }

    static class TravelTime {
        static final Map<Location, Map<Location, Long>> travelSeconds = new HashMap<>();

        static long get(Location start, Location end) {
            if (travelSeconds.isEmpty()) {
                loadMap();
            }

            Map<Location, Long> destinations = travelSeconds.get(start);

            if (destinations == null) {
                throw new MemberDataException("Start location " + start.getAddress() + " not known to TravelTime");
            }

            Long seconds = destinations.get(end);

            if (seconds == null) {
                throw new MemberDataException("End location " + end.getAddress() + " not known to TravelTime");
            }

            return seconds;
        }

        private static void loadMap() {

            Location l1 = new Location("1823 Solano Ave, Berkeley, CA", 37.891605, -122.2814284);
            Location l2 = new Location("1525 bonita avenue, Berkeley, CA", 37.8789233, -122.2746055);
            Location l3 = new Location("3004 Acton Street, Berkeley, CA", 37.8529318, -122.2837765);
            Location l4 = new Location("2920 Fulton St., Berkeley, CA", 37.8562838, -122.2660097);
            Location l5 = new Location("1158 Solano Ave, Albany, CA", 37.8902244, -122.2989933);
            Location l6 = new Location("245 Purdue Ave, Kensington, CA", 37.9082515, -122.2745612);
            Location l7 = new Location("60 Alamo Ave., Berkeley, CA", 37.8990125, -122.2713268);
            Location l8 = new Location("2550 Dana St. #9H, Berkeley, CA", 37.8632015, -122.2628637);
            Location l9 = new Location("2032 Del Norte Street, Berkeley, CA", 37.889501, -122.273232);

            // l1
            Map<Location, Long> destinations = new HashMap<>();
            destinations.put(l2, TimeUnit.MINUTES.toSeconds(4));
            destinations.put(l3, TimeUnit.MINUTES.toSeconds(11));
            destinations.put(l4, TimeUnit.MINUTES.toSeconds(10));
            travelSeconds.put(l1, destinations);

            // l2
            destinations = new HashMap<>();
            destinations.put(l1, TimeUnit.MINUTES.toSeconds(4));
            destinations.put(l3, TimeUnit.MINUTES.toSeconds(8));
            destinations.put(l4, TimeUnit.MINUTES.toSeconds(7));
            travelSeconds.put(l2, destinations);

            // l3
            destinations = new HashMap<>();
            destinations.put(l1, TimeUnit.MINUTES.toSeconds(10));
            destinations.put(l2, TimeUnit.MINUTES.toSeconds(8));
            destinations.put(l4, TimeUnit.MINUTES.toSeconds(6));
            travelSeconds.put(l3, destinations);

            // l4
            destinations = new HashMap<>();
            destinations.put(l1, TimeUnit.MINUTES.toSeconds(12));
            destinations.put(l2, TimeUnit.MINUTES.toSeconds(9));
            destinations.put(l3, TimeUnit.MINUTES.toSeconds(5));
            travelSeconds.put(l4, destinations);

            // l5
            destinations = new HashMap<>();
            destinations.put(l6, TimeUnit.MINUTES.toSeconds(10));
            destinations.put(l7, TimeUnit.MINUTES.toSeconds(9));
            destinations.put(l8, TimeUnit.MINUTES.toSeconds(15));
            destinations.put(l9, TimeUnit.MINUTES.toSeconds(7));
            travelSeconds.put(l5, destinations);

            // l6
            destinations = new HashMap<>();
            destinations.put(l5, TimeUnit.MINUTES.toSeconds(11));
            destinations.put(l7, TimeUnit.MINUTES.toSeconds(4));
            destinations.put(l8, TimeUnit.MINUTES.toSeconds(16));
            destinations.put(l9, TimeUnit.MINUTES.toSeconds(6));
            travelSeconds.put(l6, destinations);

            // l7
            destinations = new HashMap<>();
            destinations.put(l5, TimeUnit.MINUTES.toSeconds(9));
            destinations.put(l6, TimeUnit.MINUTES.toSeconds(5));
            destinations.put(l8, TimeUnit.MINUTES.toSeconds(13));
            destinations.put(l9, TimeUnit.MINUTES.toSeconds(4));
            travelSeconds.put(l7, destinations);

            // l8
            destinations = new HashMap<>();
            destinations.put(l5, TimeUnit.MINUTES.toSeconds(15));
            destinations.put(l6, TimeUnit.MINUTES.toSeconds(17));
            destinations.put(l7, TimeUnit.MINUTES.toSeconds(13));
            destinations.put(l9, TimeUnit.MINUTES.toSeconds(10));
            travelSeconds.put(l8, destinations);

            // l9
            destinations = new HashMap<>();
            destinations.put(l5, TimeUnit.MINUTES.toSeconds(6));
            destinations.put(l6, TimeUnit.MINUTES.toSeconds(6));
            destinations.put(l7, TimeUnit.MINUTES.toSeconds(4));
            destinations.put(l8, TimeUnit.MINUTES.toSeconds(10));
            travelSeconds.put(l9, destinations);
        }
    }
}
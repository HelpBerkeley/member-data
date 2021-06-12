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

import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import org.helpberkeley.memberdata.ApiClient;
import org.helpberkeley.memberdata.MemberDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GMapApiClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);
    private final GMapApi apiImpl;
    private long locationCalls;
    private long directionsCalls;

    public static GMapApiFactory apiFactory;

    GMapApiClient() {
        if (apiFactory != null) {
            apiImpl = apiFactory.createApi();
        } else {
            apiImpl = new GMapApiImpl();
        }
    }

    public void shutdown() {
        apiImpl.shutdown();
    }

    public long getLocationCalls() {
        return locationCalls;
    }

    public long getDirectionsCalls() {
        return directionsCalls;
    }

    Location getLocation(String address) {

        locationCalls++;

        try {
            GeocodingResult[] results = apiImpl.geocode(address);

            if (results.length != 1) {
                LOGGER.warn("More than 1 maps response for location of {}", address);
                // FIX THIS, DS: show all in the log message
            }

            // FIX THIS, DS; can this be 0 length?
            assert results.length != 0;
            GeocodingResult result = results[0];

            return new Location(address, result.geometry.location.lat, result.geometry.location.lng);
        } catch (ApiException | InterruptedException | IOException e) {
            // FIX THIS, DS: add another signature for getting all cause messages?
            throw new MemberDataException(
                    "Google maps errors getting location for " + address + ": " + e.getMessage());
        }
    }

    long getTravelTime(Location start, Location end) {

        directionsCalls++;

        try {
            DirectionsResult result = apiImpl.route(start, end);

            if (result.routes.length > 1) {
                throw new MemberDataException("More than one route returned");
            }

            DirectionsLeg[] legs = result.routes[0].legs;

            if (legs.length > 1) {
                throw new MemberDataException("More than one leg returned");
            }

            return legs[0].duration.inSeconds;
        } catch (ApiException | InterruptedException | IOException e) {
            // FIX THIS, DS: add another signature for getting all cause messages?
            throw new MemberDataException("Google maps errors getting directions: " + e.getMessage());
        }
    }
}
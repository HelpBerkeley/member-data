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

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import org.helpberkeley.memberdata.ApiClient;
import org.helpberkeley.memberdata.Constants;
import org.helpberkeley.memberdata.MemberDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Properties;

class GMapApiImpl implements GMapApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);
    private static GeoApiContext context;

    GMapApiImpl() {
        if (context == null) {
            initialize();
        }
    }

    @Override
    public GeocodingResult[] geocode(String address) throws InterruptedException, ApiException, IOException {
        LOGGER.trace("geocode {}", address);
        GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
        LOGGER.trace("geocode done");
        return results;
    }

    @Override
    public DirectionsResult route(Location start, Location end) throws InterruptedException, ApiException, IOException {
        LOGGER.trace("route {} to {}", start, end);
        DirectionsResult result = DirectionsApi.newRequest(context)
                .origin(new LatLng(start.getLatitude(), start.getLongitude()))
                .destination(new LatLng(end.getLatitude(), end.getLongitude()))
                .mode(TravelMode.DRIVING)
                .departureTime(Instant.now())
                .await();
        LOGGER.trace("route done");
        return result;
    }

    @Override
    public void shutdown() {
        if (context != null) {
            context.shutdown();
        }
    }

    private void initialize() {
        Properties properties = loadProperties();
        String apiKey = properties.getProperty(Constants.GMAPS_API_KEY_PROPERTY);

        if (apiKey == null) {
            throw new MemberDataException("Missing " + Constants.GMAPS_API_KEY_PROPERTY);
        }

        context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    private Properties loadProperties() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL propertiesFile = classLoader.getResource(Constants.MEMBERDATA_PROPERTIES);

        if (propertiesFile == null) {
            LOGGER.error("Required properties file {} cannot be found", Constants.MEMBERDATA_PROPERTIES);
            System.exit(1);
        }

        Properties properties = new Properties();

        //noinspection EmptyFinallyBlock
        try (InputStream is = propertiesFile.openStream())
        {
            properties.load(is);
        } catch (IOException e) {
            throw new MemberDataException(e.getMessage());
        } finally {}

        return properties;
    }
}

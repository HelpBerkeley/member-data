/*
 * Copyright (c) 2020-2021 helpberkeley.org
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

import com.cedarsoftware.util.io.JsonWriter;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public abstract class DriverPostTest extends TestBase {

    protected final Map<String, User> users;

    public DriverPostTest() {
        Loader loader = new Loader(createApiSimulator());
        users = new Tables(loader.load()).mapByUserName();
    }

    abstract int getRestaurantTemplateQuery();
    abstract int getDriverPostFormatQuery();
    abstract int getGroupInstructionsFormatQuery();
    abstract String getRoutedDeliveriesFileName();
    abstract void checkExpectedPickups(List<String> posts);
    abstract void checkExpectedDeliveries(List<String> posts);
    abstract void checkCondoConsumers(List<String> posts);

    protected String createMessageBlock(String contents) {
        return new MessageBlockJSON(contents).toJSON();
    }

    @Test
    public void emptyTest() {
        String json = new MessageBlockJSON("").toJSON();

        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), json);
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        for (String post : driverPostFormat.generateDriverPosts()) {
            assertThat(post).isEqualTo("");
        }
    }

    @Test
    public void literalStringTest() {
        String literal = "Fhqwhgads"; // Everybody to the Limit!
        String json = new MessageBlockJSON(quote(literal)).toJSON();

        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), json);
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        for (String post : driverPostFormat.generateDriverPosts()) {
            assertThat(post).isEqualTo(literal);
        }
    }

    @Test
    public void thisDriverRestaurantPickupTest() {
        String format = "LOOP &{ThisDriverRestaurant} { "
                + "LOOP &{ThisDriverRestaurant.Pickup} { "
                + " &{ThisDriverRestaurant.Name}"
                + "\"|\""
                + " &{Pickup.MemberName}"
                + "\"|\""
                + " &{Pickup.UserName}"
                + "\"\\n\""
                + " }"
                + " }";
        String json = new MessageBlockJSON(format).toJSON();
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), json);
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        checkExpectedPickups(posts);
    }

    @Test
    public void deliveriesTest() {
        String format = "LOOP &{Consumer} { "
                + " &{Consumer.Name}"
                + "\"|\""
                + " &{Consumer.CompactPhone}"
                + "\"|\""
                + " IF &{Consumer.IsAltPhone} THEN { &{Consumer.CompactAltPhone} }"
                + " IF NOT &{Consumer.IsAltPhone} THEN { \"NoAltPhone\" } "
                + "\"|\""
                + " &{Consumer.Address}"
                + "\"|\""
                + " IF &{Consumer.IsCondo} THEN { \"Condo\" } "
                + " IF NOT &{Consumer.IsCondo} THEN { \"NoCondo\" } "
                + "\"|\""
                + " &{Consumer.RestaurantEmoji}"
                + "\"|\""
                + " &{Consumer.Details}"
                + "\"\\n\""
                + " }";
        String json = new MessageBlockJSON(format).toJSON();
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), json);
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        checkExpectedDeliveries(posts);
    }

    @Test
    public void invalidContinueTest() {

        String json = new MessageBlockJSON("CONTINUE").toJSON();
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), json);
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        Throwable thrown = catchThrowable(() -> driverPostFormat.generateDriverPosts());
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(DriverPostFormat.ERROR_CONTINUE_WITHOUT_LOOP);
    }

    @Test
    public void singleLevelContinueTest() {
        String format = "LOOP &{Consumer} { "
                + " IF NOT &{Consumer.IsCondo} THEN { CONTINUE }"
                + " &{Consumer.Name}"
                + "\"\\n\""
                + " }";
        String json = new MessageBlockJSON(format).toJSON();
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), json);
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat = DriverPostFormat.create(createApiSimulator(), users, routedDeliveries,
                getRestaurantTemplateQuery(),
                getDriverPostFormatQuery(),
                getGroupInstructionsFormatQuery());
        List<String> posts = driverPostFormat.generateDriverPosts();
        checkCondoConsumers(posts);
    }

    protected String quote(String quotable) {
        return "\"" + quotable + "\"";
    }

    static private class MessageBlockJSON {
        final String[] columns;
        final Object[][] rows;

        MessageBlockJSON(String message) {
            columns = new String[] { "post_number", "raw", "deleted_at" };
            Object[] row = new Object[] { 1, "[Test]\n" + message, null };
            rows = new Object[][] { row };
        }

        String toJSON() {
            Map<String, Object> options = new HashMap<>();
            options.put(JsonWriter.TYPE, Boolean.FALSE);
            return JsonWriter.objectToJson(this, options);
        }
    }
}
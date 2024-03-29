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

import org.junit.Test;

import java.text.MessageFormat;
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

    public abstract int getRestaurantTemplateQuery();
    public abstract int getDriverPostFormatQuery();
    public abstract int getGroupInstructionsFormatQuery();
    public abstract String getRoutedDeliveriesFileName();
    public abstract void checkExpectedDeliveries(List<String> posts);
    public abstract void checkCondoConsumers(List<String> posts);
    public abstract String getEmptyRow();

    @Test
    public void emptyTest() {
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(""));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        for (String post : driverPostFormat.generateDriverPosts()) {
            assertThat(post).isEqualTo("");
        }
    }

    @Test
    public void literalStringTest() {
        String literal = "Fhqwhgads"; // Everybody to the Limit!

        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(quote(literal)));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        for (String post : driverPostFormat.generateDriverPosts()) {
            assertThat(post).isEqualTo(literal);
        }
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
                + " &{Consumer.Details}"
                + "\"\\n\""
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        List<String> posts = driverPostFormat.generateDriverPosts();
        checkExpectedDeliveries(posts);
    }

    @Test
    public void invalidContinueTest() {

        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock("CONTINUE"));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        Throwable thrown = catchThrowable(driverPostFormat::generateDriverPosts);
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
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        List<String> posts = driverPostFormat.generateDriverPosts();
        checkCondoConsumers(posts);
    }

    @Test
    public void dataRowWithoutEnoughColumnsTest() {
        String emptyRow = getEmptyRow().substring(3);
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName()) + emptyRow;
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));

        assertThat(thrown).isInstanceOf(MemberDataException.class);

        String expectedMessage = "Error parsing CSV line: "
                + routedDeliveries.split("\\n").length
                + ". ["
                + emptyRow.replace('\n', ']')
                + "\nNumber of data fields does not match number of headers.";
        assertThat(thrown).hasMessage(expectedMessage);
    }

    @Test
    public void dataRowWithTooManyColumnsTest() {
        String emptyRow = getEmptyRow();
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName())
                + ",,," + emptyRow;
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);

        String expectedMessage = "Error parsing CSV line: "
                + routedDeliveries.split("\\n").length
                + ". ["
                + ",,,"
                + emptyRow.replace('\n', ']')
                + "\nNumber of data fields does not match number of headers.";

        assertThat(thrown).hasMessage(expectedMessage);
    }

    @Test
    public void unknownPickupsListRefVariableTest() {
        String format = "LOOP &{ThisDriverRestaurant} { "
                + " &{ThisDriverRestaurant.HasPurpleSnowCones}"
                + " }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        Throwable thrown = catchThrowable(driverPostFormat::generateDriverPosts);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(MessageBlockContext.MESSAGE_ERROR,
                "Test",
                "unknown list variable &{ThisDriverRestaurant.HasPurpleSnowCones}",
                200, 1));
    }

    @Test
    public void emptyLoopTest() {
        String format = "LOOP &{BackupDriver} { }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("mismatched input");
        assertThat(thrown).hasMessageContaining(format);
    }

    @Test
    public void emptyConditionalTest() {
        String format = "IF &{ThisDriverHasCondo} { }";
        HttpClientSimulator.setQueryResponseData(getDriverPostFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        Throwable thrown = catchThrowable(() ->
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining("mismatched input");
        assertThat(thrown).hasMessageContaining(format);
    }

    @Test
    public void unknownStructVariableTest() {
        String format = "LOOP &{BackupDriver} { ${BackupDriver.FavoriteColor} }";
        HttpClientSimulator.setQueryResponseData(getGroupInstructionsFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        Throwable thrown = catchThrowable(driverPostFormat::generateGroupInstructionsPost);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(MessageBlockContext.MESSAGE_ERROR,
            "Test",
            "unknown struct variable ${BackupDriver.FavoriteColor}",
            200, 1));

    }

    @Test
    public void unknownListNameTest() {
        String format = "LOOP &{TheLoop} { \"Hi\" }";
        HttpClientSimulator.setQueryResponseData(getGroupInstructionsFormatQuery(), createMessageBlock(format));
        String routedDeliveries = readResourceFile(getRoutedDeliveriesFileName());
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, routedDeliveries);
        Throwable thrown = catchThrowable(driverPostFormat::generateGroupInstructionsPost);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(MessageBlockContext.MESSAGE_ERROR,
            "Test",
            "unknown list variable &{TheLoop}",
            200, 1));
    }
}

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

import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Exporter {
    private final List<User> users;

    public Exporter(List<User> users) {
        this.users = users;
    }

    String jsonString() {
        Map<String, Object> options = Map.of(JsonWriter.PRETTY_PRINT, Boolean.TRUE);
        String json = JsonWriter.objectToJson(users);

        return json;
    }

    void jsonToFile(final String fileName) throws IOException {

        String json = jsonString();
        Path filePath = Files.createFile(Paths.get(fileName));
        Files.writeString(filePath, json);
    }

    String generateFileName(String fileName) {
        int suffixIndex = fileName.lastIndexOf('.');
        assert suffixIndex != -1 : fileName;

        String suffix = fileName.substring(suffixIndex);
        String base = fileName.substring(0, suffixIndex);

        String timestamp =
                ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("uuMMdd-HHmm"));

        return base + '-' + timestamp + suffix;
    }

    void csvToFile(final String fileName) throws IOException {

        // FIX THIS, DS: define constant for separator
        final String separator = ",";

        StringBuilder output = new StringBuilder();
        // FIX THIS, DS: define constant for separator
        output.append(User.csvHeaders(separator));

        for (User user : users) {
            output.append(user.getName());
            output.append(separator);
            output.append(user.getUserName());
            output.append(separator);
            output.append(user.getPhoneNumber());
            output.append(separator);
            output.append(user.getNeighborhood());
            output.append(separator);
            output.append(user.getCity());
            output.append(separator);
            output.append(user.getAddress());
            output.append(separator);
            output.append(user.isConsumer());
            output.append(separator);
            output.append(user.isDispatcher());
            output.append(separator);
            output.append(user.isDriver());
            output.append(separator);
            output.append('\n');
        }

        Path filePath = Paths.get(fileName);
        Files.deleteIfExists(filePath);
        Files.createFile(Paths.get(fileName));
        Files.writeString(filePath, output.toString());
    }
}

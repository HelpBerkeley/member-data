//
// Copyright (c) 2020 helpberkeley.org
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.helpberkeley.memberdata;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LoaderTest extends TestBase {

    private final ApiClient apiClient;

    public LoaderTest() throws IOException {
        apiClient = createApiSimulator();
    }

    @Test
    public void loadTest() throws IOException, InterruptedException, CsvException {
        Loader loader = new Loader(apiClient);

        List<User> users = loader.load();

        UserExporter exporter = new UserExporter(users);
        String csv = exporter.allMembersRaw();
        List<User> roundtripUsers = Parser.users(csv);

        assertThat(roundtripUsers).containsExactlyInAnyOrderElementsOf(users);
    }

    @Test
    public void emailAddressTest() throws IOException, InterruptedException {
        Loader loader = new Loader(apiClient);

        Map<Long, String> emailAddresses = loader.loadEmailAddresses();

        Map<Long, String> expected = Map.of(
            200L, "somebody@me.com",
            201L, "somebodyelse@me.com",
            333L, "person3@me.com",
            33L, "xyzzy@me.com",
            104L, "zzz@me.com",
            346L, "jvol@me.com",
            400L, "bogus@bogus.com",
            -2L, "discobot_email");
        assertThat(emailAddresses).containsExactlyInAnyOrderEntriesOf(expected);

    }

    @Test
    public void csvReaderTest1() throws IOException, CsvValidationException {
        String input = "A,D,C,B\na,d,c,b\nA1,D1,\"C1,c1\",B1\n";

        CSVReaderHeaderAware csvReaderHeaderAware = new CSVReaderHeaderAware(new StringReader(input));

        String[] columns;
        while ((columns = csvReaderHeaderAware.readNext("A", "B", "C", "D")) != null) {
            assertThat(columns).hasSize(4);
        }
    }

    @Test
    public void csvReaderTest2() throws IOException, CsvValidationException {
        String input = "A,D,C,B\na,d,c,b\nA1,D1,\"C1,c1\",B1\n";

        CSVReaderHeaderAware csvReaderHeaderAware = new CSVReaderHeaderAware(new StringReader(input));

        Map<String, String> columnsMap;
        List<String> columnNames = List.of("A", "B", "C", "D");

        while ((columnsMap = csvReaderHeaderAware.readMap()) != null) {
            assertThat(columnsMap).containsOnlyKeys(columnNames);
        }
    }

    @Test
    public void stagedUserTest() throws IOException, InterruptedException {
        HttpClientSimulator.setQueryResponseFile(Constants.CURRENT_USERS_QUERY, "null-user.json");
        Loader loader = new Loader(apiClient);
        List<User> users = loader.load();
        assertThat(users).hasSize(0);
    }
}

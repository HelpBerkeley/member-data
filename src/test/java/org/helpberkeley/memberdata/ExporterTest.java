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

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ExporterTest extends TestBase {
    @Test
    public void errorsToFileTest() throws UserException, IOException {

        List<User> users = new ArrayList<>();

        try {
            User u1 = createUserWithUserNameCityAndNeighborhood(
                    TEST_USER_NAME_1, Constants.BERKELEY, "unknown");
        } catch (UserException ex) {
            assertThat(ex.user).isNotNull();
            assertThat(ex.user.getUserName()).isEqualTo(TEST_USER_NAME_1);
            users.add(ex.user);
        }

        User u2 = createUserWithUserNameCityAndNeighborhood(
                TEST_USER_NAME_2, Constants.ALBANY, "Solano");
        users.add(u2);

        User u3 = createUserWithUserNameCityAndNeighborhood(
                TEST_USER_NAME_3, "Altoona", "unknown");
        users.add(u3);

        Exporter exporter = new Exporter(users);
        String fileName = exporter.errorsToFile("errorsToFileTest.txt");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();

        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).contains(TEST_USER_NAME_1);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_2);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_3);

        Files.delete(Paths.get(fileName));
    }

    @Test
    public void consumerRequestsTest() throws UserException, IOException {

        User u1 = createUserWithNoRequestsNoGroups(TEST_USER_NAME_1);
        User u2 = createUserWithConsumerRequest(TEST_USER_NAME_2, true);

        Exporter exporter = new Exporter(List.of(u1, u2));
        String fileName = exporter.consumerRequests("consumerRequests.csv");
        Path filePath = Paths.get(fileName);
        assertThat(filePath).exists();
        
        String errorFileData = Files.readString(filePath);
        assertThat(errorFileData).contains(TEST_USER_NAME_2);
        assertThat(errorFileData).doesNotContain(TEST_USER_NAME_1);

        Files.delete(Paths.get(fileName));
    }
}

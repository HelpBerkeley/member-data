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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LoaderExporterTest extends TestBase {

    private final List<User> testUsers = new ArrayList<>();
    private final String testUsersExportedJson;
    private final int testUsersHashCode;

    public LoaderExporterTest() throws UserException {
        testUsers.add(createTestUser1());
        testUsers.add(createTestUser2());
        testUsers.add(createTestUser3());

        testUsersExportedJson = new Exporter(testUsers).jsonString();
        testUsersHashCode = testUsersExportedJson.hashCode();
    }

    @Test
    public void loadJsonTest() {
        Loader loader = new Loader();

        List<User> users = loader.load(testUsersExportedJson);
        assertThat(users).isNotEmpty();
        assertThat(loader.getExceptions()).isEmpty();
        assertThat(users).isEqualTo(testUsers);
    }

    @Test
    public void hashCodeTest() {

       List<User> users = new ArrayList<>();
       assertThat(new Exporter(users).jsonString().hashCode()).isNotEqualTo(testUsersHashCode);

       users.add(testUsers.get(0));
        assertThat(new Exporter(users).jsonString().hashCode()).isNotEqualTo(testUsersHashCode);
        users.add(testUsers.get(1));
        assertThat(new Exporter(users).jsonString().hashCode()).isNotEqualTo(testUsersHashCode);

        users.add(testUsers.get(2));
        assertThat(new Exporter(users).jsonString().hashCode()).isEqualTo(testUsersHashCode);
    }
}

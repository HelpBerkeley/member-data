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

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LoaderTest extends TestBase {

    @Test
    public void loadTest() throws IOException, InterruptedException {
        ApiClient apiClient = createApiSimulator();

        Loader loader = new Loader(apiClient);

        List<User> users = loader.load();

        UserExporter exporter = new UserExporter(users);
        String csv = exporter.allMembersRaw();
        List<User> roundtripUsers = Parser.users(csv);

        assertThat(roundtripUsers).containsExactlyInAnyOrderElementsOf(users);
    }

//    @Test
//    public void emailConfirmationsTest() throws IOException, InterruptedException {
//        ApiClient apiClient = createApiSimulator();
//
//        Loader loader = new Loader(apiClient);
//        List<User> users = loader.load();
//
//        Set<Long> emailConfirmations = loader.getEmailConfirmations();
//        assertThat(emailConfirmations).contains(400L);
//        assertThat(emailConfirmations).doesNotContain(-2L);
//    }
}

//
// Copyright (c) 2020-2021 helpberkeley.org
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RoundTripTest extends TestBase {

    @Test
    public void roundTripTest() throws UserException {

        User u1 = createUserWithGroups(
                Constants.GROUP_CONSUMERS,
                Constants.GROUP_DRIVERS,
                Constants.GROUP_TRAINED_DRIVERS,
                Constants.GROUP_DISPATCHERS,
                Constants.GROUP_SPECIALISTS,
                Constants.GROUP_LOGISTICS,
                Constants.GROUP_BHS,
                Constants.GROUP_HELPLINE,
                Constants.GROUP_SITELINE,
                Constants.GROUP_TRAINED_CUSTOMER_CARE_A,
                Constants.GROUP_TRAINED_CUSTOMER_CARE_B,
                Constants.GROUP_INREACH,
                Constants.GROUP_OUTREACH,
                Constants.GROUP_MARKETING,
                Constants.GROUP_MODERATORS,
                Constants.GROUP_WORKFLOW,
                Constants.GROUP_VOICEONLY,
                Constants.GROUP_FRVOICEONLY,
                Constants.GROUP_TRUST_LEVEL_4,
                Constants.GROUP_CUSTOMER_INFO,
                Constants.GROUP_ADVISOR,
                Constants.GROUP_BOARDMEMBERS,
                Constants.GROUP_COORDINATORS,
                Constants.GROUP_LIMITED,
                Constants.GROUP_AT_RISK,
                Constants.GROUP_BIKERS,
                Constants.GROUP_OUT,
                Constants.GROUP_EVENT_DRIVERS,
                Constants.GROUP_TRAINED_EVENT_DRIVERS,
                Constants.GROUP_GONE,
                Constants.GROUP_OTHER_DRIVERS,
                Constants.GROUP_ADMIN);

        List<User> users = List.of(u1);
        UserExporter exporter = new UserExporter(users);

        String csvData = exporter.allMembersRaw();

        List<User> usersFromCSV = HBParser.users(csvData);
        assertThat(usersFromCSV).isEqualTo(users);
    }
}

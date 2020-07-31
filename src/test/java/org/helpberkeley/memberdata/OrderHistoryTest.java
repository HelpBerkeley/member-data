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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class OrderHistoryTest extends TestBase {

    @Test
    public void noUserNameTest() throws UserException {
        List<User> users = List.of(createTestUser2());
        OrderHistory orderHistory = new OrderHistory(("2020/01/01"));

        UserOrder userOrder = new UserOrder(TEST_NAME_2, "", "", "", "");
        orderHistory.merge("20202/01/02", List.of(userOrder), users);
    }

    @Test
    public void nameMismatchNoUserNameTest() throws UserException {
        List<User> users = List.of(createTestUser2());
        OrderHistory orderHistory = new OrderHistory(("2020/01/01"));

        UserOrder userOrder = new UserOrder(
                TEST_NAME_2 + "qqq", "", TEST_PHONE_2, "", "someFile");

        orderHistory.merge("20202/01/02", List.of(userOrder), users);
    }

    @Test
    public void missingOrderHistoryURLTest() {
        Throwable thrown = catchThrowable(() -> HBParser.restaurantTemplatePost(""));
        assertThat(thrown).isInstanceOf(Error.class);
        assertThat(thrown).hasMessageContaining("Restaurant template upload link not found");
    }

    @Test
    public void unknownUserTest() throws IOException, InterruptedException {
        Loader loader = new Loader(createApiSimulator());
        List<User> users = loader.load();
        OrderHistory orderHistory = new OrderHistory(("2020/01/01"));
        UserOrder userOrder = new UserOrder("unregisteredUser", "", "", "", "");
        orderHistory.merge("20202/01/02", List.of(userOrder), users);
    }
}

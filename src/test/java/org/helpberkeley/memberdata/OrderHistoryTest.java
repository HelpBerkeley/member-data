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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

//    @Test
//    public void parseDelivers3_28Test() {
//        String fileName = "deliveries-3_28.csv";
//        String data = readFile(fileName);
//        List<UserOrder> userOrders = Parser.parseOrders(fileName, data);
//
//        List<UserOrder> expected = List.of(
//                new UserOrder("Ms. Somebody", "Somebody", fileName),
//                new UserOrder("Ms. Somebody", "Somebody", fileName),
//                new UserOrder("Ms. Somebody", "", fileName),
//                new UserOrder("Mr. Somebody", "SomebodyElse", fileName)
//        );
//
//        assertThat(userOrders).isEqualTo(expected);
//    }
//
//    @Test
//    public void parseDelivers3_29Test() {
//        String fileName = "deliveries-3_29.csv";
//        String data = readFile(fileName);
//        List<UserOrder> userOrders = Parser.parseOrders(fileName, data);
//
//        List<UserOrder> expected = List.of(
//                new UserOrder("Mr. Somebody", "SomebodyElse", fileName),
//                new UserOrder("Ms. Somebody", "Somebody", fileName)
//        );
//
//        assertThat(userOrders).isEqualTo(expected);
//    }
//
//    @Test
//    public void parseDelivers3_31Test() {
//        String fileName = "deliveries-3_31.csv";
//        String data = readFile(fileName);
//        List<UserOrder> userOrders = Parser.parseOrders(fileName, data);
//
//        List<UserOrder> expected = List.of(
//                new UserOrder("", "ThirdPerson", fileName),
//                new UserOrder("Ms. Somebody", "Somebody", fileName)
//        );
//
//        assertThat(userOrders).isEqualTo(expected);
//    }
}

/*
 * Copyright (c) 2021. helpberkeley.org
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

import java.util.List;
import java.util.Map;

public class ControlBlockV300Test extends ControlBlockTestBase{

    private final String controlBlockData;
    private final Map<String, User> users;
    private final Map<String, Restaurant> allRestaurants;

    private static final String EMPTY_ROW = ",,,,,,,,,,,,,,,,,,\n";

    private static final String HEADER = "Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,Address,"
            + "Condo,Details,Restaurants,std meals,alt meals,type meal,std grocery,alt grocery,type grocery,#orders\n";
    private final String  CONTROL_BLOCK_BEGIN_ROW = "FALSE,FALSE,ControlBegin,,,,,,,,,,,,,,,,\n";
    private final String  CONTROL_BLOCK_END_ROW = "FALSE,FALSE,ControlEnd,,,,,,,,,,,,,,,,\n";
    private final String  CONTROL_BLOCK_VERSION_ROW = "FALSE,FALSE,,Version ,,,,3-0-0,,,,,,,,,,,\n";

    public ControlBlockV300Test() {
        controlBlockData = readResourceFile("control-block-v300.csv");
        List<User> userList = new Loader(createApiSimulator()).load();
        users = new Tables(userList).mapByUserName();

        RestaurantTemplateParser parser =
                RestaurantTemplateParser.create(readResourceFile("restaurant-template-v300.csv"));
        allRestaurants = parser.restaurants();
    }

    @Override
    String getHeader() {
        return HEADER;
    }
    @Override
    String getBeginRow() {
        return CONTROL_BLOCK_BEGIN_ROW;
    }

    @Override
    String getEndRow() {
        return CONTROL_BLOCK_END_ROW;
    }

    @Override
    String getVersionRow() {
        return CONTROL_BLOCK_VERSION_ROW;
    }

    @Override
    String getEmptyRow() {
        return EMPTY_ROW;
    }

    @Override
    String getDirectiveRow(String directive) {
        return EMPTY_ROW.replaceFirst(",,,", "FALSE,FALSE," + directive + ",");
    }

    @Override
    String getKeyValueRow(String key, String value) {
        return EMPTY_ROW.replaceFirst(",,,,,,,", "FALSE,FALSE,," + key + ",,,," + value);
    }

    @Override
    protected String getControlBlockData() {
        return controlBlockData;
    }

    @Override
    protected Map<String, Restaurant> getAllRestaurants() {
        return allRestaurants;
    }
}

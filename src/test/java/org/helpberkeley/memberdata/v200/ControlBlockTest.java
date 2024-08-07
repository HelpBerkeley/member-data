/*
 * Copyright (c) 2021-2024. helpberkeley.org
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
package org.helpberkeley.memberdata.v200;

import org.helpberkeley.memberdata.*;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.helpberkeley.memberdata.v200.BuilderConstants.*;

@SuppressWarnings("unchecked")
public class ControlBlockTest extends ControlBlockTestBase {

    private final Map<String, Restaurant> allRestaurants;

    public ControlBlockTest() {
        RestaurantTemplateParser parser =
                RestaurantTemplateParser.create(readResourceFile("restaurant-template-v200.csv"));
        allRestaurants = parser.restaurants();
    }

    @Override
    public Map<String, Restaurant> getAllRestaurants() {
        return allRestaurants;
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public String getBeginRow() {
        return CONTROL_BLOCK_BEGIN_ROW;
    }

    @Override
    public String getEndRow() {
        return CONTROL_BLOCK_END_ROW;
    }

    @Override
    public String getVersionRow() {
        return CONTROL_BLOCK_VERSION_ROW;
    }

    @Override
    public String getVersion() {
        return Constants.CONTROL_BLOCK_VERSION_200;
    }

    @Override
    public String getEmptyRow() {
        return EMPTY_ROW;
    }

    @Override
    public String getDirectiveRow(String directive) {
        return BuilderConstants.EMPTY_ROW.replaceFirst(",,,", "FALSE,FALSE," + directive + ",");
    }

    @Override
    public String getKeyValueRow(String key, String value) {
        return BuilderConstants.EMPTY_ROW.replaceFirst(",,,,,,,", "FALSE,FALSE,," + key + ",,,," + value);
    }

    @Override
    public void audit(ControlBlock controlBlock) {
        assertThat(controlBlock).isInstanceOf(ControlBlockV200.class);
        ((ControlBlockV200)controlBlock).audit(users, Map.of(), List.of());
    }

    @Override
    public String addVersionSpecificRequiredVariables() {
        return "";
    }

    @Test
    public void altMealOptionsTest() {
        String key = Constants.CONTROL_BLOCK_ALT_MEAL_OPTIONS;
        String value = "\"none, veggie, noRed,noPork \"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(Collections.EMPTY_MAP, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.UNSUPPORTED, key, 4, Constants.CONTROL_BLOCK_VERSION_200));
    }

    @Test
    public void altGroceryOptionsTest() {
        String key = Constants.CONTROL_BLOCK_ALT_GROCERY_OPTIONS;
        String value = "\"none, veg, custom pick\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(Collections.EMPTY_MAP, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.UNSUPPORTED, key, 4, Constants.CONTROL_BLOCK_VERSION_200));
    }

    @Test
    public void startTimesTest() {
        String key = Constants.CONTROL_BLOCK_START_TIMES;
        String value = "\"3:00, 3:10, 3:15\"";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(Collections.EMPTY_MAP, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.UNSUPPORTED, key, 4, Constants.CONTROL_BLOCK_VERSION_200));
    }

    @Test
    public void pickupManagersTest() {
        String key = Constants.CONTROL_BLOCK_PICKUP_MANAGER;
        String value = "JVol";

        String workFlowData = HEADER
                + CONTROL_BLOCK_BEGIN_ROW
                + CONTROL_BLOCK_VERSION_ROW
                + getKeyValueRow(key, value)
                + CONTROL_BLOCK_END_ROW;

        WorkflowParser workflowParser = WorkflowParser.create(Collections.EMPTY_MAP, workFlowData);
        ControlBlock controlBlock = workflowParser.controlBlock();
        assertThat(controlBlock.getWarnings()).contains(
                MessageFormat.format(ControlBlock.UNSUPPORTED, key, 4, Constants.CONTROL_BLOCK_VERSION_200));
    }

    /** Verify audit of a split restaurant not having a cleanup driver in the control block */
    @Test
    public void auditSplitRestaurantNoCleanupTest() {

        Throwable thrown = catchThrowable(() ->DriverPostFormat.create(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-missing-cleanup.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.MISSING_SPLIT_RESTAURANT, "Cafe Raj"));
    }

    /** Verify disabled audit of a split restaurant not having a cleanup driver in the control block */
    @Test
    public void disabledAuditSplitRestaurantNoCleanupTest() {
        DriverPostFormat.create(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-missing-cleanup-audit-disabled.csv"));
    }

    /** Verify audit of a split restaurant with an unknown restaurant name */
    @Test
    public void auditSplitRestaurantBadNameTest() {

        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-restaurant-unknown.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.MISSING_SPLIT_RESTAURANT, "Cafe Raj"));
    }

    /**
     *  Verify audit of a split restaurant specifying a cleanup driver
     * that isn't a driver for that restaurant.
     */
    @Test
    public void auditSplitRestaurantWrongDriverTest() {

        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-wrong-cleanup.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.WRONG_CLEANUP_DRIVER, "JVol", "Cafe Raj"));
    }

    /**
     *  Verify audit of a split restaurant specifying a cleanup driver
     * that isn't a driver for that restaurant.
     */
    @Test
    public void auditSplitRestaurantUnknownDriverTest() {

        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users,
                readResourceFile("routed-deliveries-split-unknown-cleanup.csv")));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                MessageFormat.format(ControlBlock.UNKNOWN_CLEANUP_DRIVER, "Sparkles", "Cafe Raj"));
    }
}

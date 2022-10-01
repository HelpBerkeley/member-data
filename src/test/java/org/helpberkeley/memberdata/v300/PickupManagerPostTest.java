/*
 * Copyright (c) 2021-2022. helpberkeley.org
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
package org.helpberkeley.memberdata.v300;

import org.helpberkeley.memberdata.*;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PickupManagerPostTest extends TestBase {

    private final String oneDriverOneSource;
    private final String multiDriverOneSource;
    private final Map<String, User> users;
    private final int pickupManagerPostFormatQuery = Constants.QUERY_GET_ONE_KITCHEN_DRIVERS_TABLE_POST_FORMAT_V300;

    public PickupManagerPostTest() {
        oneDriverOneSource = readResourceFile("routed-deliveries-v300.csv");
        multiDriverOneSource = readResourceFile("routed-deliveries-multi-driver-v300.csv");

        Loader loader = new Loader(createApiSimulator());
        users = new Tables(loader.load()).mapByUserName();
    }

    @Test
    public void v300SingleDriverLoopTest() {
        String format = "LOOP &{Driver} { "
                + " LOOP &{Driver.Consumer} {"
                + "    &{Driver.Name}"
                + "    \"|\""
                + "    &{Driver.UserName}"
                + "    \"|\""
                + "    &{Driver.CompactPhone}"
                + "    \"|\""
                + "    &{Consumer.Name}"
                + "    \"|\""
                + "    &{Consumer.UserName}"
                + "    \"|\""
                + "    &{Consumer.StandardMeal}"
                + "    \"|\""
                + "    IF &{Consumer.AlternateMeal} THEN {"
                + "        &{Consumer.AlternateMealType} \":\" &{Consumer.AlternateMeal}"
                + "    }"
                + "    \"|\""
                + "    &{Consumer.StandardGrocery}"
                + "    \"|\""
                + "    IF &{Consumer.AlternateGrocery} THEN {"
                + "        &{Consumer.AlternateGroceryType} \":\" &{Consumer.AlternateGrocery}"
                + "    }"
                + "    \"\\n\""
                + " }"
                + "}";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, oneDriverOneSource);
        // FIX THIS, DS: refactor.  make generic and have empty impl in V200?
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 1|Cust1|0|noRed:1|0|\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 2|Cust2|2||2|\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 3|Cust3|0||2|\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 4|Cust4|0|veggie:1|0|veg:1\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 5|Cust5|0|noPork:1|0|\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 6|Cust6|0||0|custom pick:1\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 7|Cust7|0|veggie:1|0|custom pick:1\n");
    }

    @Test
    public void v300MultiDriverLoopTest() {
        String format = "LOOP &{Driver} { "
                + " LOOP &{Driver.Consumer} {"
                + "    &{Driver.Name}"
                + "    \"|\""
                + "    &{Driver.UserName}"
                + "    \"|\""
                + "    &{Driver.CompactPhone}"
                + "    \"|\""
                + "    &{Consumer.Name}"
                + "    \"|\""
                + "    &{Consumer.UserName}"
                + "    \"|\""
                + "    &{Consumer.StandardMeal}"
                + "    \"|\""
                + "    IF &{Consumer.AlternateMeal} THEN {"
                + "        &{Consumer.AlternateMealType} \":\" &{Consumer.AlternateMeal}"
                + "    }"
                + "    \"|\""
                + "    &{Consumer.StandardGrocery}"
                + "    \"|\""
                + "    IF &{Consumer.AlternateGrocery} THEN {"
                + "        &{Consumer.AlternateGroceryType} \":\" &{Consumer.AlternateGrocery}"
                + "    }"
                + "    \"\\n\""
                + " }"
                + "}";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, multiDriverOneSource);
        // FIX THIS, DS: refactor.  make generic and have empty impl in V200?
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 1|Cust1|0|noRed:1|0|\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 2|Cust2|2||2|\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 3|Cust3|0||2|\n"
                + "Joe B. Driver|jbDriver|(777) 777.7777|Cust Name 4|Cust4|0|veggie:1|0|veg:1\n"
                + "Josephine B. Driver|jsDriver|(888) 888.8888|Cust Name 5|Cust5|0|noPork:1|0|\n"
                + "Josephine B. Driver|jsDriver|(888) 888.8888|Cust Name 6|Cust6|0||0|custom pick:1\n"
                + "Josephine B. Driver|jsDriver|(888) 888.8888|Cust Name 7|Cust7|0|veggie:1|0|custom pick:1\n");
    }

    @Test
    public void v300DriversTableSingleDriverTest() {
        String format = "LOOP &{Driver} { "
                + "  &{Driver.StartTime}"
                + "  \"|\""
                + "  &{Driver.UserName}"
                + "  \"|\""
                + "  &{Driver.CompactPhone}"
                + "  \"|\""
                + "  &{Driver.StandardMeals}"
                + "  \"|\""
                + "  LOOP &{AlternateMeals} {"
                + "    &{AlternateMeals.Count}"
                + "    \"|\""
                + "  }"
                + "  &{Driver.StandardGroceries}"
                + "  \"|\""
                + "  LOOP &{AlternateGroceries} {"
                + "    &{AlternateGroceries.Count}"
                + "    \"|\""
                + "  }"
                + "}";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, oneDriverOneSource);
        // FIX THIS, DS: refactor.  make generic and have empty impl in V200?
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("3:00|jbDriver|(777) 777.7777|2|2|1|1|4|1|2|");
    }

    @Test
    public void v300DriversTableMultiDriverTest() {
        String format = "LOOP &{Driver} { "
                + "  &{Driver.StartTime}"
                + "  \"|\""
                + "  &{Driver.UserName}"
                + "  \"|\""
                + "  &{Driver.CompactPhone}"
                + "  \"|\""
                + "  &{Driver.StandardMeals}"
                + "  \"|\""
                + "  LOOP &{AlternateMeals} {"
                + "    &{AlternateMeals.Count}"
                + "    \"|\""
                + "  }"
                + "  &{Driver.StandardGroceries}"
                + "  \"|\""
                + "  LOOP &{AlternateGroceries} {"
                + "    &{AlternateGroceries.Count}"
                + "    \"|\""
                + "  }"
                + " \"\\n\""
                + "}";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, multiDriverOneSource);
        // FIX THIS, DS: refactor.  make generic and have empty impl in V200?
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo(
                "3:00|jbDriver|(777) 777.7777|2|1|1|0|4|1|0|\n"
                + "3:05|jsDriver|(888) 888.8888|0|1|0|1|0|0|2|\n");
    }

    @Test
    public void v300TotalsSingleDriverTest() {
        String format = "${TotalStandardMeal}"
                + "\"|\""
                + "LOOP &{AlternateMeals} {"
                + "    &{AlternateMeals.Total} \" \" &{AlternateMeals.Type}"
                + "    \"|\""
                + "}"
                + "${TotalStandardGrocery}"
                + "\"|\""
                + "LOOP &{AlternateGroceries} {"
                + "    &{AlternateGroceries.Total} \" \" &{AlternateGroceries.Type}"
                + "    \"|\""
                + "}"
                + "\"\\n\"";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, oneDriverOneSource);
        // FIX THIS, DS: refactor.  make generic and have empty impl in V200?
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("2|2 veggie|1 noRed|1 noPork|4|1 veg|2 custom pick|\n");
    }

    @Test
    public void v300TotalsMultipleDriverTest() {
        String format = "${TotalStandardMeal}"
                + "\"|\""
                + "LOOP &{AlternateMeals} {"
                + "    &{AlternateMeals.Total} \" \" &{AlternateMeals.Type}"
                + "    \"|\""
                + "}"
                + "${TotalStandardGrocery}"
                + "\"|\""
                + "LOOP &{AlternateGroceries} {"
                + "    &{AlternateGroceries.Total} \" \" &{AlternateGroceries.Type}"
                + "    \"|\""
                + "}"
                + "\"\\n\"";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, multiDriverOneSource);
        // FIX THIS, DS: refactor.  make generic and have empty impl in V200?
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("2|2 veggie|1 noRed|1 noPork|4|1 veg|2 custom pick|\n");
    }

    @Test
    public void v300PickupManagersTest() {
        String format = "LOOP &{PickupManager} {"
                + "&{PickupManager.UserName}"
                + "\"|\""
                + "}"
                + "\"\\n\"";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, oneDriverOneSource);
        // FIX THIS, DS: refactor.  make generic and have empty impl in V200?
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("ZZZ|ThirdPerson|\n");
    }

    @Test
    public void v300MealsOnlyTest() {

        String format = "IF ${MealsOnlyRun} THEN { \"true\" } "
                + "\"|\""
                + "IF NOT ${MealsOnlyRun} THEN { \"true\" } "
                + "\"|\""
                + "IF ${GroceriesOnlyRun} THEN { \"true\" } "
                + "\"|\""
                + "IF NOT ${GroceriesOnlyRun} THEN { \"true\" } ";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));

        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        controlBlock.withFoodSources("BFN|");
        WorkflowBuilder workflow = new WorkflowBuilder()
            .withControlBlock(controlBlock);
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, workflow.build());
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("true|||true");
    }

    @Test
    public void v300GroceriesOnlyTest() {

        String format = "IF ${MealsOnlyRun} THEN { \"true\" } "
                + "\"|\""
                + "IF NOT ${MealsOnlyRun} THEN { \"true\" } "
                + "\"|\""
                + "IF ${GroceriesOnlyRun} THEN { \"true\" } "
                + "\"|\""
                + "IF NOT ${GroceriesOnlyRun} THEN { \"true\" } ";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));

        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        controlBlock.withFoodSources("|Safeway");
        WorkflowBuilder workflow = new WorkflowBuilder()
                .withControlBlock(controlBlock);
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, workflow.build());
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        assertThat(post).isEqualTo("|true|true|");
    }

    @Test
    public void v300SingleBackupDriverTest() {
        String format = "LOOP &{BackupDriver} {"
                + "${BackupDriver.Name}\",\""
                + "${BackupDriver.UserName}\",\""
                + "${BackupDriver.CompactPhone}\"\\n\""
                + "}";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        controlBlock.withFoodSources("|Safeway");
        controlBlock .withBackupDriver("MrBackup772");
        WorkflowBuilder workflow = new WorkflowBuilder()
                .withControlBlock(controlBlock);
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, workflow.build());
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        System.out.println(post);
        assertThat(post).isEqualTo("Scotty J Backup 772th,MrBackup772,(123) 456.7890\n");
    }

    @Test
    public void v300MultiBackupDriverTest() {
        String format = "LOOP &{BackupDriver} {"
                + "${BackupDriver.Name}\",\""
                + "${BackupDriver.UserName}\",\""
                + "${BackupDriver.CompactPhone}\"\\n\""
                + "}";
        HttpClientSimulator.setQueryResponseData(pickupManagerPostFormatQuery, createMessageBlock(format));
        ControlBlockBuilder controlBlock = new ControlBlockBuilder();
        controlBlock.withFoodSources("|Safeway");
        controlBlock .withBackupDriver("MrBackup772");
        controlBlock .withBackupDriver("jsDriver");
        WorkflowBuilder workflow = new WorkflowBuilder()
                .withControlBlock(controlBlock);
        DriverPostFormat driverPostFormat =
                DriverPostFormat.create(createApiSimulator(), users, workflow.build());
        String post = ((DriverPostFormatV300)driverPostFormat).generateDriversTablePost();
        System.out.println(post);
        assertThat(post).isEqualTo("Scotty J Backup 772th,MrBackup772,(123) 456.7890\n"
                + "Josephine B Driver,jsDriver,(888) 888.8888\n");
    }
}

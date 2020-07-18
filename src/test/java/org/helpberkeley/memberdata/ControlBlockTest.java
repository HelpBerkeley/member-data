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

import com.opencsv.exceptions.CsvValidationException;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class ControlBlockTest extends TestBase {

    private final String controlBlockData;

    private static final String HEADER =
        "Consumer,Driver,Name,User Name,Phone #,Phone2 #,Neighborhood,City,"
        + "Address,Condo,Details,Restaurants,normal,veggie,#orders\n";
    private static final String  CONTROL_BLOCK_BEGIN_ROW =
            "FALSE,FALSE," + Constants.CONTROL_BLOCK_BEGIN + ",,,,,,,,,,,,\n";

    public ControlBlockTest() {
        controlBlockData = readResourceFile("control-block.csv");
    }

    @Test
    public void controlBlockTest() throws IOException, CsvValidationException {
        WorkflowParser workflowParser =
                new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, controlBlockData);

        workflowParser.drivers();
    }

    /** Test that true in a consumer column throws an exception */
    @Test
    public void consumerTrueTest() throws IOException {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "TRUE,FALSE,,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Consumer column does not contain FALSE, at line 3.\n");
    }

    /** Test that true in a driver column throws an exception */
    @Test
    public void driverTrueTest() throws IOException {

        String workFlowData = HEADER + CONTROL_BLOCK_BEGIN_ROW + "FALSE,TRUE,,,,,,,,,,,,,\n";

        WorkflowParser workflowParser = new WorkflowParser(WorkflowParser.Mode.DRIVER_MESSAGE_REQUEST, workFlowData);

        Throwable thrown = catchThrowable(workflowParser::drivers);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage("Control block Driver column does not contain FALSE, at line 3.\n");
    }
}

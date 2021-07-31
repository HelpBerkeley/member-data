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
package org.helpberkeley.memberdata.v200;

import java.util.ArrayList;
import java.util.List;

import static org.helpberkeley.memberdata.v200.BuilderConstants.*;

public class WorkflowBuilder {

    public static final String DEFAULT_GMAP_URL =
            "\"https://www.google.com/maps/dir/something+something+else\",,,,,,,,,,,,,,\n";

    private final List<DriverBlockBuilder> driverBlocks = new ArrayList<>();
    private ControlBlockBuilder controlBlockBuilder = new ControlBlockBuilder();
    private String finalRow;

    @Override
    public String toString() {
        return build();
    }

    public String build() {
        StringBuilder workflow = new StringBuilder();

        workflow.append(controlBlockBuilder.build());
        driverBlocks.forEach(workflow::append);

        if (finalRow != null) {
            workflow.append(finalRow);
        }

        return workflow.toString();
    }

    public WorkflowBuilder withDriverBlock(DriverBlockBuilder driverBlock) {
        driverBlocks.add(driverBlock);
        return this;
    }

    public WorkflowBuilder withControlBlock(ControlBlockBuilder controlBlock) {
        controlBlockBuilder = controlBlock;
        return this;
    }

    public WorkflowBuilder withTrailingEmptyRowNotEnoughColumns() {
        finalRow = EMPTY_ROW.substring(3);
        return this;
    }

    public WorkflowBuilder withTrailingEmptyRowTooManyColumns() {
        finalRow = ",,," + BuilderConstants.EMPTY_ROW;
        return this;
    }
}

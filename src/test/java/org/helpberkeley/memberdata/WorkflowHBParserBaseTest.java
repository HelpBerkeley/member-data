/*
 * Copyright (c) 2020-2024 helpberkeley.org
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

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public abstract class WorkflowHBParserBaseTest extends TestBase {

    protected final Map<String, User> users;

    public WorkflowHBParserBaseTest() {
        Loader loader = new Loader(createApiSimulator());
        users = new Tables(loader.load()).mapByUserName();
    }

    public abstract List<String> getColumnNames();
    public abstract String getMinimumControlBlock();
    public abstract String getHeader();

    @Test
    public void missingHeaderRowTest() {
        String controlBlock = getMinimumControlBlock();
        // Remove header line
        String headerless = controlBlock.substring(controlBlock.indexOf('\n') + 1 );
        List<String> badHeader;
        try (StringReader reader = new StringReader(headerless)) {
            CSVListReader csvReader = new CSVListReader(reader);
            badHeader = csvReader.readNextToList();
        }
        Throwable thrown = catchThrowable(() -> DriverPostFormat.create(createApiSimulator(), users, headerless));
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessage(MessageFormat.format(
                ControlBlock.MISSING_OR_INVALID_HEADER_ROW, badHeader.toString()));
    }

    @Test
    public void missingHeaderColumnTest() {
        List<String> columnNames = getColumnNames();

        for (int columnNum = 0; columnNum < columnNames.size(); columnNum++) {
            // Build header with columnNum column missing

            StringBuilder header = new StringBuilder();
            for (int index = 0; index < columnNames.size(); index++) {
                if (index == columnNum) {
                    continue;
                }
                header.append(columnNames.get(index)).append(',');
            }
            header.append('\n');

            String minimumControlBlock = getMinimumControlBlock();
            header.append(minimumControlBlock);

            final String expected = header.toString();

            Throwable thrown = catchThrowable(() -> WorkflowParser.create(Collections.emptyMap(), expected));
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(columnNames.get(columnNum));
        }
    }

    @Test
    public void invalidColumnNameTest() {

        List<String> columnNames = List.of(
                Constants.WORKFLOW_CONSUMER_COLUMN,
                Constants.WORKFLOW_DRIVER_COLUMN,
                Constants.WORKFLOW_NAME_COLUMN,
                Constants.WORKFLOW_USER_NAME_COLUMN,
                Constants.WORKFLOW_PHONE_COLUMN,
                Constants.WORKFLOW_ALT_PHONE_COLUMN,
                Constants.WORKFLOW_NEIGHBORHOOD_COLUMN,
                Constants.WORKFLOW_CITY_COLUMN,
                Constants.WORKFLOW_ADDRESS_COLUMN,
                Constants.WORKFLOW_CONDO_COLUMN,
                Constants.WORKFLOW_DETAILS_COLUMN);

        for (int columnNum = 0; columnNum < columnNames.size(); columnNum++) {

            String columnName = columnNames.get(columnNum);
            String delimitedColumnName = "";
            String delimitedReplacement = "";

            if (columnNum != 0) {
                delimitedColumnName = ",";
                delimitedReplacement = ",";
            }

            delimitedColumnName += columnName + ",";
            delimitedReplacement += "bogus" + columnNum + ",";

            String minimumControlBlock = (getHeader() + getMinimumControlBlock()).replace(
                    delimitedColumnName, delimitedReplacement);

            Throwable thrown = catchThrowable(() ->
                    WorkflowParser.create(Collections.emptyMap(), minimumControlBlock));

            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(MessageFormat.format(
                    WorkflowParser.ERROR_MISSING_HEADER_COLUMN, columnName));
        }
    }
}

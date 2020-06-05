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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverPostFormat {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverPostFormat.class);

    private final ApiClient apiClient;
    private String restaurantTemplate;
    private final List<Block> blocks = new ArrayList<>();
    private List<Driver> drivers;

    DriverPostFormat(ApiClient apiClient, final String routedDeliveries)
            throws IOException, InterruptedException, CsvValidationException {
        this.apiClient = apiClient;
        loadRestaurantTemplate();
        loadFormatTopic();
        loadRoutedDeliveries(routedDeliveries);
    }

    private void loadRestaurantTemplate() throws IOException, InterruptedException {
        String rawPost = Parser.postBody(apiClient.getPost(Main.RESTAURANT_TEMPLATE_POST_ID));
        RestaurantTemplatePost restaurantTemplatePost = Parser.restaurantTemplatePost(rawPost);
        restaurantTemplate = apiClient.downloadFile(restaurantTemplatePost.uploadFile.fileName);
    }

    private void loadFormatTopic() throws IOException, InterruptedException {
        String json = apiClient.runQuery(Constants.QUERY_GET_DRIVERS_POST_FORMAT);
        ApiQueryResult apiQueryResult = Parser.parseQueryResult(json);

        for (Object rowObj : apiQueryResult.rows) {
            Object[] columns = (Object[]) rowObj;
            assert columns.length == 2 : columns.length;

            Long id = ((Long)columns[0]);
            // Normalize EOL
            String raw = ((String)columns[1]).replaceAll("\\r\\n?", "\n");

            // Look for block name of the form:
            // [This is the block name]\n
            String pattern = "^\\[([A-Za-z0-9 ]+)\\]";

            Pattern re = Pattern.compile(pattern);
            Matcher match = re.matcher(raw);

            if (! match.find()) {
                LOGGER.warn("Cannot find block name in {}", raw);
                continue;
            }

            String blockName = match.group(1);
            String blockContents = raw.substring(match.end());
            blocks.add(new Block(raw, blockName, blockContents));
        }
    }

    private void loadRoutedDeliveries(final String routedDeliveries) throws IOException, CsvValidationException {
        RoutedDeliveriesParser parser = new RoutedDeliveriesParser(routedDeliveries);
        drivers = parser.drivers();
    }

    private static class Block {
        final String raw;
        final String name;
        final String contents;
        boolean hasConditional;
        String conditionalVariableName;
        final Map<String, Variable> variables = new HashMap<>();

        Block(final String raw, final String name, final String contents) {
            this.raw = raw;
            this.name = name;
            this.contents = contents;

            findVariables();
            checkForConditional();
        }

        boolean hasConditional() {
            return hasConditional;
        }

        private void findVariables() {

            int previousIndex = 0;
            int varStartIndex;

            while ((varStartIndex = contents.indexOf("${", previousIndex)) != -1) {
                int varEndIndex = contents.indexOf("}", varStartIndex);
                assert (varEndIndex != -1) : "malformed variable at offset " + varStartIndex + " in " + contents;
                String variable = contents.substring(varStartIndex, varEndIndex + 1);
                previousIndex = varEndIndex + 1;

                LOGGER.debug("found variable {} in {}", variable, name);
                createVariable(variable);
            }
        }

        private void createVariable(final String name) {

            if (isComposite(name)) {
            }

        }

        private boolean isComposite(final String variableName) {
            return variableName.contains(".");
        }

        private void checkForConditional() {

            String contentsTrimmed = contents.trim();

            if (!contentsTrimmed.startsWith("IF ${")) {
                hasConditional = false;
                return;
            }

            hasConditional = true;
            int endIndex = contentsTrimmed.indexOf("}");
            assert (endIndex != -1) : "malformed conditional in " + contents;
            conditionalVariableName = contentsTrimmed.substring(0, endIndex + 1);
        }
    }

    private static class Variable {
        final String name;

        Variable(final String name) {
            this.name = name;
        }
    }

    private static class CompositeVariable extends Variable{
        final Map<String, Variable> variables = new HashMap<>();

        CompositeVariable(final String name) {
            super(name);
        }
    }
}

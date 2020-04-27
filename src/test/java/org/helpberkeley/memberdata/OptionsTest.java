//
// Copyright (c) 2020 helpberkeley.org
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class OptionsTest extends TestBase {

    private static final String TEST_FILE_NAME = "something.csv";

    private static final String[] COMMANDS_WITH_URL = {
            Options.COMMAND_POST_ALL_MEMBERS,
            Options.COMMAND_POST_WORKFLOW,
            Options.COMMAND_POST_DRIVERS,
            Options.COMMAND_POST_INREACH,
            Options.COMMAND_UPDATE_DRIVERS,
    };

    private static final String[] COMMANDS_WITH_FILE = {
            Options.COMMAND_POST_ERRORS,
            Options.COMMAND_POST_CONSUMER_REQUESTS,
            Options.COMMAND_POST_VOLUNTEER_REQUESTS,
            Options.COMMAND_UPDATE_ERRORS,
            Options.COMMAND_UPDATE_CONSUMER_REQUESTS,
            Options.COMMAND_UPDATE_VOLUNTEER_REQUESTS,
    };

    @Test
    public void noCommandTest() {

        Options options = new Options(new String[0]);
        options.setExceptions(true);

        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.MISSING_COMMAND);
        assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
    }

    @Test
    public void unknownCommandTest() {

        String command = "exacerbate";

        Options options = new Options(new String[] { command });
        options.setExceptions(true);

        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.UNKNOWN_COMMAND + command);
        assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
        assertThat(thrown).hasMessageContaining(Options.USAGE);
    }

    @Test
    public void tooManyCommandsTest() {

        Options options = new Options(new String[] { Options.COMMAND_FETCH, Options.COMMAND_POST_ERRORS });
        options.setExceptions(true);

        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.TOO_MANY_COMMANDS);
        assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
        assertThat(thrown).hasMessageContaining(Options.USAGE);
    }

    @Test
    public void postErrorsTest() {
        Options options = new Options(new String[] { Options.COMMAND_POST_ERRORS, TEST_FILE_NAME });
        options.setExceptions(true);

        options.parse();
        assertThat(options.getCommand()).isEqualTo(Options.COMMAND_POST_ERRORS);
        assertThat(options.getFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(options.getShortURL()).isNull();
    }

    @Test
    public void missingFileTest() {
        for (String command : COMMANDS_WITH_FILE) {

            Options options = new Options(new String[]{command});
            options.setExceptions(true);

            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.COMMAND_REQUIRES_FILE_NAME);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }

    }

    @Test
    public void urlCommandMissingFileTest() {

        for (String command : COMMANDS_WITH_URL) {

            Options options = new Options(new String[]{command});
            options.setExceptions(true);

            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.COMMAND_REQUIRES_FILE_NAME);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void missingURLTest() {

        for (String command : COMMANDS_WITH_URL) {

            Options options = new Options(new String[]{command, "someFile.csv"});
            options.setExceptions(true);

            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.COMMAND_REQUIRES_SHORT_URL);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void poorlyFormedURLTest() {

        for (String command : COMMANDS_WITH_URL) {

            Options options = new Options(new String[]{command, "someFile.csv", "someFile.csv"});
            options.setExceptions(true);

            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.BAD_SHORT_URL);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void shortURLTest() {

        String fileName = "someFile.csv";
        String shortURL = "upload://asfasdfasdf.csv";

        for (String command : COMMANDS_WITH_URL) {
            Options options = new Options(new String[] { command, fileName, shortURL });
            options.parse();
            assertThat(options.getFileName()).isEqualTo(fileName);
            assertThat(options.getShortURL()).isEqualTo(shortURL);
        }
    }

    @Test
    public void fileTest() {

        String fileName = "someFile.csv";

        for (String command : COMMANDS_WITH_FILE) {
            Options options = new Options(new String[] { command, fileName });
            options.parse();
            assertThat(options.getFileName()).isEqualTo(fileName);
        }
    }
}

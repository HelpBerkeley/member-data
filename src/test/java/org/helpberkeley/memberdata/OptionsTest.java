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

    @Test
    public void noCommandTest() {

        Options options = new Options(new String[0]);
        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.USAGE);
    }

    @Test
    public void unknownCommandTest() {

        String command = "exacerbate";

        Options options = new Options(new String[] { command });
        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.UNKNOWN_COMMAND + command);
        assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
        assertThat(thrown).hasMessageContaining(Options.USAGE);
    }

    @Test
    public void tooManyArgumentsTest() {

        Options options = new Options(new String[] { Options.COMMAND_FETCH, Options.COMMAND_POST_ERRORS });
        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.USAGE);
    }

    @Test
    public void missingFileTest() {
        for (String command : COMMANDS_WITH_FILE) {

            Options options = new Options(new String[]{command});
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
            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.COMMAND_REQUIRES_FILE_NAME);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void badFileTest() {
        for (String command : COMMANDS_WITH_FILE) {
            String badFileName = "cannot-find-me";
            Options options = new Options(new String[]{command, badFileName});
            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(badFileName);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.FILE_DOES_NOT_EXIST);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void urlBadFileTest() {
        for (String command : COMMANDS_WITH_URL) {
            String badFileName = "cannot-find-me";
            Options options = new Options(new String[]{command, badFileName, TEST_SHORT_URL});
            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(badFileName);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.FILE_DOES_NOT_EXIST);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void missingURLTest() {

        for (String command : COMMANDS_WITH_URL) {

            Options options = new Options(new String[]{command, TEST_FILE_NAME});
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

            Options options = new Options(new String[]{command, TEST_FILE_NAME, TEST_FILE_NAME});
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

        for (String command : COMMANDS_WITH_URL) {
            Options options = new Options(new String[] { command, TEST_FILE_NAME, TEST_SHORT_URL});
            options.parse();
            assertThat(options.getCommand()).isEqualTo(command);
            assertThat(options.getFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(options.getShortURL()).isEqualTo(TEST_SHORT_URL);
        }
    }

    @Test
    public void fileTest() {

        for (String command : COMMANDS_WITH_FILE) {
            Options options = new Options(new String[] { command, TEST_FILE_NAME });
            options.parse();
            assertThat(options.getCommand()).isEqualTo(command);
            assertThat(options.getFileName()).isEqualTo(TEST_FILE_NAME);
        }
    }

    @Test
    public void commandsWithoutParametersTest() {
        for (String command : COMMANDS_WITH_NO_PARAMETERS) {
            Options options = new Options(new String[] { command });
            options.parse();
            assertThat(options.getCommand()).isEqualTo(command);
        }
    }
}

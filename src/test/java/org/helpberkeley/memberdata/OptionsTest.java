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

import java.nio.file.Path;

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

    @Test
    public void topicIdTest() {
        for (String command : COMMANDS_WITH_TOPIC_ID) {
            Options options = new Options(new String[] { command, TEST_TOPIC_ID });
            options.parse();
            assertThat(options.getCommand()).isEqualTo(command);
            assertThat(options.getTopicId()).isEqualTo(Long.parseLong(TEST_TOPIC_ID));
            assertThat(options.getOutputDir()).isNull();
        }
    }

    @Test
    public void topicIdWithOutputDirTest() {
        String outputDir = "some-output-dir";
        for (String command : COMMANDS_WITH_TOPIC_ID_AND_OUTPUT_DIR) {
            Options options = new Options(new String[] { command, TEST_TOPIC_ID, outputDir });
            options.parse();
            assertThat(options.getCommand()).isEqualTo(command);
            assertThat(options.getTopicId()).isEqualTo(Long.parseLong(TEST_TOPIC_ID));
            assertThat(options.getOutputDir()).isEqualTo(outputDir);
        }
    }

    @Test
    public void resolvePostIdsOutputFileTest() {
        Options options = new Options(new String[] {
                Options.COMMAND_RESOLVE_POST_IDS, TEST_FILE_NAME, "post-ids.csv" });
        options.parse();

        assertThat(options.getFileName()).isEqualTo(TEST_FILE_NAME);
        assertThat(options.outputFileName()).isEqualTo("post-ids.csv");
    }

    @Test
    public void resolvePostIdsDefaultOutputFileTest() {
        Options options = new Options(new String[] {
                Options.COMMAND_RESOLVE_POST_IDS, TEST_FILE_NAME });
        options.parse();

        // Null means "derive it" - Main asks PostIdResolver.defaultOutputFor.
        assertThat(options.outputFileName()).isNull();
        assertThat(PostIdResolver.defaultOutputFor(TEST_FILE_NAME))
                .isEqualTo(Path.of("pom-post-ids.csv"));
    }

    @Test
    public void missingTopicIdTest() {
        for (String command : COMMANDS_WITH_TOPIC_ID) {
            Options options = new Options(new String[] { command });
            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.COMMAND_REQUIRES_TOPIC_ID);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void missingCategoryNameTest() {
        for (String command : COMMANDS_WITH_CATEGORY_NAME) {
            Options options = new Options(new String[] { command });
            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(command);
            assertThat(thrown).hasMessageContaining(Options.USAGE_ERROR);
            assertThat(thrown).hasMessageContaining(Options.COMMAND_REQUIRES_CATEGORY_NAME);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void badTopicIdTest() {
        String badTopicId = "not-a-number";
        for (String command : COMMANDS_WITH_TOPIC_ID) {
            Options options = new Options(new String[] { command, badTopicId });
            Throwable thrown = catchThrowable(options::parse);
            assertThat(thrown).isInstanceOf(MemberDataException.class);
            assertThat(thrown).hasMessageContaining(badTopicId);
            assertThat(thrown).hasMessageContaining(Options.BAD_TOPIC_ID);
            assertThat(thrown).hasMessageContaining(Options.USAGE);
        }
    }

    @Test
    public void deleteTopicImagePostsNoForceTest() {
        Options options = new Options(new String[] {
                Options.COMMAND_DELETE_TOPIC_IMAGE_POSTS, TEST_TOPIC_ID });
        options.parse();
        assertThat(options.getCommand()).isEqualTo(Options.COMMAND_DELETE_TOPIC_IMAGE_POSTS);
        assertThat(options.getTopicId()).isEqualTo(Long.parseLong(TEST_TOPIC_ID));
        assertThat(options.force()).isFalse();
    }

    @Test
    public void deleteTopicImagePostsForceTest() {
        Options options = new Options(new String[] {
                Options.COMMAND_DELETE_TOPIC_IMAGE_POSTS, TEST_TOPIC_ID, Options.FORCE_ARGUMENT });
        options.parse();
        assertThat(options.getCommand()).isEqualTo(Options.COMMAND_DELETE_TOPIC_IMAGE_POSTS);
        assertThat(options.getTopicId()).isEqualTo(Long.parseLong(TEST_TOPIC_ID));
        assertThat(options.force()).isTrue();
    }

    @Test
    public void deleteTopicImagePostsBadForceTest() {
        String badForce = "yes";
        Options options = new Options(new String[] {
                Options.COMMAND_DELETE_TOPIC_IMAGE_POSTS, TEST_TOPIC_ID, badForce });
        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.BAD_FORCE_ARGUMENT);
        assertThat(thrown).hasMessageContaining(badForce);
        assertThat(thrown).hasMessageContaining(Options.USAGE);
    }

    @Test
    public void deleteTopicImagePostsTooManyArgsTest() {
        Options options = new Options(new String[] {
                Options.COMMAND_DELETE_TOPIC_IMAGE_POSTS, TEST_TOPIC_ID, Options.FORCE_ARGUMENT, "extra" });
        Throwable thrown = catchThrowable(options::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(Options.USAGE);
    }

    @Test
    public void workflowStatusTest() {
        Options options = new Options(new String[] { Options.COMMAND_WORKFLOW, TEST_FILE_NAME, "true" });
        options.parse();
        assertThat(options.getCommand()).isEqualTo(Options.COMMAND_WORKFLOW);
        assertThat(options.postStatus()).isTrue();
    }

    @Test
    public void workflowNoStatusTest() {
        Options options = new Options(new String[] { Options.COMMAND_WORKFLOW, TEST_FILE_NAME });
        options.parse();
        assertThat(options.getCommand()).isEqualTo(Options.COMMAND_WORKFLOW);
        assertThat(options.postStatus()).isFalse();
    }

    @Test
    public void oneKitchenWorkflowStatusTest() {
        Options options = new Options(new String[] {
                Options.COMMAND_ONE_KITCHEN_WORKFLOW, TEST_FILE_NAME, "true" });
        options.parse();
        assertThat(options.getCommand()).isEqualTo(Options.COMMAND_ONE_KITCHEN_WORKFLOW);
        assertThat(options.postStatus()).isTrue();
    }

    @Test
    public void oneKitchenWorkflowNoStatusTest() {
        Options options = new Options(new String[] { Options.COMMAND_ONE_KITCHEN_WORKFLOW, TEST_FILE_NAME });
        options.parse();
        assertThat(options.getCommand()).isEqualTo(Options.COMMAND_ONE_KITCHEN_WORKFLOW);
        assertThat(options.postStatus()).isFalse();
    }
}

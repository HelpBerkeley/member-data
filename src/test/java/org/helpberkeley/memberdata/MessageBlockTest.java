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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class MessageBlockTest extends TestBase {

    @Test
    public void emptyBlockTest() {

        MessageBlock messageBlock = new MessageBlock(0, "");
        Throwable thrown = catchThrowable(messageBlock::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(
                "section: unknown, post: 0, line: 0, mismatched input '<EOF>' expecting '['");
    }

    @Test
    public void labelOnlyTest() {
        String block = "[labelOnly]\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("labelOnly");
    }

    @Test
    public void labelWithSpacesTest() {
        String block = "[ spaceyLabel ]\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("spaceyLabel");
    }

    @Test
    public void simpleVarTest() {
        String block =
            "[simpleVar]\n" +
            "${simpleVar}\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("simpleVar");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockSimpleRef.class);
        assertThat(messageBlock.getElements().get(0).getName()).isEqualTo("simpleVar");
    }

    @Test
    public void listVarTest() {
        String block =
            "[listVar]\n" +
            "&{listVar.v1}\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("listVar");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockListRef.class);
        assertThat(messageBlock.getElements().get(0).getName()).isEqualTo("listVar.v1");
    }

    @Test
    public void structVarTest() {
        String block =
            "[structVar]\n" +
            "${structVar.v1}\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("structVar");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockStructRef.class);
        assertThat(messageBlock.getElements().get(0).getName()).isEqualTo("structVar.v1");
    }

    @Test
    public void quotedStringTest() {
        String block =
            "[quotedString]\n" +
            "\"quoted string\"";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("quotedString");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockQuotedString.class);
        MessageBlockQuotedString quotedString = (MessageBlockQuotedString)messageBlock.getElements().get(0);
        assertThat(quotedString.getValue()).isEqualTo("quoted string");
    }

    @Test
    public void conditionalSimpleVarTest() {
        String block =
                "[conditionalSimpleVar]\n" +
                "IF ${var1} THEN { ${var2} }\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("conditionalSimpleVar");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockConditional.class);
        MessageBlockConditional conditional = (MessageBlockConditional)messageBlock.getElements().get(0);
        assertThat(conditional.getEvaluationType()).isEqualTo(MessageBlockConditional.EvaluationType.EVAL_TRUE);
        assertThat(conditional.getConditional().getName()).isEqualTo("var1");
        assertThat(conditional.getElements()).hasSize(1);
        assertThat(conditional.getElements().get(0).getName()).isEqualTo("var2");
    }

    @Test
    public void conditionalMultiElementTest() {
        String block =
                "[conditionalMultiElement]\n" +
                "IF ${var1} THEN {\n" +
                "   \"this is str1\n\" \n" +
                "   ${var2}\n" +
                "   \"this is str2\" \n" +
                "}";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("conditionalMultiElement");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockConditional.class);
        MessageBlockConditional conditional = (MessageBlockConditional)messageBlock.getElements().get(0);
        assertThat(conditional.getEvaluationType()).isEqualTo(MessageBlockConditional.EvaluationType.EVAL_TRUE);
        assertThat(conditional.getConditional().getName()).isEqualTo("var1");
        assertThat(conditional.getElements()).hasSize(3);
        assertThat(conditional.getElements().get(0).getName()).isEqualTo("QuotedString");
        assertThat(((MessageBlockQuotedString)conditional.getElements().get(0)).getValue())
                .isEqualTo("this is str1\n");
        assertThat(conditional.getElements().get(1).getName()).isEqualTo("var2");
        assertThat(conditional.getElements().get(2).getName()).isEqualTo("QuotedString");
        assertThat(((MessageBlockQuotedString)conditional.getElements().get(2)).getValue()).isEqualTo("this is str2");
    }

    @Test
    public void conditionalFollowedByQuotedStringTest() {
        String block =
                "[conditionalFollowedByQuotedString]\n" +
                "IF ${var1} THEN {\n" +
                "   ${var2}\n" +
                "}\n" +
                "\"un ver vert va vers un verre vert\"";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("conditionalFollowedByQuotedString");
        assertThat(messageBlock.getElements()).hasSize(2);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockConditional.class);
        MessageBlockConditional conditional = (MessageBlockConditional)messageBlock.getElements().get(0);
        assertThat(conditional.getEvaluationType()).isEqualTo(MessageBlockConditional.EvaluationType.EVAL_TRUE);
        assertThat(conditional.getConditional().getName()).isEqualTo("var1");
        assertThat(conditional.getElements()).hasSize(1);
        assertThat(conditional.getElements().get(0).getName()).isEqualTo("var2");
        assertThat(messageBlock.getElements().get(1)).isInstanceOf(MessageBlockQuotedString.class);
        assertThat(((MessageBlockQuotedString)messageBlock.getElements().get(1)).getValue())
                .isEqualTo("un ver vert va vers un verre vert");
    }

    @Test
    public void conditionalNotTest() {
        String block =
                "[conditionalSimpleVar]\n" +
                        "IF NOT ${var1} THEN { ${var2} }\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("conditionalSimpleVar");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockConditional.class);
        MessageBlockConditional conditional = (MessageBlockConditional)messageBlock.getElements().get(0);
        assertThat(conditional.getEvaluationType()).isEqualTo(MessageBlockConditional.EvaluationType.EVAL_FALSE);
        assertThat(conditional.getConditional().getName()).isEqualTo("var1");
        assertThat(conditional.getElements()).hasSize(1);
        assertThat(conditional.getElements().get(0).getName()).isEqualTo("var2");
    }

    @Test
    public void loopTest() {
        String block =
                "[loop]\n" +
                "LOOP &{listVar} {\n" +
                "   &{listVar.v1}\n" +
                "   &{listVar.v2}\n" +
                "}\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("loop");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockLoop.class);
        MessageBlockLoop loop = (MessageBlockLoop)messageBlock.getElements().get(0);
        assertThat(loop.getLoopRef()).isInstanceOf(MessageBlockListNameRef.class);
        assertThat(loop.getLoopRef().getName()).isEqualTo("listVar");
        assertThat(loop.getElements()).hasSize(2);
        assertThat(loop.getElements().get(0).getName()).isEqualTo("listVar.v1");
        assertThat(loop.getElements().get(1).getName()).isEqualTo("listVar.v2");
    }

    /** Test a block with two simple loops */
    @Test
    public void multipleLoopsTest() {
        String block =
            "[multiLoopBlock]\n" +
            "LOOP &{loopVar1} { \"string1\n\" }\n" +
            "LOOP &{loopVar2} { \"string2\" }";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("multiLoopBlock");
        assertThat(messageBlock.getElements()).hasSize(2);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockLoop.class);
        MessageBlockLoop loop = (MessageBlockLoop)messageBlock.getElements().get(0);
        assertThat(loop.getLoopRef()).isInstanceOf(MessageBlockListNameRef.class);
        assertThat(loop.getLoopRef().getName()).isEqualTo("loopVar1");
        assertThat(loop.getElements()).hasSize(1);
        assertThat(loop.getElements().get(0)).isInstanceOf(MessageBlockQuotedString.class);
        assertThat(((MessageBlockQuotedString)loop.getElements().get(0)).getValue()).isEqualTo("string1\n");
        loop = (MessageBlockLoop)messageBlock.getElements().get(1);
        assertThat(loop.getLoopRef().getName()).isEqualTo("loopVar2");
        assertThat(loop.getElements()).hasSize(1);
        assertThat(loop.getElements().get(0)).isInstanceOf(MessageBlockQuotedString.class);
        assertThat(((MessageBlockQuotedString)loop.getElements().get(0)).getValue()).isEqualTo("string2");
    }

    @Test
    public void loopListElementTest() {
        String block =
                "[loopListElement]\n" +
                        "LOOP &{List.LoopVar} {\n" +
                        "    &{LoopVar.v1}\n" +
                        "}\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();

        assertThat(messageBlock.getName()).isEqualTo("loopListElement");
        assertThat(messageBlock.getElements()).hasSize(1);
        assertThat(messageBlock.getElements().get(0)).isInstanceOf(MessageBlockLoop.class);
        MessageBlockLoop loop = (MessageBlockLoop)messageBlock.getElements().get(0);
        assertThat(loop.getLoopRef()).isInstanceOf(MessageBlockListRef.class);
        assertThat(loop.getLoopRef().getName()).isEqualTo("List.LoopVar");
        assertThat(loop.getElements()).hasSize(1);
        assertThat(loop.getElements().get(0)).isInstanceOf(MessageBlockListRef.class);
        MessageBlockListRef listRef = (MessageBlockListRef)loop.getElements().get(0);
        assertThat(listRef.getListName()).isEqualTo("LoopVar");
        assertThat(listRef.getName()).isEqualTo("LoopVar.v1");
    }

    @Test
    public void nestedCommentTest() {
        String block =
                "[comment]\n" + "hi mom\n"
                + "[comment]\n" + "bye mom\n";

        MessageBlock messageBlock = new MessageBlock(0, block);
        messageBlock.parse();
    }

    @Test
    public void tooManyBackquotesTest() {
        String block =
                "````\n" +
                "[tooManyBackQuotes]\n" +
                "```";
        MessageBlock messageBlock = new MessageBlock(0, block);
        Throwable thrown = catchThrowable(messageBlock::parse);
        assertThat(thrown).isInstanceOf(MemberDataException.class);
        assertThat(thrown).hasMessageContaining(block);
    }
}

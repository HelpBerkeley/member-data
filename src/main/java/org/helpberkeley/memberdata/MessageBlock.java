/*
 * Copyright (c) 2020-2021. helpberkeley.org
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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MessageBlock implements MessageBlockScope {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBlock.class);

    final long postNumber;
    final String raw;
    String name;

    private final Stack<MessageBlockScope> scopeStack = new Stack<>();
    private final List<MessageBlockElement> elements = new ArrayList<>();

    public MessageBlock(long postNumber, final String raw) {
        this.postNumber = postNumber;
        // Normalize EOL
        this.raw = raw.replaceAll("\\r\\n?", "\n");

        // quick search for the section name to provide the lexer and parser
        // with this information, that isn't otherwise available until the grammar finds it
        //
        int startIndex = this.raw.indexOf('[');
        int endIndex = this.raw.indexOf(']');

        if ((startIndex != -1) && (endIndex != -1)) {
            setName(this.raw.substring(startIndex + 1, endIndex));
        }
        scopeStack.push(this);
    }

    public void parse() {
        // create a lexer that feeds off of input CharStream
        MessageBlockLexer lexer = new MessageBlockBailLexer(name, postNumber, CharStreams.fromString(raw));

        // remove ConsoleErrorListener
        lexer.removeErrorListeners();

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        MessageBlockParser parser = new MessageBlockParser(tokens);

        // remove ConsoleErrorListener
        parser.removeErrorListeners();

        // Create/add custom error listener
        MessageBlockErrorListener errorListener = new MessageBlockErrorListener(name, postNumber);
        parser.addErrorListener(errorListener);

        // Begin parsing at the block rule
        ParseTree tree =  parser.block();

        // Check for parsing errors
        errorListener.throwIfErrorsPresent();

        LOGGER.trace(tree.toStringTree(parser));

        // Walk the tree
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new Listener(this), tree);
    }

    void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getPostNumber() {
        return postNumber;
    }

    @Override
    public void addElement(MessageBlockElement element) {
        elements.add(element);
    }

    public List<MessageBlockElement> getElements() {
        return elements;
    }

    private MessageBlockScope getScope() {
        assert ! scopeStack.isEmpty();
        return scopeStack.peek();
    }

    void addSimpleRef(String name) {
        getScope().addElement(new MessageBlockSimpleRef(name));
    }

    void addListNameRef(String name) {
        getScope().addElement(new MessageBlockListNameRef(name));
    }

    void addListRef(String name) {
        getScope().addElement(new MessageBlockListRef(name));
    }

    void addStructRef(String name) {
        getScope().addElement(new MessageBlockStructRef(name));
    }

    void addQuotedString(String string) {
        getScope().addElement(new MessageBlockQuotedString(string));
    }

    void addContinue() {
        // FIX THIS, DS: audit here that there is an enclosing loop?
        //               or do we have to wait until parse is complete?
        //               we could stil be in the middle of a conditional.
        getScope().addElement(new MessageBlockContinue());
    }

    void startConditional(MessageBlockConditional.EvaluationType evaluationType) {
        scopeStack.push(new MessageBlockConditional(evaluationType));
    }

    void completeConditional() {
        MessageBlockScope scope = scopeStack.pop();

        // FIX THIS, DS: refactor to get rid of the cast
        assert scope instanceof MessageBlockConditional : scope;
        getScope().addElement((MessageBlockElement)scope);
    }

    void startLoop() {
        scopeStack.push(new MessageBlockLoop());
    }

    void completeLoop() {
        MessageBlockScope scope = scopeStack.pop();

        // FIX THIS, DS: refactor to get rid of the cast
        assert scope instanceof MessageBlockLoop : scope;
        getScope().addElement((MessageBlockElement)scope);
    }

    public static class Listener extends MessageBlockBaseListener {

        private final MessageBlock messageBlock;

        Listener(MessageBlock messageBlock) {
            this.messageBlock = messageBlock;
        }

        @Override
        public void enterLabel(MessageBlockParser.LabelContext ctx) {
            messageBlock.setName(ctx.ID().getText());
        }

        @Override
        public void enterSimple_ref(MessageBlockParser.Simple_refContext ctx) {
            messageBlock.addSimpleRef(ctx.ID().getText());
        }

        @Override
        public void enterList_name_ref(MessageBlockParser.List_name_refContext ctx) {
            messageBlock.addListNameRef(ctx.ID().getText());
        }

        @Override
        public void enterList_ref(MessageBlockParser.List_refContext ctx) {
            messageBlock.addListRef(ctx.COMPOSITE().getText());
        }

        @Override
        public void enterStruct_ref(MessageBlockParser.Struct_refContext ctx) {
            messageBlock.addStructRef(ctx.COMPOSITE().getText());
        }

        @Override
        public void enterString(MessageBlockParser.StringContext ctx) {
            messageBlock.addQuotedString(ctx.QUOTED_STRING().getText());
        }

        @Override
        public void enterConditionalTrue(MessageBlockParser.ConditionalTrueContext ctx) {
            messageBlock.startConditional(MessageBlockConditional.EvaluationType.EVAL_TRUE);
        }

        @Override
        public void exitConditionalTrue(MessageBlockParser.ConditionalTrueContext ctx) {
            messageBlock.completeConditional();
        }

        @Override
        public void enterConditionalFalse(MessageBlockParser.ConditionalFalseContext ctx) {
            messageBlock.startConditional(MessageBlockConditional.EvaluationType.EVAL_FALSE);
        }

        @Override
        public void enterContinueElement(MessageBlockParser.ContinueElementContext ctx) {
            messageBlock.addContinue();
        }

        @Override
        public void exitConditionalFalse(MessageBlockParser.ConditionalFalseContext ctx) {
            messageBlock.completeConditional();
        }

        @Override
        public void enterLoopElement(MessageBlockParser.LoopElementContext ctx) {
            messageBlock.startLoop();
        }

        @Override
        public void exitLoopElement(MessageBlockParser.LoopElementContext ctx) {
            messageBlock.completeLoop();
        }
    }
}

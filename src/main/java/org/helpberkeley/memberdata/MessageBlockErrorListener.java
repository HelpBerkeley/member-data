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

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;

public class MessageBlockErrorListener extends BaseErrorListener {

    private final String blockName;
    private final long postNumber;

    MessageBlockErrorListener(String name, long postNumber) {
        this.blockName = (name == null) ? "unknown" : name;
        this.postNumber = postNumber;
    }

    private final StringBuilder errorMessages = new StringBuilder();

    void throwIfErrorsPresent() {
        if (errorMessages.length() != 0) {
            throw new MemberDataException(errorMessages.toString());
        }
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        System.out.println("reportAmbiguity");
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        System.out.println("reportAmbiguityFullContext");
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        System.out.println("reportContextSensitivity");
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                int line, int charPositionInLine, String msg, RecognitionException e) {

        errorMessages.append("section: ").append(blockName);
        errorMessages.append(", post: ").append(postNumber);
        errorMessages.append(", line: ").append(charPositionInLine).append(", ").append(msg).append("\n");
        underlineError(recognizer, (Token) offendingSymbol, line, charPositionInLine);
    }

    private void underlineError(Recognizer<?, ?> recognizer, Token offendingToken, int line, int charPositionInLine) {
        CommonTokenStream tokens =
                (CommonTokenStream) recognizer.getInputStream();
        String input = tokens.getTokenSource().getInputStream().toString();
        String[] lines = input.split("\n");
        String errorLine = lines[line - 1];
        errorMessages.append(errorLine).append('\n');
        errorMessages.append(" ".repeat(Math.max(0, charPositionInLine)));
        int start = offendingToken.getStartIndex();
        int stop = offendingToken.getStopIndex();
        if (start >= 0 && stop >= 0) {
            errorMessages.append("^".repeat(Math.max(0, stop - start + 1)));
        }
        errorMessages.append('\n');
    }
}

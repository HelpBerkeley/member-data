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

public class MessageBlockBailLexer extends MessageBlockLexer {

    final String blockName;
    final long postNumber;

    public MessageBlockBailLexer(String blockName, long postNumber, CharStream input) {
        super(input);
        this.blockName = (blockName == null) ? "unknown" : blockName;
        this.postNumber = postNumber;
    }

    @Override
    public void recover(LexerNoViableAltException lexerException) {

        int startIndex = lexerException.getStartIndex();
        String input = lexerException.getInputStream().toString();

        String error = "Section: " + blockName + ", "
                + "post: " + postNumber + ", "
                + "problem at offset " + startIndex + ", "
                + lexerException.toString() + ", "
                + "in : " + input
                + "\n";

        throw new MemberDataException(error);
    }

}

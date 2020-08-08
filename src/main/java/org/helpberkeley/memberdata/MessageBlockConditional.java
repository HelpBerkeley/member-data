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

import java.util.ArrayList;
import java.util.List;

public class MessageBlockConditional implements MessageBlockScope, MessageBlockElement {

    enum EvaluationType {
        EVAL_TRUE,
        EVAL_FALSE
    }

    private final List<MessageBlockElement> elements = new ArrayList<>();
    private final EvaluationType evaluationType;
    private MessageBlockElement conditionalRef;

    MessageBlockConditional(EvaluationType evaluatationType) {
        this.evaluationType = evaluatationType;
    }

    MessageBlockElement getConditional() {
        return conditionalRef;
    }

    EvaluationType getEvaluationType() {
        return evaluationType;
    }

    @Override
    public String getName() {
        return "IF";
    }

    @Override
    public void addElement(MessageBlockElement element) {
        //
        // The first element added is the variable inside the condition
        // Subsequent elements are the body.
        //
        if (conditionalRef == null) {
            conditionalRef = element;
        } else {
            elements.add(element);
        }
    }

    @Override
    public List<MessageBlockElement> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return getName()
            + ((evaluationType == EvaluationType.EVAL_FALSE) ? " NOT" : "")
            + ": "
            + conditionalRef.toString();
    }
}
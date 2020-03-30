/******************************************************************************
 * Copyright (c) 2020 helpberkeley.org
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
 ******************************************************************************/

package org.helpberkeley.memberdata;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.HashMap;
import java.util.Map;

class PostUpdate {

    final long topic_id;
    final String raw;
    final String raw_old;
    final String edit_reason;
    final String cooked;

    PostUpdate(
        final long topic_id,
        final String raw,
        final String raw_old,
        final String edit_reason,
        final String cooked
        ) {

        this.topic_id = topic_id;
        this.raw = raw;
        this.raw_old = raw_old;
        this.edit_reason = edit_reason;
        this.cooked = cooked;
    }

    String toJson() {
        Map<String, Object> options = new HashMap<>();
        options.put(JsonWriter.TYPE, Boolean.FALSE);
        return JsonWriter.objectToJson(this, options);
    }
}

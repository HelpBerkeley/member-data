//
// Copyright (c) 2024 helpberkeley.org
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

import java.sql.Timestamp;

/**
 * Represents a single entry in a cache that stores multiple versions of data.
 * Each entry contains a key identifying the type of data, the data content itself,
 * and a timestamp marking when this version was stored in the cache.
 */
public interface CacheEntry {

    /**
     * Retrieves the key identifying this cache entry, typically representing a
     * specific dependency required by a Job.
     *
     * @return the key that identifies the type of entry
     */
    JobDependency getKey();

    /**
     * Retrieves the data content stored in this cache entry.
     *
     * @return the data content of this cache entry
     */
    String getData();

    /**
     * Retrieves the timestamp marking when this data was stored in the cache.
     *
     * @return the timestamp for this cache entry
     */
    Timestamp getTimestamp();
}

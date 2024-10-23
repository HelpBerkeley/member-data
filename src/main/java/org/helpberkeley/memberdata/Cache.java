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


public interface Cache {

    /**
     * Represents types of cached files. Cache may contain up to 2 files with the same cacheKey.
     */
    enum cacheKey {
        CONSUMER_REQUESTS,
        VOLUNTEER_REQUESTS,
        MEMBERDATA_ERRORS,
        MEMBERDATA_REPORT,
        MEMBERDATA_RAW,
        DRIVERS,
        DISPATCHERS
    }

    /**
     * Store file in cache.
     *
     * @param key representing the type of file we are storing
     * @param data containing text data to store
     */
    void storeFile(cacheKey key, String data);

    /**
     * Return most recent cachedFile with the cacheKey in the argument.
     *
     * @param key representing the type of file we are retrieving
     * @return cachedFile with the provided cacheKey and most recent timestamp
     */
    cachedFile getCurrentVersion(cacheKey key);

    /**
     * Return oldest cachedFile with the cacheKey in the argument.
     *
     * @param key representing the type of file we are retrieving
     * @return cachedFile with the provided cacheKey and the oldest timestamp
     */
    cachedFile getPreviousVersion(cacheKey key);

    /**
     * Determine if there are differences between the current and previous version of the cachedFiles with the
     * cacheKey in the argument.
     *
     * @param key representing the type of file we are retrieving
     * @return boolean True if there are differences between the 2 files, False if there are no changes
     */
    boolean hasChanged(cacheKey key);

    /**
     * Remove all files with the cacheKey provided in the argument.
     *
     * @param key representing the type of file we are retrieving
     */
    void removeFile(cacheKey key);

    /**
     * Delete all files from cache.
     */
    void cleanup();

}

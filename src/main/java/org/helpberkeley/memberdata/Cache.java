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

import java.util.*;
import java.sql.Timestamp;

/**
 * Interface for a caching mechanism that stores and manages multiple timestamped versions of
 * text data. Each data object (CacheEntry) is stored under a JobDependency key and can be retrieved or compared
 * based on versions. The Cache interface supports storing, retrieving, detecting changes, and
 * cleaning up old versions of data.
 */
public interface Cache {

    /**
     * Create a new Cache.
     *
     * @param maxKeyDepth specifies how many files may be stored with the same JobDependency
     */
    Cache create(int maxKeyDepth);

    /**
     * Store data in cache. Data will be timestamped by the system when it is stored. If max depth has already
     * been reached for a particular JobDependency, then the oldest version will be deleted and replaced.
     *
     * @param key representing the type of file we are storing
     * @param data containing text data to store
     * @return timestamp created by system when the data is stored
     */
     Timestamp storeEntry(JobDependency key, String data);

    /**
     * Return a list containing all versions of the entry with the specified key.
     *
     * @param key representing the type of file we are retrieving
     * @return a list of all CacheEntries with the provided key
     * @throws MemberDataException if no entry exists with the provided key
     */
    List<CacheEntry> getAllVersions(JobDependency key) throws MemberDataException;

    /**
     * Return most recent CacheEntry with the JobDependency key in the argument.
     *
     * @param key representing the type of file we are retrieving
     * @return CacheEntry with the provided CacheKey and most recent timestamp
     * @throws MemberDataException if no entry exists with the provided key
     */
    CacheEntry getLatestVersion(JobDependency key) throws MemberDataException;

    /**
     * Return entry with the specified JobDependency key and depth.
     *
     * @param key representing the type of file we are retrieving
     * @param depth represents the depth of the stored entry, the most recent entry has a depth of 0
     * @return CachedFile with the provided key and depth
     * @throws MemberDataException if no entry exists with the provided key and depth
     */
    CacheEntry getVersion(JobDependency key, int depth) throws MemberDataException;

    /**
     * Return entry with the specified JobDependency key and Timestamp.
     *
     * @param key representing the type of file we are retrieving
     * @param timestamp of when the entry was stored
     * @return CacheEntry with the provided key and timestamp
     * @throws MemberDataException if no entry exists with the provided key and timestamp
     */
    CacheEntry getVersion(JobDependency key, Timestamp timestamp) throws MemberDataException;

    /**
     * Return the number of entries with the provided key.
     *
     * @param key representing the type of entry
     * @return an int representing the number of entries cached with the provided key
     */
    int getNumVersions(JobDependency key);

    /**
     * Determine if there are differences between the 2 most recent versions of the entry with the specified key.
     *
     * @param key representing the type of file we are retrieving
     * @return boolean True if there are differences between the 2 files, False if there are no changes
     * @throws MemberDataException if fewer than 2 entries exist with the provided key
     */
    boolean hasChanged(JobDependency key) throws MemberDataException;

    /**
     * Remove all entry versions with the JobDependency key provided in the argument.
     *
     * @param key representing the type of file we are retrieving
     */
    void removeEntries(JobDependency key);

    /**
     * Delete all files from cache.
     */
    void destroy();

}

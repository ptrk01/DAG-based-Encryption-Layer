/**
 * Copyright (c) 2011, github.com/ptrk01
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package encryptionlayer.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * An LRU cache, based on <code>LinkedHashMap</code> holding
 * last key changes for a user.
 *
 * @author ptrk01
 */
public class KeyCache {

    /**
     * Capacity of the cache.
     */
    static final int CACHE_CAPACITY = 100;

    /**
     * The collection to hold the maps.
     */
    private final Map<String, LinkedList<Long>> mMap;

    /**
     * Constructor creates a new key cache.
     */
    public KeyCache() {

        mMap = new LinkedHashMap<String, LinkedList<Long>>(CACHE_CAPACITY) {
            private static final long serialVersionUID = 1;

            @Override
            protected boolean removeEldestEntry(
                final Map.Entry<String, LinkedList<Long>> mEldest) {
                boolean returnVal = false;
                if (size() > CACHE_CAPACITY) {
                    returnVal = true;
                }
                return returnVal;
            }

        };

    }

    /**
     * Returns the stored <code>LinkedList</code> of corresponding user.
     *
     * @param paramUser
     *            User key.
     * @return linked list of user.
     */
    public final LinkedList<Long> get(final String paramUser) {
        final LinkedList<Long> list = mMap.get(paramUser);
        return list; // returns null if no value for this user exists in cache.
    }

    /**
     * Stores a new entry in cache consisting of a user name as key and
     * a linked list for storing node keys as value.
     *
     * @param paramUser
     *            user name as key.
     * @param paramList
     *            linked list as values.
     */
    public final void put(final String paramUser,
        final LinkedList<Long> paramList) {

        mMap.put(paramUser, paramList);
    }

    /**
     * Clears the cache.
     */
    public final void clear() {
        mMap.clear();
    }

    /**
     * Returns the number of used entries in the cache.
     *
     * @return the number of entries currently in the cache.
     */
    public final int usedEntries() {
        return mMap.size();
    }

    /**
     * Returns a <code>Collection</code> that contains a copy of all cache
     * entries.
     *
     * @return a <code>Collection</code> with a copy of the cache content.
     */

    public final Collection<Map.Entry<String, LinkedList<Long>>> getAll() {
        return new ArrayList<Map.Entry<String, LinkedList<Long>>>(mMap
            .entrySet());

    }
}

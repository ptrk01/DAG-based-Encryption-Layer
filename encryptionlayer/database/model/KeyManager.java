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
package encryptionlayer.database.model;

import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * This class represents the key manager model holding all key data
 * for a user comprising the initial keys and all TEKs a user owns.
 *
 * @author ptrk01
 */
@Entity
public class KeyManager {

    /**
     * User and primary key for database.
     */
    @PrimaryKey
    private String mUser;

    /**
     * Set of keys the user owns.
     */
    private Set<Long> mKeySet;

    /**
     * Standard constructor.
     */
    public KeyManager() {
        super();
    }

    /**
     * Constructor for building an new key manager instance.
     *
     * @param paramUser
     *            user.
     * @param paramKeys
     *            set of initial keys.
     */
    public KeyManager(final String paramUser, final Set<Long> paramKeys) {
        this.mUser = paramUser;
        this.mKeySet = paramKeys;
    }

    /**
     * Returns a user.
     *
     * @return
     *         user.
     */
    public final String getUser() {
        return mUser;
    }

    /**
     * Returns a set of all keys a user owns.
     *
     * @return
     *         set of users keys.
     */
    public final Set<Long> getKeySet() {
        return mKeySet;
    }

    /**
     * Adds a new key to the set.
     *
     * @param paramTrail
     *            new key the user owns.
     */
    public final void addKey(final long paramKey) {
        mKeySet.add(paramKey);
    }

    /**
     * Removes a key from the set.
     *
     * @param paramKey
     *            the key to be withdraw.
     */
    public final void removeKey(final long paramKey) {
        mKeySet.remove(paramKey);
    }

}

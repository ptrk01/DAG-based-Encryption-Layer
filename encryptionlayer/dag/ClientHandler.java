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

package encryptionlayer.dag;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import encryptionlayer.cache.KeyCache;
import encryptionlayer.database.KeyManagerDatabase;
import encryptionlayer.database.KeySelectorDatabase;
import encryptionlayer.utils.NodeEncryption;

/**
 * Class represents the client side handler.
 *
 * @author ptrk01
 */
public class ClientHandler {

    /**
     * Decrypts key trails and put it to users key cache to make it available to
     * encrypt/decrypt data.
     *
     * @param paramKeyTails
     *            map of key trails.
     */
    public final void decryptKeyTrails(final Map<Long, byte[]> paramKeyTails) {
        // if map contains no key trails user has been completely removed
        // from DAG and all keys for user has to be removed.
        if (paramKeyTails.size() != 0) {

            if (Controller.getInstance().getKeyCache().get(Controller.getInstance().getUser()) == null) {
                initKeyCacheKeys(Controller.getInstance().getUser());
            }

            final Iterator<Long> mIter = paramKeyTails.keySet().iterator();
            while (mIter.hasNext()) {
                long mapKey = (Long)mIter.next();

                byte[] mChildSecretKey =
                    Controller.getInstance().getDAGDb().getEntry(mapKey)
                        .getSecretKey();

                byte[] mDecryptedBytes =
                    NodeEncryption.decrypt(paramKeyTails.get(mapKey),
                        mChildSecretKey);

                long mEncryptedKey =
                    NodeEncryption.byteArrayToLong(mDecryptedBytes);

                final LinkedList<Long> mUserCache =
                    Controller.getInstance().getKeyCache().get(
                        Controller.getInstance().getUser());


                if (!mUserCache.contains(mEncryptedKey)) {
                    mUserCache.add(mEncryptedKey);
                }

                Controller.getInstance().getKeyCache().put(
                    Controller.getInstance().getUser(), mUserCache);
            }
        }

    }

    /**
     * When user is changed, keys for this user has to be brought from key manager to the cache.
     *
     * @param paramUser
     *            new user.
     */
    public final void initKeyCacheKeys(final String paramUser) {

        final Set<Long> keySet =
            Controller.getInstance().getManDb().getEntry(paramUser).getKeySet();

        final LinkedList<Long> keyList = new LinkedList<Long>();
        final Iterator<Long> mIter = keySet.iterator();
        while (mIter.hasNext()) {
            keyList.add(mIter.next());
        }

        Controller.getInstance().getKeyCache().put(paramUser, keyList);

    }

}

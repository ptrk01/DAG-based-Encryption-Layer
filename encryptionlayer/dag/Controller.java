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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import encrpytionlayer.exception.TTEncryptionException;
import encryptionlayer.cache.KeyCache;
import encryptionlayer.database.CurrentDAGDatabase;
import encryptionlayer.database.KeyManagerDatabase;
import encryptionlayer.database.KeySelectorDatabase;
import encryptionlayer.database.model.DAGSelector;
import encryptionlayer.database.model.KeyManager;
import encryptionlayer.database.model.KeySelector;
import encryptionlayer.utils.NodeEncryption;

public class Controller {

    // #################SETTINGS START#######################

    /**
     * Instance for enabling or disabling encryption process.
     */
    private static boolean mNodeEncryption = false;

    /**
     * The key data should be encrypted.
     */
    private long mDataEncryptionKey = 0;

    /**
     * Current session user.
     */
    private static String mLoggedUser;

    // #################SETTINGS END#######################

    /**
     * Instance of KeySelectorDatabase holding key selection stuff.
     */
    private static KeySelectorDatabase mKeyDB;

    /**
     * Instance of CurrentDAGDatabase holding last DAG revision stuff.
     */
    private static CurrentDAGDatabase mDAGDB;

    /**
     * Instance of KeyManagerDatabase holding key manager stuff.
     */
    private static KeyManagerDatabase mManDB;

    /**
     * Instance of KeyCache holding all current keys of user.
     */
    private static KeyCache mKeyCache;

    /**
     * Singleton instance.
     */
    private static Controller mINSTANCE = new Controller();

    /**
     * Returns singleton instance of handler.
     *
     * @return Handler instance.
     */
    public static Controller getInstance() {
        return mINSTANCE;
    }

    /**
     * Global key selector database key.
     */
    private int mSelectorKey = 0;
    /**
     * Global dag selector database key.
     */
    private int mDAGKey = 0;

    /**
     * Store path of berkeley key selector db.
     */
    private static final File SEL_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("selectordb").toString());

    /**
     * Store path of berkeley dag selector db.
     */
    private static final File DAG_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("dagdb").toString());

    /**
     * Store path of berkeley key manager db.
     */
    private static final File MAN_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("keymanagerdb").toString());

    /**
     * Initiates all needed instances comprising Berkeley DBs and key cache.
     * Additionally it initiates parsing of initial right tree and setup of
     * Berkeley DBs.
     *
     * @throws TTEncryptionException
     */
    public void init() throws TTEncryptionException {
        if (mNodeEncryption) {
            mLoggedUser = "ALL";
            mKeyDB = new KeySelectorDatabase(SEL_STORE);
            final KeySelector rootSel =
                new KeySelector("ROOT", new LinkedList<Long>(),
                    new LinkedList<Long>(), 0, 0, NodeEncryption
                        .generateSecretKey());
            mKeyDB.putEntry(rootSel);

            mDAGDB = new CurrentDAGDatabase(DAG_STORE);
            final DAGSelector rootDAG =
                new DAGSelector("ROOT", new LinkedList<Long>(),
                    new LinkedList<Long>(), 0, 0, NodeEncryption
                        .generateSecretKey());
            mDAGDB.putEntry(rootDAG);

            mManDB = new KeyManagerDatabase(MAN_STORE);
            final Set<Long> manSet = new HashSet<Long>();
            manSet.add(rootSel.getPrimaryKey());
            mManDB.putEntry(new KeyManager(mLoggedUser, manSet));

            mKeyCache = new KeyCache();
            final LinkedList<Long> keyList = new LinkedList<Long>();
            keyList.add(rootSel.getPrimaryKey());
            mKeyCache.put(mLoggedUser, keyList);

        } else {
            throw new TTEncryptionException("Encryption is disabled!");
        }

    }

    /**
     * Creates a new key selector key.
     * @return
     */
    public int newSelectorKey() {
        return mSelectorKey++;
    }

    /**
     * Creates a new dag selector key.
     * @return
     */
    public int newDAGKey() {
        return mDAGKey++;
    }

    public void print() {

        final SortedMap<Long, KeySelector> mSelMap = mKeyDB.getEntries();
        Iterator<Long> iter = mSelMap.keySet().iterator();

        System.out.println("\nSelector DB Size: " + mKeyDB.count());

        while (iter.hasNext()) {

            final KeySelector mSelector = mSelMap.get(iter.next());
            final LinkedList<Long> mParentsList = mSelector.getParents();
            final List<Long> mChildsList = mSelector.getChilds();

            final StringBuilder mParentsString = new StringBuilder();
            for (int k = 0; k < mParentsList.size(); k++) {
                mParentsString.append("#" + mParentsList.get(k));
            }

            final StringBuilder mChildsString = new StringBuilder();
            for (int k = 0; k < mChildsList.size(); k++) {
                mChildsString.append("#" + mChildsList.get(k));
            }

            System.out
                .println("Selector: " + mSelector.getPrimaryKey() + " "
                    + mSelector.getName() + " " + mParentsString.toString()
                    + " " + mChildsString.toString() + " "
                    + mSelector.getRevision() + " " + mSelector.getVersion()
                    + " " + mSelector.getSecretKey());
        }
        System.out.println(" ");

        final SortedMap<Long, DAGSelector> mDAGMap = mDAGDB.getEntries();
        Iterator<Long> iter2 = mDAGMap.keySet().iterator();

        System.out.println("\nDAG DB Size: " + mDAGDB.count());

        while (iter2.hasNext()) {

            final DAGSelector mSelector = mDAGMap.get(iter2.next());
            final LinkedList<Long> mParentsList = mSelector.getParents();
            final List<Long> mChildsList = mSelector.getChilds();

            final StringBuilder mParentsString = new StringBuilder();
            for (int k = 0; k < mParentsList.size(); k++) {
                mParentsString.append("#" + mParentsList.get(k));
            }

            final StringBuilder mChildsString = new StringBuilder();
            for (int k = 0; k < mChildsList.size(); k++) {
                mChildsString.append("#" + mChildsList.get(k));
            }

            System.out
                .println("Selector: " + mSelector.getPrimaryKey() + " "
                    + mSelector.getName() + " " + mParentsString.toString()
                    + " " + mChildsString.toString() + " "
                    + mSelector.getRevision() + " " + mSelector.getVersion()
                    + " " + mSelector.getSecretKey());
        }
        System.out.println(" ");

        /*
         * print key manager db
         */
        final SortedMap<String, KeyManager> sMap = mManDB.getEntries();

        // iterate through all users
        final Iterator<String> outerIter = sMap.keySet().iterator();

        System.out.println("Key manager DB Size: " + mManDB.count());

        StringBuilder sb;
        while (outerIter.hasNext()) {
            final String user = (String)outerIter.next();
            sb = new StringBuilder(user + ": ");

            final Set<Long> mKeySet = mManDB.getEntry(user).getKeySet();

            // iterate through user's key set.
            final Iterator<Long> innerIter = mKeySet.iterator();
            while (innerIter.hasNext()) {
                sb.append(innerIter.next() + " ");
            }

            System.out.println(sb.toString());

        }
        System.out.println(" ");

        /*
         * print key cache.
         */
        final LinkedList<Long> mKeyList =
            mKeyCache.get(new Controller().getUser());
        final StringBuilder cacheString =
            new StringBuilder("Key Cache of " + new Controller().getUser() + ": ");
        for (long aKey : mKeyList) {
            cacheString.append(aKey + " ");
        }
        System.out.println(cacheString.toString());

    }

    /**
     * Returns key selector database instance.
     *
     * @return KeySelector instance.
     */
    public KeySelectorDatabase getSelDb() {
        return mKeyDB;
    }

    /**
     * Returns dag selector database instance.
     *
     * @return CurrentDAGSelector instance.
     */
    public CurrentDAGDatabase getDAGDb() {
        return mDAGDB;
    }
    /**
     * Returns key manager database instance.
     *
     * @return KeyManager instance.
     */
    public KeyManagerDatabase getManDb() {
        return mManDB;
    }
    /**
     * Returns key cache instance.
     *
     * @return KeyCache instance.
     */
    public KeyCache getKeyCache() {
        return mKeyCache;
    }

    /**
     * Clears all established berkeley dbs.
     *
     * @throws AbsTTException
     */
    public void clear() throws TTEncryptionException {
        if (SEL_STORE.exists()) {
            recursiveDelete(SEL_STORE);
        }
        if (DAG_STORE.exists()) {
            recursiveDelete(DAG_STORE);
        }
        if (MAN_STORE.exists()) {
            recursiveDelete(MAN_STORE);
        }

    }

    /**
     * Deletes berkeley db file recursively.
     *
     * @param paramFile
     *            File to delete.
     * @return if some more files available.
     */
    protected static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    /**
     * Returns whether encryption is enabled or not.
     *
     * @return encryption enabled.
     */
    public boolean checkEncryption() {
        return mNodeEncryption;
    }

    /**
     * Enables or disables encryption option.
     *
     * @param paramBol
     *            if encryption should be enabled or not.
     */
    public void setEncryptionOption(final boolean paramBol) {
        mNodeEncryption = paramBol;
    }

    /**
     * Set session user.
     */
    public void setUser(final String paramUser) {
        mLoggedUser = paramUser;
    }

    /**
     * Set dek.
     */
    public void setDek(final long paramDek) {
        mDataEncryptionKey = paramDek;
    }

    /**
     * Returns session user.
     *
     * @return current logged user.
     */
    public String getUser() {
        return mLoggedUser;
    }

    /**
     * Returns data encryption key.
     *
     * @return data encryption key.
     */
    public long getDataEncryptionKey() {
        return mDataEncryptionKey;
    }

}

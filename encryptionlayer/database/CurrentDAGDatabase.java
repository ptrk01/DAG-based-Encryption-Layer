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
package encryptionlayer.database;

import java.io.File;
import java.util.SortedMap;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import encryptionlayer.exception.TTEncryptionException;
import encryptionlayer.database.model.DAGSelector;
import encryptionlayer.database.model.KeySelector;

/**
 * Berkeley implementation of a persistent DAG database. That means
 * that all data of current dag is stored within this storage.
 *
 * @author ptrk01
 */
public class CurrentDAGDatabase extends AbsKeyDatabase {

    /**
     * Berkeley Environment for the database.
     */
    private Environment mEnv;

    /**
     * Berkeley Entity store instance for the database.
     */
    private EntityStore mStore;

    /**
     * Name for the database.
     */
    private static final String NAME = "berkeleyDAGSelector";

    /**
     * Constructor. Building up the berkeley db and setting necessary settings.
     *
     * @param paramFile
     *            the place where the berkeley db is stored.
     */
    public CurrentDAGDatabase(final File paramFile) {
        super(paramFile);
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(true);

        final DatabaseConfig conf = new DatabaseConfig();
        conf.setTransactional(true);
        conf.setKeyPrefixing(true);

        try {
            mEnv = new Environment(place, environmentConfig);

            StoreConfig storeConfig = new StoreConfig();
            storeConfig.setAllowCreate(true);
            storeConfig.setTransactional(true);
            mStore = new EntityStore(mEnv, NAME, storeConfig);

        } catch (final Exception mELExp) {
            mELExp.printStackTrace();
        }
    }

    /**
     * Clearing the database. That is removing all elements
     */
    public final void clearPersistent() {
        try {
            for (final File file : place.listFiles()) {
                if (!file.delete()) {
                    throw new TTEncryptionException("Couldn't delete!");
                }
            }
            if (!place.delete()) {
                throw new TTEncryptionException("Couldn't delete!");
            }
            if (mStore != null) {
                mStore.close();
            }
            if (mEnv != null) {
                mEnv.close();
            }
        } catch (final Exception mDbExp) {
            mDbExp.printStackTrace();
        }

    }

    /**
     * Putting a {@link DAGSelector} into the database.
     *
     * @param paramEntity
     *            key selector instance to put into database.
     */
    public final void putEntry(final DAGSelector paramEntity) {
        PrimaryIndex<Long, DAGSelector> primaryIndex;
        try {
            primaryIndex =
                (PrimaryIndex<Long, DAGSelector>)mStore.getPrimaryIndex(
                    Long.class, DAGSelector.class);

            primaryIndex.put(paramEntity);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

    }

    /**
     * Getting a {@link DAGSelector} related to a given key.
     *
     * @param paramKey
     *            key for related dag selector instance.
     * @return
     *         dag selector instance.
     */
    public final DAGSelector getEntry(final long paramKey) {
        PrimaryIndex<Long, DAGSelector> primaryIndex;
        DAGSelector entity = null;
        try {
            primaryIndex =
                (PrimaryIndex<Long, DAGSelector>)mStore.getPrimaryIndex(
                    Long.class, DAGSelector.class);

            entity = (DAGSelector)primaryIndex.get(paramKey);

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return entity;
    }

    /**
     * Deletes an entry from database.
     *
     * @param paramKey
     *            key to delete.
     * @return
     *         status of deletion.
     */
    public final boolean deleteEntry(final long paramKey) {
        PrimaryIndex<Long, DAGSelector> primaryIndex;
        boolean status = false;
        try {
            primaryIndex =
                (PrimaryIndex<Long, DAGSelector>)mStore.getPrimaryIndex(
                    Long.class, DAGSelector.class);

            status = primaryIndex.delete(paramKey);

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return status;
    }

    /**
     * Returns number of database entries.
     *
     * @return
     *         number of entries in database.
     */
    public final int count() {
        PrimaryIndex<Long, DAGSelector> primaryIndex;
        long counter = 0;
        try {
            primaryIndex =
                (PrimaryIndex<Long, DAGSelector>)mStore.getPrimaryIndex(
                    Long.class, DAGSelector.class);
            counter = primaryIndex.count();

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return (int)counter;
    }

    /**
     * Returns all database entries as {@link SortedMap}.
     *
     * @return
     *         all database entries.
     */
    public final SortedMap<Long, DAGSelector> getEntries() {
        PrimaryIndex<Long, DAGSelector> primaryIndex;
        SortedMap<Long, DAGSelector> sMap = null;
        try {
            primaryIndex =
                (PrimaryIndex<Long, DAGSelector>)mStore.getPrimaryIndex(
                    Long.class, DAGSelector.class);
            sMap = primaryIndex.sortedMap();

        } catch (final DatabaseException mDbExp) {
            mDbExp.printStackTrace();
        }
        return sMap;
    }

}

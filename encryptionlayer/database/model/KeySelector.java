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

import java.util.LinkedList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import encryptionlayer.dag.Controller;
import encryptionlayer.utils.NodeEncryption;

/**
 * This class represents the key selector model holding all data
 * for a node of the right tree consisting of group and user nodes.
 *
 * @author ptrk01
 */
@Entity
public class KeySelector {

    /**
     * Selector key and primary key of database.
     */
    @PrimaryKey
    private long mSelectorKey;

    /**
     * Name of the node (group or user name).
     */
    private String mName;

    /**
     * List of parent nodes.
     */
    private LinkedList<Long> mParents;

    /**
     * List of child nodes.
     */
    private LinkedList<Long> mChilds;

    /**
     * Current revision of node.
     */
    private int mRevision;

    /**
     * Current version of node.
     */
    private int mVersion;

    /**
     * Secret key using for data en-/decryption.
     */
    private byte[] mSecretKey;

    /**
     * Standard constructor.
     */
    public KeySelector() {
        super();
    }

    /**
     * Constructor for building an new key selector instance.
     *
     * @param paramName
     *            node name.
     */
    public KeySelector(final String paramName, final LinkedList<Long> paramPar,
        final LinkedList<Long> paramChild, final int paramRev,
        final int paramVer, byte[] mSecretKey) {
        this.mSelectorKey = Controller.getInstance().newSelectorKey();

        this.mName = paramName;
        this.mParents = paramPar;
        this.mChilds = paramChild;
        this.mRevision = paramRev;
        this.mVersion = paramVer;
        this.mSecretKey = mSecretKey;
    }

    /**
     * Returns selector id.
     *
     * @return
     *         selector id.
     */
    public final long getPrimaryKey() {
        return mSelectorKey;
    }

    /**
     * Returns node name.
     *
     * @return
     *         node name.
     */
    public final String getName() {
        return mName;
    }

    /**
     * Returns a list of parent nodes for node.
     *
     * @return
     *         set of parent nodes.
     */
    public final LinkedList<Long> getParents() {
        return mParents;
    }

    /**
     * Returns a list of child nodes for node.
     *
     * @return
     *         set of child nodes.
     */
    public final LinkedList<Long> getChilds() {
        return mChilds;
    }

    /**
     * Adds a new parent node to the list.
     *
     * @param paramParent
     *            parent to add to list.
     */
    public final void addParent(final long paramParent) {
        mParents.add(paramParent);
    }

    /**
     * Adds a new child node to the list.
     *
     * @param paramParent
     *            parent to add to list.
     */
    public final void addChild(final long paramChild) {
        mChilds.add(paramChild);
    }

    /**
     * Removes a parent node from list.
     *
     * @param paramParent
     *            parent node to remove.
     */
    public void removeParent(final long paramParent) {
        mParents.remove(paramParent);
    }

    /**
     * Removes a child node from list.
     *
     * @param paramChild
     *            child node to remove.
     */
    public void removeChild(final long paramChild) {
        mChilds.remove(paramChild);
    }

    /**
     * Sets new primary key. Usually not needed.
     *
     * @param paramKey
     *            new key for node.
     */
    public final void setPrimaryKey(final long paramKey) {
        this.mSelectorKey = paramKey;
    }

    /**
     * Returns current revision of node.
     *
     * @return
     *         node's revision.
     */
    public final int getRevision() {
        return mRevision;
    }

    /**
     * Increases node revision by 1.
     */
    public final void increaseRevision() {
        this.mRevision += 1;
    }

    /**
     * Returns current version of node.
     *
     * @return
     *         node's version.
     */
    public final int getVersion() {
        return mVersion;
    }

    /**
     * Increases node version by 1.
     */
    public final void increaseVersion() {
        this.mVersion += 1;
    }

    /**
     * Returns secret key.
     *
     * @return
     *         secret key.
     */
    public final byte[] getSecretKey() {
        return mSecretKey;
    }
}

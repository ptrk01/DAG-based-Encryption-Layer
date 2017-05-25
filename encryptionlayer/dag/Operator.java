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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;

import encrpytionlayer.exception.TTEncryptionException;
import encryptionlayer.database.model.DAGSelector;
import encryptionlayer.database.model.KeyManager;
import encryptionlayer.database.model.KeySelector;
import encryptionlayer.utils.NodeEncryption;

public class Operator {

    /**
     * Adds nodes to the DAG.
     *
     * @param parent
     *            root new node(s) should be attached.
     * @param descendants
     *            new node(s) array.
     * @throws TTEncryptionException
     */
    public void join(final String parent, final String[] descendants)
        throws TTEncryptionException {

        final LinkedList<Long> idsChanged = new LinkedList<Long>();
        String user = "ALL";
        if (descendants.length > 0) {
            user = descendants[descendants.length - 1];
            if(!nodeExists(user)){
                // create key manager entry for new user.
                Controller.getInstance().getManDb().putEntry(new KeyManager(user,
                    new HashSet<Long>()));
            }
        }
        if (nodeExists(parent)) {
            final long parentKey = getNodeKey(parent);

            // calculate all nodes that are affected by the join update
            final Queue<Long> queue = new LinkedList<Long>();
            final LinkedList<Long> visited = new LinkedList<Long>();
            queue.add(parentKey);
            visited.add(parentKey);
            idsChanged.add(parentKey);
            while (!queue.isEmpty()) {
                final DAGSelector mDAG = getDAGSelector(queue.remove());
                final LinkedList<Long> parents = mDAG.getParents();
                for (long aParent : parents) {
                    if (!visited.contains(aParent)) {
                        queue.add(aParent);
                        if (!idsChanged.contains(aParent)) {
                            idsChanged.add(aParent);
                        }
                        visited.add(aParent);
                    }
                }
            }

            // create all new nodes
            long prevNode = -1;
            final long lastNodeRevChild = getNodeKey(descendants[0]);
            final long lastNodeRevParent = getNodeKey(parent);
            for (int i = descendants.length - 1; i >= 0; i--) {
                // check if node to be inserted already exits, if so, do only a connection
                if (descendants.length == 1 && lastNodeRevChild != -1) {
                    final DAGSelector mParentNode =
                        Controller.getInstance().getDAGDb().getEntry(
                            lastNodeRevParent);
                    mParentNode.addChild(lastNodeRevChild);
                    Controller.getInstance().getDAGDb().putEntry(mParentNode);

                    final DAGSelector mChildNode =
                        Controller.getInstance().getDAGDb().getEntry(
                            lastNodeRevChild);
                    mChildNode.addParent(lastNodeRevParent);
                    Controller.getInstance().getDAGDb().putEntry(mChildNode);

                } else {

                    final LinkedList<Long> parentList = new LinkedList<Long>();
                    final LinkedList<Long> childList = new LinkedList<Long>();

                    if (prevNode != -1) {
                        childList.add(prevNode);
                    }

                    final DAGSelector mNewNode =
                        new DAGSelector(descendants[i], parentList, childList,
                            0, 0, NodeEncryption.generateSecretKey());

                    if (i == 0) {
                        mNewNode.addParent(parentKey);

                        final DAGSelector mPrevNode =
                            Controller.getInstance().getDAGDb().getEntry(
                                parentKey);
                        mPrevNode.addChild(mNewNode.getPrimaryKey());
                        Controller.getInstance().getDAGDb().putEntry(mPrevNode);
                    }

                    Controller.getInstance().getDAGDb().putEntry(mNewNode);

                    if (prevNode != -1) {
                        final DAGSelector mPrevNode =
                            Controller.getInstance().getDAGDb().getEntry(
                                prevNode);
                        final LinkedList<Long> prevNodeParents =
                            mPrevNode.getParents();
                        prevNodeParents.add(mNewNode.getPrimaryKey());
                        Controller.getInstance().getDAGDb().putEntry(mPrevNode);
                    }

                    prevNode = mNewNode.getPrimaryKey();

                }
            }


            // update revisions and secret material
            for (long idChanged : idsChanged) {
                final DAGSelector mNode =
                    Controller.getInstance().getDAGDb().getEntry(idChanged);
                mNode.increaseRevision();
                mNode.setSecretKey(NodeEncryption.generateSecretKey());
                Controller.getInstance().getDAGDb().putEntry(mNode);
            }

            // write new DAG revision to selector store
            final Map<Long, Long> newOldIds = new HashMap<Long, Long>();
            final LinkedList<KeySelector> keySels =
                new LinkedList<KeySelector>();
            final SortedMap<Long, DAGSelector> mMap =
                Controller.getInstance().getDAGDb().getEntries();
            final Iterator<Long> iter = mMap.keySet().iterator();
            while (iter.hasNext()) {
                final DAGSelector mDAG = mMap.get(iter.next());
                final KeySelector mSel =
                    new KeySelector(mDAG.getName(), mDAG.getParents(), mDAG
                        .getChilds(), mDAG.getRevision(), mDAG.getVersion(),
                        mDAG.getSecretKey());
                keySels.add(mSel);
                newOldIds.put(mDAG.getPrimaryKey(), mSel.getPrimaryKey());
            }

            for (int i = 0; i < keySels.size(); i++) {
                KeySelector aSel = keySels.get(i);

                final Iterator<Long> mIter = newOldIds.keySet().iterator();
                while (mIter.hasNext()) {
                    final long key = mIter.next();
                    final long value = newOldIds.get(key);

                    if (aSel.getParents().contains(key)) {
                        aSel.removeParent(key);
                        aSel.addParent(value);

                    }
                    if (aSel.getChilds().contains(key)) {
                        aSel.removeChild(key);
                        aSel.addChild(value);

                    }
                }
                Controller.getInstance().getSelDb().putEntry(aSel);
            }

            updateKeyManagerJoin(newOldIds, user);

            // create and transmit key trails
            final Map<Long, byte[]> mKeyTrails = encryptKeyTrails(idsChanged);
            transmitKeyTrails(mKeyTrails);

        } else {
            throw new TTEncryptionException("Join: Parent node does not exist!");
        }

    }

    /**
     * Remove nodes or edges from the DAG.
     *
     * @param child
     *            node or node edge to remove.
     * @param parents
     *            parent node of child, connection should remove.
     * @throws TTEncryptionException
     */
    public void leave(final String child, final String[] parents)
        throws TTEncryptionException {
        final LinkedList<Long> idsChanged = new LinkedList<Long>();

        final long childKey = getNodeKey(child);
        final DAGSelector mDAGSel = getDAGSelector(childKey);
        if (!nodeExists(child) || mDAGSel.getChilds().size() == 0) {

            final String user = child;

            // check if all parent nodes exits and whether they are parents of child
            for (String aParent : parents) {
                if (!nodeExists(aParent)) {
                    throw new TTEncryptionException("Leave: Parent node "
                        + aParent + " does not exist!");
                }
            }

            // calculate all nodes that are affected by the leave update
            final Queue<Long> queue = new LinkedList<Long>();
            final LinkedList<Long> visited = new LinkedList<Long>();
            if (parents.length == 0) {
                queue.add(childKey);
                visited.add(childKey);
                idsChanged.add(childKey);
                while (!queue.isEmpty()) {
                    final DAGSelector mDAG = getDAGSelector(queue.remove());
                    final LinkedList<Long> parentList = mDAG.getParents();
                    for (long aParent : parentList) {
                        if (!visited.contains(aParent)) {
                            queue.add(aParent);
                            if (!idsChanged.contains(aParent)) {
                                idsChanged.add(aParent);
                            }
                            visited.add(aParent);
                        }
                    }
                }
            } else {
                for (String aParent : parents) {
                    final long parentKey = getNodeKey(aParent);
                    queue.add(parentKey);
                    visited.add(parentKey);
                    idsChanged.add(parentKey);
                }
                while (!queue.isEmpty()) {
                    final DAGSelector mDAG = getDAGSelector(queue.remove());
                    final LinkedList<Long> parentList = mDAG.getParents();
                    for (long aParent : parentList) {
                        if (!visited.contains(aParent)) {
                            queue.add(aParent);
                            if (!idsChanged.contains(aParent)) {
                                idsChanged.add(aParent);
                            }
                            visited.add(aParent);
                        }
                    }
                }
            }

            // update version and secret material and node references
            for (long idChanged : idsChanged) {
                final DAGSelector mNode =
                    Controller.getInstance().getDAGDb().getEntry(idChanged);
                mNode.increaseVersion();
                mNode.setSecretKey(NodeEncryption.generateSecretKey());

                // delete all references of delete node
                if (mNode.getParents().contains(childKey)) {
                    mNode.removeParent(childKey);

                }
                if (mNode.getChilds().contains(childKey)) {
                    mNode.removeChild(childKey);
                }

                Controller.getInstance().getDAGDb().putEntry(mNode);
            }

            // remove node from DAG if parents to delete is same size as its parents; if not, remove its
            // references from delete parents
            DAGSelector node =
                Controller.getInstance().getDAGDb().getEntry(childKey);
            final Map<Long, Long> newOldIds = new HashMap<Long, Long>();
            if (parents.length == node.getParents().size()) {
                Controller.getInstance().getDAGDb().deleteEntry(childKey);
                Controller.getInstance().getManDb().deleteEntry(user);
                newOldIds.put(childKey, -1L);
            } else {
                for (String aParent : parents) {
                    if (node.getParents().contains(getNodeKey(aParent))) {
                        node.removeParent(getNodeKey(aParent));
                    }
                }
                Controller.getInstance().getDAGDb().putEntry(node);
            }

            // write new DAG revision to selector store
            LinkedList<KeySelector> keySels = new LinkedList<KeySelector>();
            final SortedMap<Long, DAGSelector> mMap =
                Controller.getInstance().getDAGDb().getEntries();
            final Iterator<Long> iter = mMap.keySet().iterator();
            while (iter.hasNext()) {
                final DAGSelector mDAG = mMap.get(iter.next());
                KeySelector mSel =
                    new KeySelector(mDAG.getName(), mDAG.getParents(), mDAG
                        .getChilds(), mDAG.getRevision(), mDAG.getVersion(),
                        mDAG.getSecretKey());
                keySels.add(mSel);
                newOldIds.put(mDAG.getPrimaryKey(), mSel.getPrimaryKey());
            }

            for (int i = 0; i < keySels.size(); i++) {
                KeySelector aSel = keySels.get(i);

                final Iterator<Long> mIter = newOldIds.keySet().iterator();
                while (mIter.hasNext()) {
                    final long key = mIter.next();
                    final long value = newOldIds.get(key);

                    if (aSel.getParents().contains(key)) {
                        aSel.removeParent(key);
                        if (value != -1) {
                            aSel.addParent(value);
                        }
                    }
                    if (aSel.getChilds().contains(key)) {
                        aSel.removeChild(key);
                        if (value != -1) {
                            aSel.addChild(value);
                        }
                    }
                }
                Controller.getInstance().getSelDb().putEntry(aSel);
            }

            updateKeyManagerLeave(newOldIds, user);

            // create and transmit key trails
            final Map<Long, byte[]> mKeyTrails = encryptKeyTrails(idsChanged);
            transmitKeyTrails(mKeyTrails);

        } else {
            throw new TTEncryptionException(
                "Leave: Node to be deleted does not exist or is not a leaf node!");
        }
    }

    /**
     * Checks if node exists in DAG.
     *
     * @param nodeName
     *            node name to to be checked.
     * @return
     *         bool result.
     */
    public boolean nodeExists(final String nodeName) {
        final SortedMap<Long, DAGSelector> mMap =
            Controller.getInstance().getDAGDb().getEntries();
        final Iterator<Long> iter = mMap.keySet().iterator();
        while (iter.hasNext()) {
            final DAGSelector mSelector = mMap.get(iter.next());
            if (mSelector.getName().equals(nodeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns node's DAG id of given node name.
     *
     * @param nodeName
     *            name of node id should be found.
     * @return
     *         id of node.
     */
    public long getNodeKey(final String nodeName) {
        final SortedMap<Long, DAGSelector> mMap =
            Controller.getInstance().getDAGDb().getEntries();
        final Iterator<Long> iter = mMap.keySet().iterator();
        long curNode = -1;
        while (iter.hasNext()) {
            final DAGSelector mSelector = mMap.get(iter.next());
            if (mSelector.getName().equals(nodeName)) {
                curNode = mSelector.getPrimaryKey();
            }
        }
        return curNode;
    }

    /**
     * Checks whether node is child of given parent node.
     *
     * @param child
     *            child node.
     * @param parent
     *            parent node.
     * @return
     *         bool result.
     */
    public boolean checkMembership(final String child, final String parent) {
        final long parentKey = getNodeKey(parent);
        final long childKey = getNodeKey(child);
        if (nodeExists(child) && nodeExists(parent)) {
            if (Controller.getInstance().getDAGDb().getEntry(childKey)
                .getParents().contains(parentKey)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Returns DAGSelector instance of given id.
     *
     * @param id
     *            id of DAGSelector instance.
     * @return
     *         DAGSelector instance.
     */
    public DAGSelector getDAGSelector(long id) {
        return Controller.getInstance().getDAGDb().getEntry(id);
    }

    /**
     * Returns KeyManager instance of given id.
     *
     * @param id
     *            id of KeyManager instance.
     * @return
     *         KeyManager instance.
     */
    public KeyManager getManager(String id) {
        return Controller.getInstance().getManDb().getEntry(id);
    }

    /**
     * Transmits the key to the client.
     *
     * @param paramKeyTails
     *            map of key trails.
     */
    private void transmitKeyTrails(final Map<Long, byte[]> paramKeyTails) {
        new ClientHandler().decryptKeyTrails(paramKeyTails);
    }

    /**
     * Updates the key manager on a join operation.
     *
     * @param paramMap
     *            map containing all through join effected nodes with old and new id.
     */
    private void updateKeyManagerJoin(final Map<Long, Long> paramMap,
        final String user) {
        final Iterator<String> mOuterIter =
            Controller.getInstance().getManDb().getEntries().keySet()
                .iterator();

        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                Controller.getInstance().getManDb().getEntries().get(mKeyUser);

            final Iterator<Long> mInnerIter = paramMap.keySet().iterator();
            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                final long mId = (Long)mInnerIter.next();
                if (mKeyUser.equals(user)) {
                    mManager.addKey(paramMap.get(mId));
                } else if (mManager.getKeySet().contains(mId)) {
                    mManager.addKey(paramMap.get(mId));
                }
            }
            Controller.getInstance().getManDb().putEntry(mManager);
        }
    }

    /**
     * Updates the key manager on a leave operation.
     *
     * @param paramMap
     *            map containing all through leave effected nodes with old and new id.
     */
    private void updateKeyManagerLeave(final Map<Long, Long> paramMap,
        final String user) {
        final Iterator<String> mOuterIter =
            Controller.getInstance().getManDb().getEntries().keySet()
                .iterator();
        while (mOuterIter.hasNext()) { // iterate through all users.
            final String mKeyUser = (String)mOuterIter.next();
            final KeyManager mManager =
                Controller.getInstance().getManDb().getEntries().get(mKeyUser);

            final Iterator<Long> mInnerIter = paramMap.keySet().iterator();
            while (mInnerIter.hasNext()) { // iterate through all keys that have changed.
                long mId = (Long)mInnerIter.next();
                if (mManager.getKeySet().contains(mId)) {
                    mManager.addKey(paramMap.get(mId));
                }
            }

            // remove all old keys from user's key manager it is losing through group leaving.
            if (mKeyUser.equals(user)) {
                final Iterator<Long> mapIter = paramMap.keySet().iterator();
                while (mapIter.hasNext()) {
                    final long mMapKey = (Long)mapIter.next();
                    if (mManager.getKeySet().contains(mMapKey)) {
                        if (!mManager.getKeySet().contains(
                            paramMap.get(mMapKey))) {
                            mManager.removeKey(mMapKey);

                            final Iterator<Long> mIter =
                                Controller.getInstance().getDAGDb()
                                    .getEntries().keySet().iterator();
                            while (mIter.hasNext()) {
                                long mMapId = (Long)mIter.next();
                                final DAGSelector mInnerSel =
                                    Controller.getInstance().getDAGDb()
                                        .getEntries().get(mMapId);
                                if (mInnerSel.getName().equals(
                                    Controller.getInstance().getDAGDb()
                                        .getEntry(mMapKey).getName())) {
                                    mManager.removeKey(mMapId);
                                }
                            }
                        }
                    }
                }
            }
            Controller.getInstance().getManDb().putEntry(mManager);
        }
    }

    /**
     * Creates and encrypts key trails.
     *
     * @param paramList
     *            id list of all node which are affected by update.
     * @return
     *         key trails map.
     */
    private Map<Long, byte[]> encryptKeyTrails(final List<Long> paramList) {

        final Map<Long, byte[]> mKeyTrails = new HashMap<Long, byte[]>();
        final KeyManager mKeyManager = getManager(new Controller().getUser());

        // mKeyManager is NULL, when logged user is not a member of any group
        if (mKeyManager != null) {
            final Set<Long> mUserKeySet = mKeyManager.getKeySet();

            final Iterator<Long> mSetIter = mUserKeySet.iterator();

            while (mSetIter.hasNext()) {
                final long mMapId = (Long)mSetIter.next();

                if (paramList.contains(mMapId)) {

                    final List<Long> mChilds =
                        Controller.getInstance().getDAGDb().getEntry(mMapId)
                            .getChilds();
                    for (int i = 0; i < mChilds.size(); i++) {
                        if (mUserKeySet.contains(mChilds.get(i))) {
                            final byte[] mChildSecretKey =
                                Controller.getInstance().getDAGDb().getEntry(
                                    mChilds.get(i)).getSecretKey();
                            final byte[] mIdAsByteArray =
                                NodeEncryption.longToByteArray(mMapId);
                            final byte[] mEncryptedId =
                                NodeEncryption.encrypt(mIdAsByteArray,
                                    mChildSecretKey);
                            mKeyTrails.put(mChilds.get(i), mEncryptedId);
                        }
                    }
                }
            }
        }

        return mKeyTrails;
    }

}

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
import java.util.LinkedList;

import encryptionlayer.exception.TTEncryptionException;
import encryptionlayer.database.CurrentDAGDatabase;
import encryptionlayer.database.model.DAGSelector;
import encryptionlayer.utils.NodeEncryption;

public class DBTest {

    /**
     * Store path of berkeley dag selector db.
     */
    private static final File DAG_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("dagdb").toString());

    public static void main(String[] args) {
        int runs = 100000;

        LinkedList<Long> parents = new LinkedList<Long>();
        LinkedList<Long> childs = new LinkedList<Long>();

        for (long i = 0; i < runs; i++) {
            parents.add(i);
            childs.add(i);
        }

//        CurrentDAGDatabase dagdb = new CurrentDAGDatabase(DAG_STORE);
//       // dagdb.clearPersistent();
        try {
            Controller.getInstance().setEncryptionOption(true);
            Controller.getInstance().clear();
            Controller.getInstance().init();
        } catch (TTEncryptionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        long totaltime = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        System.out.println("Started...");

        for (int i = 0; i < runs; i++) {

            Controller.getInstance().getDAGDb().putEntry(new DAGSelector(String.valueOf(i), parents, childs,
                0, 0, NodeEncryption.generateSecretKey()));

            if (i % 100 == 0) {
                System.out.println(i);
                System.out.println("Time needed: "
                    + (System.currentTimeMillis() - time) + "ms");
                time = System.currentTimeMillis();
                System.out.println("");
            }

        }

        System.out.println("Ended...");
        System.out.println("Total time needed: "
            + (System.currentTimeMillis() - totaltime) + "ms");

    }
}

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import encryptionlayer.exception.TTEncryptionException;

public class TestDataParser {

    private static final String DATAFILE =
        "<add folder path here>/testdata100.txt";

    private static int joinOps = 0;
    private static int leaveOps = 0;

    public static void main(String[] args) {
        try {
            new Controller().clear();
            new Controller().setEncryptionOption(true);
            new Controller().init();

            long time = System.currentTimeMillis();
            System.out.println("Started...");
            init();
            System.out.println("Ended...");
            System.out.println("Total time needed: "
                + (System.currentTimeMillis() - time) + "ms");
            System.out.println("Total joins: " + joinOps);
            System.out.println("Total leaves: " + leaveOps);
            System.out.println("Total ops: " + (joinOps + leaveOps));

            new Controller().print();
            new Controller().clear();

        } catch (TTEncryptionException e) {
            e.printStackTrace();
        }
    }

    public static void init() throws TTEncryptionException {
        BufferedReader in;
        try {
            in =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    DATAFILE)));

            char splitter = '$';
            String line;
            int counter = 0;
            long time = System.currentTimeMillis();

            Operator op;

            while ((line = in.readLine()) != null) {
                op = new Operator();
                counter++;

                if (counter % 100 == 0) {
                    System.out.println(counter);
                    System.out.println("Time needed: "
                        + (System.currentTimeMillis() - time) + "ms");
                    time = System.currentTimeMillis();
                    System.out.println("Joins: " + joinOps + " Leaves: "
                        + leaveOps);
                    System.out.println("");
                }

                final char[] chars = line.toCharArray();
                final String[] dataString = new String[5];

                int stringCount = 0;
                int charCount = 1;

                final StringBuilder sb = new StringBuilder();

                for (char aChar : chars) {
                    if (aChar == splitter || charCount == chars.length) {
                        dataString[stringCount++] = sb.toString();
                        sb.setLength(0);
                    } else {
                        sb.append(aChar);
                    }
                    charCount++;
                }

                LinkedList<String> addedGroups = new LinkedList<String>();

                String cleanedGroup = cleanGroup(dataString[2]);
                String[] splittedGroup = splitGroup(cleanedGroup);

                String[] newGroups = new String[splittedGroup.length + 1];

                // rebuild array + user
                for (int i = 0; i < newGroups.length - 1; i++) {
                    newGroups[i] = splittedGroup[i];
                }
                newGroups[newGroups.length - 1] = dataString[0];

                // join / leave
                if (newGroups.length > 1) {
                    String childNode = newGroups[newGroups.length - 1];
                    String parentNode = newGroups[newGroups.length - 2];
                    // System.out.println("child: " + childNode);
                    // System.out.println("Parent: " + parentNode);

                    if (op.checkMembership(childNode, parentNode)) {
                        op.leave(childNode, new String[] {
                            parentNode
                        });
                        leaveOps++;
                        op.join(parentNode, new String[] {
                            childNode
                        });
                        joinOps++;
                    }

                    else {
                        String parent = "ROOT";
                        int i = 0;
                        while (i < newGroups.length) {
                            if (!addedGroups.contains(newGroups[i])) {
                                LinkedList<String> list =
                                    new LinkedList<String>();
                                for (int j = i; j < newGroups.length; j++) {
                                    list.add(newGroups[j]);
                                    addedGroups.add(newGroups[j]);
                                }
                                String[] childs = list.toArray(new String[0]);
                                op.join(parent, childs);
                                joinOps++;
                                break;
                            }

                            parent = newGroups[i];
                            i++;
                        }

                    }

                }
            }

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static String cleanGroup(final String group) {
        char[] groupChars = group.toCharArray();
        int pos = 0;
        // entferne Nullen
        int i = groupChars.length - 1;
        while (i >= 0) {
            if (groupChars[i] != '0') {
                pos = i;
                break;
            }
            i--;
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < pos + 1; j++) {
            sb.append(groupChars[j]);
        }

        return sb.toString();

    }

    public static String[] splitGroup(final String group) {
        char[] groupChars = group.toCharArray();

        String[] groups;

        if (groupChars.length > 1) {
            groups = new String[groupChars.length - 1];
        } else {
            groups = new String[groupChars.length];
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        int j = 0;
        while (i < groupChars.length) {
            if (i == 0) {
                sb.append(groupChars[i++]);
                if (groupChars.length > 1) {
                    sb.append(groupChars[i++]);
                }
                groups[j++] = sb.toString();
            } else {
                sb.append(groupChars[i++]);
                groups[j++] = sb.toString();
            }
        }

        return groups;

    }

}

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
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileParser {

    private static final String INPUTFILE =
        "<add folder path here>/testdata.txt";

    private static final String OUTPUTFILE =
        "<add folder path here>/testdata100.txt";

    public static void main(String[] args) {

        int total_runs = 100;

        System.out.println("Parsing started .., ");
        init(total_runs);
        System.out.println("Parsing ended ...");
    }

    public static void init(final int runs) {
        BufferedReader in;
        try {
            in =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    INPUTFILE)));

            // Create file
            FileWriter fstream = new FileWriter(OUTPUTFILE);
            BufferedWriter out = new BufferedWriter(fstream);

            String line;
            char splitter = '$';
            int run = 0;

            while (run < runs) {
                line = in.readLine();
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

                String cleanedGroup = cleanGroup(dataString[2]);
                String[] splittedGroup = splitGroup(cleanedGroup);

                StringBuilder writeLine = new StringBuilder();
                writeLine.append(dataString[0]);
                writeLine.append("$");

                for (int i = 0; i < splittedGroup.length; i++) {
                    writeLine.append(splittedGroup[i]);
                    if (i != (splittedGroup.length - 1)) {
                        writeLine.append(";");
                    }
                }
                out.write(writeLine.toString() + "\r\n");

                run++;
            }
            // Close the output stream
            out.close();

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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

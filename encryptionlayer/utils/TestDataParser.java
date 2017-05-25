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
package encryptionlayer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestDataParser {

    private static final String DATAFILE = "src" + File.separator + "main"
        + File.separator + "resources" + File.separator + "testdata.txt";

    public static void main(String[] args) {
        init();
    }

    public static void init() {

        BufferedReader in;
        try {
            in =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    DATAFILE)));

            char splitter = '$';
            String line;

            while ((line = in.readLine()) != null) {
                final char[] chars = line.toCharArray();
                final String[] dataString = new String[5];

                int stringCount = 0;
                int charCount = 0;

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

                for (String data : dataString) {
                    System.out.println(data);
                }
                System.out.println("");
            }

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

}

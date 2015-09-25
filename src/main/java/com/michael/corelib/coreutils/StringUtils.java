/*
 * Copyright (C) 2014 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michael.corelib.coreutils;

import com.michael.corelib.internet.NetworkLog;

import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class StringUtils {

    /** Splits a String based on a single character, which is usually faster than regex-based String.split(). */
    public static String[] fastSplit(String string, char delimiter) {
        List<String> list = new ArrayList<String>();
        int size = string.length();
        int start = 0;
        for (int i = 0; i < size; i++) {
            if (string.charAt(i) == delimiter) {
                if (start < i) {
                    list.add(string.substring(start, i));
                } else {
                    list.add("");
                }
                start = i + 1;
            } else if (i == size - 1) {
                list.add(string.substring(start, size));
            }
        }
        String[] elements = new String[list.size()];
        list.toArray(elements);
        return elements;
    }

    /**
     * URL-Encodes a given string using UTF-8 (some web pages have problems with UTF-8 and umlauts, consider
     * {@link #encodeUrlIso(String)} also). No UnsupportedEncodingException to handle as it is dealt with in this
     * method.
     */
    public static String encodeUrl(String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * URL-encodes a given string using ISO-8859-1, which may work better with web pages and umlauts compared to UTF-8.
     * No UnsupportedEncodingException to handle as it is dealt with in this method.
     */
    public static String encodeUrlIso(String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, "ISO-8859-1");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * URL-Decodes a given string using UTF-8. No UnsupportedEncodingException to handle as it is dealt with in this
     * method.
     */
    public static String decodeUrl(String stringToDecode) {
        try {
            return URLDecoder.decode(stringToDecode, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * URL-Decodes a given string using ISO-8859-1. No UnsupportedEncodingException to handle as it is dealt with in
     * this method.
     */
    public static String decodeUrlIso(String stringToDecode) {
        try {
            return URLDecoder.decode(stringToDecode, "ISO-8859-1");
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * Generates the MD5 digest for a given String based on UTF-8. The digest is padded with zeroes in the front if
     * necessary.
     *
     * @return MD5 digest (32 characters).
     */
    public static String generateMD5String(String stringToEncode) {
        return generateDigestString(stringToEncode, "MD5", "UTF-8", 32);
    }

    /**
     * Generates the SHA-1 digest for a given String based on UTF-8. The digest is padded with zeroes in the front if
     * necessary. The SHA-1 algorithm is considers to produce less collisions than MD5.
     *
     * @return SHA-1 digest (40 characters).
     */
    public static String generateSHA1String(String stringToEncode) {
        return generateDigestString(stringToEncode, "SHA-1", "UTF-8", 40);
    }

    public static String
    generateDigestString(String stringToEncode, String digestAlgo, String encoding, int lengthToPad) {
        // Loosely inspired by http://workbench.cadenhead.org/news/1428/creating-md5-hashed-passwords-java
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance(digestAlgo);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        try {
            digester.update(stringToEncode.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return toHexString(digester.digest(), lengthToPad);
    }

    public static String toHexString(byte[] bytes, int lengthToPad) {
        BigInteger hash = new BigInteger(1, bytes);
        String digest = hash.toString(16);

        while (digest.length() < lengthToPad) {
            digest = "0" + digest;
        }
        return digest;
    }

    /**
     * Simple HTML/XML entity resolving: Only supports unicode enitities and a very limited number text represented
     * entities (apos, quot, gt, lt, and amp). There are many more: http://www.w3.org/TR/REC-html40/sgml/dtd.html
     *
     * @param entity The entity name without & and ; (null throws NPE)
     * @return Resolved entity or the entity itself if it could not be resolved.
     */
    public static String resolveEntity(String entity) {
        if (entity.length() > 1 && entity.charAt(0) == '#') {
            if (entity.charAt(1) == 'x') {
                return String.valueOf((char) Integer.parseInt(entity.substring(2), 16));
            } else {
                return String.valueOf((char) Integer.parseInt(entity.substring(1)));
            }
        } else if (entity.equals("apos")) {
            return "'";
        } else if (entity.equals("quot")) {
            return "\"";
        } else if (entity.equals("gt")) {
            return ">";
        } else if (entity.equals("lt")) {
            return "<";
        } else if (entity.equals("amp")) {
            return "&";
        } else {
            return entity;
        }
    }

    /**
     * Cuts the string at the end if it's longer than maxLength and appends "..." to it. The length of the resulting
     * string including "..." is always less or equal to the given maxLength. It's valid to pass a null text; in this
     * case null is returned.
     */
    public static String ellipsize(String text, int maxLength) {
        if (text != null && text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    public static String[] splitLines(String text, boolean skipEmptyLines) {
        if (skipEmptyLines) {
            return text.split("[\n\r]+");
        } else {
            return text.split("\\r?\\n");
        }
    }

    public static List<String> findLinesContaining(String text, String searchText) {
        String[] splitLinesSkipEmpty = splitLines(text, true);
        List<String> matching = new ArrayList<String>();
        for (String line : splitLinesSkipEmpty) {
            if (line.contains(searchText)) {
                matching.add(line);
            }
        }
        return matching;
    }

    /**
     * Returns a concatenated string consisting of the given lines seperated by a new line character \n. The last line
     * does not have a \n at the end.
     */
    public static String concatLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        int countMinus1 = lines.size() - 1;
        for (int i = 0; i < countMinus1; i++) {
            builder.append(lines.get(i)).append('\n');
        }
        if (!lines.isEmpty()) {
            builder.append(lines.get(countMinus1));
        }
        return builder.toString();
    }

    public static String joinIterableOnComma(Iterable<?> iterable) {
        if (iterable != null) {

            StringBuilder buf = new StringBuilder();
            Iterator<?> it = iterable.iterator();
            while (it.hasNext()) {
                buf.append(it.next());
                if (it.hasNext()) {
                    buf.append(',');
                }
            }
            return buf.toString();
        } else {
            return "";
        }
    }

    public static String joinArrayOnComma(int[] array) {
        if (array != null) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (i != 0) {
                    buf.append(',');
                }
                buf.append(array[i]);
            }
            return buf.toString();
        } else {
            return "";
        }

    }

    public static String joinArrayOnComma(String[] array) {
        if (array != null) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (i != 0) {
                    buf.append(',');
                }
                buf.append(array[i]);
            }
            return buf.toString();
        } else {
            return "";
        }

    }

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder(512);
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    public static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n >>> 4 & 0xf;
        int d2 = n & 0xf;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String unGzipBytesToString(InputStream in) {
        try {
            PushbackInputStream pis = new PushbackInputStream(in, 2);
            byte[] signature = new byte[2];
            int readLength = pis.read(signature);
            pis.unread(signature);
            if (readLength == -1) {
                return null;
            }

            int head = ((signature[0] & 0x00FF) | ((signature[1] << 8) & 0xFF00));
            if (head != GZIPInputStream.GZIP_MAGIC) {
                return new String(toByteArray(pis), "UTF-8").trim();
            }
            GZIPInputStream gzip = new GZIPInputStream(pis);
            byte[] readBuf = new byte[8 * 1024];
            ByteArrayOutputStream outputByte = new ByteArrayOutputStream();
            int readCount = 0;
            do {
                readCount = gzip.read(readBuf);
                if (readCount > 0) {
                    outputByte.write(readBuf, 0, readCount);
                }
            } while (readCount > 0);
            if (outputByte.size() > 0) {
                return new String(outputByte.toByteArray(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将input流转为byte数组，自动关闭
     *
     * @param input
     * @return
     */
    public static byte[] toByteArray(InputStream input) throws Exception {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream output = null;
        byte[] result = null;
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 100];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            result = output.toByteArray();
        } finally {
            closeQuietly(input);
            closeQuietly(output);
        }
        return result;
    }

    /**
     * 关闭InputStream
     */
    private static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭InputStream
     */
    private static void closeQuietly(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dumpLongStringToLogcat(String longString) {
        int step = 2048;
        int index = 0;
        do {
            if (index >= longString.length()) {
                break;
            } else {
                if ((index + step) < longString.length()) {
                    NetworkLog.LOGD(longString.substring(index, index + step));
                } else {
                    NetworkLog.LOGD(longString.substring(index, longString.length()));
                }
            }
            index = index + step;
        } while (index < longString.length());
    }

}

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

package com.michael.corelib.io;

import com.michael.corelib.coreutils.StringUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Checksum;

/**
 * Utils for dealing with IO (streams, readers, ...).
 */
public class IoUtils {
    private static final int BUFFER_SIZE = 4096;

    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyAllBytes(in, out);
        return out.toByteArray();
    }

    public static byte[] readAllBytesAndClose(InputStream in) throws IOException {
        try {
            return readAllBytes(in);
        } finally {
            safeClose(in);
        }
    }

    public static String readAllChars(Reader reader) throws IOException {
        char[] buffer = new char[2048];
        StringBuilder builder = new StringBuilder();
        while (true) {
            int read = reader.read(buffer);
            if (read == -1) {
                break;
            }
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }

    public static String readAllCharsAndClose(Reader reader) throws IOException {
        try {
            return readAllChars(reader);
        } finally {
            safeClose(reader);
        }
    }

    public static void writeAllCharsAndClose(Writer writer, CharSequence text) throws IOException {
        try {
            writer.append(text);
        } finally {
            safeClose(writer);
        }
    }

    public static void updateChecksum(InputStream in, Checksum checksum) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            checksum.update(buffer, 0, read);
        }
    }

    /** @return MD5 digest (32 characters). */
    public static String getMd5(InputStream in) throws IOException {
        byte[] digest = getDigest(in, "MD5");
        return StringUtils.toHexString(digest, 32);
    }

    /** @return SHA-1 digest (40 characters). */
    public static String getSha1(InputStream in) throws IOException {
        byte[] digest = getDigest(in, "SHA-1");
        return StringUtils.toHexString(digest, 40);
    }

    public static byte[] getDigest(InputStream in, String digestAlgo) throws IOException {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance(digestAlgo);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }

        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            digester.update(buffer, 0, read);
        }
        return digester.digest();
    }

    /**
     * Copies all available data from in to out without closing any stream.
     *
     * @return number of bytes copied
     */
    public static int copyAllBytes(InputStream in, OutputStream out) throws IOException {
        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            out.write(buffer, 0, read);
            byteCount += read;
        }
        return byteCount;
    }


    /** Closes the given stream inside a try/catch. Does nothing if stream is null. */
    public static void safeClose(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // Silent
            }
        }
    }

    /** Closes the given stream inside a try/catch. Does nothing if stream is null. */
    public static void safeClose(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // Silent
            }
        }
    }

    /** Closes the given stream inside a try/catch. Does nothing if stream is null. */
    public static void safeClose(Reader in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // Silent
            }
        }
    }

    /** Closes the given stream inside a try/catch. Does nothing if stream is null. */
    public static void safeClose(Writer out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // Silent
            }
        }
    }

}

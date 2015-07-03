/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.michael.corelib.coreutils;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class StringUtils {

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
                                                  "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String unGzipBytesToString(InputStream in) {

        try {
            PushbackInputStream pis = new PushbackInputStream(in, 2);
            byte[] signature = new byte[2];
            pis.read(signature);
            pis.unread(signature);
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
                return new String(outputByte.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(origin.getBytes()));
        } catch (Exception ex) {

        }
        return resultString;
    }

    public static String stringHashCode(String src) {
        if (!TextUtils.isEmpty(src)) {
            return String.valueOf(src.hashCode());
        }

        return null;
    }

    public static int intHashCode(String src) {
        if (!TextUtils.isEmpty(src)) {
            return src.hashCode();
        }

        return 0;
    }

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

    /**
     * 关闭InputStream
     */
    public static void closeQuietly(InputStream is) {
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
    public static void closeQuietly(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * @param temp
     * @return
     */
    public static Object replaceBlank(CharSequence temp) {
        String dest = "";
        if (temp != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(temp);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String format(int temp) {
        String dest = "";
        DecimalFormat df = new DecimalFormat();
        if (temp >= 0 && temp < 10000) {
            dest = String.valueOf(temp);
        }
        if (temp >= 10000 && temp <= 999000) {
            String style = "#.#K";
            df.applyPattern(style);
            double temDou = temp;
            dest = df.format(temDou / 1000);
        }

        if (temp > 999000 && temp <= 999000000) {
            String style = "#.#M";
            df.applyPattern(style);
            double temDou = temp;
            dest = df.format(temDou / 1000000);
        }

        if (temp > 999000000 && temp <= Math.pow(2, 31) - 1) {
            String style = "#.#B";
            df.applyPattern(style);
            double temDou = temp;
            dest = df.format(temDou / 1000000000);
        }
        if (temp > Math.pow(2, 31) - 1) {
            String style = "#.#B";
            df.applyPattern(style);
            double temDou = Math.pow(2, 31) - 1;
            dest = df.format(temDou / 1000000000);
        }

        return dest;
    }
}

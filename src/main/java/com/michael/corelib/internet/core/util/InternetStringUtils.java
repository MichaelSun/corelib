package com.michael.corelib.internet.core.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

public class InternetStringUtils {
    
    private static final int DEFAULT_BUFFER_SIZE = 16 * 1024;
    
    private final static String[] hexDigits = {
        "0", "1", "2", "3", "4", "5", "6", "7",
        "8", "9", "a", "b", "c", "d", "e", "f"};
    
    public static String unGzipBytesToString(InputStream in) {

        GZIPInputStream gzip = null;
        ByteArrayOutputStream outputByte = null;
        try {
            PushbackInputStream pis = new PushbackInputStream(in, 2);
            byte[] signature = new byte[2];
            pis.read(signature);
            pis.unread(signature);
            int head = ((signature[0] & 0x00FF) | ((signature[1] << 8) & 0xFF00));
            if (head != GZIPInputStream.GZIP_MAGIC) {
                return new String(toByteArray(pis), "UTF-8").trim();
            }
            gzip = new GZIPInputStream(pis);
            byte[] readBuf = new byte[DEFAULT_BUFFER_SIZE];
            outputByte = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
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
        } finally {
            try {
                if (gzip != null) {
                    gzip.close();
                }
                if (outputByte != null) {
                    outputByte.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public static String MD5Encode(String origin) {
        String resultString = null;
        try {           
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(origin.getBytes()));
        }
        catch (Exception ex) {
        
        }
        return resultString;
    }
    
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }
    
    private static String byteToHexString(byte b) {
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
            output = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
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
}

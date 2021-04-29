package com.qiniu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5 {
    private Md5() {

    }

    public static String md5(byte[] data) {
        return md5(data, 0, data.length);
    }

    public static String md5(byte[] data, int offset, int len) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(data, offset, len);
        byte[] secretBytes = md.digest();
        return getFormattedText(secretBytes);
    }

    public static String md5(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            return md5(fis, file.length());
        } finally {
            fis.close();
        }
    }


    public static String md5(InputStream in, final long length) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] buffer = new byte[1024 * 4];
        int buffSize = buffer.length;
        long len = length;
        while (len != 0) {
            int next = (int) (buffSize > len ? len : buffSize);
            next = in.read(buffer, 0, next);
            if (next == -1) {
                throw new IOException("input stream length: " + len + " does not match the argument: " + length);
            }
            if (next != 0) {
                md.update(buffer, 0, next);
                len -= next;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
        byte[] secretBytes = md.digest();
        return getFormattedText(secretBytes);
    }


    private static String getFormattedText(byte[] src) {
        if (src == null || src.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(32);
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}

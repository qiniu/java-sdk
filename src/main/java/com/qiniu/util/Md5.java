package com.qiniu.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5 {
    private Md5() {

    }

    public static String md5(byte[] data) {
        return md5(data, 0, data.length);
    }

    public static String md5(byte[] data, int offset, int len) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(data, offset, len);
        byte[] secretBytes = md.digest();
        return getFormattedText(secretBytes);
    }

    public static String md5(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return md5(fis, file.length());
    }


    public static String md5(InputStream in, long len) throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[1024 * 4];
        int buffSize = buffer.length;
        while (len != 0) {
            int next = (int) (buffSize > len ? len : buffSize);
            //noinspection ResultOfMethodCallIgnored
            in.read(buffer, 0, next);
            md.update(buffer, 0, next);
            len -= next;
        }
        byte[] secretBytes = md.digest();
        return getFormattedText(secretBytes);
    }



    private static String getFormattedText(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder(32);
        if (src == null || src.length == 0) {
            return "";
        }
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

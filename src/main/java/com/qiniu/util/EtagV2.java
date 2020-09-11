package com.qiniu.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class EtagV2 {
    private EtagV2() {
    }

    public static String data(byte[] data, int offset, int length, long blockSize) {
        try {
            return stream(new ByteArrayInputStream(data, offset, length), length, blockSize);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String data(byte[] data, long blockSize) {
        return data(data, 0, data.length, blockSize);
    }

    public static String data(byte[] data, int offset, int length, long[] parts) {
        try {
            return stream(new ByteArrayInputStream(data, offset, length), length, parts);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String data(byte[] data, long[] parts) {
        return data(data, 0, data.length, parts);
    }

    public static String file(String filePath, long blockSize) throws IOException {
        return file(new File(filePath), blockSize);
    }

    public static String file(String filePath, long[] parts) throws IOException {
        return file(new File(filePath), parts);
    }

    public static String file(File file, long blockSize) throws IOException {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            return stream(fi, file.length(), blockSize);
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (Throwable t) {
                }
            }
        }
    }

    public static String file(File file, long[] parts) throws IOException {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            return stream(fi, file.length(), parts);
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (Throwable t) {
                }
            }
        }
    }


    public static String stream(InputStream in, long len, long blockSize) throws IOException {
        // // 和 is4MBParts(new long[]{len}) 实质一样，只有一个 part 的简化版 // //
        if (blockSize == 1024 * 1024 * 4 || (len <= blockSize && len <= 1024 * 1024 * 4)) {
            return Etag.stream(in, len);
        }
        int size = (int) ((len + blockSize - 1) / blockSize);
        long[] parts = new long[size];
        Arrays.fill(parts, 0, size - 1, blockSize);
        parts[size - 1] = len % blockSize;

        return etagV2(in, len, parts);
    }

    public static String stream(InputStream in, long len, long[] parts) throws IOException {
        if (is4MBParts(parts)) {
            return Etag.stream(in, len);
        }

        return etagV2(in, len, parts);
    }

    private static boolean is4MBParts(long[] parts) {
        int idx = 0;
        int last = parts.length - 1;
        for (long part : parts) {
            if (idx != last && part != 1024 * 1024 * 4 || part > 1024 * 1024 * 4) {
                return false;
            }
            idx += 1;
        }
        return true;
    }

    private static String etagV2(InputStream in, long len, long[] parts) throws IOException {
        long partSize = 0;
        for (long part : parts) {
            partSize += part;
        }
        if (len != partSize) {
            throw new IOException("etag calc failed: size not equal with part size");
        }

        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("sha-1");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        for (long part : parts) {
            String partEtag = Etag.stream(in, part);
            byte[] bytes = UrlSafeBase64.decode(partEtag);
            sha1.update(bytes, 1, bytes.length - 1);
        }
        byte[] digest = sha1.digest();
        byte[] ret = new byte[digest.length + 1];
        ret[0] = (byte) 0x9e;
        System.arraycopy(digest, 0, ret, 1, digest.length);
        return UrlSafeBase64.encodeToString(ret);
    }
}


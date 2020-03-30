package test.com.qiniu.util;

import com.qiniu.common.Constants;
import com.qiniu.util.Etag;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Test;
import test.com.qiniu.TempFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class EtagTest {
    @Test
    public void testData() {
        String m = Etag.data(new byte[0]);
        assertEquals("Fto5o-5ea0sNMlW_75VgGJCv2AcJ", m);

        String etag = Etag.data("etag".getBytes(Constants.UTF_8));
        assertEquals("FpLiADEaVoALPkdb8tJEJyRTXoe_", etag);
    }

    @Test
    public void testFile() throws IOException {
        File f = TempFile.createFileOld(1024);
        assertEquals("FqOV9T8l48x1u9dFEOROzwp4b0jr", Etag.file(f));
        TempFile.remove(f);

        f = TempFile.createFileOld(4 * 1024);
//        System.out.println(Etag.file(f));
        assertEquals("FgtlGCfS97kgqopAxq0vvKRA5o_R", Etag.file(f));
        TempFile.remove(f);

        f = TempFile.createFileOld(5 * 1024);
//        System.out.println(Etag.file(f));
        assertEquals("lvtM_iKNnZXkZR8U3Fvi_QQO2yyi", Etag.file(f));
        TempFile.remove(f);

        f = TempFile.createFileOld(8 * 1024);
//        System.out.println(Etag.file(f));
        assertEquals("lhiIbcJgsRrl46LwMD9KHAQRLH2O", Etag.file(f));
        TempFile.remove(f);

        f = TempFile.createFileOld(9 * 1024);
//        System.out.println(Etag.file(f));
        assertEquals("ll1xhlUFKQqynVgMMt_J1TuTrdB1", Etag.file(f));
        TempFile.remove(f);
    }

    public static String etagV2(File file) throws IOException {
        return etagV2(file, 1024 * 1024 * 4);
    }

    public static String etagV2(File file, long blockSize) throws IOException {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            return etagV2(fi, file.length(), blockSize);
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (Throwable t) {
                }
            }
        }
    }

    public static String etagV2(File file, long[] parts) throws IOException {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            return etagV2(fi, file.length(), parts);
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (Throwable t) {
                }
            }
        }
    }

    public static String etagV2(InputStream in, long len) throws IOException {
        return etagV2(in, len, 1024 * 1024 * 4);
    }

    public static String etagV2(InputStream in, long len, long blockSize) throws IOException {
        if (blockSize == 1024 * 1024 * 4 || (len <= blockSize && len <= 1024 * 1024 * 4)) {
            return Etag.stream(in, len);
        }
        int size = (int) ((len + blockSize - 1) / blockSize);
        long[] parts = new long[size];
        Arrays.fill(parts, blockSize);
        parts[size - 1] = len % blockSize;

        // is4MBParts(parts)  include (len <= blockSize && len <= 1024 * 1024 * 4)
        // means only one block, and it's size <= 4M

        return etagV2NoCheck(in, len, parts);
    }

    public static String etagV2(InputStream in, long len, long[] parts) throws IOException {
        if (is4MBParts(parts)) {
            return Etag.stream(in, len);
        }
        long partSize = 0;
        for (long part : parts) {
            partSize += part;
        }
        if (len != partSize) {
            throw new IOException("etag calc failed: size not equal with part size");
        }
        return etagV2NoCheck(in, len, parts);
    }

    private static boolean is4MBParts(long[] parts) {
        if (parts.length == 0) {
            return true;
        }
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

    private static String etagV2NoCheck(InputStream in, long len, long[] parts) throws IOException {
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

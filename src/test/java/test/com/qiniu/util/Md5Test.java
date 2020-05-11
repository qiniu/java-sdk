package test.com.qiniu.util;

import com.qiniu.util.Md5;
import com.qiniu.util.StringUtils;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TempFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class Md5Test {
    File f;

    @Before
    public void init() throws IOException {
        f = TempFile.createFileOld(1);
    }

    @Test
    public void test1() throws IOException, NoSuchAlgorithmException {
        System.out.println(f.getAbsolutePath());
        String md5 = Md5.md5(f);
        System.out.println(md5);

        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[1024 * 1024 * 7];
        int l = fis.read(data);
        String md5_data = Md5.md5(data, 0, l);
        System.out.println(l + ",  " + data.length + "  md5  " + md5_data);

        assertEquals(md5, md5_data);
        assertEquals(md5, "0f343b0931126a20f133d67c2b018a3b");
        String src = "FileInputStream fis = new FileInputStream(f);";

        String md5Str = StringUtils.md5Lower(src);
        System.out.println(md5Str);

        assertEquals(md5Str, Md5.md5(src.getBytes()));
    }
}

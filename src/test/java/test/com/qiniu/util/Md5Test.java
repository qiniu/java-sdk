package test.com.qiniu.util;

import com.qiniu.util.Md5;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TempFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public class Md5Test {
    File f;

    @Before
    public void init() throws IOException {
        f = TempFile.createFileOld(1);
    }

    @Test
    public void test1() throws IOException {
        System.out.println(f.getAbsolutePath());
        String md5 = Md5.md5(f);
        System.out.println(md5);

        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[1024 * 1024 * 7];
        int l = fis.read(data);
        String md5_data = Md5.md5(data, 0, l);
        System.out.println(l + ",  " +  data.length + "  md5  " + md5_data);

        assertEquals(md5, md5_data);
        assertEquals(md5, "543757d0e58f2f581df42529d50baa4a");
    }
}

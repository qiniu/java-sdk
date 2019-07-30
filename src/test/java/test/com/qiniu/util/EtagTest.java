package test.com.qiniu.util;

import com.qiniu.common.Constants;
import com.qiniu.util.Etag;
import org.junit.Test;
import test.com.qiniu.TempFile;

import java.io.File;
import java.io.IOException;

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
}

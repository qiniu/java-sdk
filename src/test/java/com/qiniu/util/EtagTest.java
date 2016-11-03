package com.qiniu.util;

import com.qiniu.TempFile;
import com.qiniu.common.Constants;
import org.junit.Test;

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
        assertEquals("Foyl8onxBLWeRLL5oItRJphv6i4b", Etag.file(f));
        TempFile.remove(f);
        f = TempFile.createFileOld(4 * 1024);
        assertEquals("FicHOveBNs5Kn9d74M3b9tI4D-8r", Etag.file(f));
        TempFile.remove(f);
        f = TempFile.createFileOld(5 * 1024);
        assertEquals("lg-Eb5KFCuZn-cUfj_oS2PPOU9xy", Etag.file(f));
        TempFile.remove(f);
        f = TempFile.createFileOld(8 * 1024);
        assertEquals("lkSKZOMToDp-EqLDVuT1pyjQssl-", Etag.file(f));
        TempFile.remove(f);
        f = TempFile.createFileOld(9 * 1024);
        assertEquals("ljgVjMtyMsOgIySv79U8Qz4TrUO4", Etag.file(f));
        TempFile.remove(f);
    }
}

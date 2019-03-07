package test.com.qiniu.util;

import com.qiniu.util.Crc32;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class CrcTest {
    @Test
    public void testCrc() {
        byte[] data = "Hello, World!".getBytes();
        long result = Crc32.bytes(data);
        assertEquals(3964322768L, result);
    }
}

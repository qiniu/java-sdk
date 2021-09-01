package test.com.qiniu.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.qiniu.util.Crc32;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class CrcTest {
    @Test
    @Tag("UnitTest")
    public void testCrc() {
        byte[] data = "Hello, World!".getBytes();
        long result = Crc32.bytes(data);
        assertEquals(3964322768L, result);
    }
}

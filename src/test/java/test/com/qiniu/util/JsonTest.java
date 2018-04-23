package test.com.qiniu.util;

import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JsonTest {
    @Test
    public void testMapToString() {
        StringMap map = new StringMap().put("k", "v").put("n", 1);
        String j = Json.encode(map);
        assertTrue(j.equals("{\"k\":\"v\",\"n\":1}") || j.equals("{\"n\":1,\"k\":\"v\"}"));
    }
}

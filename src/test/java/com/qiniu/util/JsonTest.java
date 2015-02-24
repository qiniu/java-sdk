package com.qiniu.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonTest {
    @Test
    public void testMapToString() {
        StringMap map = new StringMap().put("k", "v").put("n", 1);
        String j = Json.encode(map);
        assertEquals("{\"k\":\"v\",\"n\":1}", j);
    }
}

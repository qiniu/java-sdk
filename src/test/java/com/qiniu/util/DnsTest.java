package com.qiniu.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DnsTest {
    @Test
    public void testDns() {
        String ip = Dns.getAddressesString("qiniu.com");
        assertTrue(!ip.equals(""));
        ip = Dns.getAddressesString("nodns.qiniu.com");
        assertEquals("", ip);
    }
}

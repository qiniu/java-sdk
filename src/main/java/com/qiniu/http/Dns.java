package com.qiniu.http;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public interface Dns {
    List<InetAddress> lookup(String hostname) throws UnknownHostException;
}

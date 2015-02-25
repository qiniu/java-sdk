package com.qiniu.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Dns解析类
 */
public final class Dns {

    private Dns() {
    }

    /**
     * 根据域名解析出来 IP数组
     *
     * @param hostName 域名
     * @return IP 数组
     */
    public static String[] getAddresses(String hostName) {
        InetAddress[] ret = null;
        try {
            ret = InetAddress.getAllByName(hostName);
        } catch (UnknownHostException e) {
//            e.printStackTrace();
            return new String[0];
        }
        String[] r = new String[ret.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = ret[i].getHostAddress();
        }
        return r;
    }

    /**
     * 根据域名解析出来IP列表，并合并为一个字符串，通过';'分隔
     *
     * @param hostName 域名
     * @return IP列表
     */
    public static String getAddressesString(String hostName) {
        return StringUtils.join(getAddresses(hostName), ";");
    }
}

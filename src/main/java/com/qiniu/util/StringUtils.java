package com.qiniu.util;

import com.qiniu.common.Config;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * 字符串连接工具类
 */
public final class StringUtils {

    /**
     * 以指定的分隔符来进行字符串元素连接
     * <p>
     * 例如有字符串数组array和连接符为逗号(,)
     * <code>
     * String[] array = new String[] { "hello", "world", "qiniu", "cloud","storage" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * hello,world,qiniu,cloud,storage
     * </code>
     * </p>
     *
     * @param array 需要连接的字符串数组
     * @param sep   元素连接之间的分隔符
     * @return 连接好的新字符串
     */
    public static String join(String[] array, String sep) {
        return join(array, sep, null);
    }

    /**
     * 以指定的分隔符来进行字符串元素连接
     * <p>
     * 例如有字符串数组array和连接符为逗号(,)
     * <code>
     * String[] array = new String[] { "hello", "world", "qiniu", "cloud","storage" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * hello,world,qiniu,cloud,storage
     * </code>
     * </p>
     *
     * @param array  需要连接的字符串数组
     * @param sep    元素连接之间的分隔符
     * @param prefix 前缀字符串
     * @return 连接好的新字符串
     */
    public static String join(String[] array, String sep, String prefix) {
        if (array == null) {
            return null;
        }

        int arraySize = array.length;
        int sepSize = 0;
        if (sep != null && !sep.equals("")) {
            sepSize = sep.length();
        }

        int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].length()) + sepSize) * arraySize);
        StringBuilder buf = new StringBuilder(bufSize);
        if (prefix != null) {
            buf.append(prefix);
        }
        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(sep);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * 以指定的分隔符来进行字符串列表连接
     *
     * @param list   需要连接的字符串列表
     * @param sep    元素连接之间的分隔符
     * @param prefix 前缀字符串
     * @return 连接好的新字符串
     */
    public static String join(Collection<String> list, String sep, String prefix) {
        if (list == null) {
            return null;
        }
        int arraySize = list.size();
        if (arraySize == 0) {
            return prefix;
        }
        int sepSize = 0;
        if (sep != null && !sep.equals("")) {
            sepSize = sep.length();
        }
        String first = list.iterator().next();
        int bufSize = ((first == null ? 16 : first.length()) + sepSize) * arraySize;
        StringBuilder buf = new StringBuilder(bufSize);
        if (prefix != null) {
            buf.append(prefix);
        }
        for (String it : list) {
            if (it != null) {
                buf.append(it);
                buf.append(sep);
            }
        }
        return buf.toString();
    }

    /**
     * 以指定的分隔符来进行字符串元素连接
     * <p>
     * 例如有字符串数组array和连接符为逗号(,)
     * <code>
     * String[] array = new String[] { "hello", "world", "qiniu", "cloud","storage" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * hello,world,qiniu,cloud,storage
     * </code>
     * </p>
     *
     * @param array  需要连接的对象数组
     * @param sep    元素连接之间的分隔符
     * @param prefix 前缀字符串
     * @return 连接好的新字符串
     */
    public static String join(Object[] array, String sep, String prefix) {
        if (array == null) {
            return null;
        }

        int arraySize = array.length;

        if (arraySize == 0) {
            return prefix;
        }
        int sepSize = 0;
        if (sep != null && !sep.equals("")) {
            sepSize = sep.length();
        }

        int bufSize = ((array[0] == null ? 16 : array[0].toString().length()) + sepSize) * arraySize;
        StringBuilder buf = new StringBuilder(bufSize);
        if (prefix != null) {
            buf.append(prefix);
        }
        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(sep);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * 以json元素的方式连接字符串中元素
     * <p>
     * 例如有字符串数组array
     * <code>
     * String[] array = new String[] { "hello", "world", "qiniu", "cloud","storage" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * "hello","world","qiniu","cloud","storage"
     * </code>
     * </p>
     *
     * @param array 需要连接的字符串数组
     * @return 以json元素方式连接好的新字符串
     */
    public static String jsonJoin(String[] array) {
        int arraySize = array.length;
        int bufSize = arraySize * (array[0].length() + 3);
        StringBuilder buf = new StringBuilder(bufSize);
        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(',');
            }

            buf.append('"');
            buf.append(array[i]);
            buf.append('"');
        }
        return buf.toString();
    }

    public static boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }

    public static boolean inStringArray(String s, String[] array) {
        for (String x : array) {
            if (x.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static byte[] utf8Bytes(String data) {
        try {
            return data.getBytes(Config.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static String utf8String(byte[] data) {
        try {
            return new String(data, Config.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}


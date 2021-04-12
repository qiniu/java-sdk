package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;

import java.util.Map;

class ApiUtils {

    private ApiUtils() {
    }

    /**
     * 分片上传 v2 对 key encode
     *
     * @param key key
     * @return encode key
     */
    static String resumeV2EncodeKey(String key) {
        String encodeKey = null;
        if (key == null) {
            encodeKey = "~";
        } else if (key.equals("")) {
            encodeKey = "";
        } else {
            encodeKey = UrlSafeBase64.encodeToString(key);
        }
        return encodeKey;
    }

    /**
     * 抛出参数异常
     *
     * @param paramName 异常参数
     * @throws QiniuException 异常
     */
    static void throwInvalidRequestParamException(String paramName) throws QiniuException {
        if (StringUtils.isNullOrEmpty(paramName)) {
            throw QiniuException.unrecoverable("");
        } else {
            throw QiniuException.unrecoverable(paramName + " is invalid, set before request!");
        }
    }

    /**
     * Object 转 Long
     * 注意：Object 必须是一个数字， 否则转换不成功, 返回 null
     *
     * @return Integer
     */
    static Long objectToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return ((Double) value).longValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            return new Long((String) value);
        } else {
            return null;
        }
    }

    /**
     * Object 转 Integer
     * 注意：Object 必须是一个数字， 否则转换不成功, 返回 null
     *
     * @return Integer
     */
    static Integer objectToInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof String) {
            return new Integer((String) value);
        } else {
            return null;
        }
    }

    /**
     * 根据 keyPath 读取 map 中对应的 value
     * eg：
     * map: {"key00" : { "key10" : "key10_value"}}
     * keyPath = new String[]{"key00", "key10"}
     * 调用方法后 value = key10_value
     *
     * @param keyPath keyPath
     * @return keyPath 对应的 value
     */
    static Object getValueFromMap(Map<String, Object> map, String... keyPath) {
        if (map == null || keyPath == null || keyPath.length == 0) {
            return null;
        }

        Object value = map;
        for (String key : keyPath) {
            if (value instanceof Map) {
                value = ((Map) value).get(key);
            } else {
                value = null;
                break;
            }
        }

        return value;
    }
}

package com.qiniu.util;

import com.google.gson.annotations.SerializedName;
import com.qiniu.common.Constants;
import com.qiniu.http.Client;
import com.qiniu.http.Headers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Auth {
    public static final String DISABLE_QINIU_TIMESTAMP_SIGNATURE_ENV_KEY = "DISABLE_QINIU_TIMESTAMP_SIGNATURE";
    public static final String DTOKEN_ACTION_VOD = "linking:vod";
    public static final String DTOKEN_ACTION_STATUS = "linking:status";
    public static final String DTOKEN_ACTION_TUTK = "linking:tutk";
    public static final String HTTP_HEADER_KEY_CONTENT_TYPE = "Content-Type";

    private static final String QBOX_AUTHORIZATION_PREFIX = "QBox ";
    private static final String QINIU_AUTHORIZATION_PREFIX = "Qiniu ";

    /**
     * 上传策略
     * 参考文档：<a href="https://developer.qiniu.com/kodo/manual/put-policy">上传策略</a>
     */
    private static final String[] policyFields = new String[]{
            "callbackUrl",
            "callbackBody",
            "callbackHost",
            "callbackBodyType",
            "callbackFetchKey",

            "returnUrl",
            "returnBody",

            "endUser",
            "saveKey",
            "insertOnly",
            "isPrefixalScope",

            "detectMime",
            "mimeLimit",
            "fsizeLimit",
            "fsizeMin",

            "persistentOps",
            "persistentNotifyUrl",
            "persistentPipeline",

            "deleteAfterDays",
            "fileType",
    };
    private static final String[] deprecatedPolicyFields = new String[]{
            "asyncOps",
    };
    private static final boolean[] isTokenTable = genTokenTable();
    private static final byte toLower = 'a' - 'A';
    public final String accessKey;
    private final SecretKeySpec secretKey;

    private Auth(String accessKey, SecretKeySpec secretKeySpec) {
        this.accessKey = accessKey;
        this.secretKey = secretKeySpec;
    }

    public static Auth create(String accessKey, String secretKey) {
        if (StringUtils.isNullOrEmpty(accessKey) || StringUtils.isNullOrEmpty(secretKey)) {
            throw new IllegalArgumentException("empty key");
        }
        byte[] sk = StringUtils.utf8Bytes(secretKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(sk, "HmacSHA1");
        return new Auth(accessKey, secretKeySpec);
    }

    private static void copyPolicy(final StringMap policy, StringMap originPolicy, final boolean strict) {
        if (originPolicy == null) {
            return;
        }
        originPolicy.forEach(new StringMap.Consumer() {
            @Override
            public void accept(String key, Object value) {
                if (StringUtils.inStringArray(key, deprecatedPolicyFields)) {
                    throw new IllegalArgumentException(key + " is deprecated!");
                }
                if (!strict || StringUtils.inStringArray(key, policyFields)) {
                    policy.put(key, value);
                }
            }
        });
    }

    // https://github.com/golang/go/blob/master/src/net/textproto/reader.go#L596
    // CanonicalMIMEHeaderKey returns the canonical format of the
    // MIME header key s. The canonicalization converts the first
    // letter and any letter following a hyphen to upper case;
    // the rest are converted to lowercase. For example, the
    // canonical key for "accept-encoding" is "Accept-Encoding".
    // MIME header keys are assumed to be ASCII only.
    // If s contains a space or invalid header field bytes, it is
    // returned without modifications.
    private static String canonicalMIMEHeaderKey(String name) {
        // com.qiniu.http.Headers 已确保 header name 字符的合法性，直接使用 byte ，否则要使用 char //
        byte[] a = name.getBytes(Constants.UTF_8);
        for (int i = 0; i < a.length; i++) {
            byte c = a[i];
            if (!validHeaderFieldByte(c)) {
                return name;
            }
        }

        boolean upper = true;
        for (int i = 0; i < a.length; i++) {
            byte c = a[i];
            if (upper && 'a' <= c && c <= 'z') {
                c -= toLower;
            } else if (!upper && 'A' <= c && c <= 'Z') {
                c += toLower;
            }
            a[i] = c;
            upper = c == '-'; // for next time
        }
        return new String(a, Constants.UTF_8);
    }

    private static boolean validHeaderFieldByte(byte b) {
        //byte: -128 ~ 127, char:  0 ~ 65535
        return 0 < b && b < isTokenTable.length && isTokenTable[b];
    }

    private static boolean[] genTokenTable() {
        int[] idx = new int[]{
                '!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'W', 'V', 'X', 'Y', 'Z', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
                'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '|', '~'};
        boolean[] tokenTable = new boolean[127];
        Arrays.fill(tokenTable, false);
        for (int i : idx) {
            tokenTable[i] = true;
        }
        return tokenTable;
    }

    private Mac createMac() {
        Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return mac;
    }

    @Deprecated // private
    public String sign(byte[] data) {
        Mac mac = createMac();
        String encodedSign = UrlSafeBase64.encodeToString(mac.doFinal(data));
        return this.accessKey + ":" + encodedSign;
    }

    @Deprecated // private
    public String sign(String data) {
        return sign(StringUtils.utf8Bytes(data));
    }

    @Deprecated // private
    public String signWithData(byte[] data) {
        String s = UrlSafeBase64.encodeToString(data);
        return sign(StringUtils.utf8Bytes(s)) + ":" + s;
    }

    @Deprecated // private
    public String signWithData(String data) {
        return signWithData(StringUtils.utf8Bytes(data));
    }

    /**
     * 生成HTTP请求签名字符串
     *
     * @param urlString   签名请求的 url
     * @param body        签名请求的 body
     * @param contentType 签名请求的 contentType
     * @return 签名信息
     */
    @Deprecated // private
    public String signRequest(String urlString, byte[] body, String contentType) {
        URI uri = URI.create(urlString);
        String path = uri.getRawPath();
        String query = uri.getRawQuery();

        Mac mac = createMac();

        mac.update(StringUtils.utf8Bytes(path));

        if (query != null && query.length() != 0) {
            mac.update((byte) ('?'));
            mac.update(StringUtils.utf8Bytes(query));
        }
        mac.update((byte) '\n');
        if (body != null && Client.FormMime.equalsIgnoreCase(contentType)) {
            mac.update(body);
        }

        String digest = UrlSafeBase64.encodeToString(mac.doFinal());

        return this.accessKey + ":" + digest;
    }

    /**
     * 验证回调签名是否正确，此方法仅能验证 QBox 签名以及 GET 请求的 Qiniu 签名
     *
     * @param originAuthorization 待验证签名字符串，以 "QBox " 作为起始字符，GET 请求支持 "Qiniu " 开头。
     * @param url                 回调地址
     * @param body                回调请求体。原始请求体，不要解析后再封装成新的请求体--可能导致签名不一致。
     * @param contentType         回调 ContentType
     * @return 签名是否正确
     */
    @Deprecated
    public boolean isValidCallback(String originAuthorization, String url, byte[] body, String contentType) {
        Headers header = null;
        if (!StringUtils.isNullOrEmpty(contentType)) {
            header = new Headers.Builder()
                    .add(HTTP_HEADER_KEY_CONTENT_TYPE, contentType)
                    .build();
        }
        return isValidCallback(originAuthorization, new Request(url, "GET", header, body));
    }

    /**
     * 验证回调签名是否正确，此方法支持验证 QBox 和 Qiniu 签名
     *
     * @param originAuthorization 待验证签名字符串，以 "QBox " 或 "Qiniu " 作为起始字符
     * @param callback            callback 请求信息
     * @return 签名是否正确
     */
    public boolean isValidCallback(String originAuthorization, Request callback) {
        if (callback == null) {
            return false;
        }

        String authorization = "";
        if (originAuthorization.startsWith(QINIU_AUTHORIZATION_PREFIX)) {
            authorization = QINIU_AUTHORIZATION_PREFIX + signQiniuAuthorization(callback.url, callback.method, callback.body, callback.header);
        } else if (originAuthorization.startsWith(QBOX_AUTHORIZATION_PREFIX)) {
            String contentType = null;
            if (callback.header != null) {
                contentType = callback.header.get(HTTP_HEADER_KEY_CONTENT_TYPE);
            }
            authorization = QBOX_AUTHORIZATION_PREFIX + signRequest(callback.url, callback.body, contentType);
        }
        return authorization.equals(originAuthorization);
    }


    /**
     * 下载签名
     *
     * @param baseUrl 待签名文件url，如 http://img.domain.com/u/3.jpg 、
     *                http://img.domain.com/u/3.jpg?imageView2/1/w/120
     * @return 签名
     */
    public String privateDownloadUrl(String baseUrl) {
        return privateDownloadUrl(baseUrl, 3600);
    }

    /**
     * 下载签名
     *
     * @param baseUrl 待签名文件url，如 http://img.domain.com/u/3.jpg 、
     *                http://img.domain.com/u/3.jpg?imageView2/1/w/120
     * @param expires 有效时长，单位秒。默认3600s
     * @return 签名
     */
    public String privateDownloadUrl(String baseUrl, long expires) {
        long deadline = System.currentTimeMillis() / 1000 + expires;
        return privateDownloadUrlWithDeadline(baseUrl, deadline);
    }

    public String privateDownloadUrlWithDeadline(String baseUrl, long deadline) {
        StringBuilder b = new StringBuilder();
        b.append(baseUrl);
        int pos = baseUrl.indexOf("?");
        if (pos > 0) {
            b.append("&e=");
        } else {
            b.append("?e=");
        }
        b.append(deadline);
        String token = sign(StringUtils.utf8Bytes(b.toString()));
        b.append("&token=");
        b.append(token);
        return b.toString();
    }

    /**
     * scope = bucket
     * 一般情况下可通过此方法获取token
     *
     * @param bucket 空间名
     * @return 生成的上传token
     */
    public String uploadToken(String bucket) {
        return uploadToken(bucket, null, 3600, null, true);
    }

    /**
     * scope = bucket:key
     * 同名文件覆盖操作、只能上传指定key的文件可以可通过此方法获取token
     *
     * @param bucket 空间名
     * @param key    key，可为 null
     * @return 生成的上传token
     */
    public String uploadToken(String bucket, String key) {
        return uploadToken(bucket, key, 3600, null, true);
    }

    /**
     * 生成上传token
     *
     * @param bucket  空间名
     * @param key     key，可为 null
     * @param expires 有效时长，单位秒
     * @param policy  上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。
     *                scope通过 bucket、key间接设置，deadline 通过 expires 间接设置
     * @return 生成的上传token
     */
    public String uploadToken(String bucket, String key, long expires, StringMap policy) {
        return uploadToken(bucket, key, expires, policy, true);
    }

    /**
     * 生成上传token
     *
     * @param bucket  空间名
     * @param key     key，可为 null
     * @param expires 有效时长，单位秒。默认3600s
     * @param policy  上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。
     *                scope通过 bucket、key间接设置，deadline 通过 expires 间接设置
     * @param strict  是否去除非限定的策略字段，默认true
     * @return 生成的上传token
     */
    public String uploadToken(String bucket, String key, long expires, StringMap policy, boolean strict) {
        long deadline = System.currentTimeMillis() / 1000 + expires;
        return uploadTokenWithDeadline(bucket, key, deadline, policy, strict);
    }

    public String uploadTokenWithDeadline(String bucket, String key, long deadline, StringMap policy, boolean strict) {
        // TODO   UpHosts Global
        String scope = bucket;
        if (key != null) {
            scope = bucket + ":" + key;
        }
        StringMap x = new StringMap();
        copyPolicy(x, policy, strict);
        x.put("scope", scope);
        x.put("deadline", deadline);

        String s = Json.encode(x);
        return signWithData(StringUtils.utf8Bytes(s));
    }

    public String uploadTokenWithPolicy(Object obj) {
        String s = Json.encode(obj);
        return signWithData(StringUtils.utf8Bytes(s));
    }

    @Deprecated
    public StringMap authorization(String url, byte[] body, String contentType) {
        String authorization = "QBox " + signRequest(url, body, contentType);
        return new StringMap().put("Authorization", authorization);
    }

    @Deprecated
    public StringMap authorization(String url) {
        return authorization(url, null, null);
    }

    /**
     * 生成 HTTP 请求签名字符串
     *
     * @param url         签名请求的 url
     * @param method      签名请求的 method
     * @param body        签名请求的 body
     * @param contentType 签名请求的 contentType
     * @return 签名
     */
    @Deprecated
    public String signRequestV2(String url, String method, byte[] body, String contentType) {
        return signQiniuAuthorization(url, method, body, contentType);
    }

    public String signQiniuAuthorization(String url, String method, byte[] body, String contentType) {
        Headers headers = null;
        if (!StringUtils.isNullOrEmpty(contentType)) {
            headers = new Headers.Builder().set(HTTP_HEADER_KEY_CONTENT_TYPE, contentType).build();
        }
        return signQiniuAuthorization(url, method, body, headers);
    }

    public String signQiniuAuthorization(String url, String method, byte[] body, Headers headers) {
        URI uri = URI.create(url);
        if (StringUtils.isNullOrEmpty(method)) {
            method = "GET";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(uri.getPath());

        if (uri.getQuery() != null) {
            sb.append("?").append(uri.getRawQuery());
        }

        sb.append("\nHost: ").append(uri.getHost() != null ? uri.getHost() : "");

        if (uri.getPort() > 0) {
            sb.append(":").append(uri.getPort());
        }

        String contentType = null;

        if (null != headers) {
            contentType = headers.get(HTTP_HEADER_KEY_CONTENT_TYPE);
            if (contentType != null) {
                sb.append("\n").append(HTTP_HEADER_KEY_CONTENT_TYPE).append(": ").append(contentType);
            }

            List<Header> xQiniuheaders = genXQiniuSignHeader(headers);
            java.util.Collections.sort(xQiniuheaders);
            if (xQiniuheaders.size() > 0) {
                for (Header h : xQiniuheaders) {
                    sb.append("\n").append(h.name).append(": ").append(h.value);
                }
            }
        }

        sb.append("\n\n");

        if (body != null && body.length > 0 && null != contentType && !"".equals(contentType)
                && !"application/octet-stream".equals(contentType)) {
            sb.append(new String(body, Constants.UTF_8));
        }
        Mac mac = createMac();
        mac.update(StringUtils.utf8Bytes(sb.toString()));

        String digest = UrlSafeBase64.encodeToString(mac.doFinal());
        return this.accessKey + ":" + digest;
    }

    private List<Header> genXQiniuSignHeader(Headers headers) {

        ArrayList<Header> hs = new ArrayList<Header>();
        for (String name : headers.names()) {
            if (name.length() > "X-Qiniu-".length() && name.startsWith("X-Qiniu-")) {
                String value = headers.get(name);
                if (value != null) {
                    hs.add(new Header(canonicalMIMEHeaderKey(name), value));
                }
            }
        }
        return hs;
    }

    public Headers qiniuAuthorization(String url, String method, byte[] body, Headers headers) {
        Headers.Builder builder = null;
        if (headers == null) {
            builder = new Headers.Builder();
        } else {
            builder = headers.newBuilder();
        }

        setDefaultHeader(builder);

        String authorization = "Qiniu " + signQiniuAuthorization(url, method, body, builder.build());
        builder.set("Authorization", authorization).build();
        return builder.build();
    }

    @Deprecated
    public StringMap authorizationV2(String url, String method, byte[] body, String contentType) {
        Headers headers = null;
        if (!StringUtils.isNullOrEmpty(contentType)) {
            headers = new Headers.Builder().set(HTTP_HEADER_KEY_CONTENT_TYPE, contentType).build();
        }

        headers = qiniuAuthorization(url, method, body, headers);
        if (headers == null) {
            return null;
        }

        Set<String> headerKeys = headers.names();
        StringMap headerMap = new StringMap();
        for (String key : headerKeys) {
            String value = headers.get(key);
            if (value != null) {
                headerMap.put(key, value);
            }
        }
        return headerMap;
    }

    @Deprecated
    public StringMap authorizationV2(String url) {
        return authorizationV2(url, "GET", null, (String) null);
    }

    //连麦 RoomToken
    public String signRoomToken(String roomAccess) throws Exception {
        String encodedRoomAcc = UrlSafeBase64.encodeToString(roomAccess);
        byte[] sign = createMac().doFinal(encodedRoomAcc.getBytes());
        String encodedSign = UrlSafeBase64.encodeToString(sign);
        return this.accessKey + ":" + encodedSign + ":" + encodedRoomAcc;
    }

    public String generateLinkingDeviceToken(String appid, String deviceName, long deadline, String[] actions) {
        LinkingDtokenStatement[] staments = new LinkingDtokenStatement[actions.length];

        for (int i = 0; i < actions.length; ++i) {
            staments[i] = new LinkingDtokenStatement(actions[i]);
        }

        SecureRandom random = new SecureRandom();
        StringMap map = new StringMap();
        map.put("appid", appid).put("device", deviceName).put("deadline", deadline)
                .put("random", random.nextInt()).put("statement", staments);
        String s = Json.encode(map);
        return signWithData(StringUtils.utf8Bytes(s));
    }

    public String generateLinkingDeviceTokenWithExpires(String appid, String deviceName,
                                                        long expires, String[] actions) {
        long deadline = (System.currentTimeMillis() / 1000) + expires;
        return generateLinkingDeviceToken(appid, deviceName, deadline, actions);
    }

    public String generateLinkingDeviceVodTokenWithExpires(String appid, String deviceName, long expires) {
        return generateLinkingDeviceTokenWithExpires(appid, deviceName, expires, new String[]{DTOKEN_ACTION_VOD});
    }

    public String generateLinkingDeviceStatusTokenWithExpires(String appid, String deviceName, long expires) {
        return generateLinkingDeviceTokenWithExpires(appid, deviceName, expires, new String[]{DTOKEN_ACTION_STATUS});
    }


    private static void setDefaultHeader(Headers.Builder builder) {
        if (!isDisableQiniuTimestampSignature()) {
            builder.set("X-Qiniu-Date", xQiniuDate());
        }
    }

    private static boolean isDisableQiniuTimestampSignature() {
        String value = System.getenv(DISABLE_QINIU_TIMESTAMP_SIGNATURE_ENV_KEY);
        if (value == null) {
            return false;
        }
        value = value.toLowerCase();
        return value.equals("true") || value.equals("yes") || value.equals("y") || value.equals("1");
    }

    private static String xQiniuDate() {
        DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    class LinkingDtokenStatement {
        @SerializedName("action")
        private String action;

        LinkingDtokenStatement(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }

    private class Header implements Comparable<Header> {
        String name;
        String value;

        Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public int compareTo(Header o) {
            int c = this.name.compareTo(o.name);
            if (c == 0) {
                return this.value.compareTo(o.value);
            } else {
                return c;
            }
        }
    }

    public static class Request {
        private final String url;
        private final String method;
        private final Headers header;
        private final byte[] body;

        /**
         * @param url    回调地址
         * @param method 回调请求方式
         * @param header 回调头，注意 Content-Type Key 字段需为：{@link Auth#HTTP_HEADER_KEY_CONTENT_TYPE}，大小写敏感
         * @param body   回调请求体。原始请求体，不要解析后再封装成新的请求体--可能导致签名不一致。
         **/
        public Request(String url, String method, Headers header, byte[] body) {
            this.url = url;
            this.method = method;
            this.header = header;
            this.body = body;
        }
    }
}

package com.qiniu.cdn;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;

import java.util.HashMap;

/**
 * Created by bailong on 16/9/21.
 */
public final class CdnManager {
    private final Auth auth;
    private final String server;
    private final Client client;

    public CdnManager(Auth auth) {
        this(auth, "http://fusion.qiniuapi.com");
    }

    private CdnManager(Auth auth, String server) {
        this.auth = auth;
        this.server = server;
        this.client = new Client(null, false, null,
                Constants.CONNECT_TIMEOUT, Constants.RESPONSE_TIMEOUT, Constants.WRITE_TIMEOUT);
    }

    public Response refreshUrls(String[] urls) throws QiniuException {
        return refreshUrlsAndDirs(urls, null);
    }

    public Response refreshDirs(String[] dirs) throws QiniuException {
        return refreshUrlsAndDirs(null, dirs);
    }

    public Response refreshUrlsAndDirs(String[] urls, String[] dirs) throws QiniuException {
        HashMap<String, String[]> req = new HashMap<>();
        if (urls != null) {
            req.put("urls", urls);
        }
        if (dirs != null) {
            req.put("dirs", dirs);
        }
        byte[] body = Json.encode(req).getBytes(Constants.UTF_8);
        String url = server + "/v2/tune/refresh";
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }
}

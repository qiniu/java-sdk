package com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlUtils;

public class StatsManager {
    private final String apiServer;
    private final Client client;
    private final Auth auth;

    public StatsManager(Auth auth) {
        this(auth, "http://qvs.qiniuapi.com");
    }

    public StatsManager(Auth auth, String apiServer) {
        this(auth, apiServer, new Client());
    }

    public StatsManager(Auth auth, String apiServer, Client client) {
        this.auth = auth;
        this.apiServer = apiServer;
        this.client = client;
    }

    public Response queryFlow(String namespaceId, String streamId, String tu, int start, int end) throws QiniuException {
        String url = String.format("%s/v1/stats/flow", apiServer);
        StringMap map = new StringMap().put("nsId", namespaceId).putNotNull("streamId", streamId).put("start", start).put("end", end).put("tu", tu);
        url = UrlUtils.composeUrlWithQueries(url, map);
        return QvsResponse.get(url, client, auth);
    }

    public Response queryBandwidth(String namespaceId, String streamId, String tu, int start, int end) throws QiniuException {
        String url = String.format("%s/v1/stats/bandwidth", apiServer);
        StringMap map = new StringMap().put("nsId", namespaceId).putNotNull("streamId", streamId).put("start", start).put("end", end).put("tu", tu);
        url = UrlUtils.composeUrlWithQueries(url, map);
        return QvsResponse.get(url, client, auth);
    }
}

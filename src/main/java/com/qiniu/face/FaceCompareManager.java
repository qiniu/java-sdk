package com.qiniu.face;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

/**
 * Created by jemy on 2018/6/6.
 */
public class FaceCompareManager {
    private final Auth auth;
    private final String host;
    private final Client client;

    public FaceCompareManager(Auth auth) {
        this(auth, "http://argus.atlab.ai");
    }

    FaceCompareManager(Auth auth, String host) {
        this.auth = auth;
        this.host = host;
        client = new Client();
    }

    /**
     *
     * @param id face_id
     * @param data you can open url: https://developer.qiniu.com/dora/manual/4438/face-recognition
     * @return
     * @throws QiniuException
     */
    public Response createFaceDB(String id, String data) throws QiniuException {
        String url = String.format("%s%s%s%s", host, "/v1/face/group/", id, "/new");
        byte[] body = data.getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    public Response deleteFaceDB(String id) throws QiniuException {
        String url = String.format("%s%s%s%s", host, "/v1/face/group/", id, "/remove");
        StringMap headers = auth.authorizationV2(url, "POST", null, null);
        return client.post(url, null, headers, Client.JsonMime);
    }

    public Response listFaceDB() throws QiniuException {
        String url = String.format("%s%s", host, "/v1/face/group");
        StringMap headers = auth.authorizationV2(url);
        return client.get(url, headers);
    }

    public Response createFace(String id, String data) throws QiniuException {
        String url = String.format("%s%s%s%s", host, "/v1/face/group/", id, "/add");
        byte[] body = data.getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    public Response getFace(String id) throws QiniuException {
        String url = String.format("%s%s%s", host, "/v1/face/group/", id);
        StringMap headers = auth.authorizationV2(url);
        return client.get(url, headers);
    }

    public Response deleteFace(String id, String data) throws QiniuException {
        String url = String.format("%s%s%s%s", host, "/v1/face/group/", id, "/delete");
        byte[] body = data.getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }

    public Response compareFace(String id, String data) throws QiniuException {
        String url = String.format("%s%s%s%s", host, "/v1/face/group/", id, "/search");
        byte[] body = data.getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "POST", body, Client.JsonMime);
        return client.post(url, body, headers, Client.JsonMime);
    }
}

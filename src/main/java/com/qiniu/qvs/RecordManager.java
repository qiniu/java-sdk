package com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

public class RecordManager {
    private final String apiServer;
    private final Client client;
    private final Auth auth;

    public RecordManager(Auth auth) {
        this(auth, "http://qvs.qiniuapi.com");
    }

    public RecordManager(Auth auth, String apiServer) {
        this(auth, apiServer, new Client());
    }

    public RecordManager(Auth auth, String apiServer, Client client) {
        this.auth = auth;
        this.apiServer = apiServer;
        this.client = client;
    }

    /*
     * 启动按需录制
     */
    public Response startRecord(String namespaceId, String streamId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/record/start", apiServer, namespaceId, streamId);
        return QvsResponse.post(url, new StringMap(), client, auth);
    }

    /*
     * 停止按需录制
     */
    public Response stopRecord(String namespaceId, String streamId) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/record/stop", apiServer, namespaceId, streamId);
        return QvsResponse.post(url, new StringMap(), client, auth);
    }

    /*
     * 删除录制片段
     */
    public Response deleteStreamRecordHistories(String namespaceId, String streamId, String[] files) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/recordhistories", apiServer, namespaceId, streamId);
        StringMap params = new StringMap();
        params.putNotNull("files", files);
        return QvsResponse.delete(url, params, client, auth);
    }

    /*
     * 录制视频片段合并
     * 参数：
     *      fname 保存的文件名(需要带上文件格式后缀),不指定系统会随机生成
     *      format 保存的文件格式,可以为m3u8, mp4或者flv
     *      start 查询开始时间(unix时间戳,单位为秒)
     *      end 查询结束时间(unix时间戳,单位为秒)
     *      deleteTs 在不生成m3u8格式文件时是否删除对应的ts文件
     *      pipeline 数据处理的私有队列，不指定则使用公共队列
     *      notifyUrl 保存成功回调通知地址，不指定则不通知
     *      deleteAfterDays 文件过期时间,默认和录制模版中的设置保持一致
     */
    public Response recordClipsSaveas(String namespaceId, String streamId, String fname, String format, int start, int end, boolean deleteTs, String pipeline, String notifyUrl, int deleteAfterDays) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/saveas", apiServer, namespaceId, streamId);
        StringMap params = new StringMap();
        params.putNotNull("fname", fname);
        params.putNotNull("format", format);
        params.putNotNull("start", start);
        params.putNotNull("end", end);
        params.putNotNull("deleteTs", deleteTs);
        params.putNotNull("pipeline", pipeline);
        params.putNotNull("notifyUrl", notifyUrl);
        params.putNotNull("deleteAfterDays", deleteAfterDays);

        return QvsResponse.post(url, params, client, auth);
    }

    /*
     * 录制回放
     */
    public Response recordsPlayback(String namespaceId, String streamId, int start, int end) throws QiniuException {
        String url = String.format("%s/v1/namespaces/%s/streams/%s/records/playback.m3u8?start=%d&end=%d", apiServer, namespaceId, streamId, start, end);
        return QvsResponse.get(url, client, auth);
    }
}

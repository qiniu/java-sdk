package com.qiniu.storage;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FixBlockUploader {
    private final int blockSize;
    private final ConfigHelper configHelper;
    private final Client client;
    private final Recorder recorder;

    private final int retryMax;

    private String host = null;

    /**
     * @param blockSize must be multiples of 4M.
     * @param configuration Nullable, if null, then create a new one.
     * @param client    Nullable, if null, then create a new one with configuration.
     * @param recorder  Nullable.
     */
    public FixBlockUploader(int blockSize, Configuration configuration, Client client, Recorder recorder) {
        assert blockSize > 0 && blockSize % (4 * 1024 * 1024) == 0 : "blockSize must be multiples of 4M ";

        if (configuration == null) {
            configuration = new Configuration();
        }
        if (client == null) {
            client = new Client(configuration);
        }
        this.configHelper = new ConfigHelper(configuration);
        this.client = client;
        this.blockSize = blockSize;
        this.recorder = recorder;
        this.retryMax = configuration.retryMax;
    }


    public Response upload(final File file, final String token, String key) throws QiniuException {
        return upload(file, token, key, null);
    }


    public Response upload(final File file, final String token, String key, StringMap metaParams)
            throws QiniuException {
        BlockData blockData;
        try {
            blockData = new FileBlockData(this.blockSize, file);
        } catch (IOException e) {
            throw new QiniuException(e);
        }
        try {
            return upload(blockData, new StaticToken(token), key, metaParams);
        } finally {
            blockData.close();
        }
    }


    public Response upload(final InputStream is, long inputStreamLength, final String token, String key)
            throws QiniuException {
        return upload(is, inputStreamLength, token, key, null);
    }

    public Response upload(final InputStream is, long inputStreamLength, final String token,
                           String key, StringMap metaParams) throws QiniuException {
        BlockData blockData;
        blockData = new InputStreamBlockData(this.blockSize, is, inputStreamLength);
        try {
            return upload(blockData, new StaticToken(token), key, metaParams);
        } finally {
            blockData.close();
        }
    }


    public Response upload(BlockData blockData, String token, String key, StringMap metaParams) throws QiniuException {
        return upload(blockData, new StaticToken(token), key, metaParams);
    }


    public Response upload(BlockData blockData, Token token, String key, StringMap metaParams) throws QiniuException {
        assert !StringUtils.isNullOrEmpty(key) : "key must not be null or empty";
        if (key == null) { // assert may not be enabled. we do not like null pointer exception//
            key = "";
        }
        String bucket = parseBucket(token.getUpToken());
        String base64Key = UrlSafeBase64.encodeToString(key);
        if (host == null) {
            host = configHelper.upHost(token.getUpToken());
        }
        if (metaParams == null) {
            metaParams = new StringMap();
        }
        RetryCounter counter = new RetryCounter(this.retryMax);

        String uploadId = null;
        List<EtagIdx> etags = null;
        Record record = null;

        UploadRecordHelper helper = new UploadRecordHelper(recorder, bucket, base64Key,
                blockData.getContentUUID(), this.blockSize + "*:|>?^ \b" + this.getClass().getName());

        if (blockData.isRetryable()) {
            record = helper.reloadRecord();
            if (helper.isActiveRecord(record, blockData)) {
                // 有效的 record 才拿来用 //
                try {
                    blockData.skipByte(record.size); // may throw exception
                    blockData.skipBlock(record.etagIdxes.size());
                    etags = record.etagIdxes;
                    uploadId = record.uploadId;
                } catch (IOException e) {
                    // need reset blockData ? how ?
                    // mark(int readlimit) readlimit is not big enough, it's useless for large entity.
                    // fileinputstream does not support mark and reset.
                    // be simple, only delete record file, then throw an exception. the invoker maybe need to retry.
                    helper.delRecord();
                    throw new QiniuException(e, "blockData skip failed. "
                            + "record file is already deleted, please retry if needed.");
                }
            }
        }

        if (uploadId == null) {
            uploadId = init(bucket, base64Key, token.getUpToken());
            etags = new ArrayList<EtagIdx>();
            record = initRecord(uploadId, etags);
        }

        while (blockData.hasNext()) {
            try {
                blockData.nextBlock();
            } catch (IOException e) {
                throw new QiniuException(e);
            }
            byte[] data = blockData.getCurrentBlockData();
            // usually, size equals data.length, except the last block of blockdata
            int size = blockData.getCurrentRead();
            int index = blockData.getCurrentIndex();

            String etag = uploadBlock(bucket, base64Key, token, uploadId, data, size, index, counter);
            etags.add(new EtagIdx(etag, index));
            // 对应的 etag、index 通过 etags 添加 //
            record.size += size;

            helper.syncRecord(record);
        }

        Response res = makeFile(bucket, base64Key, token, uploadId, etags, metaParams);

        if (res.isOK()) {
            helper.delRecord();
        }

        return res;
    }


    String init(String bucket, String base64Key, String upToken) throws QiniuException {
        String url = host + "/buckets/" + bucket + "/objects/" + base64Key + "/uploads";
        byte[] data = new byte[0];
        StringMap headers = new StringMap().put("Authorization", "UpToken " + upToken);
        String contentType = "";

        Response res = null;
        try {
            // 1
            res = client.post(url, data, headers, contentType);
        } catch (QiniuException e) {
            if (res == null && e.response != null) {
                res = e.response;
            }
        } catch (Exception e) {
            // ignore, retry
        }

        // 重试一次，初始不计入重试次数 //
        if (res == null || res.needRetry()) {
            if (res == null || res.needSwitchServer()) {
                changeHost(upToken, host);
            }
            try {
                // 2
                res = client.post(url, data, headers, contentType);
            } catch (QiniuException e) {
                if (res == null && e.response != null) {
                    res = e.response;
                }
            } catch (Exception e) {
                // ignore, retry
            }

            if (res == null || res.needRetry()) {
                if (res == null || res.needSwitchServer()) {
                    changeHost(upToken, host);
                }
                // 3
                res = client.post(url, data, headers, contentType);
            }
        }

        try {
            String uploadId = res.jsonToMap().get("uploadId").toString();
            if (uploadId.length() > 10) {
                return uploadId;
            }
        } catch (Exception e) {
            // ignore, see next line
        }

        throw new QiniuException(res);
    }


    String uploadBlock(String bucket, String base64Key, Token token, String uploadId, byte[] data,
                       int dataLength, int partNum, RetryCounter counter) throws QiniuException {
        Response res = uploadBlockWithRetry(bucket, base64Key, token, uploadId, data, dataLength, partNum, counter);
        try {
            String etag = res.jsonToMap().get("etag").toString();
            if (etag.length() > 10) {
                return etag;
            }
        } catch (Exception e) {
            // ignore, see next line
        }
        throw new QiniuException(res);
    }


    Response uploadBlockWithRetry(String bucket, String base64Key, Token token, String uploadId,
                                  byte[] data, int dataLength, int partNum, RetryCounter counter)
            throws QiniuException {
        String url = host + "/buckets/" + bucket + "/objects/" + base64Key + "/uploads/" + uploadId + "/" + partNum;
        StringMap headers = new StringMap().
                put("Content-MD5", Md5.md5(data, 0, dataLength)).
                put("Authorization", "UpToken " + token.getUpToken());

        // 在 最多重试次数 范围内， 每个块至多上传 3 次 //
        // 1
        Response res = uploadBlock1(url, data, dataLength, headers, true);
        if (res.isOK()) {
            return res;
        }

        if (res.needSwitchServer()) {
            changeHost(token.getUpToken(), host);
        }

        if (!counter.inRange()) {
            return res;
        }

        if (res.needRetry()) {
            counter.retried();
            // 2
            res = uploadBlock1(url, data, dataLength, headers, true);

            if (res.isOK()) {
                return res;
            }

            if (res.needSwitchServer()) {
                changeHost(token.getUpToken(), host);
            }

            if (!counter.inRange()) {
                return res;
            }

            if (res.needRetry()) {
                counter.retried();
                // 3
                res = uploadBlock1(url, data, dataLength, headers, false);
            }
        }

        return res;
    }


    Response uploadBlock1(String url, byte[] data,
                          int dataLength,  StringMap headers, boolean ignoreError) throws QiniuException {
        // put PUT
        try {
            Response res = client.put(url, data, 0, dataLength, headers, "application/octet-stream");
            return res;
        } catch (QiniuException e) {
            if (ignoreError) {
                if (e.response != null) {
                    return e.response;
                }
                return Response.createError(null, null, -1, e.getMessage());
            } else {
                throw e;
            }
        }
    }

    Response makeFile(String bucket, String base64Key, Token token, String uploadId, List<EtagIdx> etags,
                       StringMap metaParams) throws QiniuException {
        String url = host + "/buckets/" + bucket + "/objects/" + base64Key + "/uploads/" + uploadId;
        byte[] data = new EtagIdxPart(etags).toString().getBytes(Charset.forName("UTF-8"));
        final StringMap headers = new StringMap().put("Authorization", "UpToken " + token.getUpToken());

        metaParams.forEach(new StringMap.Consumer() {
            @Override
            public void accept(String key, Object value) {
                if (key != null && key.startsWith("X-Qn-Meta-")) {
                    headers.put(key, value);
                }
            }
        });

        // 1
        Response res = makeFile1(url, data, headers, true);
        if (res.needRetry()) {
            // 2
            res = makeFile1(url, data, headers, true);
        }
        if (res.needRetry()) {
            if (res.needSwitchServer()) {
                changeHost(token.getUpToken(), host);
            }
            // 3
            res = makeFile1(url, data, headers, false);
        }
        // keep the same, with com.qiniu.http.Client#L337
        if (res.statusCode >= 300) {
            throw new QiniuException(res);
        }
        return res;
    }


    Response makeFile1(String url, byte[] data, StringMap headers, boolean ignoreError) throws QiniuException {
        try {
            Response res = client.post(url, data, headers, "text/plain");
            return res;
        } catch (QiniuException e) {
            if (ignoreError) {
                if (e.response != null) {
                    return e.response;
                }
                return Response.createError(null, null, -1, e.getMessage());
            } else {
                throw e;
            }
        }
    }


    private void changeHost(String upToken, String host) {
        try {
            this.host = configHelper.tryChangeUpHost(upToken, host);
        } catch (Exception e) {
            // ignore
            // use the old up host //
        }
    }


    static String parseBucket(String upToken) throws QiniuException {
        try {
            String part3 = upToken.split(":")[2];
            byte[] b = UrlSafeBase64.decode(part3);
            StringMap m = Json.decode(new String(b, Charset.forName("UTF-8")));
            String scope = m.get("scope").toString();
            return scope.split(":")[0];
        } catch (Exception e) {
            throw new QiniuException(e, "invalid uptoken : " + upToken);
        }
    }


    class EtagIdxPart {
        List<EtagIdx> parts;

        EtagIdxPart(List<EtagIdx> parts) {
            this.parts = parts;
        }

        public String toString() {
            return new Gson().toJson(this);
        }
    }


    class EtagIdx {
        @SerializedName("Etag")
        String etag;
        @SerializedName("PartNumber")
        int idx;
        EtagIdx(String etag, int idx) {
            this.etag = etag;
            this.idx = idx;
        }

        public String toString() {
            return new Gson().toJson(this);
        }

    }


    ///////////////////////////////////////


    class Record {
        long createdTime;
        String uploadId;
        long size;
        List<EtagIdx> etagIdxes;
    }


    Record initRecord(String uploadId, List<EtagIdx> etagIdxes) {
        Record record = new Record();
        record.createdTime = System.currentTimeMillis();
        record.uploadId = uploadId;
        record.size = 0;
        record.etagIdxes = etagIdxes != null ? etagIdxes : new ArrayList<EtagIdx>();

        return record;
    }


    class UploadRecordHelper {
        Recorder recorder;
        String recordFileKey;

        public UploadRecordHelper(Recorder recorder, String bucket, String base64Key,
                                  String contentUUID, String uploaderSUID) {
            if (recorder != null) {
                this.recorder = recorder;
                recordFileKey = recorder.recorderKeyGenerate(bucket, base64Key, contentUUID, uploaderSUID);
            }
        }

        public Record reloadRecord() {
            Record record = null;
            if (recorder != null) {
                try {
                    byte[] data = recorder.get(recordFileKey);
                    record = new Gson().fromJson(new String(data, Charset.forName("UTF-8")), Record.class);
                } catch (Exception e) {
                    // do nothing
                }
            }
            if (record == null) {
                record = new Record();
            }
            return record;
        }

        public void delRecord() {
            if (recorder != null) {
                recorder.del(recordFileKey);
            }
        }


        public void syncRecord(Record record) {
            if (recorder != null) {
                recorder.set(recordFileKey, new Gson().toJson(record).getBytes(Charset.forName("UTF-8")));
            }
        }

        public boolean isActiveRecord(Record record, BlockData blockData) {
            //// 服务端 7 天内有效，设置 5 天 ////
            boolean isOk = record.createdTime > System.currentTimeMillis() - 1000 * 3600 * 24 * 5
                    && !StringUtils.isNullOrEmpty(record.uploadId)
                    && record.etagIdxes != null && record.etagIdxes.size() > 0
                    && record.size > 0 && record.size <= blockData.size();
            if (isOk) {
                int p = 0;
                // PartNumber start with 1 and increase by 1 //
                // 当前文件各块串行. 若并行，需额外考虑 //
                for (EtagIdx ei : record.etagIdxes) {
                    if (ei.idx == p + 1) {
                        p = ei.idx;
                    } else {
                        return false;
                    }
                }
            }

            return isOk;
        }
    }


    ///////////////////////////////////////


    public abstract static class BlockData {
        public final int blockDataSize;

        BlockData(int blockDataSize) {
            this.blockDataSize = blockDataSize;
        }

        public abstract int getCurrentIndex();

        public abstract byte[] getCurrentBlockData();

        public abstract int getCurrentRead();

        public abstract boolean hasNext();

        public abstract void nextBlock() throws IOException;

        public abstract void skipByte(long n) throws IOException;
        public abstract void skipBlock(int blockCount);

        public abstract void close();

        public abstract long size();

        public abstract boolean isRetryable();

        public abstract String getContentUUID();
    }


    public interface Token {
        String getUpToken();
    }

    class RetryCounter {
        int count;

        RetryCounter(int max) {
            this.count = max;
        }

        public void retried() {
            this.count--;
        }

        public boolean inRange() {
            return this.count > 0;
        }
    }


    public static class FileBlockData extends BlockData {
        final long totalLength;
        String contentUUID;

        FileInputStream fis;
        byte[] data;
        int readLength = -1;
        int index = 0; // start at 1, read a block , add 1

        long alreadyReadSize = 0;

        public FileBlockData(int blockDataSize, File file) throws FileNotFoundException {
            super(blockDataSize);
            fis = new FileInputStream(file);
            totalLength = file.length();
            data = new byte[blockDataSize];
            contentUUID = file.lastModified() + "_.-^ \b" + file.getAbsolutePath();
        }

        @Override
        public long size() {
            return totalLength;
        }

        @Override
        public byte[] getCurrentBlockData() {
            return data;
        }

        @Override
        public int getCurrentRead() {
            return readLength;
        }

        @Override
        public int getCurrentIndex() {
            return index;
        }


        @Override
        public boolean hasNext() {
            return alreadyReadSize < totalLength;
        }

        @Override
        public void nextBlock() throws IOException {
            readLength = fis.read(data);
            alreadyReadSize += readLength;
            index++;
        }

        @Override
        public void skipByte(long n) throws IOException {
            fis.skip(n);
            alreadyReadSize += n;
        }

        @Override
        public void skipBlock(int blockCount) {
            this.index += blockCount;
        }

        @Override
        public boolean isRetryable() {
            return true;
        }

        @Override
        public String getContentUUID() {
            return contentUUID;
        }


        @Override
        public void close() {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static class InputStreamBlockData extends BlockData {
        final long totalLength;
        final boolean closedAfterUpload;

        boolean retryable;
        String contentUUID;

        InputStream is;
        byte[] data;
        int readLength = -1;
        int index = 0; // start at 1, read a block , add 1

        long alreadyReadSize = 0;

        public InputStreamBlockData(int blockDataSize, InputStream is, long totalLength) {
            this(blockDataSize, is, totalLength, true);
        }

        public InputStreamBlockData(int blockDataSize, InputStream is, long totalLength, boolean closedAfterUpload) {
            this(blockDataSize, is, totalLength, closedAfterUpload, false, "");
        }

        public InputStreamBlockData(int blockDataSize, InputStream is, long totalLength, boolean closedAfterUpload,
                                    boolean retryable, String contentUUID) {
            super(blockDataSize);
            this.is = is;
            this.totalLength = totalLength;
            this.closedAfterUpload = closedAfterUpload;
            this.data = new byte[blockDataSize];
            this.retryable = retryable;
            this.contentUUID = contentUUID;
        }

        @Override
        public long size() {
            return totalLength;
        }

        @Override
        public byte[] getCurrentBlockData() {
            return data;
        }

        @Override
        public int getCurrentRead() {
            return readLength;
        }

        @Override
        public int getCurrentIndex() {
            return index;
        }


        @Override
        public boolean hasNext() {
            return alreadyReadSize < totalLength;
        }

        @Override
        public void nextBlock() throws IOException {
            readLength = 0;
            int rl = is.read(data);
            int rlt = rl;
            // no enough data //
            while (rlt < blockDataSize) {
                // eof
                if (rl == -1) {
                    break;
                }
                sleep(100);
                rl = is.read(data, rlt, blockDataSize - rlt);
                if (rl > 0) {
                    rlt += rl;
                }
            }

            if (rlt != -1) {
                readLength = rlt;
                alreadyReadSize += readLength;
                index++;
            }
        }


        @Override
        public void skipByte(final long n) throws IOException {
            long sn = is.skip(n);
            long snt = sn;
            while (snt < n) {
                if (sn == -1) {
                    throw new IOException("input stream does not have enough content: " + n);
                }
                sleep(100);
                sn = is.skip(n - snt);
                if (sn > 0) {
                    snt += sn;
                }
            }
            alreadyReadSize += n;
        }


        @Override
        public void skipBlock(int blockCount) {
            this.index += blockCount;
        }

        @Override
        public boolean isRetryable() {
            return retryable;
        }

        @Override
        public String getContentUUID() {
            return contentUUID;
        }

        @Override
        public void close() {
            if (closedAfterUpload) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    static void sleep(long millis) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }
    }


    public static class StaticToken implements Token {
        String token;

        public StaticToken(String token) {
            this.token = token;
        }

        @Override
        public String getUpToken() {
            return token;
        }
    }


}

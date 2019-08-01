package com.qiniu.storage;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        return upload(file, token, key, null, null, 0);
    }

    public Response upload(final File file, final String token, String key,
                           ExecutorService pool) throws QiniuException {
        return upload(file, token, key, null, pool, 8);
    }

    public Response upload(final File file, final String token, String key,
                           StringMap metaParams, ExecutorService pool, int maxRunningBlock) throws QiniuException {
        BlockData blockData;
        try {
            blockData = new FileBlockData(this.blockSize, file);
        } catch (IOException e) {
            throw new QiniuException(e);
        }
        return upload(blockData, new StaticToken(token), key, metaParams, pool, maxRunningBlock);
    }


    public Response upload(final InputStream is, long inputStreamLength, final String token, String key)
            throws QiniuException {
        return upload(is, inputStreamLength, token, key, null, null, 0);
    }


    public Response upload(final InputStream is, long inputStreamLength, final String token,
                           String key, ExecutorService pool) throws QiniuException {
        return upload(is, inputStreamLength, token, key, null, pool, 8);
    }


    public Response upload(final InputStream is, long inputStreamLength,
                           final String token, String key, StringMap metaParams,
                           ExecutorService pool, int maxRunningBlock) throws QiniuException {
        BlockData blockData;
        blockData = new InputStreamBlockData(this.blockSize, is, inputStreamLength);
        return upload(blockData, new StaticToken(token), key, metaParams, pool, maxRunningBlock);
    }


    Response upload(BlockData blockData, String token, String key,
                           StringMap metaParams, ExecutorService pool, int maxRunningBlock) throws QiniuException {
        return upload(blockData, new StaticToken(token), key, metaParams, pool, maxRunningBlock);
    }


    Response upload(BlockData blockData, Token token, String key, StringMap metaParams,
                           ExecutorService pool, int maxRunningBlock) throws QiniuException {
        try {
            assert !StringUtils.isNullOrEmpty(key) : "key must not be null or empty";
            if (key == null) { // assert may not be enabled. we do not like null pointer exception//
                key = "";
            }
            String bucket = parseBucket(token.getUpToken());
            String base64Key = UrlSafeBase64.encodeToString(key);
            String recordFileKey = (recorder == null) ? ""
                    : recorder.recorderKeyGenerate(bucket, base64Key, blockData.getContentUUID(),
                        this.blockSize + "*:|>?^ \b" + this.getClass().getName());
            // must before any http request //
            if (host == null) {
                host = configHelper.upHost(token.getUpToken());
            }
            UploadRecordHelper helper = new UploadRecordHelper(recorder, recordFileKey, blockData.repeatable());
            Record record = initRecorder(blockData, helper, bucket, base64Key, token);
            boolean repeatable = recorder != null &&  blockData.repeatable();

            Response res;
            try {
                upBlock(blockData, token, bucket, base64Key, repeatable, record, pool, maxRunningBlock);
                res = makeFile(bucket, base64Key, token, record.uploadId, record.etagIdxes, metaParams);
            } catch (QiniuException e) {
                // if everything is ok, do not need to sync record  //
                helper.syncRecord(record);
                throw e;
            }
            if (res.isOK()) {
                helper.delRecord();
            }
            return res;
        } finally {
            blockData.close();
        }
    }

    Record initRecorder(BlockData blockData, UploadRecordHelper helper,
                        String bucket, String base64Key, Token token) throws QiniuException {
        Record record = null;
        if (blockData.repeatable()) {
            record = helper.reloadRecord();
            // 有效的 record 才拿来用 //
            if (!helper.isActiveRecord(record, blockData)) {
                record = null;
            }
        }

        if (record == null || record.uploadId == null) {
            String uploadId = init(bucket, base64Key, token.getUpToken());
            List<EtagIdx> etagIdxes = new ArrayList<>();
            record = initRecord(uploadId, etagIdxes);
        }
        return record;
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


    private void upBlock(BlockData blockData, Token token, String bucket, String base64Key, boolean repeatable,
                         Record record, ExecutorService pool, int maxRunningBlock) throws QiniuException {
        boolean useAsync = useAsync(pool, blockData, record);

        if (!useAsync) {
            syncUpload(blockData, token, bucket, base64Key, record);
        } else {
            asyncUpload(blockData, token, bucket, base64Key, record, repeatable, pool, maxRunningBlock);
        }
    }

    private boolean useAsync(ExecutorService pool, BlockData blockData, Record record) {
        return pool != null && ((blockData.size() - record.size) > this.blockSize);
    }

    private void syncUpload(BlockData blockData, Token token, String bucket,
                            String base64Key, Record record) throws QiniuException {
        final String uploadId = record.uploadId;
        final List<EtagIdx> etagIdxes = record.etagIdxes;
        RetryCounter counter = new NormalRetryCounter(retryMax);
        while (blockData.hasNext()) {
            try {
                blockData.nextBlock();
            } catch (IOException e) {
                throw new QiniuException(e, e.getMessage());
            }
            DataWraper wrapper = blockData.getCurrentBlockData();
            if (alreadyDone(wrapper.getIndex(), etagIdxes)) {
                continue;
            }

            EtagIdx etagIdx;
            try {
                etagIdx = uploadBlock(bucket, base64Key, token, uploadId,
                        wrapper.getData(), wrapper.getSize(), wrapper.getIndex(), counter);
            } catch (IOException e) {
                throw new QiniuException(e, e.getMessage());
            }
            etagIdxes.add(etagIdx);
            // 对应的 etag、index 通过 etags 添加 //
            record.size += etagIdx.size;
        }
    }

    private void asyncUpload(BlockData blockData, final Token token,
                             final String bucket, final String base64Key, Record record,
                             boolean needRecord, ExecutorService pool, int maxRunningBlock) throws QiniuException {
        final String uploadId = record.uploadId;
        final List<EtagIdx> etagIdxes = record.etagIdxes;
        final RetryCounter counter = new AsyncRetryCounter(retryMax);
        List<Future<EtagIdx>> futures =
                new ArrayList<>((int) ((blockData.size() - record.size + blockSize - 1) / blockSize));
        QiniuException qiniuEx = null;
        while (blockData.hasNext()) {
            try {
                blockData.nextBlock();
            } catch (IOException e) {
                qiniuEx = new QiniuException(e, e.getMessage());
                break;
            }
            final DataWraper wrapper = blockData.getCurrentBlockData();
            if (alreadyDone(wrapper.getIndex(), etagIdxes)) {
                continue;
            }

            Callable<EtagIdx> runner = new Callable<EtagIdx>() {
                @Override
                public EtagIdx call() throws Exception {
                    return uploadBlock(bucket, base64Key, token, uploadId,
                            wrapper.getData(), wrapper.getSize(), wrapper.getIndex(), counter);
                }
            };

            waitingEnough(maxRunningBlock, futures);

            try {
                futures.add(pool.submit(runner));
            } catch (Exception e) {
                qiniuEx = new QiniuException(e, e.getMessage());
                break;
            }
        }
        for (Future<EtagIdx> future : futures) {
            if (!needRecord && qiniuEx != null) {
                future.cancel(true);
                continue;
            }
            EtagIdx etagIdx;
            try {
                etagIdx = future.get();
                etagIdxes.add(etagIdx);
                record.size += etagIdx.size;
            } catch (Exception e) {
                if (qiniuEx == null) {
                    qiniuEx = new QiniuException(e, e.getMessage());
                }
            }
        }
        if (qiniuEx != null) {
            throw qiniuEx;
        }
    }


    private boolean alreadyDone(int index, List<EtagIdx> etagIdxes) {
        for (EtagIdx etagIdx : etagIdxes) {
            if (etagIdx.idx == index) {
                return true;
            }
        }
        return false;
    }


    private void waitingEnough(int maxRunningBlock, List<Future<EtagIdx>> futures) {
        for ( ; ; ) {
            int count = futures.size();
            if (count < maxRunningBlock) {
                break;
            }
            int done = 0;
            for (Future<EtagIdx> future : futures) {
                if (future.isDone()) {
                    done++;
                }
            }
            if (count - done < maxRunningBlock) {
                break;
            }
            sleepMillis(500);
        }
    }


    EtagIdx uploadBlock(String bucket, String base64Key, Token token, String uploadId, byte[] data,
                       int dataLength, int partNum, RetryCounter counter) throws QiniuException {
        Response res = uploadBlockWithRetry(bucket, base64Key, token, uploadId, data, dataLength, partNum, counter);
        try {
            String etag = res.jsonToMap().get("etag").toString();
            if (etag.length() > 10) {
                return new EtagIdx(etag, partNum, dataLength);
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
        final StringMap headers = new StringMap().put("Authorization", "UpToken " + token.getUpToken());
        if (metaParams != null) {
            metaParams.forEach(new StringMap.Consumer() {
                @Override
                public void accept(String key, Object value) {
                    if (key != null && key.startsWith("X-Qn-Meta-")) {
                        headers.put(key, value);
                    }
                }
            });
        }
        Collections.sort(etags, new Comparator<EtagIdx>() {
            @Override
            public int compare(EtagIdx o1, EtagIdx o2) {
                return o1.idx - o2.idx; // small enough and both greater than 0 //
            }
        });
        byte[] data = new EtagIdxPart(etags).toString().getBytes(Charset.forName("UTF-8"));

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
        transient int size;
        EtagIdx(String etag, int idx, int size) {
            this.etag = etag;
            this.idx = idx;
            this.size = size;
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
        boolean needRecord;
        Recorder recorder;
        String recordFileKey;

        UploadRecordHelper(Recorder recorder, String recordFileKey, boolean needRecord) {
            this.needRecord = needRecord;
            if (recorder != null) {
                this.recorder = recorder;
                this.recordFileKey = recordFileKey;
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
            if (needRecord && recorder != null && record.etagIdxes.size() > 0) {
                Collections.sort(record.etagIdxes, new Comparator<EtagIdx>() {
                    @Override
                    public int compare(EtagIdx o1, EtagIdx o2) {
                        return o1.idx - o2.idx; // small enough and both greater than 0 //
                    }
                });
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
                // 当前文件各块串行 if (ei.idx == p + 1) . 若并行，需额外考虑 //
                for (EtagIdx ei : record.etagIdxes) {
                    if (ei.idx > p) {
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


    abstract static class BlockData {
        protected final int blockDataSize;

        BlockData(int blockDataSize) {
            this.blockDataSize = blockDataSize;
        }

        abstract DataWraper getCurrentBlockData();

        abstract boolean hasNext();

        abstract void nextBlock() throws IOException;

        abstract void close();

        abstract long size();

        abstract boolean repeatable();

        abstract String getContentUUID();
    }

    interface DataWraper {
        byte[] getData() throws IOException;
        int getSize();
        int getIndex();
    }

    interface Token {
        String getUpToken();
    }

    interface RetryCounter {
        void retried();
        boolean inRange();
    }

    class NormalRetryCounter implements RetryCounter {
        int count;

        NormalRetryCounter(int max) {
            this.count = max;
        }

        @Override
        public void retried() {
            this.count--;
        }

        @Override
        public boolean inRange() {
            return this.count > 0;
        }
    }

    class AsyncRetryCounter implements RetryCounter {
        volatile int count;

        AsyncRetryCounter(int max) {
            this.count = max;
        }

        @Override
        public synchronized void retried() {
            this.count--;
        }

        @Override
        public synchronized boolean inRange() {
            return this.count > 0;
        }
    }


    static class FileBlockData extends BlockData {
        final long totalLength;
        String contentUUID;
        DataWraper dataWraper;
        RandomAccessFile fis;
        int index = 0; // start at 1, read a block , add 1
        long alreadyReadSize = 0;
        Lock lock;

        FileBlockData(int blockDataSize, File file) throws IOException {
            super(blockDataSize);
            fis = new RandomAccessFile(file, "r");
            totalLength = file.length();
            contentUUID = file.lastModified() + "_.-^ \b" + file.getAbsolutePath();
            lock = new ReentrantLock();
        }

        @Override
        public long size() {
            return totalLength;
        }

        @Override
        public DataWraper getCurrentBlockData() {
            return dataWraper;
        }

        @Override
        public boolean hasNext() {
            return alreadyReadSize < totalLength;
        }

        @Override
        public void nextBlock() throws IOException {
            final long start = alreadyReadSize + 0;
            final int readLength = (int) Math.min(totalLength - alreadyReadSize, blockDataSize);
            alreadyReadSize += readLength;
            index++;
            final int idx = index + 0;
            dataWraper = new DataWraper() {
                public int getSize() {
                    return readLength;
                }

                public int getIndex() {
                    return idx;
                }

                @Override
                public byte[] getData() throws IOException {
                    byte[] data = new byte[blockDataSize];
                    lock.lock();
                    try {
                        fis.seek(start);
                        int size = fis.read(data);
                        assert readLength == size : "read size should equals "
                                + "(int)Math.min(totalLength - alreadyReadSize, blockDataSize): " + readLength;
                    } finally {
                        lock.unlock();
                    }
                    return data;
                }
            };
        }

        @Override
        public boolean repeatable() {
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


    static class InputStreamBlockData extends BlockData {
        final long totalLength;
        final boolean closedAfterUpload;

        boolean repeatable;
        String contentUUID;
        DataWraper dataWraper;
        InputStream is;
        int index = 0; // start at 1, read a block , add 1

        long alreadyReadSize = 0;

        InputStreamBlockData(int blockDataSize, InputStream is, long totalLength) {
            this(blockDataSize, is, totalLength, true);
        }

        InputStreamBlockData(int blockDataSize, InputStream is, long totalLength, boolean closedAfterUpload) {
            this(blockDataSize, is, totalLength, closedAfterUpload, false, "");
        }

        InputStreamBlockData(int blockDataSize, InputStream is, long totalLength, boolean closedAfterUpload,
                                    boolean repeatable, String contentUUID) {
            super(blockDataSize);
            this.is = is;
            this.totalLength = totalLength;
            this.closedAfterUpload = closedAfterUpload;
            this.repeatable = repeatable;
            this.contentUUID = contentUUID;
        }

        @Override
        public long size() {
            return totalLength;
        }

        @Override
        public DataWraper getCurrentBlockData() {
            return dataWraper;
        }

        @Override
        public boolean hasNext() {
            return alreadyReadSize < totalLength;
        }

        @Override
        public void nextBlock() throws IOException {
            final byte[] data = new byte[blockDataSize];
            int rl = is.read(data);
            int rlt = rl;
            // no enough data //
            while (rlt < blockDataSize) {
                // eof
                if (rl == -1) {
                    break;
                }
                sleepMillis(100);
                rl = is.read(data, rlt, blockDataSize - rlt);
                if (rl > 0) {
                    rlt += rl;
                }
            }

            if (rlt != -1) {
                alreadyReadSize += rlt;
                index++;
            }
            final int dataLen = rlt;
            final int idx = index;
            dataWraper = new DataWraper() {
                @Override
                public byte[] getData() {
                    return data;
                }

                @Override
                public int getSize() {
                    return dataLen;
                }

                public int getIndex() {
                    return idx;
                }
            };
        }

        @Override
        public boolean repeatable() {
            return repeatable;
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


    static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }


    static class StaticToken implements Token {
        String token;

        StaticToken(String token) {
            this.token = token;
        }

        @Override
        public String getUpToken() {
            return token;
        }
    }


}

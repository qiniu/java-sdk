package com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.*;
import com.qiniu.storage.MultipartUpload.EtagIdx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分片上传实现
 * https://developer.qiniu.com/kodo/api/6364/multipartupload-interface
 *
 * 若上传到同区域，如全上传到 华东存储，则可只使用一个实例；
 * 若上传到不同区域，则每个区域最好单独使用一个示例。一个实例多区域并发上传可能导致内部频繁报错，上传效率低；
 */
public class FixBlockUploader {
    private final int blockSize;
    private final Recorder recorder;

    private final MultipartUpload uploader;

    private final int retryMax;

    private String host = null;

    /**
     * 若上传到同区域，如全上传到 华东存储，则可只使用一个实例；
     * 若上传到不同区域，则每个区域最好单独使用一个示例。一个实例多区域并发上传可能导致内部频繁报错，上传效率低；
     *
     * @param blockSize     block size, eg: 4 * 1024 * 1024
     * @param configuration Nullable, if null, then create a new one.
     * @param client        Nullable, if null, then create a new one with configuration.
     * @param recorder      Nullable.
     */
    public FixBlockUploader(int blockSize, Configuration configuration, Client client, Recorder recorder) {
        if (blockSize <= 0) {
            blockSize = 4 * 1024 * 1024;
        }

        if (configuration == null) {
            configuration = new Configuration();
        }
        if (client == null) {
            client = new Client(configuration);
        }
        this.uploader = new MultipartUpload(configuration, client);
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

    public Response upload(final File file, final String token, String key, MultipartUpload.OptionsMeta params,
                           ExecutorService pool, int maxRunningBlock) throws QiniuException {
        BlockData blockData;
        try {
            blockData = new FileBlockData(this.blockSize, file);
        } catch (IOException e) {
            throw new QiniuException(e);
        }
        return upload(blockData, new StaticToken(token), key, params, pool, maxRunningBlock);
    }


    public Response upload(final InputStream is, long inputStreamLength, String fileName,
                           final String token, String key) throws QiniuException {
        return upload(is, inputStreamLength, fileName, token, key, null, null, 0);
    }


    public Response upload(final InputStream is, long inputStreamLength, String fileName,
                           final String token, String key, ExecutorService pool) throws QiniuException {
        return upload(is, inputStreamLength, fileName, token, key, null, pool, 8);
    }


    public Response upload(final InputStream is, long inputStreamLength, String fileName,
                           final String token, String key, MultipartUpload.OptionsMeta params,
                           ExecutorService pool, int maxRunningBlock) throws QiniuException {
        BlockData blockData;
        blockData = new InputStreamBlockData(this.blockSize, is, inputStreamLength, fileName);
        return upload(blockData, new StaticToken(token), key, params, pool, maxRunningBlock);
    }


    Response upload(BlockData blockData, String token, String key, MultipartUpload.OptionsMeta params,
                    ExecutorService pool, int maxRunningBlock) throws QiniuException {
        return upload(blockData, new StaticToken(token), key, params, pool, maxRunningBlock);
    }


    Response upload(BlockData blockData, Token token, String key, MultipartUpload.OptionsMeta params,
                    ExecutorService pool, int maxRunningBlock) throws QiniuException {
        try {
            String bucket = parseBucket(token.getUpToken());
            String recordFileKey = (recorder == null) ? "NULL"
                    : recorder.recorderKeyGenerate(bucket, MultipartUpload.genKey(key), blockData.getContentUUID(),
                    this.blockSize + "*:|>?^ \b" + this.getClass().getName());
            // must before any http request //
            uploader.initUpHost(token.getUpToken());

            UploadRecordHelper recordHelper = new UploadRecordHelper(recorder, recordFileKey, blockData.repeatable());
            // 1. initParts
            Record record = initUpload(blockData, recordHelper, bucket, key, token);
            boolean repeatable = recorder != null && blockData.repeatable();

            Response res;
            try {
                // 2. uploadPart
                upBlock(blockData, token, bucket, key, repeatable, record, pool, maxRunningBlock);
                // 3. completeParts
                res = makeFile(bucket, key, token, record.uploadId, record.etagIdxes,
                        blockData.getFileName(), params);
            } catch (QiniuException e) {
                // if everything is ok, do not need to sync record  //
                recordHelper.syncRecord(record);
                throw e;
            }
            if (res.isOK()) {
                recordHelper.delRecord();
            }
            return res;
        } finally {
            blockData.close();
        }
    }

    Record initUpload(BlockData blockData, UploadRecordHelper recordHelper,
                      String bucket, String key, Token token) throws QiniuException {
        Record record = null;
        if (blockData.repeatable()) {
            record = recordHelper.reloadRecord();
            // 有效的 record 才拿来用 //
            if (!recordHelper.isActiveRecord(record, blockData)) {
                record = null;
            }
        }

        if (record == null || record.uploadId == null) {
            MultipartUpload.InitRet ret = init(bucket, key, token);

            List<EtagIdx> etagIdxes = new ArrayList<>();
            record = initRecord(ret, etagIdxes);
        }
        return record;
    }

    MultipartUpload.InitRet init(String bucket, String key, Token token) throws QiniuException {
        Response res = null;
        try {
            // 1
            res = uploader.initiateMultipartUpload(bucket, key, token.getUpToken());
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
                uploader.changeHost(token.getUpToken(), host);
            }
            try {
                // 2
                res = uploader.initiateMultipartUpload(bucket, key, token.getUpToken());
            } catch (QiniuException e) {
                if (res == null && e.response != null) {
                    res = e.response;
                }
            } catch (Exception e) {
                // ignore, retry
            }

            if (res == null || res.needRetry()) {
                if (res == null || res.needSwitchServer()) {
                    uploader.changeHost(token.getUpToken(), host);
                }
                // 3
                res = uploader.initiateMultipartUpload(bucket, key, token.getUpToken());
            }
        }

        try {
            MultipartUpload.InitRet ret = res.jsonToObject(MultipartUpload.InitRet.class);
            if (ret != null && ret.uploadId != null && ret.uploadId.length() > 10 && ret.expireAt > 1000) {
                return ret;
            }
        } catch (Exception e) {
            // ignore, see next line
        }

        throw new QiniuException(res);
    }


    private void upBlock(BlockData blockData, Token token, String bucket, String key, boolean repeatable,
                         Record record, ExecutorService pool, int maxRunningBlock) throws QiniuException {
        boolean useParallel = useParallel(pool, blockData, record);

        if (!useParallel) {
            seqUpload(blockData, token, bucket, key, record);
        } else {
            parallelUpload(blockData, token, bucket, key, record, repeatable, pool, maxRunningBlock);
        }
    }

    private boolean useParallel(ExecutorService pool, BlockData blockData, Record record) {
        return pool != null && ((blockData.size() - record.size) > this.blockSize);
    }

    private void seqUpload(BlockData blockData, Token token, String bucket,
                           String key, Record record) throws QiniuException {
        final String uploadId = record.uploadId;
        final List<EtagIdx> etagIdxes = record.etagIdxes;
        RetryCounter counter = new NormalRetryCounter(retryMax);
        while (blockData.hasNext()) {
            try {
                blockData.nextBlock();
            } catch (Exception e) {
                throw new QiniuException(e, e.getMessage());
            }
            DataWraper wrapper = blockData.getCurrentBlockData();
            if (alreadyDone(wrapper.getIndex(), etagIdxes)) {
                continue;
            }

            EtagIdx etagIdx;
            try {
                etagIdx = uploadBlock(bucket, key, token, uploadId,
                        wrapper.getData(), wrapper.getSize(), wrapper.getIndex(), counter);
            } catch (Exception e) {
                throw new QiniuException(e, e.getMessage());
            }
            etagIdxes.add(etagIdx);
            // 对应的 etag、index 通过 etagIdx 添加 //
            record.size += etagIdx.size;
        }
    }

    private void parallelUpload(BlockData blockData, final Token token,
                                final String bucket, final String key, Record record,
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
            } catch (Exception e) {
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
                    return uploadBlock(bucket, key, token, uploadId,
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
            try {
                EtagIdx etagIdx = future.get();
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
            if (etagIdx.partNumber == index) {
                return true;
            }
        }
        return false;
    }

    private void waitingEnough(int maxRunningBlock, List<Future<EtagIdx>> futures) {
        while (true) {
            if (futures.size() < maxRunningBlock) {
                break;
            }
            int done = 0;
            // max(len(futures)) = 10000
            for (Future<EtagIdx> future : futures) {
                if (future.isDone()) {
                    done++;
                }
            }
            if (futures.size() - done < maxRunningBlock) {
                break;
            }
            sleepMillis(100);
        }
    }

    EtagIdx uploadBlock(String bucket, String key, Token token, String uploadId, byte[] data,
                        int dataLength, int partNum, RetryCounter counter) throws QiniuException {
    Response res = uploadBlockWithRetry(bucket, key, token, uploadId, data, dataLength, partNum, counter);
        try {
            MultipartUpload.UploadPartRet ret = res.jsonToObject(MultipartUpload.UploadPartRet.class);
            String etag = ret.getEtag();
            if (etag.length() > 10) {
                return new EtagIdx(etag, partNum, dataLength);
            }
        } catch (Exception e) {
            // ignore, see next line
        }
        throw new QiniuException(res);
    }

    Response uploadBlockWithRetry(String bucket, String key, Token token, String uploadId, byte[] data,
                                  int dataLength, int partNum, RetryCounter counter)
            throws QiniuException {
        // 在 最多重试次数 范围内， 每个块至多上传 3 次 //
        // 1
        Response res = uploadBlock1(bucket, key, token, uploadId, data, dataLength, partNum, true);
        if (res.isOK()) {
            return res;
        }

        if (res.needSwitchServer()) {
            uploader.changeHost(token.getUpToken(), host);
        }

        if (!counter.inRange()) {
            return res;
        }

        if (res.needRetry()) {
            counter.retried();
            // 2
            res = uploadBlock1(bucket, key, token, uploadId, data, dataLength, partNum, true);

            if (res.isOK()) {
                return res;
            }

            if (res.needSwitchServer()) {
                uploader.changeHost(token.getUpToken(), host);
            }

            if (!counter.inRange()) {
                return res;
            }

            if (res.needRetry()) {
                counter.retried();
                // 3
                res = uploadBlock1(bucket, key, token, uploadId, data, dataLength, partNum, false);
            }
        }

        return res;
    }

    Response uploadBlock1(String bucket, String key, Token token, String uploadId, byte[] data,
                          int dataLength, int partNum, boolean ignoreError) throws QiniuException {
        // put PUT
        try {
            return uploader.uploadPart(bucket, key, token.getUpToken(), uploadId, data, 0, dataLength, partNum);
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

    Response makeFile(String bucket, String key, Token token, String uploadId, List<EtagIdx> etags,
                      String fileName, MultipartUpload.OptionsMeta params) throws QiniuException {
        // 1
        Response res = makeFile1(bucket, key, token, uploadId, etags, fileName, params, true);
        if (res.needRetry()) {
            // 2
            res = makeFile1(bucket, key, token, uploadId, etags, fileName, params, true);
        }
        if (res.needRetry()) {
            if (res.needSwitchServer()) {
                uploader.changeHost(token.getUpToken(), host);
            }
            // 3
            res = makeFile1(bucket, key, token, uploadId, etags, fileName, params, false);
        }
        // keep the same, with com.qiniu.http.Client#L337
        if (res.statusCode >= 300) {
            throw new QiniuException(res);
        }
        return res;
    }

    Response makeFile1(String bucket, String key, Token token, String uploadId, List<EtagIdx> etags,
                       String fileName, MultipartUpload.OptionsMeta params, boolean ignoreError) throws QiniuException {
        try {
            return uploader.completeMultipartUpload(bucket, key, token.getUpToken(), uploadId, etags, fileName, params);
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

    private String parseBucket(String upToken) throws QiniuException {
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




    ///////////////////////////////////////


    class Record {
        // second
        long expireAt;
        String uploadId;
        long size;
        List<EtagIdx> etagIdxes;
    }


    Record initRecord(MultipartUpload.InitRet ret, List<EtagIdx> etagIdxes) {
        Record record = new Record();
        record.uploadId = ret.uploadId;
        //// 服务端 7 天内有效，设置 5 天 ////
        record.expireAt = ret.expireAt - 3600 * 24 * 2;
        record.etagIdxes = etagIdxes != null ? etagIdxes : new ArrayList<EtagIdx>();
        for (EtagIdx l : record.etagIdxes) {
            record.size += l.size;
        }

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
            return record;
        }

        public void delRecord() {
            if (recorder != null) {
                recorder.del(recordFileKey);
            }
        }


        public void syncRecord(Record record) {
            if (needRecord && recorder != null && record.etagIdxes.size() > 0) {
                MultipartUpload.sortByPartNumberAsc(record.etagIdxes);
                recorder.set(recordFileKey, new Gson().toJson(record).getBytes(Charset.forName("UTF-8")));
            }
        }

        public boolean isActiveRecord(Record record, BlockData blockData) {
            boolean isOk = record != null
                    && record.expireAt < (System.currentTimeMillis() / 1000)
                    && !StringUtils.isNullOrEmpty(record.uploadId)
                    && record.etagIdxes != null && record.etagIdxes.size() > 0
                    && record.size > 0 && record.size <= blockData.size();
            if (isOk) {
                int p = 0;
                // PartNumber start with 1 and increase by 1 //
                //  并行上传，中间块可能缺失（上传失败） //
                for (EtagIdx ei : record.etagIdxes) {
                    if (ei.partNumber > p) {
                        p = ei.partNumber;
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

        abstract String getFileName();
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
        String fileName;
        int index = 0; // start at 1, read a block , add 1
        long alreadyReadSize = 0;
        Lock lock;

        FileBlockData(int blockDataSize, File file) throws IOException {
            super(blockDataSize);
            fis = new RandomAccessFile(file, "r");
            fileName = file.getName();
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

        @Override
        public String getFileName() {
            return fileName;
        }
    }


    static class InputStreamBlockData extends BlockData {
        final long totalLength;
        final boolean closedAfterUpload;

        boolean repeatable;
        String contentUUID;
        DataWraper dataWraper;
        InputStream is;
        String fileName;
        int index = 0; // start at 1, read a block , add 1

        long alreadyReadSize = 0;

        InputStreamBlockData(int blockDataSize, InputStream is, long totalLength, String fileName) {
            this(blockDataSize, is, totalLength, fileName, true);
        }

        InputStreamBlockData(int blockDataSize, InputStream is, long totalLength,
                             String fileName, boolean closedAfterUpload) {
            this(blockDataSize, is, totalLength, fileName, closedAfterUpload, false, "");
        }

        InputStreamBlockData(int blockDataSize, InputStream is, long totalLength, String fileName,
                             boolean closedAfterUpload, boolean repeatable, String contentUUID) {
            super(blockDataSize);
            this.is = is;
            this.fileName = fileName;
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

        @Override
        public String getFileName() {
            return fileName;
        }
    }


    static void sleepMillis(long millis) {
        // LockSupport.parkNanos(millis * 1000 * 1000); // or
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

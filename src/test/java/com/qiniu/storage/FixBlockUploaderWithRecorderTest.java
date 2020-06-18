package com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.EtagV2;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class FixBlockUploaderWithRecorderTest {
    int blockSize = 1024 * 1024 * 8;
    Configuration config;
    Client client;
    FixBlockUploader up;
    String bucket;
    Recorder recorder;

    @Before
    public void init() {
        init2(false);
    }

    private void init2(boolean useHttpsDomains) {
        config = new Configuration();
        config.useHttpsDomains = useHttpsDomains;
        client = new Client(config);

        try {
            File dir = File.createTempFile("qiniu_", ".qiniu").getParentFile();
            recorder = new FileRecorder(dir);
            System.out.println("FileRecorder path: " + dir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        up = new FixBlockUploader(blockSize, config, client, recorder);
        bucket = TestConfig.testBucket_as0;
    }

    @Test
    public void breakThenUpload1() throws IOException {
        breakThenUpload(null, null, null, 10, 14);
    }

    @Test
    public void breakThenUpload2() throws IOException {
        ExecutorService pool = new ThreadPoolExecutor(0, 2,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
        breakThenUpload(pool, Executors.newFixedThreadPool(2), Executors.newCachedThreadPool(), 10, 10, 2);
    }


    @Test
    public void breakThenUpload4() throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();
        breakThenUpload(null, null, pool, 15, 20);
    }


    @Test
    public void breakThenUpload5() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        breakThenUpload(null, pool, null, 15, 7, 2);
    }

    @Test
    public void breakThenUpload6() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        breakThenUpload(pool, null, null, 7, 20, 2); // 可能无法中断 //
    }


    public void breakThenUpload(ExecutorService pool1, ExecutorService pool2, ExecutorService pool3,
                                int upSecondsTime1, int upSecondsTime2) throws IOException {
        breakThenUpload(pool1, pool2, pool3, upSecondsTime1, upSecondsTime2, 8);
    }

    public void breakThenUpload(final ExecutorService pool1, final ExecutorService pool2, final ExecutorService pool3,
                                int upSecondsTime1, int upSecondsTime2, final int maxRunningBlock) throws IOException {
        final long size = 1024 * 53 + 1039;
        final String expectKey = "\r\n?&r=" + size + "k" + System.currentTimeMillis();
        final File f = TempFile.createFileOld(size);
        final FixBlockUploader.FileBlockData fbd = new FixBlockUploader.FileBlockData(blockSize, f);
        System.out.println(f.getAbsolutePath());
        final String etag = EtagV2.file(f, blockSize);
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";

        StringMap p = new StringMap().put("returnBody", returnBody);

        final String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600, p);
        final int[] t1Finished = {0};

        Thread t1 = new Thread() {
            @Override
            public void run() {
                upload(1, fbd, token, expectKey, pool1, maxRunningBlock, t1Finished, etag);
            }
        };
        t1.setDaemon(true);
        t1.start();
        assertEquals(fbd.size(), f.length());
        String base64Key = UrlSafeBase64.encodeToString(expectKey);

        final UploadRecordHelper helper = new UploadRecordHelper(recorder, bucket, base64Key,
                fbd.getContentUUID(), this.blockSize + "*:|>?^ \b" + FixBlockUploader.class.getName());
        final String recordKey = helper.recordFileKey;
        System.out.println(recordKey);

        // 显示断点记录文件
        Thread showRecord = new Thread() {
            public void run() {
                for (; ; ) {
                    doSleep(1000);
                    showRecord("normal: " + size + " :", recorder, recordKey);
                }
            }
        };
        showRecord.setDaemon(true);
        showRecord.start();

        // 400s, 800
        // 20s, 40
        long lastSize = 0;
        for (int i = 0; i < upSecondsTime1 * 2; i++) {
            doSleep(500);
            Record r = helper.reloadRecord();
            System.out.println("1 r.size: " + r.size + "       blockSize:" + blockSize);
            if (r.size > blockSize - 1) {
                lastSize = r.size;
                break;
            }
        }
        fbd.close();
        if (pool1 != null) {
            System.out.println("pool1.shutdownNow();");
            pool1.shutdown();
            pool1.shutdownNow();
        }
        System.out.println("pool1");
        int it1Finished = 0;
        for (; it1Finished < 1500; it1Finished++) {
            doSleep(100);
            if (t1Finished[0] == 1) {
                break;
            }
        }
        System.out.println("t1Finished[0] == 1    " + (t1Finished[0] == 1) + "     " + it1Finished);
        doSleep(1500);
        final FixBlockUploader.FileBlockData fbd2 = new FixBlockUploader.FileBlockData(blockSize, f);
        final int[] t2Finished = {0};
        Thread t2 = new Thread() {
            @Override
            public void run() {
                assertEquals(fbd2.size(), f.length());
                upload(2, fbd2, token, expectKey, pool2, maxRunningBlock, t2Finished, etag);
                assertEquals(fbd2.size(), f.length());
            }
        };
        t2.setDaemon(true);
        t2.start();
        // 400s, 800
        // 20s, 40
        System.out.println(lastSize);
        for (int i = 0; i < upSecondsTime2 * 2; i++) {
            doSleep(500);
            Record r = helper.reloadRecord();
            System.out.println("2 r.size: " + r.size + "       blockSize:" + blockSize);
            if (lastSize != 0 && r.size > lastSize + blockSize) {
                lastSize = r.size;
                break;
            }
        }
        System.out.println(lastSize);
        System.out.println(new Date());
        fbd2.close();
        if (pool2 != null) {
            System.out.println("pool2.shutdownNow();");
            pool2.shutdown();
            pool2.shutdownNow();
        }
        System.out.println("pool1");


        int it2Finished = 0;
        for (; it2Finished < 1500; it2Finished++) {
            doSleep(100);
            if (t2Finished[0] == 1) {
                break;
            }
        }
        System.out.println("t2Finished[0] == 1    " + (t2Finished[0] == 1) + "     " + it2Finished);
        doSleep(1500);
        System.out.println("------ start 3, " + new Date());
        try {
//            upload(3, fbd2, token, expectKey, pool2, maxRunningBlock, t2Finished, etag);
            Response r = up.upload(f, token, expectKey, null, pool3, maxRunningBlock);

            System.out.println("======= end 3:     " + r.getInfo());
            MyRet ret = r.jsonToObject(MyRet.class);
            assertEquals(expectKey, ret.key);
//            assertEquals(f.getName(), ret.fname);
            assertEquals(String.valueOf(f.length()), ret.fsize);
            assertEquals(etag, ret.hash);
//            if (msg[0] != null) {
//                fail(msg[0]);
//            }
//            if (recordSize[0] == 0) {
//                fail("//断点记录的大小应该大于 0 //" + recordSize[0]);
//            }
        } catch (QiniuException e) {
            System.out.println("======= end 3:     ");
            if (e.response != null) {
                System.out.println(e.response.getInfo());
            }
            throw e;
        } finally {
            TempFile.remove(f);
        }
    }


    private void upload(int idx, FixBlockUploader.FileBlockData fbd, String token, String expectKey,
                        final ExecutorService pool, int maxRunningBlock, final int[] tFinished, String etag) {
        try {
            System.out.println("------ start " + idx + ", " + new Date());
            Response r = up.upload(fbd, new FixBlockUploader.StaticToken(token), expectKey,
                    null, pool, maxRunningBlock);
            System.out.println("======= end " + idx + ":     " + r.getInfo());
            MyRet ret = r.jsonToObject(MyRet.class);
            assertEquals(expectKey, ret.key);
            assertEquals(String.valueOf(fbd.size()), ret.fsize);
            assertEquals(etag, ret.hash);
            tFinished[0] = 1;
        } catch (Exception e) {
            System.out.println("======= end " + idx + ":     ");
            tFinished[0] = 1;
            e.printStackTrace();
        }
    }

    private void showRecord(String pre, Recorder recorder, String recordKey) {
        try {
            String jsonStr = pre;
            byte[] data = recorder.get(recordKey);
            if (data != null) {
                jsonStr += new String(data);
            }
            System.out.println(jsonStr);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void doSleep(int m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }


    class Record {
        long size;
    }


    class UploadRecordHelper {
        Recorder recorder;
        String recordFileKey;

        UploadRecordHelper(Recorder recorder, String bucket, String base64Key,
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

    }

    class MyRet {
        public String hash;
        public String key;
        public String fsize;
        public String fname;
        public String mimeType;
    }
}

package com.qiniu.storage;

import com.qiniu.TempFile;
import com.qiniu.TestConfig;
import com.qiniu.http.Response;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Created by Simon on 2015/3/30.
 */
public class RecordUploadTest {

    private boolean isDone = false;
    private Response response = null;

    private void template(int size) throws IOException {
        isDone = false;
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        final String expectKey = "\r\n?&r=" + size + "k";
        final File f = TempFile.createFile(size);
        final String token = TestConfig.testAuth.uploadToken(TestConfig.bucket, expectKey);
        final FileRecorder recorder = new FileRecorder(f.getParentFile());
        final RecordKeyGenerator keyGen = new RecordKeyGenerator() {
            @Override
            public String gen(String key, File file) {
                return key + "_._" + file.getAbsolutePath();
            }
        };
        final String recordKey = keyGen.gen(expectKey, f);

        UploadManager uploadManager = new UploadManager(recorder, keyGen);
//        Response res = uploadManager.put(f, expectKey, token);

        Up up = new Up(uploadManager, f, expectKey, token);


        // 显示断点记录文件
        Thread showRecord = new Thread() {
            public void run() {
                for (; ; ) {
                    doSleep(30);
                    showRecord("normal: ", recorder, recordKey);
                }
            }
        };
        showRecord.setDaemon(true);
        showRecord.start();

        final String p = f.getParentFile().getAbsolutePath() + "\\" + UrlSafeBase64.encodeToString(recordKey);
        System.out.println(p);
        System.out.println(new File(p).exists());

        final Random r = new Random();

        boolean shutDown = true;

        for (int i = 10; i > 0; i--) {
            final int t = r.nextInt(100) + 80;
            System.out.println(i + "  :  " + t);
            final Future<Response> future = threadPool.submit(up);

            // CHECKSTYLE:OFF
            // 中断线程 1 次
            if (shutDown) {
                new Thread() {
                    public void run() {
                        // 百毫秒
                        doSleep(t);
                        if (!future.isDone()) {
                            future.cancel(true);
                        }
                    }
                }.start();
                shutDown = false;
            }
            // CHECKSTYLE:ON
            showRecord("new future: ", recorder, recordKey);

            try {
                Response res = future.get();
                response = res;
                showRecord("done: ", recorder, recordKey);
                System.out.println("break");
                break;
            } catch (Exception e) {
                System.out.println("Exception");
                System.out.println(Thread.currentThread().getId() + " : future.isCancelled : " + future.isCancelled());
//                e.printStackTrace();
                if (isDone) {
                    break;
                }
            }
            doSleep(30);
            System.out.println(p);
            System.out.println(new File(p).exists());
        }

        TempFile.remove(f);
        assertFalse(new File(p).exists());
        assertNotNull(response);
        assertTrue(response.isOK());

        showRecord("nodata: ", recorder, recordKey);
    }

    private void showRecord(String pre, FileRecorder recorder, String recordKey) {
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

    private void doSleep(int bm) {
        try {
            Thread.sleep(100 * bm);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    @Test
    public void test1K() throws Throwable {
        template(1);
    }

    @Test
    public void test600k() throws Throwable {
        template(600);
    }

    @Test
    public void test4M() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 4);
    }

    @Test
    public void test8M1k() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 8 + 1);
    }

    @Test
    public void test25M1k() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 25 + 1);
    }

    class Up implements Callable<Response> {
        private final UploadManager uploadManager;
        private final File file;
        private final String key;
        private final String token;

        public Up(UploadManager uploadManager, File file, String key, String token) {
            this.uploadManager = uploadManager;
            this.file = file;
            this.key = key;
            this.token = token;
        }

        @Override
        public Response call() throws Exception {
            Response res = uploadManager.put(file, key, token);
            System.out.println("up:  " + res);
            System.out.println("up:  " + res.bodyString());
            isDone = true;
            response = res;
            return res;
        }
    }
}

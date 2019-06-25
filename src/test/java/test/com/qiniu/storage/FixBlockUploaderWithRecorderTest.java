package test.com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.FixBlockUploader;
import com.qiniu.storage.Recorder;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Etag;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
    public void breakThenUpload() throws IOException {
        final long size = 1024 * 43 + 1039; // 43M
        System.out.println("Start testing " + new Date());
        final String expectKey = "\r\n?&r=" + size + "k";
        final File f = TempFile.createFileOld(size);
        final FixBlockUploader.FileBlockData fbd = new FixBlockUploader.FileBlockData(blockSize, f);
        System.out.println(f.getAbsolutePath());
        final String etag = Etag.file(f);
        final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\"}";

        StringMap p = new StringMap().put("returnBody", returnBody);

        final String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600, p);

        Thread t1 = new Thread() {
            @Override
            public void run() {
                try {
                    Response r = up.upload(fbd, new FixBlockUploader.StaticToken(token), expectKey, null);
                    System.out.println("first upload: " + r.getInfo());
                } catch (QiniuException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.setDaemon(true);
        t1.start();

        String base64Key = UrlSafeBase64.encodeToString(expectKey);
        String contentUUID = fbd.getContentUUID();

        final UploadRecordHelper helper = new UploadRecordHelper(recorder, bucket, base64Key, contentUUID);
        final String recordKey = helper.recordFileKey;

        final boolean[] ch = new boolean[]{true};
        final String[] msg = new String[1];
        final long[] recordSize = new long[]{0};

        // 显示断点记录文件
        Thread showRecord = new Thread() {
            long lastSize = 0;
            public void run() {
                for (; ch[0]; ) {
                    doSleep(2000);
                    showRecord("normal: " + size + " :", recorder, recordKey);
                    long s = helper.reloadRecord().size;
                    if (s >= lastSize) {
                        lastSize = s;
                        recordSize[0] = lastSize;
                    } else {
                        msg[0] = "// 记录文件被回滚了 // r.size: " + s + ", lastSize: " + lastSize;
                        fail("// 记录文件被回滚了 // r.size: " + s + ", lastSize: " + lastSize);
                        throw new RuntimeException("// 记录文件被回滚了 // r.size: " + s + ", lastSize: " + lastSize);
                    }

                }
            }
        };
        showRecord.setDaemon(true);
        showRecord.start();

        // 400s
        for (int i = 0 ; i < 800 ; i++) {
            doSleep(500);
            Record r = helper.reloadRecord();
            System.out.println("1 r.size: " + r.size + "       blockSize:" + blockSize);
            if (r.size > blockSize - 1) {
                fbd.close();
                break;
            }
        }
        // 关闭，多次关闭问题不大 //
        fbd.close();

        doSleep(5000);

        final FixBlockUploader.FileBlockData fbd2 = new FixBlockUploader.FileBlockData(blockSize, f);
        Thread t2 = new Thread() {
            @Override
            public void run() {
                try {
                    Response r = up.upload(fbd2, new FixBlockUploader.StaticToken(token), expectKey, null);
                    System.out.println("2nd upload: " + r.getInfo());
                } catch (QiniuException e) {
                    e.printStackTrace();
                }
            }
        };
        t2.setDaemon(true);
        t2.start();

        // 400s
        for (int i = 0 ; i < 800 ; i++) {
            doSleep(500);
            Record r = helper.reloadRecord();
            System.out.println("2 r.size: " + r.size + "       blockSize:" + blockSize);
            if (r.size > blockSize * 2) {
                fbd2.close();
                break;
            }
        }
        // 关闭，多次关闭问题不大 //
        fbd2.close();

        doSleep(5000);

        try {
            Response r = up.upload(f, token, expectKey);

            System.out.println(r.getInfo());
            ResumeUploadTest.MyRet ret = r.jsonToObject(ResumeUploadTest.MyRet.class);
            assertEquals(expectKey, ret.key);
//            assertEquals(f.getName(), ret.fname);
            assertEquals(String.valueOf(f.length()), ret.fsize);
            assertEquals(etag, ret.hash);
            if (msg[0] != null) {
                fail(msg[0]);
            }
            if (recordSize[0] == 0) {
                fail("//断点记录的大小应该大于 0 //" + recordSize[0]);
            }
        } catch (QiniuException e) {
            System.out.println(e.response.getInfo());
            throw e;
        } finally {
            ch[0] = false;
            TempFile.remove(f);
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

        public UploadRecordHelper(Recorder recorder, String bucket, String base64Key, String contentUUID) {
            if (recorder != null) {
                this.recorder = recorder;
                recordFileKey = recorder.recorderKeyGenerate(bucket, base64Key, contentUUID,
                        blockSize + "_-" + FixBlockUploader.class.getName());
            }
        }

        public Record reloadRecord() {
            Record record = null;
            if (recorder != null) {
                try {
                    byte[] data = recorder.get(recordFileKey);
                    String d = new String(data, Charset.forName("UTF-8"));
//                    System.out.println(d);
                    record = new Gson().fromJson(d, Record.class);
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
}

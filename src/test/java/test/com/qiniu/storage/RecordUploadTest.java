package test.com.qiniu.storage;

import com.qiniu.common.Constants;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.ConcurrentResumeUploader;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.ResumeUploader;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Etag;
import org.junit.Test;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by Simon on 2015/3/30.
 */
public class RecordUploadTest {
    final Random r = new Random();

    final Client client = new Client();
    FileRecorder recorder = null;
    private Response response = null;

    private static boolean[][] testConfigList = {
            // isResumeV2, isConcurrent
            {true, false},
            {true, true},
            {false, false},
            {false, true}
    };

    /**
     * 断点续传
     *
     * @param size         文件大小
     * @param isResumeV2   是否使用分片上传 api v2, 反之为 v1
     * @param isConcurrent 是否采用并发方式上传
     * @throws IOException
     */
    private void template(final int size, boolean isResumeV2, boolean isConcurrent) throws IOException {

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile file : files) {
            // 雾存储不支持 v1
            if (file.isFog() && !isResumeV2) {
                continue;
            }
            String bucket = file.getBucketName();
            final Region region = file.getRegion();

            System.out.println("\n\n");
            System.out.printf("bucket:%s zone:%s \n", bucket, region);

            response = null;
            String key = "";
            key += isResumeV2 ? "_resumeV2" : "_resumeV1";
            key += isConcurrent ? "_concurrent" : "_serial";
            final String expectKey = "\r\n?&r=" + size + "k" + key;
            final File f = TempFile.createFile(size);
            recorder = new FileRecorder(f.getParentFile());

            System.out.printf("key:%s \n", expectKey);

            final Complete complete = new Complete();
            try {
                final String token = TestConfig.testAuth.uploadToken(bucket, expectKey, 3600, null);
                final String recordKey = recorder.recorderKeyGenerate(expectKey, f);

                // 开始第一部分上传
                final Up up = new Up(f, expectKey, token, isResumeV2, isConcurrent);
                new Thread() {
                    @Override
                    public void run() {
                        int i = r.nextInt(10000);
                        try {
                            System.out.println("UP: " + i + ",  enter run");
                            response = up.up(region);
                            System.out.println("UP:  " + i + ", left run");
                        } catch (Exception e) {
                            System.out.println("UP:  " + i + ", exception run");
                            e.printStackTrace();
                        }
                        complete.isComplete = true;
                    }
                }.start();

                final boolean[] ch = new boolean[]{true};
                // 显示断点记录文件
                Thread showRecord = new Thread() {
                    public void run() {
                        for (; ch[0]; ) {
                            doSleep(1000);
                            showRecord("normal: " + size + " :", recorder, recordKey);
                        }
                    }
                };
                showRecord.setDaemon(true);
                showRecord.start();

                if (f.length() > Constants.BLOCK_SIZE) {
                    // 终止第一部分上传,期望其部分成功
                    for (int i = 15; i > 0; --i) {
                        byte[] data = getRecord(recorder, recordKey);
                        if (data != null) {
                            doSleep(1000);
                            up.close();
                            break;
                        }
                        doSleep(200);
                    }
                    up.close();
                    doSleep(500);
                }

                System.out.println("response is " + response);
                while (!complete.isComplete) {
                    doSleep(200);
                }

                System.out.println("\r\n断点续传:");
                // 若第一部分上传部分未全部成功,再次上传
                if (response == null) {
                    try {
                        response = new Up(f, expectKey, token, isResumeV2, isConcurrent).up(region);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                showRecord("done: " + size + " :", recorder, recordKey);

                ch[0] = false;

                String etag = Etag.file(f);
                System.out.println("etag: " + etag);
                String hash = response.jsonToMap().get("hash").toString();
                System.out.println("hash: " + hash);

                assertNotNull(response);
                assertTrue(response.isOK());
                assertEquals(etag, hash);
                doSleep(2000);
                showRecord("nodata: " + size + " :", recorder, recordKey);
                assertNull("文件上传成功,但断点记录文件未清理", recorder.get(recordKey));
            } finally {
                TempFile.remove(f);
            }
        }
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

    private byte[] getRecord(FileRecorder recorder, String recordKey) {
        byte[] data = recorder.get(recordKey);
        if (data != null && data.length < 100) {
            return null;
        }
        return data;
    }

    private void doSleep(int m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    @Test
    public void test1K() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1, config[0], config[1]);
        }
    }

    @Test
    public void test600k() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(600, config[0], config[1]);
        }
    }

    @Test
    public void test4M() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 4, config[0], config[1]);
        }
    }

    @Test
    public void test4M1K() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 4 + 1, config[0], config[1]);
        }
    }

    @Test
    public void test8M1k() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 8 + 1, config[0], config[1]);
        }
    }

    @Test
    public void test25M1k() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 25 + 1, config[0], config[1]);
        }
    }

    @Test
    public void test50M1k() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 50 + 1, config[0], config[1]);
        }
    }

    @Test
    public void test100M1k() throws Throwable {
        for (boolean[] config : testConfigList) {
            template(1024 * 100 + 1, config[0], config[1]);
        }
    }

    @Test
    public void testLastModify() throws IOException {
        File f = File.createTempFile("qiniutest", "b");
        String folder = f.getParent();
        FileRecorder fr = new FileRecorder(folder);

        String key = fr.recorderKeyGenerate("b", "k", "c", "test_profile_");
        byte[] data = new byte[3];
        data[0] = 'a';
        data[1] = '8';
        data[2] = 'b';

        fr.set(key, data);
        byte[] data2 = fr.get(key);

        File recoderFile = new File(folder, key);

        long m1 = recoderFile.lastModified();

        assertEquals(3, data2.length);
        assertEquals('8', data2[1]);

        recoderFile.setLastModified(new Date().getTime() - 1000 * 3600 * 24 * 5 + 2300);
        data2 = fr.get(key);
        assertEquals(3, data2.length);
        assertEquals('8', data2[1]);

        recoderFile.setLastModified(new Date().getTime() - 1000 * 3600 * 24 * 5 - 2300);

        long m2 = recoderFile.lastModified();

        byte[] data3 = fr.get(key);

        assertNull(data3);
        assertTrue(m1 - m2 > 1000 * 3600 * 24 * 5 && m1 - m2 < 1000 * 3600 * 24 * 5 + 5500);

        try {
            Thread.sleep(2300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fr.set(key, data);
        long m4 = recoderFile.lastModified();
        assertTrue(m4 > m1);
    }

    class Up {
        private final File file;
        private final String key;
        private final String token;
        private final boolean isResumeV2;
        private final boolean isConcurrent;
        ResumeUploader uploader = null;

        Up(File file, String key, String token, boolean isResumeV2, boolean isConcurrent) {
            this.file = file;
            this.key = key;
            this.token = token;
            this.isResumeV2 = isResumeV2;
            this.isConcurrent = isConcurrent;
        }

        public void close() {
            System.out.println("UP going to close");
            // 调用 uploader 私有方法 close()
            try {
                Method m_close = ResumeUploader.class.getDeclaredMethod("close");
                m_close.setAccessible(true);
                m_close.invoke(uploader);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            System.out.println("UP closed");
        }

        public Response up(Region region) throws Exception {
            int i = r.nextInt(10000);
            try {
                System.out.println("UP: " + i + ",  enter up");
                if (recorder == null) {
                    recorder = new FileRecorder(file.getParentFile());
                }

                Configuration config = new Configuration(region);

                if (isResumeV2) {
                    config.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
                }

                if (isConcurrent) {
                    config.resumableUploadMaxConcurrentTaskCount = 3;
                    uploader = new ConcurrentResumeUploader(client, token, key, file,
                            null, Client.DefaultMime, recorder, config);
                } else {
                    uploader = new ResumeUploader(client, token, key, file,
                            null, Client.DefaultMime, recorder, config);
                }

                Response res = uploader.upload();
                System.out.println("UP:  " + i + ", left up");
                return res;
            } catch (Exception e) {
                System.out.println("UP:  " + i + ", exception up");
                throw e;
            }
        }
    }

    static class Complete {
        boolean isComplete = false;
    }
}

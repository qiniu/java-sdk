package com.qiniu.storage;

import com.qiniu.TempFile;
import com.qiniu.TestConfig;
import com.qiniu.common.Constants;
import com.qiniu.common.Zone;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Etag;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

    private void template(final int size) throws IOException {
        Map<String, Zone> bucketKeyMap = new HashMap<String, Zone>();
        bucketKeyMap.put(TestConfig.testBucket_z0, Zone.zone0());
        bucketKeyMap.put(TestConfig.testBucket_na0, Zone.zoneNa0());
        for (Map.Entry<String, Zone> entry : bucketKeyMap.entrySet()) {
            String bucket = entry.getKey();
            final Zone zone = entry.getValue();
            response = null;
            final String expectKey = "\r\n?&r=" + size + "k";
            final File f = TempFile.createFile(size);
            recorder = new FileRecorder(f.getParentFile());
            try {
                final String token = TestConfig.testAuth.uploadToken(bucket, expectKey);
                final String recordKey = recorder.recorderKeyGenerate(expectKey, f);

                // 开始第一部分上传
                final Up up = new Up(f, expectKey, token);
                new Thread() {
                    @Override
                    public void run() {
                        int i = r.nextInt(10000);
                        try {
                            System.out.println("UP: " + i + ",  enter run");
                            response = up.up(zone);
                            System.out.println("UP:  " + i + ", left run");
                        } catch (Exception e) {
                            System.out.println("UP:  " + i + ", exception run");
                            e.printStackTrace();
                        }
                    }
                } .start();

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
                    for (int i = 150; i > 0; --i) {
                        byte[] data = getRecord(recorder, recordKey);
                        if (data != null) {
                            up.close();
                            doSleep(100);
                            break;
                        }
                        doSleep(200);
                    }
                    up.close();
                    doSleep(500);
                }

                System.out.println("response is " + response);

                // 若第一部分上传部分未全部成功,再次上传
                if (response == null) {
                    try {
                        response = new Up(f, expectKey, token).up(zone);
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
                doSleep(500);
                showRecord("nodata: " + size + " :", recorder, recordKey);
                assertNull("文件上传成功,但断点记录文件为清理", recorder.get(recordKey));
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
    public void test4M1K() throws Throwable {
        if (TestConfig.isTravis()) {
            return;
        }
        template(1024 * 4 + 1);
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

    @Test
    public void testLastModify() throws IOException {
        File f = File.createTempFile("qiniutest", "b");
        String folder = f.getParent();
        FileRecorder fr = new FileRecorder(folder);

        String key = "test_profile_";
        byte[] data = new byte[3];
        data[0] = 'a';
        data[1] = '8';
        data[2] = 'b';

        fr.set(key, data);
        byte[] data2 = fr.get(key);

        File recoderFile = new File(folder, hash(key));

        long m1 = recoderFile.lastModified();

        assertEquals(3, data2.length);
        assertEquals('8', data2[1]);

        recoderFile.setLastModified(new Date().getTime() - 1000 * 3600 * 48 + 2300);
        data2 = fr.get(key);
        assertEquals(3, data2.length);
        assertEquals('8', data2[1]);

        recoderFile.setLastModified(new Date().getTime() - 1000 * 3600 * 48 - 2300);

        long m2 = recoderFile.lastModified();

        byte[] data3 = fr.get(key);

        assertNull(data3);
        assertTrue(m1 - m2 > 1000 * 3600 * 48 && m1 - m2 < 1000 * 3600 * 48 + 5500);

        try {
            Thread.sleep(2300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fr.set(key, data);
        long m4 = recoderFile.lastModified();
        assertTrue(m4 > m1);
    }

    // copied from FileRecorder
    private static String hash(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(base.getBytes());
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    class Up {
        private final File file;
        private final String key;
        private final String token;
        ResumeUploader uploader = null;

        public Up(File file, String key, String token) {
            this.file = file;
            this.key = key;
            this.token = token;
        }

        public void close() {
            System.out.println("UP going to close");
            // 调用 uploader 私有方法 close()
            try {
                Method m_close = ResumeUploader.class.getDeclaredMethod("close", new Class[]{});
                m_close.setAccessible(true);
                m_close.invoke(uploader, new Object[]{});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            System.out.println("UP closed");
        }


        public Response up(Zone zone) throws Exception {
            int i = r.nextInt(10000);
            try {
                System.out.println("UP: " + i + ",  enter up");
                if (recorder == null) {
                    recorder = new FileRecorder(file.getParentFile());
                }

                uploader = new ResumeUploader(client, token, key, file,
                        null, Client.DefaultMime, recorder, new Configuration(zone));
                Response res = uploader.upload();
                System.out.println("UP:  " + i + ", left up");
                return res;
            } catch (Exception e) {
                System.out.println("UP:  " + i + ", exception up");
                throw e;
            }
        }
    }
}

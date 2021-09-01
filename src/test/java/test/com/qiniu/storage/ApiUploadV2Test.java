package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.storage.*;
import com.qiniu.util.Md5;
import com.qiniu.util.StringMap;
import test.com.qiniu.TempFile;
import test.com.qiniu.TestConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiUploadV2Test {

    @Test
    @Tag("IntegrationTest")
    public void testUploadBytes() {
        testUpload(true, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void testUploadStream() {
        testUpload(false, false);
    }

    @Test
    @Tag("IntegrationTest")
    public void testUploadStreamWithContentLength() {
        testUpload(false, true);
    }

    private void testUpload(boolean isUploadBytes, boolean isSetContentLength) {
        long fileSize = 1024 * 7 + 2341; // 单位： k
        File f = null;
        try {
            f = TempFile.createFile(fileSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileSize *= 1024; // 单位： B

        /**
         * 一个文件被分成多个 part，上传所有的 part，然后在七牛云根据 part 信息合成文件 |-----------------------------
         * file -----------------------------| |------ part ------|------ part
         * ------|------ part ------|... |----- etag01 -----|----- etag02 -----|-----
         * etag03 -----|... allBlockCtx = [{"partNumber":1, "etag", etag01},
         * {"partNumber":2, "etag", etag02}, {"partNumber":3, "etag", etag03}, ...]
         *
         * 上传过程： 1. 调用 ApiUploadV2InitUpload api 创建一个 upload 任务，获取 uploadId 2. 重复调用
         * ApiUploadV2UploadPart api 直到文件所有的 part 均上传完毕, part 的大小可以不相同 3. 调用
         * ApiUploadV2CompleteUpload api 组装 api 4.
         * ApiUploadV2InitUpload、ApiUploadV2UploadPart、ApiUploadV2CompleteUpload 等分片 V2
         * API的 key 需要统一（要么有设置且相同，要么均不设置）
         *
         * 注： 1. partNumber 范围是 1 ~ 10000 2. 除最后一个 Part 外，单个 Part 大小范围 1 MB ~ 1 GB 3.
         * 如果你用同一个 PartNumber 上传了新的数据，那么服务端已有的这个号码的 Part 数据将被覆盖
         */
        int defaultPartSize = 1024 * 1024 * 2;

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile testFile : files) {
            String bucket = testFile.getBucketName();
            Region region = testFile.getRegion();

            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(f, "r");
            } catch (IOException e) {
                e.printStackTrace();
            }

            String fileName = "java_api_v2_test.zip";
            String key = "java_api_v2_test";

            final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                    + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\",\"foo\":\"$(x:foo)\"}";
            String token = TestConfig.testAuth.uploadToken(bucket, key, 3600,
                    new StringMap().put("returnBody", returnBody));

            String urlPrefix = null;
            try {
                Field hostsFiled = Region.class.getDeclaredField("srcUpHosts");
                hostsFiled.setAccessible(true);
                List<String> hosts = (List<String>) hostsFiled.get(region);
                urlPrefix = "http://" + hosts.get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Configuration configuration = new Configuration();
            Client client = new Client(configuration);

            // 1. init upload
            String uploadId = null;
            ApiUploadV2InitUpload initUploadApi = new ApiUploadV2InitUpload(client);
            ApiUploadV2InitUpload.Request initUploadRequest = new ApiUploadV2InitUpload.Request(urlPrefix, token)
                    .setKey(key);
            try {
                ApiUploadV2InitUpload.Response initUploadResponse = initUploadApi.request(initUploadRequest);
                uploadId = initUploadResponse.getUploadId();
                System.out.println("init upload:" + initUploadResponse.getResponse());
                System.out.println("init upload id::" + initUploadResponse.getUploadId());

                assertTrue(initUploadResponse.isOK(), initUploadResponse.getResponse() + "");
                assertNotNull(initUploadResponse.getUploadId(), initUploadResponse.getUploadId() + "");
                assertNotNull(initUploadResponse.getExpireAt(), initUploadResponse.getExpireAt() + "");
            } catch (QiniuException e) {
                e.printStackTrace();
            }

            // 2. 上传文件数据
            List<Map<String, Object>> partsInfo = new ArrayList<>();
            long partOffset = 0; // 块在文件中的偏移量
            int partNumber = 1; // part num 从 1 开始
            while (partOffset < fileSize) {
                // 1.2.1 读取 part 数据
                long partSize = fileSize - partOffset;
                if (partSize > defaultPartSize) {
                    partSize = defaultPartSize;
                }
                byte[] partData = getUploadData(file, partOffset, (int) partSize);

                // 1.2.2 上传 part 数据
                ApiUploadV2UploadPart uploadPartApi = new ApiUploadV2UploadPart(client);
                ApiUploadV2UploadPart.Request uploadPartRequest = new ApiUploadV2UploadPart.Request(urlPrefix, token,
                        uploadId, partNumber).setKey(key);
                if (isUploadBytes) {
                    uploadPartRequest.setUploadData(partData, 0, partData.length, null);
                } else {
                    uploadPartRequest.setUploadData(new ByteArrayInputStream(partData), null,
                            isSetContentLength ? partData.length + 1 : -1);
                }
                try {
                    ApiUploadV2UploadPart.Response uploadPartResponse = uploadPartApi.request(uploadPartRequest);
                    String etag = uploadPartResponse.getEtag();
                    String md5 = uploadPartResponse.getMd5();
                    Map<String, Object> partInfo = new HashMap<>();
                    partInfo.put(ApiUploadV2CompleteUpload.Request.PART_NUMBER, partNumber);
                    partInfo.put(ApiUploadV2CompleteUpload.Request.PART_ETG, etag);
                    partsInfo.add(partInfo);
                    System.out.println("upload part:" + uploadPartResponse.getResponse());

                    assertTrue(uploadPartResponse.isOK(), uploadPartResponse.getResponse() + "");
                    assertEquals(uploadPartResponse.getMd5(), Md5.md5(partData), md5);
                    assertNotNull(uploadPartResponse.getEtag(), etag);
                } catch (QiniuException e) {
                    e.printStackTrace();
                    break;
                }

                // 1.2.3 计算下一个 part 的 number
                partNumber += 1;
                partOffset += partSize;
            }
            int lastPartNum = partNumber - 1;

            // 获取上传的 part 信息
            Integer partNumberMarker = null;
            List<Map<String, Object>> listPartInfo = new ArrayList<>();
            while (true) {
                ApiUploadV2ListParts listPartsApi = new ApiUploadV2ListParts(client);
                ApiUploadV2ListParts.Request listPartsRequest = new ApiUploadV2ListParts.Request(urlPrefix, token,
                        uploadId).setKey(key).setMaxParts(2) // 此处仅为示例分页拉去，实际可不配置使用默认值1000
                                .setPartNumberMarker(partNumberMarker);
                try {
                    ApiUploadV2ListParts.Response listPartsResponse = listPartsApi.request(listPartsRequest);
                    partNumberMarker = listPartsResponse.getPartNumberMarker();
                    listPartInfo.addAll(listPartsResponse.getParts());
                    System.out.println("list part:" + listPartsResponse.getResponse());

                    assertTrue(listPartsResponse.isOK(), listPartsResponse.getResponse() + "");
                    assertEquals(listPartsResponse.getUploadId(), uploadId, listPartsResponse.getUploadId() + "");
                    assertNotNull(listPartsResponse.getExpireAt(), listPartsResponse.getExpireAt() + "");
                    assertNotNull(partNumberMarker, listPartsResponse.getResponse() + "");

                    // 列举结束
                    if (partNumberMarker == 0) {
                        break;
                    }
                } catch (QiniuException e) {
                    e.printStackTrace();
                    break;
                }
            }
            System.out.println("list parts info:" + listPartInfo);
            assertEquals(listPartInfo.size(), lastPartNum, listPartInfo + "");

            // 3. 组装文件
            String fooKey = "foo";
            String fooValue = "foo-Value";
            Map<String, Object> customParam = new HashMap<>();
            customParam.put("x:foo", fooValue);
            ApiUploadV2CompleteUpload completeUploadApi = new ApiUploadV2CompleteUpload(client);
            ApiUploadV2CompleteUpload.Request completeUploadRequest = new ApiUploadV2CompleteUpload.Request(urlPrefix,
                    token, uploadId, partsInfo).setKey(key).setFileName(fileName).setCustomParam(customParam);
            try {
                ApiUploadV2CompleteUpload.Response completeUploadResponse = completeUploadApi
                        .request(completeUploadRequest);
                System.out.println("complete upload:" + completeUploadResponse.getResponse());

                assertTrue(completeUploadResponse.isOK(), completeUploadResponse.getResponse() + "");
                assertEquals(completeUploadResponse.getKey(), key, completeUploadResponse.getKey() + "");
                assertNotNull(completeUploadResponse.getHash(), completeUploadResponse.getHash() + "");
                String fSize = completeUploadResponse.getStringValueFromDataMap("fsize") + "";
                assertEquals(fSize, fileSize + "", fSize + ":" + fileSize);
                String fName = completeUploadResponse.getStringValueFromDataMap("fname");
                assertEquals(fName, fileName, fName + ":" + fileName);
                String foo = completeUploadResponse.getStringValueFromDataMap(fooKey);
                assertEquals(foo, fooValue, foo + ":" + fooValue);
            } catch (QiniuException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testAbortUpload() {
        long fileSize = 1024 * 7 + 2341; // 单位： k
        File f = null;
        try {
            f = TempFile.createFile(fileSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileSize *= 1024; // 单位： B

        /**
         * 一个文件被分成多个 part，上传所有的 part，然后在七牛云根据 part 信息合成文件 |-----------------------------
         * file -----------------------------| |------ part ------|------ part
         * ------|------ part ------|... |----- etag01 -----|----- etag02 -----|-----
         * etag03 -----|... allBlockCtx = [{"partNumber":1, "etag", etag01},
         * {"partNumber":2, "etag", etag02}, {"partNumber":3, "etag", etag03}, ...]
         *
         * 上传过程： 1. 调用 ApiUploadV2InitUpload api 创建一个 upload 任务，获取 uploadId 2. 重复调用
         * ApiUploadV2UploadPart api 直到文件所有的 part 均上传完毕, part 的大小可以不相同 3. 调用
         * ApiUploadV2CompleteUpload api 组装 api 4.
         * ApiUploadV2InitUpload、ApiUploadV2UploadPart、ApiUploadV2CompleteUpload 等分片 V2
         * API的 key 需要统一（要么有设置且相同，要么均不设置）
         *
         * 注： 1. partNumber 范围是 1 ~ 10000 2. 除最后一个 Part 外，单个 Part 大小范围 1 MB ~ 1 GB 3.
         * 如果你用同一个 PartNumber 上传了新的数据，那么服务端已有的这个号码的 Part 数据将被覆盖
         */
        int defaultPartSize = 1024 * 1024 * 2;

        TestConfig.TestFile[] files = TestConfig.getTestFileArray();
        for (TestConfig.TestFile testFile : files) {
            String bucket = testFile.getBucketName();
            Region region = testFile.getRegion();

            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(f, "r");
            } catch (IOException e) {
                e.printStackTrace();
            }

            String fileName = "java_api_v2_test.zip";
            String key = "java_api_v2_test";

            final String returnBody = "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"fsize\":\"$(fsize)\""
                    + ",\"fname\":\"$(fname)\",\"mimeType\":\"$(mimeType)\",\"foo\":\"$(x:foo)\"}";
            String token = TestConfig.testAuth.uploadToken(bucket, key, 3600,
                    new StringMap().put("returnBody", returnBody));

            String urlPrefix = null;
            try {
                Field hostsFiled = Region.class.getDeclaredField("srcUpHosts");
                hostsFiled.setAccessible(true);
                List<String> hosts = (List<String>) hostsFiled.get(region);
                urlPrefix = "http://" + hosts.get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Configuration configuration = new Configuration();
            Client client = new Client(configuration);

            // 1. init upload
            String uploadId = null;
            ApiUploadV2InitUpload initUploadApi = new ApiUploadV2InitUpload(client);
            ApiUploadV2InitUpload.Request initUploadRequest = new ApiUploadV2InitUpload.Request(urlPrefix, token)
                    .setKey(key);
            try {
                ApiUploadV2InitUpload.Response initUploadResponse = initUploadApi.request(initUploadRequest);
                uploadId = initUploadResponse.getUploadId();
                System.out.println("init upload:" + initUploadResponse.getResponse());
                System.out.println("init upload id::" + initUploadResponse.getUploadId());

                assertTrue(initUploadResponse.isOK(), initUploadResponse.getResponse() + "");
                assertNotNull(initUploadResponse.getUploadId(), initUploadResponse.getUploadId() + "");
                assertNotNull(initUploadResponse.getExpireAt(), initUploadResponse.getExpireAt() + "");
            } catch (QiniuException e) {
                e.printStackTrace();
            }

            // 2. 上传文件数据
            List<Map<String, Object>> partsInfo = new ArrayList<>();
            long partOffset = 0; // 块在文件中的偏移量
            int partNumber = 1; // part num 从 1 开始
            while (partOffset < fileSize - 10) {
                // 1.2.1 读取 part 数据
                long partSize = fileSize - partOffset;
                if (partSize > defaultPartSize) {
                    partSize = defaultPartSize;
                }
                byte[] partData = getUploadData(file, partOffset, (int) partSize);

                // 1.2.2 上传 part 数据
                ApiUploadV2UploadPart uploadPartApi = new ApiUploadV2UploadPart(client);
                ApiUploadV2UploadPart.Request uploadPartRequest = new ApiUploadV2UploadPart.Request(urlPrefix, token,
                        uploadId, partNumber).setKey(key).setUploadData(partData, 0, partData.length, null);
                try {
                    ApiUploadV2UploadPart.Response uploadPartResponse = uploadPartApi.request(uploadPartRequest);
                    String etag = uploadPartResponse.getEtag();
                    String md5 = uploadPartResponse.getMd5();
                    Map<String, Object> partInfo = new HashMap<>();
                    partInfo.put(ApiUploadV2CompleteUpload.Request.PART_NUMBER, partNumber);
                    partInfo.put(ApiUploadV2CompleteUpload.Request.PART_ETG, etag);
                    partsInfo.add(partInfo);
                    System.out.println("upload part:" + uploadPartResponse.getResponse());

                    assertTrue(uploadPartResponse.isOK(), uploadPartResponse.getResponse() + "");
                    assertEquals(md5, uploadPartResponse.getMd5(), Md5.md5(partData));
                    assertNotNull(etag, uploadPartResponse.getEtag());
                } catch (QiniuException e) {
                    e.printStackTrace();
                    break;
                }

                // 1.2.3 计算下一个 part 的 number
                partNumber += 1;
                partOffset += partSize;
            }
            int lastPartNum = partNumber - 1;

            // 2. abort
            ApiUploadV2AbortUpload abortUploadApi = new ApiUploadV2AbortUpload(client);
            ApiUploadV2AbortUpload.Request abortUploadRequest = new ApiUploadV2AbortUpload.Request(urlPrefix, token,
                    uploadId).setKey(key);
            try {
                ApiUploadV2AbortUpload.Response abortUploadResponse = abortUploadApi.request(abortUploadRequest);
                System.out.println("abort upload:" + abortUploadResponse.getResponse());

                assertTrue(abortUploadResponse.isOK(), abortUploadResponse.getResponse() + "");
            } catch (QiniuException e) {
                e.printStackTrace();
            }

            // 获取上传的 part 信息
            ApiUploadV2ListParts listPartsApi = new ApiUploadV2ListParts(client);
            ApiUploadV2ListParts.Request listPartsRequest = new ApiUploadV2ListParts.Request(urlPrefix, token, uploadId)
                    .setKey(key).setMaxParts(2) // 此处仅为示例分页拉去，实际可不配置使用默认值1000
                    .setPartNumberMarker(null);
            try {
                ApiUploadV2ListParts.Response listPartsResponse = listPartsApi.request(listPartsRequest);
                System.out.println("list part:" + listPartsResponse.getResponse());

                assertFalse(listPartsResponse.isOK(), listPartsResponse.getResponse() + "");
                // 列举结束
                break;
            } catch (QiniuException e) {
                e.printStackTrace();
                assertTrue(e.response.statusCode == 612, e.response + "");
                break;
            }
        }
    }

    private byte[] getUploadData(RandomAccessFile file, long offset, int size) {
        byte[] uploadData = new byte[size];
        try {
            file.seek(offset);
            int readSize = 0;
            while (readSize != size) {
                int s = 0;
                try {
                    s = file.read(uploadData, readSize, size - readSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s >= 0) {
                    readSize += s;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            uploadData = null;
        }
        return uploadData;
    }
}

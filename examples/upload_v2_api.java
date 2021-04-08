import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.util.Auth;
import test.com.qiniu.TempFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadDemo {

    //设置好账号的ACCESS_KEY和SECRET_KEY
    String ACCESS_KEY = "Access_Key";
    String SECRET_KEY = "Secret_Key";
    /**
     * 要上传的空间对应的 urlPrefix scheme + host
     * host:
     * 华东机房(region0): up.qiniup.com 或 upload.qiniup.com
     * 华北机房(region1): up-z1.qiniup.com 或 upload-z1.qiniup.com
     * 华南机房(region2): up-z2.qiniup.com 或 upload-z2.qiniup.com
     * 北美机房(regionNa0): up-na0.qiniup.com 或 upload-na0.qiniup.com
     * 新加坡机房(regionAs0): up-as0.qiniup.com 或 upload-as0.qiniup.com
     * 雾存储华东一区(regionFogCnEast1): up-fog-cn-east-1.qiniup.com 或 upload-fog-cn-east-1.qiniup.com
     */
    String urlPrefix = "urlPrefix" // http://up.qiniup.com";
    // 要上传的空间
    String bucketName = "Bucket_Name";
    //上传到七牛后保存的文件名
    String key = "my-java.png";

    //密钥配置
    Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    //简单上传，使用默认策略，只需要设置上传的空间名就可以了
    public String getUpToken() {
        return auth.uploadToken(bucketName);
    }

    public static void main(String args[]) throws IOException {
        new UploadDemo().upload();
    }

    public void testUpload() {

        String fileName = "java_api_v2_test.zip";
        long fileSize = 1024 * 7 + 2341; // 单位： k
        File f = null;
        try {
            f = TempFile.createFile(fileSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileSize *= 1024; // 单位： B

        /**
         * 一个文件被分成多个 part，上传所有的 part，然后在七牛云根据 part 信息合成文件
         * |----------------------------- file -----------------------------|
         * |------ part ------|------ part ------|------ part ------|...
         * |----- etag01 -----|----- etag02 -----|----- etag03 -----|...
         * allBlockCtx = [{"partNumber":1, "etag", etag01}, {"partNumber":2, "etag", etag02}, {"partNumber":3, "etag", etag03}, ...]
         *
         * 上传过程：
         * 1. 调用 ApiUploadV2InitUpload api 创建一个 upload 任务，获取 uploadId
         * 2. 重复调用 ApiUploadV2UploadPart api 直到文件所有的 part 均上传完毕, part 的大小可以不相同
         * 3. 调用 ApiUploadV2CompleteUpload api 组装 api
         * 4. ApiUploadV2InitUpload、ApiUploadV2UploadPart、ApiUploadV2CompleteUpload 等分片 V2 API的 key 需要统一（要么有设置且相同，要么均不设置）
         *
         * 注：
         * 1. partNumber 范围是 1 ~ 10000
         * 2. 除最后一个 Part 外，单个 Part 大小范围 1 MB ~ 1 GB
         * 3. 如果你用同一个 PartNumber 上传了新的数据，那么服务端已有的这个号码的 Part 数据将被覆盖
         */
        int defaultPartSize = 1024 * 1024 * 2;

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(f, "r");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String token = getUpToken();

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
            ApiUploadV2UploadPart.Request uploadPartRequest = new ApiUploadV2UploadPart.Request(urlPrefix, token, uploadId, partNumber)
                    .setKey(key)
                    .setUploadData(partData, 0, partData.length, null);
            try {
                ApiUploadV2UploadPart.Response uploadPartResponse = uploadPartApi.request(uploadPartRequest);
                String etag = uploadPartResponse.getEtag();
                String md5 = uploadPartResponse.getMd5();
                Map<String, Object> partInfo = new HashMap<>();
                partInfo.put(ApiUploadV2CompleteUpload.Request.PART_NUMBER, partNumber);
                partInfo.put(ApiUploadV2CompleteUpload.Request.PART_ETG, etag);
                partsInfo.add(partInfo);
                System.out.println("upload part:" + uploadPartResponse.getResponse());
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
            ApiUploadV2ListParts.Request listPartsRequest = new ApiUploadV2ListParts.Request(urlPrefix, token, uploadId)
                    .setKey(key)
                    .setMaxParts(2)  // 此处仅为示例分页拉去，实际可不配置使用默认值1000
                    .setPartNumberMarker(partNumberMarker);
            try {
                ApiUploadV2ListParts.Response listPartsResponse = listPartsApi.request(listPartsRequest);
                partNumberMarker = listPartsResponse.getPartNumberMarker();
                listPartInfo.addAll(listPartsResponse.getParts());
                System.out.println("list part:" + listPartsResponse.getResponse());
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

        // 3. 组装文件
        String fooKey = "foo";
        String fooValue = "foo-Value";
        Map<String, Object> customParam = new HashMap<>();
        customParam.put("x:foo", fooValue);
        ApiUploadV2CompleteUpload completeUploadApi = new ApiUploadV2CompleteUpload(client);
        ApiUploadV2CompleteUpload.Request completeUploadRequest = new ApiUploadV2CompleteUpload.Request(urlPrefix, token, uploadId, partsInfo)
                .setKey(key)
                .setFileName(fileName)
                .setCustomParam(customParam);
        try {
            ApiUploadV2CompleteUpload.Response completeUploadResponse = completeUploadApi.request(completeUploadRequest);
            System.out.println("complete upload:" + completeUploadResponse.getResponse());
        } catch (QiniuException e) {
            e.printStackTrace();
        }

        f.delete();
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
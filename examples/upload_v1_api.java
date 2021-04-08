import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.storage.ApiUploadV1MakeBlock;
import com.qiniu.storage.ApiUploadV1MakeFile;
import com.qiniu.storage.ApiUploadV1PutChunk;
import com.qiniu.storage.Configuration;
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
    String urlPrefix = "urlPrefix"; // http://up.qiniup.com
    //要上传的空间
    String bucketName = "Bucket_Name";
    //上传到七牛后保存的文件名
    String key = "java-api-test.zip";

    //密钥配置
    Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

    //简单上传，使用默认策略，只需要设置上传的空间名就可以了
    public String getUpToken() {
        return auth.uploadToken(bucketName);
    }

    public static void main(String args[]) throws IOException {
        new UploadDemo().upload();
    }

    public void upload() throws IOException {

        long fileSize = 1024 * 7 + 2341; // 单位： k
        File f = null;
        try {
            f = TempFile.createFile(fileSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileSize *= 1024; // 单位： B

        /**
         * 一个文件被分成多个 block ，一个块可以被分成多个 chunk
         * |----------------------------- file -----------------------------|
         * |------ block ------|------ block ------|------ block ------|...
         * |- chunk -|- chunk -|- chunk -|- chunk -|- chunk -|- chunk -|...
         * |- ctx01 -|- ctx02 -|- ctx10 -|- ctx12 -|- ctx20 -|- ctx22 -|...
         * allBlockCtx = [ctx02, ctx12, ctx22, ...]
         *
         * 上传过程：
         * 1. 把文件分成 block，把块分成 chunk
         * 2. 调用 ApiUploadV1MakeBlock 创建 block，并附带 block 的第一个 chunk
         * 3. 如果 block 中还有 chunk 未上传，则调用 ApiUploadV1PutChunk 上传 chunk, 直到该 block 中所有的 chunk 上传完毕
         * 4. 回到【步骤 2】继续上传 block，循环【步骤 2】~【步骤 3】直到所有 block 上传完毕
         * 3. 调用 ApiUploadV1MakeFile 根据 allBlockCtx 创建文件
         *
         * 注：
         * 1. 除了最后一个 block 外， 其他 block 的大小必须为 4M
         * 2. block 中所有的 chunk size 总和必须和 block size 相同
         * 3. 同一个 block 中的块上传需要依赖该块中上一次上传的返回的 ctx, 所以同一个块的上传无法实现并发，
         *    如果想实现并发，可以使一个 block 中仅包含一个 chunk, 也即 chunk size = 4M, make block 接口
         *    不依赖 ctx，可以实现并发；需要注意的一点是 ctx 的顺序必须与 block 在文件中的顺序一致。
         */
        int defaultBlockSize = 1024 * 1024 * 4;
        int defaultChunkSize = 1024 * 1024 * 2;

        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(f, "r");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fileName = "java_api_v1.zip";
        String token = getUpToken();

        Configuration configuration = new Configuration();
        Client client = new Client(configuration);

        List<String> allBlockCtx = new ArrayList<>();
        long blockOffset = 0; // 块在文件中的偏移量

        // 1. 上传文件
        while (blockOffset < fileSize) {

            // 1.1 初始化将要上传块的信息
            // block 大小: 保证 block 大小除最后一块外，其他均为 4M, 最后一块大小 <= 4M
            long blockSize = fileSize - blockOffset;
            if (blockSize > defaultBlockSize) {
                blockSize = defaultBlockSize;
            }

            String blockLastCtx = ""; // 当前上传块上次上传操作返回的 ctx
            long chunkOffset = 0; // 片在块中的偏移量

            // 1.2 上传 block 数据
            while (chunkOffset < blockSize) {
                // 1.2.1 读取片数据
                long chunkSize = fileSize - blockOffset - chunkOffset;
                if (chunkSize > defaultChunkSize) {
                    chunkSize = defaultChunkSize;
                }
                byte[] chunkData = getUploadData(file, blockOffset + chunkOffset, (int) chunkSize);

                // 1.2.2 上传块
                if (chunkOffset == 0) {
                    // 1.2.2.1 块中第一片，采用 make block 接口
                    ApiUploadV1MakeBlock makeBlockApi = new ApiUploadV1MakeBlock(client);
                    ApiUploadV1MakeBlock.Request makeBlockRequest = new ApiUploadV1MakeBlock.Request(urlPrefix, token, (int) blockSize);
                    makeBlockRequest.setFirstChunkData(chunkData, 0, (int) chunkSize, null);
                    try {
                        ApiUploadV1MakeBlock.Response makeBlockResponse = makeBlockApi.request(makeBlockRequest);
                        blockLastCtx = makeBlockResponse.getCtx();
                        System.out.println("make block:" + makeBlockResponse.getResponse());
                    } catch (QiniuException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 1.2.2.2 非块中第一片，采用 make block 接口
                    ApiUploadV1PutChunk putChunkApi = new ApiUploadV1PutChunk(client);
                    ApiUploadV1PutChunk.Request putChunkRequest = new ApiUploadV1PutChunk.Request(urlPrefix, token, blockLastCtx, (int) chunkOffset);
                    putChunkRequest.setChunkData(chunkData, 0, (int) chunkSize, null);
                    try {
                        ApiUploadV1PutChunk.Response putChunkResponse = putChunkApi.request(putChunkRequest);
                        blockLastCtx = putChunkResponse.getCtx();
                        System.out.println("put data:" + putChunkResponse.getResponse());
                    } catch (QiniuException e) {
                        e.printStackTrace();
                    }
                }
                chunkOffset += chunkSize;
            }

            allBlockCtx.add(blockLastCtx);
            blockOffset += blockSize;
        }

        // 2 组装文件
        String fooKey = "foo";
        String fooValue = "foo-Value";
        Map<String, Object> customParam = new HashMap<>();
        customParam.put("x:foo", fooValue);
        ApiUploadV1MakeFile makeFileApi = new ApiUploadV1MakeFile(client);
        ApiUploadV1MakeFile.Request makeFileRequest = new ApiUploadV1MakeFile.Request(urlPrefix, token, fileSize, allBlockCtx.toArray(new String[0]))
                .setKey(key)
                .setFileName(fileName)
                .setCustomParam(customParam);
        try {
            ApiUploadV1MakeFile.Response makeFileResponse = makeFileApi.request(makeFileRequest);
            System.out.println("make file:" + makeFileResponse.getResponse());
        } catch (QiniuException e) {
            e.printStackTrace();
        }

        try {
            file.close();
        } catch (IOException e) {
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
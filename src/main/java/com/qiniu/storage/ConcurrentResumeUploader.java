package com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.StringMap;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 并发分片上传
 * <p>
 * 分片上传 v1
 * 参考文档：<a href="https://developer.qiniu.com/kodo/7443/shard-to-upload">分片上传</a>
 * <p/>
 * 上传通过将一个文件分割为固定大小的块(4M)，每次上传一个块的内容（服务端只分块，没有分片）。
 * 等待所有块都上传完成之后，再将这些块拼接起来，构成一个完整的文件。
 * <p/>
 * <p>
 * 分片上传 v2
 * 参考文档：<a href="https://developer.qiniu.com/kodo/6364/multipartupload-interface">分片上传</a>
 * <p/>
 * 上传通过将一个文件分割为固定大小的块(大小可配置，通过 Configuration.resumableUploadAPIV2BlockSize)，每次上传一个块的内容。
 * 等待所有块都上传完成之后，再将这些块拼接起来，构成一个完整的文件。
 * <p/>
 * <p>
 * 另外分片上传还支持纪录上传进度，如果本次上传被暂停，那么下次还可以从上次
 * 上次完成的文件偏移位置，继续开始上传，这样就实现了断点续传功能。
 * <p>
 * 服务端网络较稳定，较大文件（如500M以上）才需要将块记录保存下来。
 * 小文件没有必要，可以有效地实现大文件的上传。
 */
public class ConcurrentResumeUploader extends ResumeUploader {

    /**
     * 构建分片上传文件的对象【兼容老版本】
     * 分片上传时，每个上传操作会占用 blockSize 大小内存，blockSize 也即分片大小，
     * 在分片 v1 中 blockSize 为 4M；
     * 分片 v2 可自定义 blockSize，定义方式为：Configuration.resumableUploadAPIV2BlockSize，范围为：1M ~ 1GB，分片 v2 需要注意每个文件最大分片数量为 10000；
     * 当采用并发分片时，占用内存大小和当时启用并发任务数量有关，即：blockSize * 并发数量，
     * 并发任务数量配置方式：Configuration.resumableUploadMaxConcurrentTaskCount
     * <p>
     * 支持分片上传 v1/v2，支持断点续传，支持并发
     *
     * @param client        上传 client【必须】
     * @param upToken       上传凭证【必须】
     * @param key           文件保存名称【可选】
     * @param file          文件【必须】
     * @param params        自定义参数【可选】
     *                      自定义文件 metadata 信息，key 需要增加前缀 x-qn-meta- ：如 params.put("x-qn-meta-key", "foo")
     *                      用户自定义变量，key 需要增加前缀 x: ：如 params.put("x:foo", "foo")
     * @param mime          文件 mime type【可选】
     * @param recorder      断点续传信息记录对象【可选】
     * @param configuration 上传配置信息【必须】
     */
    public ConcurrentResumeUploader(Client client, String upToken, String key, File file, StringMap params, String mime,
                                    Recorder recorder, Configuration configuration) {
        super(client, upToken, key, file, params, mime, recorder, configuration);
    }

    /**
     * 构建分片上传文件流的对象【兼容老版本】
     * 分片上传时，每个上传操作会占用 blockSize 大小内存，blockSize 也即分片大小，
     * 在分片 v1 中 blockSize 为 4M；
     * 分片 v2 可自定义 blockSize，定义方式为：Configuration.resumableUploadAPIV2BlockSize，范围为：1M ~ 1GB，分片 v2 需要注意每个文件最大分片数量为 10000；
     * 当采用并发分片时，占用内存大小和当时启用并发任务数量有关，即：blockSize * 并发数量，
     * 并发任务数量配置方式：Configuration.resumableUploadMaxConcurrentTaskCount
     * <p>
     * 支持分片上传 v1/v2，支持并发
     * 不支持断点续传，不支持定义file name
     *
     * @param client        上传 client 【必须】
     * @param upToken       上传凭证 【必须】
     * @param key           文件保存名称 【可选】
     * @param stream        文件流 【必须】
     * @param params        自定义参数【可选】
     *                      自定义文件 metadata 信息，key 需要增加前缀 x-qn-meta- ：如 params.put("x-qn-meta-key", "foo")
     *                      用户自定义变量，key 需要增加前缀 x: ：如 params.put("x:foo", "foo")
     * @param mime          文件 mime type【可选】
     * @param configuration 上传配置信息 【必须】
     */
    public ConcurrentResumeUploader(Client client, String upToken, String key, InputStream stream,
                                    StringMap params, String mime, Configuration configuration) {
        super(client, upToken, key, stream, null, params, mime, configuration);
    }

    /**
     * 构建分片上传文件流的对象
     * 分片上传时，每个上传操作会占用 blockSize 大小内存，blockSize 也即分片大小，
     * 在分片 v1 中 blockSize 为 4M；
     * 分片 v2 可自定义 blockSize，定义方式为：Configuration.resumableUploadAPIV2BlockSize，范围为：1M ~ 1GB，分片 v2 需要注意每个文件最大分片数量为 10000；
     * 当采用并发分片时，占用内存大小和当时启用并发任务数量有关，即：blockSize * 并发数量，
     * 并发任务数量配置方式：Configuration.resumableUploadMaxConcurrentTaskCount
     * <p>
     * 支持分片上传 v1/v2，支持并发，支持定义file name
     * 不支持断点续传
     *
     * @param client        上传 client 【必须】
     * @param upToken       上传凭证 【必须】
     * @param key           文件保存名称 【可选】
     * @param stream        文件流 【必须】
     * @param fileName      文件名 【可选】
     * @param params        自定义参数【可选】
     *                      自定义文件 metadata 信息，key 需要增加前缀 x-qn-meta- ：如 params.put("x-qn-meta-key", "foo")
     *                      用户自定义变量，key 需要增加前缀 x: ：如 params.put("x:foo", "foo")
     * @param mime          文件 mime type 【可选】
     * @param configuration 上传配置信息 【必须】
     */
    public ConcurrentResumeUploader(Client client, String upToken, String key, InputStream stream,
                                    String fileName, StringMap params, String mime, Configuration configuration) {
        super(client, upToken, key, stream, fileName, params, mime, configuration);
    }

    @Override
    Response uploadData() throws QiniuException {
        // 处理参数
        int maxConcurrentTaskCount = config.resumableUploadMaxConcurrentTaskCount;
        ExecutorService pool = config.resumableUploadConcurrentTaskExecutorService;

        if (maxConcurrentTaskCount < 1) {
            maxConcurrentTaskCount = 1;
        }

        // 外部传入 pool 不 shutdown，内部创建需要 shutdown
        boolean needPollShutdown = false;
        if (pool == null) {
            needPollShutdown = true;
            pool = Executors.newFixedThreadPool(maxConcurrentTaskCount);
        }

        try {
            return uploadDataWithPool(pool, maxConcurrentTaskCount);
        } finally {
            if (needPollShutdown) {
                pool.shutdown();
            }
        }
    }

    private Response uploadDataWithPool(ExecutorService pool, int maxConcurrentTaskCount) throws QiniuException {

        // 开启并发任务
        List<Future<Response>> futures = new ArrayList<>();
        for (int i = 0; i < maxConcurrentTaskCount; i++) {
            Future<Response> future = pool.submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return ConcurrentResumeUploader.super.uploadData();
                }
            });
            futures.add(future);
        }

        // 等待所有并发任务完成
        Response response = null;
        QiniuException exception = null;
        for (Future<Response> future : futures) {
            try {
                Response responseP = future.get();
                if (response == null || (responseP != null && responseP.isOK())) {
                    response = responseP;
                }
            } catch (Exception e) {
                exception = new QiniuException(e);
            }
        }

        // 所有块上传完成说明上传成功
        if (uploadPerformer.isAllBlocksUploaded()) {
            return response;
        }

        // 未完成 如果有异常则抛出异常，理论上未完成必定有异常
        if (exception != null) {
            throw exception;
        }

        return response;
    }
}

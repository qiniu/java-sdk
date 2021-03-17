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

public class ConcurrentResumeUploader extends ResumeUploader {

    public ConcurrentResumeUploader(Client client, String upToken, String key, File file, StringMap params, String mime,
                                    Recorder recorder, Configuration configuration) {
        super(client, upToken, key, file, params, mime, recorder, configuration);
    }

    public ConcurrentResumeUploader(Client client, String upToken, String key, InputStream stream,
                                    StringMap params, String mime, Configuration configuration) {
        super(client, upToken, key, stream, null, params, mime, configuration);
    }

    public ConcurrentResumeUploader(Client client, String upToken, String key, InputStream stream,
                                    String fileName, StringMap params, String mime, Configuration configuration) {
        super(client, upToken, key, stream, fileName, params, mime, configuration);
    }

    @Override
    Response uploadData() throws QiniuException {

        int maxRunningBlock = config.resumeMaxConcurrentTaskCount;
        ExecutorService pool = config.resumeConcurrentTaskExecutorService;

        if (maxRunningBlock < 1) {
            maxRunningBlock = 1;
        }
        if (pool == null) {
            pool = Executors.newFixedThreadPool(maxRunningBlock);
        }

        List<Future<Response>> futures = new ArrayList<>();
        for (int i = 0; i < maxRunningBlock; i++) {
            Future<Response> future = pool.submit(new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return ConcurrentResumeUploader.super.uploadData();
                }
            });
            futures.add(future);
        }

        Response response = null;
        QiniuException exception = null;
        for (Future<Response> future : futures) {
            while (!future.isDone()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }

            try {
                response = future.get();
            } catch (Exception e) {
                exception = new QiniuException(e);
            }
        }

        if (exception != null) {
            throw exception;
        }

        return response;
    }
}

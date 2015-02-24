package com.qiniu;

import java.util.concurrent.CountDownLatch;


public class HttpTest {
    final CountDownLatch signal = new CountDownLatch(1);
//    private HttpManager httpManager;
//    private ResponseInfo info;
//    private JSONObject resp;
//
//    @Override
//    protected void setUp() throws Exception {
//        httpManager = new HttpManager();
//    }
//
//    @SmallTest
//    public void testPost1() throws Throwable {
//
//        httpManager.postData("http://www.baidu.com", "hello".getBytes(), null, null, new CompletionHandler() {
//            @Override
//            public void complete(ResponseInfo rinfo, JSONObject response) {
//                Log.d("qiniutest", rinfo.toString());
//                info = rinfo;
//                signal.countDown();
//            }
//        });
//
//        try {
//            signal.await(60, TimeUnit.SECONDS); // wait for callback
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertNull(info.reqId);
//    }
//
//    @SmallTest
//    public void testPost2() throws Throwable {
//
//        httpManager.postData("http://up.qiniu.com", "hello".getBytes(), null, null, new CompletionHandler() {
//            @Override
//            public void complete(ResponseInfo rinfo, JSONObject response) {
//                Log.d("qiniutest", rinfo.toString());
//                info = rinfo;
//                signal.countDown();
//            }
//        });
//
//        try {
//            signal.await(60, TimeUnit.SECONDS); // wait for callback
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertNotNull(info.reqId);
//    }
//
//    @SmallTest
//    public void testPost3() throws Throwable {
//        runTestOnUiThread(new Runnable() { // THIS IS THE KEY TO SUCCESS
//            public void run() {
//                httpManager.postData("http://httpbin.org/status/500", "hello".getBytes(), null, null, new CompletionHandler() {
//                    @Override
//                    public void complete(ResponseInfo rinfo, JSONObject response) {
//                        Log.d("qiniutest", rinfo.toString());
//                        info = rinfo;
//                        signal.countDown();
//                    }
//                });
//            }
//        });
//
//        try {
//            signal.await(60, TimeUnit.SECONDS); // wait for callback
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals(500, info.statusCode);
//        Assert.assertNotNull(info.error);
//    }
//
//    @SmallTest
//    public void testPost4() throws Throwable {
//        runTestOnUiThread(new Runnable() { // THIS IS THE KEY TO SUCCESS
//            public void run() {
//                httpManager.postData("http://httpbin.org/status/418", "hello".getBytes(), null, null, new CompletionHandler() {
//                    @Override
//                    public void complete(ResponseInfo rinfo, JSONObject response) {
//                        Log.d("qiniutest", rinfo.toString());
//                        info = rinfo;
//                        signal.countDown();
//                    }
//                });
//            }
//        });
//
//        try {
//            signal.await(60, TimeUnit.SECONDS); // wait for callback
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals(418, info.statusCode);
//        Assert.assertNotNull(info.error);
//    }
}

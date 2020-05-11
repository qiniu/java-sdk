package com.qiniu.storage;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.util.Auth;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.ResCode;
import test.com.qiniu.TestConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RegionTest {

    @Test
    public void testChangeHost1() throws QiniuException {
        System.out.println("\n\n1 Zone.autoZone()");
        Configuration cfg = new Configuration(Zone.autoZone());
        testChangeHost(cfg);
    }

    @Test
    public void testChangeHost2() throws QiniuException {
        System.out.println("\n\n2 ''");
        Configuration cfg = new Configuration();
        cfg.useHttpsDomains = false;
        testChangeHost(cfg);
    }

    @Test
    public void testChangeHost3() throws QiniuException {
        System.out.println("\n\n3 Region.autoRegion()");
        Configuration cfg = new Configuration(Region.autoRegion());
        testChangeHost(cfg);
    }

    @Test
    public void testChangeHost4() throws QiniuException {
        System.out.println("\n\n4 Region.region0()");
        Configuration cfg = new Configuration(Region.region0());
        testChangeHost(cfg);
    }

    @Test
    public void testChangeHost5() throws QiniuException {
        System.out.println("\n\n5 Zone.zone0()");
        Configuration cfg = new Configuration(Zone.zone0());
        testChangeHost(cfg);
    }


    @Test
    public void testChangeHost6() throws QiniuException {
        System.out.println("\n\n6 Region.region1(), accUpHostFirst = false");
        Configuration cfg = new Configuration(Region.region1());
        cfg.accUpHostFirst = false;
        testChangeHost(cfg);
    }

    @Test
    public void testChangeHost7() throws QiniuException {
        System.out.println("\n\n7 Zone.zone1(), accUpHostFirst = false");
        Configuration cfg = new Configuration(Zone.zone1());
        cfg.accUpHostFirst = false;
        testChangeHost(cfg);
    }

    public void testChangeHost(Configuration cfg0) throws QiniuException {
        Auth auth = Auth.create(TestConfig.testAccessKey, TestConfig.testSecretKey);
        ConfigHelper cfg = new ConfigHelper(cfg0);
        String h1 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0));
        System.out.println("h1\t" + h1);
        String h1_ = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0));
        System.out.println(h1_);
        String h2 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_na0)); // na0
        System.out.println("h2 auto region na0\t" + h2);
        String h3 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h1);
        System.out.println(h3);
        String h2_ = cfg.upHost(auth.uploadToken(TestConfig.testBucket_na0)); // na0
        System.out.println("h2_ auto region na0\t" + h2_);
        String h3_ = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0));
        System.out.println("h3_ \t" + h3_);
        String h4 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h3);
        System.out.println(h4);
        String h5 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h4);
        System.out.println("h5\t" + h5);
        String h6 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h5);
        System.out.println(h6);

        String h7 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0));
        System.out.println(h7);

        String h7_ = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0));
        System.out.println(h7_);

        String h8 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h7);
        System.out.println(h8);
        String h9 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h8);
        System.out.println(h9);
        String h10 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h9);
        System.out.println(h10);
        String h11 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h10);
        System.out.println(h11);
        String h12 = cfg.tryChangeUpHost(auth.uploadToken(TestConfig.testBucket_z0), h11);
        System.out.println(h12);

        Assert.assertEquals(h1, h1_);
        if (cfg0.region instanceof AutoRegion) {
            Assert.assertNotEquals(h1, h2); // 不同region //
            Assert.assertNotEquals(h1, h3_); // 切回 region 后，继续保持状态 //
        }

        Assert.assertNotEquals(h3, h1);
        Assert.assertNotEquals(h3, h2);
        Assert.assertNotEquals(h3, h4);
        Assert.assertNotEquals(h5, h6);

        Assert.assertEquals(h7, h6);
        Assert.assertEquals(h7, h7_);

        // upload.qiniup.com,   up.qiniup.com
        Assert.assertNotEquals("" + h1.indexOf("up.") + h1.indexOf("up-"),
                "" + h5.indexOf("up.") + h5.indexOf("up-"));
        //
        Assert.assertTrue(h1.equals(h4) || h1.equals(h5) || h1.equals(h6) || h1.equals(h7)
                || h1.equals(h8) || h1.equals(h9) || h1.equals(h10) || h1.equals(h11) || h1.equals(h12));
    }


    @Test
    public void testGetFailedUpHost() throws QiniuException {
        Configuration cfg0 = new Configuration();
        ConfigHelper configHelper = new ConfigHelper(cfg0);
        Auth auth = Auth.create(TestConfig.testAccessKey, TestConfig.testSecretKey);
        String upToken = auth.uploadToken(TestConfig.testBucket_z0 + "notexitbucket38_-4rfjiu4r3u4t83d");
        try {
            String h1 = configHelper.upHost(upToken);
            Assert.fail("should failed: no such bucket: ");
        } catch (QiniuException e) {
            Assert.assertTrue(ResCode.find(e.code(), ResCode.getPossibleResCode(631)));
        }
    }


    @Test
    public void testChangeHostPeriod() throws QiniuException {
        Configuration cfg0 = new Configuration();
        UpHostHelper helper = new UpHostHelper(cfg0, 20);
        Auth auth = Auth.create(TestConfig.testAccessKey, TestConfig.testSecretKey);
        String upToken = auth.uploadToken(TestConfig.testBucket_z0);

        cfg0.upHost(auth.uploadToken(TestConfig.testBucket_z0)); // make sure there region is not null

        String h1 = helper.upHost(cfg0.region, upToken, null, false, false);
        System.out.println("h1\t" + h1);
        String h2 = helper.upHost(cfg0.region, upToken, h1, false, false);
        System.out.println(h2);
        String h3 = helper.upHost(cfg0.region, upToken, h2, true, false);
        System.out.println(h3);
//        String h4 = helper.upHost(cfg0.region, upToken, true);
//        System.out.println(h4);
        String h5 = helper.upHost(cfg0.region, upToken, h3, true, false);
        System.out.println("h5\t" + h5);
        String h6 = helper.upHost(cfg0.region, upToken, h5, true, false);
        System.out.println(h6);
        try {
            Thread.sleep(21 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String h7 = helper.upHost(cfg0.region, upToken, h6, false, false);
        System.out.println("h7\t" + h7);

        String h8 = helper.upHost(cfg0.region, upToken, h7, true, false);
        System.out.println(h8);
        try {
            Thread.sleep(21 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String h9 = helper.upHost(cfg0.region, upToken, h8, true, false);
        System.out.println("h9\t" + h9);
        String h10 = helper.upHost(cfg0.region, upToken, h9, true, false);
        System.out.println(h10);
        try {
            Thread.sleep(11 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String h11 = helper.upHost(cfg0.region, upToken, h10, true, false);
        System.out.println("h11\t" + h11);
        String h12 = helper.upHost(cfg0.region, upToken, h11, false, false);
        System.out.println(h12);

        Assert.assertEquals(h1, h2);

        Assert.assertNotEquals(h3, h2);
//        Assert.assertNotEquals(h3, h4);
        Assert.assertNotEquals(h5, h6);

        // 标记过期，重来 //
        Assert.assertEquals(h1, h7);
        Assert.assertEquals(h1, h9);

        // 均过期 //
        Assert.assertEquals(h8, h3);

        Assert.assertNotEquals(h11, h1);

        // upload.qiniup.com,   up.qiniup.com, upload-xs.qiniup.com
        Assert.assertNotEquals("" + h1.indexOf("up.") + h1.indexOf("up-"),
                "" + h5.indexOf("up.") + h5.indexOf("up-"));
    }

    @Test
    public void testZoneToRegion() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, QiniuException {
        Configuration cfg = new Configuration(Zone.zone0());
        ConfigHelper c = new ConfigHelper(cfg);
        String m = "makeSureRegion";

        System.out.println(cfg.region);
//        System.out.println(cfg.zone);

        Class clazz = c.getClass();
        Method m1 = clazz.getDeclaredMethod(m);
        m1.setAccessible(true);
        m1.invoke(c);
        Region rz = cfg.region;
        System.out.println("cfg.region : " + new Gson().toJson(cfg.region));
//        System.out.println(new Gson().toJson(cfg.zone));

        Region r0 = Region.region0();
        System.out.println("Region.region0() : " + new Gson().toJson(r0));

        Assert.assertTrue(r0.getSrcUpHost(null).contains(rz.getSrcUpHost(null).get(0)));
        Assert.assertTrue(r0.getAccUpHost(null).contains(rz.getAccUpHost(null).get(0)));
        Assert.assertEquals(r0.getIovipHost(null), rz.getIovipHost(null));
        Assert.assertEquals(r0.getRsHost(null), rz.getRsHost(null));
        Assert.assertEquals(r0.getRsfHost(null), rz.getRsfHost(null));
        Assert.assertEquals(r0.getApiHost(null), rz.getApiHost(null));
    }
}

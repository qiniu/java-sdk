package com.qiniu.storage;

import com.qiniu.common.*;
import com.qiniu.util.Auth;
import org.junit.Assert;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import java.util.List;

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
        System.out.println("\n\n6 Region.region1(), useAccUpHost = false");
        Configuration cfg = new Configuration(Region.region1());
        cfg.useAccUpHost = false;
        testChangeHost(cfg);
    }

    @Test
    public void testChangeHost7() throws QiniuException {
        System.out.println("\n\n7 Zone.zone1(), useAccUpHost = false");
        Configuration cfg = new Configuration(Zone.zone1());
        cfg.useAccUpHost = false;
        testChangeHost(cfg);
    }

    public void testChangeHost(Configuration cfg) throws QiniuException {
        Auth auth = Auth.create(TestConfig.testAccessKey, TestConfig.testSecretKey);
        String h1 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0));
        System.out.println("h1\t" + h1);
        String h1_ = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0));
        System.out.println(h1_);
        String h2 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_na0));
        System.out.println(h2);
        String h3 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println(h3);
        String h4 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println(h4);
        String h5 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println("h5\t" + h5);
        String h6 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println(h6);

        String h7 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), false);
        System.out.println(h7);

        String h8 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println(h8);
        String h9 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println(h9);
        String h10 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println(h10);
        String h11 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), true);
        System.out.println(h11);
        String h12 = cfg.upHost(auth.uploadToken(TestConfig.testBucket_z0), false);
        System.out.println(h12);

        Assert.assertEquals(h1, h1_);
        if (cfg.region instanceof AutoRegion) {
            Assert.assertNotEquals(h1, h2);
        }

        Assert.assertNotEquals(h3, h1);
        Assert.assertNotEquals(h3, h2);
        Assert.assertNotEquals(h3, h4);
        Assert.assertNotEquals(h5, h6);

        Assert.assertEquals(h7, h6);

        // upload.qiniup.com,   up.qiniup.com
        Assert.assertNotEquals("" + h1.indexOf("up.") + h1.indexOf("up-"),
                "" + h5.indexOf("up.") + h5.indexOf("up-"));
        //
        Assert.assertTrue(h1.equals(h4) || h1.equals(h5) || h1.equals(h6) || h1.equals(h7)
                || h1.equals(h8) || h1.equals(h9) || h1.equals(h10) || h1.equals(h11) || h1.equals(h12));
    }


    @Test
    public void testChangeHostPeriod() throws QiniuException {
        Configuration cfg0 = new Configuration();
        UpHostHelper helper = new UpHostHelper(cfg0, 20);
        Auth auth = Auth.create(TestConfig.testAccessKey, TestConfig.testSecretKey);
        RegionReqInfo regionReqInfo = new RegionReqInfo(auth.uploadToken(TestConfig.testBucket_z0));
        cfg0.upHost(auth.uploadToken(TestConfig.testBucket_z0)); // make sure there region is not null
        List<String> accUpHosts = cfg0.region.getAccUpHost(regionReqInfo);
        List<String> srcUpHosts = cfg0.region.getSrcUpHost(regionReqInfo);

        String h1 = helper.upHost(accUpHosts, srcUpHosts, false);
        System.out.println("h1\t" + h1);
        String h2 = helper.upHost(accUpHosts, srcUpHosts, false);
        System.out.println(h2);
        String h3 = helper.upHost(accUpHosts, srcUpHosts, true);
        System.out.println(h3);
//        String h4 = helper.upHost(accUpHosts, srcUpHosts, true);
//        System.out.println(h4);
        String h5 = helper.upHost(accUpHosts, srcUpHosts, true);
        System.out.println("h5\t" + h5);
        String h6 = helper.upHost(accUpHosts, srcUpHosts, true);
        System.out.println(h6);
        try {
            Thread.sleep(21 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String h7 = helper.upHost(accUpHosts, srcUpHosts, false);
        System.out.println("h7\t" + h7);

        String h8 = helper.upHost(accUpHosts, srcUpHosts, true);
        System.out.println(h8);
        try {
            Thread.sleep(21 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String h9 = helper.upHost(accUpHosts, srcUpHosts, true);
        System.out.println("h9\t" + h9);
        String h10 = helper.upHost(accUpHosts, srcUpHosts, true);
        System.out.println(h10);
        try {
            Thread.sleep(11 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String h11 = helper.upHost(accUpHosts, srcUpHosts, true);
        System.out.println("h11\t" + h11);
        String h12 = helper.upHost(accUpHosts, srcUpHosts, false);
        System.out.println(h12);

        Assert.assertEquals(h1, h2);

        Assert.assertNotEquals(h3, h2);
//        Assert.assertNotEquals(h3, h4);
        Assert.assertNotEquals(h5, h6);

        // 标记过期，重来 //
        Assert.assertEquals(h7, h1);
        Assert.assertEquals(h9, h1);

        // 均过期 //
        Assert.assertEquals(h8, h3);

        Assert.assertNotEquals(h11, h1);

        // upload.qiniup.com,   up.qiniup.com, upload-xs.qiniup.com
        Assert.assertNotEquals("" + h1.indexOf("up.") + h1.indexOf("up-"),
                "" + h5.indexOf("up.") + h5.indexOf("up-"));
    }


}

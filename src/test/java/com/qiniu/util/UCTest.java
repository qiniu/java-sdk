package com.qiniu.util;

import com.qiniu.TestConfig;
import com.qiniu.common.Config;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Simon on 6/22/16.
 */
public class UCTest {
    private String ak = TestConfig.ak;
    private String bkt = TestConfig.bucket;

    @Test
    public void testHttp() {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = false;
            Zone zone = UC.zone(ak, bkt);
            assertEquals(zone.upHost, "http://up.qiniu.com");
            assertEquals(zone.upHostBackup, "http://upload.qiniu.com");
        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
            Assert.fail();
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
        }
    }

    @Test
    public void testHttps() {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = true;
            Zone zone = UC.zone(ak, bkt);
            assertEquals(zone.upHost, "https://up.qbox.me");
            assertEquals(zone.upHostBackup, "https://up.qbox.me");
        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
            Assert.fail();
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
        }
    }

    @Test
    public void testHttpFail() {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = false;
            Zone zone = UC.zone(ak + "_not_be_ak", bkt);
            Assert.fail();
        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
            Assert.assertEquals(e.code(), 612);
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
        }
    }

    @Test
    public void testE() {
        Assert.assertEquals(new Zone("upHost", "upHostBackup"), new Zone("upH" + "ost", "upHost" + "Backup"));
    }

    @Test
    public void testSplitE() {
        String s1 = "bkt:key";
        String s2 = "bkt";
        Assert.assertEquals(s1.split(":")[0], s2.split(":")[0]);
    }

    @Test
    public void testC1() {
        boolean h = Config.UPLOAD_BY_HTTPS;
        try {
            Config.UPLOAD_BY_HTTPS = false;
            final Zone zone0 = UC.zone(ak, bkt);
            System.out.println("zone0: " + zone0.toString());


            new Thread() {
                public void run() {
                    boolean h = Config.UPLOAD_BY_HTTPS;
                    try {
                        Config.UPLOAD_BY_HTTPS = false;
                        Zone zone1 = UC.zone(ak, bkt);
                        System.out.println("zone1: " + zone1.toString());
                        Assert.assertEquals(zone1, zone0);
                    } catch (QiniuException e) {
                        e.printStackTrace();
                        System.out.println(e.response.url());
                        System.out.println(e.response.toString());
                        Assert.fail();
                    } finally {
                        Config.UPLOAD_BY_HTTPS = h;
                    }
                }
            }.start();

            new Thread() {
                public void run() {
                    boolean h = Config.UPLOAD_BY_HTTPS;
                    try {
                        Config.UPLOAD_BY_HTTPS = false;
                        Zone zone2 = UC.zone(ak, bkt);
                        System.out.println("zone2: " + zone2.toString());
                        Assert.assertEquals(zone2, zone0);
                    } catch (QiniuException e) {
                        e.printStackTrace();
                        System.out.println(e.response.url());
                        System.out.println(e.response.toString());
                        Assert.fail();
                    } finally {
                        Config.UPLOAD_BY_HTTPS = h;
                    }
                }
            }.start();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Thread() {
                public void run() {
                    boolean h = Config.UPLOAD_BY_HTTPS;
                    try {
                        Config.UPLOAD_BY_HTTPS = false;
                        Zone zone3 = UC.zone(ak, bkt);
                        System.out.println("zone3: " + zone3.toString());
                        Assert.assertEquals(zone3, zone0);
                    } catch (QiniuException e) {
                        e.printStackTrace();
                        System.out.println(e.response.url());
                        System.out.println(e.response.toString());
                        Assert.fail();
                    } finally {
                        Config.UPLOAD_BY_HTTPS = h;
                    }
                }
            }.start();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
            Assert.fail();
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
            UC.clear();
        }
    }

    @Test
    public void testC2() {
        final Zone[] zones = new Zone[5];
        boolean h = Config.UPLOAD_BY_HTTPS;
        new Thread() {
            public void run() {
                boolean h = Config.UPLOAD_BY_HTTPS;
                try {
                    Config.UPLOAD_BY_HTTPS = false;
                    Zone zone0 = UC.zone(ak, bkt);
                    System.out.println("zone0: " + zone0.toString());
                    zones[0] = zone0;
                } catch (QiniuException e) {
                    e.printStackTrace();
                    System.out.println(e.response.url());
                    System.out.println(e.response.toString());
                    Assert.fail();
                } finally {
                    Config.UPLOAD_BY_HTTPS = h;
                }
            }
        }.start();

        new Thread() {
            public void run() {
                boolean h = Config.UPLOAD_BY_HTTPS;
                try {
                    Config.UPLOAD_BY_HTTPS = false;
                    Zone zone1 = UC.zone(ak, bkt);
                    System.out.println("zone1: " + zone1.toString());
                    zones[1] = zone1;
                } catch (QiniuException e) {
                    e.printStackTrace();
                    System.out.println(e.response.url());
                    System.out.println(e.response.toString());
                    Assert.fail();
                } finally {
                    Config.UPLOAD_BY_HTTPS = h;
                }
            }
        }.start();

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread() {
            public void run() {
                boolean h = Config.UPLOAD_BY_HTTPS;
                try {
                    Config.UPLOAD_BY_HTTPS = false;
                    Zone zone2 = UC.zone(ak, bkt);
                    System.out.println("zone2: " + zone2.toString());
                    zones[2] = zone2;
                } catch (QiniuException e) {
                    e.printStackTrace();
                    System.out.println(e.response.url());
                    System.out.println(e.response.toString());
                    Assert.fail();
                } finally {
                    Config.UPLOAD_BY_HTTPS = h;
                }
            }
        }.start();

        new Thread() {
            public void run() {
                boolean h = Config.UPLOAD_BY_HTTPS;
                try {
                    Config.UPLOAD_BY_HTTPS = false;
                    Zone zone3 = UC.zone(ak, bkt);
                    System.out.println("zone3: " + zone3.toString());
                    zones[3] = zone3;
                } catch (QiniuException e) {
                    e.printStackTrace();
                    System.out.println(e.response.url());
                    System.out.println(e.response.toString());
                    Assert.fail();
                } finally {
                    Config.UPLOAD_BY_HTTPS = h;
                }
            }
        }.start();

        try {
            Config.UPLOAD_BY_HTTPS = false;
            Zone zone4 = UC.zone(ak, bkt);
            System.out.println("zone4: " + zone4.toString());
            zones[4] = zone4;
        } catch (QiniuException e) {
            e.printStackTrace();
            System.out.println(e.response.url());
            System.out.println(e.response.toString());
            Assert.fail();
        } finally {
            Config.UPLOAD_BY_HTTPS = h;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Zone z : zones) {
            assertEquals(zones[0], z);
        }
        UC.clear();
    }

}

package com.qiniu.processing;

import com.qiniu.processing.util.GeneralOp;
import com.qiniu.processing.util.Command;
import com.qiniu.processing.util.SaveAsOp;
import com.qiniu.util.UrlSafeBase64;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by Simon on 2015/4/1.
 */
public class FopHelperTest {
    @Test
    public void exif() {
        String cmd = "exif";
        GeneralOp op = new GeneralOp("exif");
        String gcmd = op.build();
        String fcmd = FopHelper.genFop(op);

        assertAllEquals(cmd, gcmd, fcmd);
    }

    @Test
    public void imageView2() {
        String cmd = "imageView2/2/w/200/h/250";
        GeneralOp op = new GeneralOp("imageView2", 2).set("w", 200).set("h", 250);
        String gcmd = op.build();
        String fcmd = FopHelper.genFop(op);

        assertAllEquals(cmd, gcmd, fcmd);
    }

    @Test
    public void imageView2Saveas() {
        String bucket = "mybucket";
        String key = "onkey" + UUID.randomUUID();
        String cmd = "imageView2/2/w/200/h/250";
        cmd += "|saveas/" + UrlSafeBase64.encodeToString(bucket + ":" + key);
        GeneralOp op = new GeneralOp("imageView2", 2).set("w", 200).set("h", 250);
        SaveAsOp sop = new SaveAsOp(bucket, key);
        String fcmd = FopHelper.genFop(op, sop);

        String pcmd = Command.create().append(op).append(sop).toString();

        assertAllEquals(cmd, pcmd, fcmd);
    }

    @Test
    public void vframe() {
        String cmd = "vframe/jpg/offset/3/w/480/h/480";
        GeneralOp op = new GeneralOp("vframe", "jpg").set("offset", 3).set("w", 480).set("h", 480);
        String gcmd = op.build();
        String fcmd = FopHelper.genFop(op);

        assertAllEquals(cmd, gcmd, fcmd);
    }

    @Test
    public void vframeSaveas() {
        String bucket = "mybucket";
        String key = "onkey" + UUID.randomUUID();
        String cmd = "vframe/jpg/offset/3/w/480/h/480";
        cmd += "|saveas/" + UrlSafeBase64.encodeToString(bucket + ":" + key);
        GeneralOp op = new GeneralOp("vframe", "jpg").set("offset", 3).set("w", 480).set("h", 480);
        SaveAsOp sop = new SaveAsOp(bucket, key);
        String fcmd = FopHelper.genFop(op, sop);

        String pcmd = Command.create().append(op).append(sop).toString();

        assertAllEquals(cmd, pcmd, fcmd);
    }

    @Test
    public void avthumb() {
        String cmd = "avthumb/m3u8/segtime/15/ab/192k/vb/1.25m";
        GeneralOp op = new GeneralOp("avthumb", "m3u8").set("segtime", 15)
                .set("ab", "192k").set("vb", "1.25m");
        String gcmd = op.build();
        String fcmd = FopHelper.genFop(op);

        assertAllEquals(cmd, gcmd, fcmd);
    }

    @Test
    public void avthumbSaveas() {
        String bucket = "mybucket";
        String key = "onkey_" + UUID.randomUUID();
        String cmd = "avthumb/m3u8/segtime/15/ab/192k/vb/1.25m";
        cmd += "|saveas/" + UrlSafeBase64.encodeToString(bucket + ":" + key);

        GeneralOp op = new GeneralOp("avthumb", "m3u8").set("segtime", 15)
                .set("ab", "192k").set("vb", "1.25m");

        SaveAsOp sop = new SaveAsOp(bucket, key);

        String pcmd = Command.create().append(op).append(sop).toString();

        String fcmd = FopHelper.genFop(op, sop);

        assertAllEquals(cmd, pcmd, fcmd);
    }


    @Test
    public void avthumbMu() {
        String bucket = "mybucket";
        String key = "onkey_" + UUID.randomUUID();
        String cmd = "avthumb/m3u8/segtime/15/ab/192k/vb/1.25m;avthumb/m3u8/segtime/15/ab/128k/vb/128k";
        cmd += "|saveas/" + UrlSafeBase64.encodeToString(bucket + ":" + key);

        GeneralOp op1 = new GeneralOp("avthumb", "m3u8").set("segtime", 15)
                .set("ab", "192k").set("vb", "1.25m");
        GeneralOp op2 = new GeneralOp("avthumb", "m3u8").set("segtime", 15)
                .set("ab", "128k").set("vb", "128k");

        SaveAsOp sop = new SaveAsOp(bucket, key);

        Command p1 = FopHelper.genCmd(op1);
        Command p2 = FopHelper.genCmd(op2, sop);

        String fcmd = FopHelper.genFops(p1, p2);

        assertAllEquals(cmd, fcmd);
    }

    @Test
    public void mkzip() {
        String url1 = "http://testres.qiniudn.com/gogopher.jpg";
        String alias1 = "gogopher.jpg";
        String url2 = "http://testres.qiniudn.comm/_log/aaa5/2015-03-29/part0.gz";

        String cmd = "mkzip/2/url/" + UrlSafeBase64.encodeToString(url1)
                + "/alias/" + UrlSafeBase64.encodeToString(alias1)
                + "/url/" + UrlSafeBase64.encodeToString(url2);
        GeneralOp op = new GeneralOp("mkzip", "2").set("url", UrlSafeBase64.encodeToString(url1))
                .set("alias", UrlSafeBase64.encodeToString(alias1))
                .set("url", UrlSafeBase64.encodeToString(url2));

        String gcmd = op.build();
        String fcmd = FopHelper.genFop(op);

        assertAllEquals(cmd, gcmd, fcmd);
    }

    @Test
    public void mkzipSaveas() {
        String bucket = "mybucket";
        String key = "onkey_" + UUID.randomUUID();
        String url1 = "http://testres.qiniudn.com/gogopher.jpg";
        String alias1 = "gogopher.jpg";
        String url2 = "http://testres.qiniudn.com/_log/aaa5/2015-03-29/part0.gz";

        String cmd = "mkzip/2/url/" + UrlSafeBase64.encodeToString(url1)
                + "/alias/" + UrlSafeBase64.encodeToString(alias1)
                + "/url/" + UrlSafeBase64.encodeToString(url2)
                + "|saveas/" + UrlSafeBase64.encodeToString(bucket + ":" + key);

        GeneralOp op = new GeneralOp("mkzip", "2").set("url", UrlSafeBase64.encodeToString(url1))
                .set("alias", UrlSafeBase64.encodeToString(alias1))
                .set("url", UrlSafeBase64.encodeToString(url2));

        SaveAsOp sop = new SaveAsOp(bucket, key);

        String pcmd = Command.create().append(op).append(sop).toString();

        String fcmd = FopHelper.genFop(op, sop);


        assertAllEquals(cmd, pcmd, fcmd);
    }

    public static void assertAllEquals(Object... objs){
        try{
            Object f = objs[0];
            for(Object o : objs){
                assertEquals(f, o);
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}

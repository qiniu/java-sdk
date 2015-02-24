package com.qiniu.api.fop;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.net.CallRet;
import com.qiniu.api.net.EncodeUtils;

/**
 * @ClassName: ImageView2
 * @author: mingyu.zhao
 * @date: 14/10/29 下午7:07
 */
public class ImageView2 extends ImageView {
    private String saveAsUri = null;
    private Mac mac = null;

    @Override
    public String makeParams() {
        StringBuilder params = new StringBuilder();
        params.append("/").append(this.mode);
        if (this.width > 0) {
            params.append("/w/").append(this.width);
        }
        if (this.height > 0) {
            params.append("/h/").append(this.height);
        }
        if (this.quality > 0) {
            params.append("/q/").append(this.quality);
        }
        if (this.format != null && this.format != "") {
            params.append("/format/").append(this.format);
        }

        return params.toString();
    }

    public void saveAs(String bucketId, String key) {
        saveAsUri = EncodeUtils.urlsafeEncode(bucketId + ":" + key);
    }

    public void setMac(Mac mac) {
        this.mac = mac;
    }

    @Override
    public String makeRequest(String url) {
        StringBuilder buf = new StringBuilder();
        int pos = url.indexOf("http://");
        if (pos != -1) {
            url = url.substring(pos + "http://".length());
        }
        buf.append(url).append("?imageView2").append(this.makeParams());
        if (saveAsUri != null) {
            //"|savesas"需要提前Url转码
            buf.append("%7Csaveas/").append(saveAsUri);
            String newURL = buf.toString();
            try {
                String sign = mac.sign(newURL.getBytes());
                buf.append("/sign/").append(sign);
            } catch (Exception e) {
                return newURL;
            }

        }
        buf.insert(0, "http://");
        String reqUrl = buf.toString();
        return reqUrl;
    }

    public static void main(String[] args) {
        //DEMO
        Config.ACCESS_KEY = "<access_key>";
        Config.SECRET_KEY = "<secret_key>";

        Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
        ImageView2 iv2 = new ImageView2();
        iv2.mode = 2;
        iv2.width = 200;
        iv2.height = 100;
        iv2.setMac(mac);
        iv2.saveAs("bucketId", "pics/1_new.jpeg");
        CallRet ret = iv2.call("http://bucketId.qiniudn.com/pics/1.jpeg");

    }
}

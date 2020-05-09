package com.qiniu.linking;

import com.qiniu.common.Constants;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.linking.model.*;
import com.qiniu.util.Auth;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;

public class LinkingDeviceManager {


    private final Auth auth;
    private final String host;
    private final Client client;

    public LinkingDeviceManager(Auth auth) {
        this(auth, "http://linking.qiniuapi.com");
    }

    public LinkingDeviceManager(Auth auth, String host) {
        this(auth, host, new Client());
    }

    public LinkingDeviceManager(Auth auth, String host, Client client) {
        this.auth = auth;
        this.host = host;
        this.client = client;
    }

    public void createDevice(String appid, String deviceName) throws QiniuException {
        StringMap params = new StringMap().put("device", deviceName);
        String url = String.format("%s/v1/apps/%s/devices", host, appid);
        Response res = post(url, params);
        res.close();
    }

    public void createDevice(String appid, Device device) throws QiniuException {
        StringMap params = new StringMap();
        params.put("device", device.getDeviceName());
        params.put("segmentExpireDays", device.getSegmentExpireDays());
        params.put("device", device.getDeviceName());
        params.put("segmentExpireDays", device.getSegmentExpireDays());
        params.put("uploadMode", device.getUploadMode());
        params.put("meta", device.getMeta());
        params.put("sdcardRotatePolicy", device.getSdcardRotatePolicy());
        params.put("sdcardRotateValue", device.getSdcardRotateValue());
        params.put("type", device.getType());
        params.put("maxChannel", device.getMaxChannel());
        params.put("channels", device.getChannels());

        String url = String.format("%s/v1/apps/%s/devices", host, appid);
        Response res = post(url, params);
        res.close();
    }

    public void deleteDevice(String appid, String deviceName) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        String url = String.format("%s/v1/apps/%s/devices/%s", host, appid, encodedDeviceName);
        StringMap headers = auth.authorizationV2(url, "DELETE", null, null);
        Response res = client.delete(url, headers);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        res.close();
    }

    public DeviceListing listDevice(String appid, String prefix,
                                    String marker, int limit, boolean online) throws QiniuException {
        StringMap map = new StringMap().putNotEmpty("marker", marker)
                .putNotEmpty("prefix", prefix).putWhen("limit", limit, limit > 0)
                .putWhen("online", online, online);
        String queryString = map.formString();
        if (map.size() > 0) {
            queryString = "?" + queryString;
        }
        String url = String.format("%s/v1/apps/%s/devices%s", host, appid, queryString);
        Response res = get(url);
        DeviceListing ret = res.jsonToObject(DeviceListing.class);
        res.close();
        return ret;
    }

    private Response get(String url) throws QiniuException {
        StringMap headers = auth.authorizationV2(url, "GET", null, null);
        Response res = client.get(url, headers);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    public Device getDevice(String appid, String deviceName) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        String url = String.format("%s/v1/apps/%s/devices/%s", host, appid, encodedDeviceName);
        Response res = get(url);
        Device ret = res.jsonToObject(Device.class);
        res.close();
        return ret;
    }

    public Device getDeviceByAccessKey(String deviceAccessKey) throws QiniuException {
        String url = String.format("%s/v1/keys/%s", host, deviceAccessKey);
        Response res = get(url);
        Device ret = res.jsonToObject(Device.class);
        res.close();
        return ret;
    }

    public Device updateDevice(String appid, String deviceName, PatchOperation[] operations) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        StringMap params = new StringMap().put("operations", operations);
        String url = String.format("%s/v1/apps/%s/devices/%s", host, appid, encodedDeviceName);
        byte[] body = Json.encode(params).getBytes(Constants.UTF_8);
        StringMap headers = auth.authorizationV2(url, "PATCH", body, Client.JsonMime);
        Response res = client.patch(url, body, headers, Client.JsonMime);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        Device ret = res.jsonToObject(Device.class);
        res.close();
        return ret;

    }

    public DeviceHistoryListing listDeviceHistory(String appid, String deviceName,
                                                  long start, long end, String marker,
                                                  int limit) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        StringMap map = new StringMap().putNotEmpty("marker", marker).
                put("start", start).put("end", end).putWhen("limit", limit, limit > 0);
        String queryString = map.formString();
        String url = String.format("%s/v1/apps/%s/devices/%s/historyactivity?%s",
                host, appid, encodedDeviceName, queryString);
        Response res = get(url);
        DeviceHistoryListing ret = res.jsonToObject(DeviceHistoryListing.class);
        res.close();
        return ret;

    }

    public DeviceKey[] addDeviceKey(String appid, String deviceName) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        String url = String.format("%s/v1/apps/%s/devices/%s/keys", host, appid, encodedDeviceName);
        Response res = post(url, null);
        DeviceKeyRet ret = res.jsonToObject(DeviceKeyRet.class);
        res.close();
        if (ret != null) {
            return ret.keys;
        }
        return null;
    }

    public DeviceKey[] queryDeviceKey(String appid, String deviceName) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        String url = String.format("%s/v1/apps/%s/devices/%s/keys", host, appid, encodedDeviceName);
        Response res = get(url);
        DeviceKeyRet ret = res.jsonToObject(DeviceKeyRet.class);
        res.close();
        if (ret != null) {
            return ret.keys;
        }
        return null;
    }

    public void deleteDeviceKey(String appid, String deviceName, String accessKey) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        String url = String.format("%s/v1/apps/%s/devices/%s/keys/%s", host, appid, encodedDeviceName, accessKey);
        StringMap headers = auth.authorizationV2(url, "DELETE", null, null);
        Response res = client.delete(url, headers);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        DeviceKeyRet ret = res.jsonToObject(DeviceKeyRet.class);
        res.close();
    }

    public void updateDeviceKeyState(String appid, String deviceName,
                                     String accessKey, int state) throws QiniuException {
        String encodedDeviceName = UrlSafeBase64.encodeToString(deviceName);
        StringMap params = new StringMap().put("state", state);
        String url = String.format("%s/v1/apps/%s/devices/%s/keys/%s/state", host, appid, encodedDeviceName, accessKey);
        Response res = post(url, params);
        res.close();
    }

    public DeviceKey[] cloneDeviceKey(String appid, String fromDeviceName,
                                      String toDeviceName, boolean cleanSelfKeys,
                                      boolean deleteDevice, String deviceAccessKey) throws QiniuException {
        String encodedFromDeviceName = UrlSafeBase64.encodeToString(fromDeviceName);
        String encodedToDeviceName = UrlSafeBase64.encodeToString(toDeviceName);
        String url = String.format("%s/v1/apps/%s/devices/%s/keys/clone", host, appid, encodedToDeviceName);
        StringMap params = new StringMap().put("fromDevice", fromDeviceName).put("cleanSelfKeys", cleanSelfKeys).
                put("deleteDevice", deleteDevice).put("deviceAccessKey", deviceAccessKey);

        Response res = post(url, params);
        DeviceKeyRet ret = res.jsonToObject(DeviceKeyRet.class);
        res.close();
        if (ret != null) {
            return ret.keys;

        }
        return null;
    }

    private Response post(String url, StringMap params) throws QiniuException {
        byte[] body;
        String contentType = null;
        if (params == null) {
            body = null;
        } else {
            contentType = Client.JsonMime;
            body = Json.encode(params).getBytes(Constants.UTF_8);
        }
        StringMap headers = auth.authorizationV2(url, "POST", body, contentType);
        Response res = client.post(url, body, headers, Client.JsonMime);
        if (!res.isOK()) {
            throw new QiniuException(res);
        }
        return res;
    }

    private class DeviceKeyRet {
        DeviceKey[] keys;
    }
}

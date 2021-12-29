package com.qiniu.qvs.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.qiniu.util.StringMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private int type; //可选项为摄像头、平台两类，1：摄像头，2：平台。
    private String name; // 设备名称 (可包含 字母、数字、中划线、下划线；1 ~ 100 个字符长)
    private String username; // 用户名, 4~40位，可包含大写字母、小写字母、数字、中划线，建议与设备国标ID一致
    private String password; // 密码, 4~40位，可包含大写字母、小写字母、数字、中划线
    private boolean pullIfRegister; // 注册成功后启动拉流, 默认关闭
    private String desc; // 关于设备的描述信息
    private String gbId; // 设备国标ID

    /**
     * 转换为POST参数对象
     *
     * @return POST参数对象
     */
    public StringMap transferPostParam() {
        Map<String, Object> paramMap = getStringObjectMap();
        StringMap result = new StringMap();
        result.putAll(paramMap);
        return result;
    }

    private Map<String, Object> getStringObjectMap() {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Map<String, Object> paramMap = gson.fromJson(gson.toJson(this), new TypeToken<HashMap<String, String>>() {
        }.getType());
        paramMap.put("type", type);
        paramMap.put("pullIfRegister", pullIfRegister);
        return paramMap;
    }
}

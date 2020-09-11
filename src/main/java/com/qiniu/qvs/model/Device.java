package com.qiniu.qvs.model;

public class Device {
    private String name; // 设备名称 (可包含 字母、数字、中划线、下划线；1 ~ 100 个字符长)
    private String username; // 用户名, 4~40位，可包含大写字母、小写字母、数字、中划线，建议与设备国标ID一致
    private String password; // 密码, 4~40位，可包含大写字母、小写字母、数字、中划线
    private boolean pullIfRegister; // 注册成功后启动拉流, 默认关闭
    private String desc; // 关于设备的描述信息
    private String gbId; // 设备国标ID

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGbId() {
        return gbId;
    }

    public void setGbId(String gbId) {
        this.gbId = gbId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPullIfRegister() {
        return pullIfRegister;
    }

    public void setPullIfRegister(boolean pullIfRegister) {
        this.pullIfRegister = pullIfRegister;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

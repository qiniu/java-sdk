package com.qiniu.qvs.model;

public class PatchOperation {
    // 更该或删除某个属性，replace：更改，delete：删除
    private String op;

    // 要修改或删除的属性
    private String key;

    // 要修改或删除属性的值
    private Object value;


    public PatchOperation(String op, String key, Object value) {
        this.op = op;
        this.key = key;
        this.value = value;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

package com.qiniu.http;

public enum MethodType {
    GET(false),
    PUT(true),
    POST(true),
    DELETE(false),
    HEAD(false),
    OPTIONS(false);

    private boolean hasContent;

    MethodType(boolean hasContent) {
        this.hasContent = hasContent;
    }

    public boolean hasContent() {
        return hasContent;
    }

    @Override
    public String toString() {
        String m;
        switch (this) {
            case PUT:
                m = "PUT";
                break;
            case POST:
                m = "POST";
                break;
            case HEAD:
                m = "HEAD";
                break;
            case DELETE:
                m = "DELETE";
                break;
            default:
                m = "GET";
        }
        return m;
    }
}

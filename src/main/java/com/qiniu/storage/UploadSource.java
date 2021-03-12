package com.qiniu.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class UploadSource {

    private final long size;
    private final String id; // 作为断点续传存储键值
    private final String name;
    private final File file;
    private InputStream inputStream;

    UploadSource(String id, File file) {
        this.id = id;
        this.size = file.length();
        this.name = file.getName();
        this.file = file;
    }

    UploadSource(String id, String fileName, long size, InputStream inputStream) {
        this.id = id;
        this.size = size;
        this.name = fileName;
        this.file = null;
        this.inputStream = inputStream;
    }

    boolean isValid() {
        return (file != null && file.canRead()) || inputStream != null;
    }

    String getID() {
        return id;
    }

    String getFileName() {
        return name;
    }

    long getSize() {
        return size;
    }

    int readData(byte[] buff, int offset, int len) throws IOException {
        return inputStream().read(buff, offset, len);
    }

    long skip(long len) throws IOException {
        return inputStream().skip(len);
    }

    void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    private InputStream inputStream() throws IOException {
        if (inputStream == null && file != null) {
            inputStream = new FileInputStream(file);
        }
        return inputStream;
    }
}

package com.qiniu.storage.persistent;

import com.qiniu.storage.Recorder;
import com.qiniu.util.UrlSafeBase64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 实现分片上传时上传进度的接口方法
 */
public final class FileRecorder implements Recorder {
    private final File directory;

    /**
     * 断点记录文件保存的目录
     *
     * @param directory
     * @throws IOException
     */
    public FileRecorder(String directory) throws IOException {
        this(new File(directory));
    }

    /**
     * 断点记录文件保存的目录
     *
     * @param directory
     * @throws IOException
     */
    public FileRecorder(File directory) throws IOException {
        this.directory = directory;
        if (!directory.exists()) {
            boolean r = directory.mkdirs();
            if (!r) {
                throw new IOException("mkdir failed");
            }
            return;
        }
        if (!directory.isDirectory()) {
            throw new IOException("does not mkdir");
        }
    }

    /**
     * 纪录分片上传进度
     *
     * @param key  上传文件进度文件保存名
     * @param data 上传文件的进度数据
     */
    @Override
    public void set(String key, byte[] data) {
        File f = new File(directory, UrlSafeBase64.encodeToString(key));
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(f);
            fo.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fo != null) {
            try {
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取分片上传进度
     *
     * @param key 上传文件进度文件保存名
     */
    @Override
    public byte[] get(String key) {
        File f = new File(directory, UrlSafeBase64.encodeToString(key));
        if (!f.exists()) {
            return null;
        }
        FileInputStream fi = null;
        byte[] data = new byte[(int) f.length()];
        int read = 0;
        try {
            fi = new FileInputStream(f);
            read = fi.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fi != null) {
            try {
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (read == 0) {
            return null;
        }
        return data;
    }

    /**
     * 删除已上传文件的进度文件
     *
     * @param key 上传文件进度文件保存名
     */
    @Override
    public void del(String key) {
        File f = new File(directory, UrlSafeBase64.encodeToString(key));
        f.delete();
    }
}

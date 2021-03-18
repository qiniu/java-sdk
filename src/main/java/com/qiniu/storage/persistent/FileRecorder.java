package com.qiniu.storage.persistent;

import com.qiniu.storage.Recorder;
import com.qiniu.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * 实现分片上传时上传进度的接口方法
 */
public final class FileRecorder implements Recorder {
    private static final String SPLIT = "*:|>?^ \b";
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

    private static String hash(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(base.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * 纪录分片上传进度
     *
     * @param key  上传文件进度文件保存名
     * @param data 上传文件的进度数据
     */
    @Override
    public synchronized void set(String key, byte[] data) {
        if (StringUtils.isNullOrEmpty(key)) {
            return;
        }
        File f = new File(directory, key);
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(f);
            fo.write(data);
            fo.flush();
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
    public synchronized byte[] get(String key) {
        if (StringUtils.isNullOrEmpty(key)) {
            return null;
        }
        File f = new File(directory, key);
        if (!f.exists()) {
            return null;
        }
        FileInputStream fi = null;
        byte[] data = null;
        int read = 0;
        try {
            if (outOfDate(f)) {
                f.delete();
                return null;
            }
            data = new byte[(int) f.length()];
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

    private boolean outOfDate(File f) {
        return f.lastModified() + 1000 * 3600 * 24 * 5 < System.currentTimeMillis();
    }

    /**
     * 删除已上传文件的进度文件
     *
     * @param key 上传文件进度文件保存名
     */
    @Override
    public synchronized void del(String key) {
        if (StringUtils.isNullOrEmpty(key)) {
            return;
        }
        File f = new File(directory, key);
        f.delete();
    }

    @Override
    public String recorderKeyGenerate(String key, File file) {
        return hash(key + SPLIT + file.lastModified() + SPLIT + file.getAbsolutePath());
    }

    @Override
    public String recorderKeyGenerate(String bucket, String key, String contentDataSUID, String uploaderSUID) {
        return hash(bucket + SPLIT + key + SPLIT + contentDataSUID + SPLIT + uploaderSUID);
    }
}

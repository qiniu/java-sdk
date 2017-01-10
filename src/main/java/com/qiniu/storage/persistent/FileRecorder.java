package com.qiniu.storage.persistent;

import com.qiniu.storage.Recorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;

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
        File f = new File(directory, hash(key));
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
        File f = new File(directory, hash(key));
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
        return f.lastModified() + 1000 * 3600 * 24 * 2 < new Date().getTime();
    }

    /**
     * 删除已上传文件的进度文件
     *
     * @param key 上传文件进度文件保存名
     */
    @Override
    public void del(String key) {
        File f = new File(directory, hash(key));
        f.delete();
    }

    @Override
    public String recorderKeyGenerate(String key, File file) {
        return key + "_._" + file.getAbsolutePath();
    }

    private static String hash(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(base.getBytes());
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                hexString.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
            return hexString.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

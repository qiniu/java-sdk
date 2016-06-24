package com.qiniu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by bailong on 14/10/11.
 */
public final class TempFile {
    private TempFile() {
    }

    static final Random r = new Random();

    public static void remove(File f) {
        f.delete();
    }

    public static File createFile(int kiloSize) throws IOException {
        FileOutputStream fos = null;
        try {
            long size = (long) (1024 * kiloSize);
            File f = File.createTempFile("qiniu_" + kiloSize + "k", "tmp");
            f.createNewFile();
            fos = new FileOutputStream(f);
            byte[] b = getByte((byte) r.nextInt());
            long s = 0;
            while (s < size) {
                int l = (int) Math.min(b.length, size - s);
                fos.write(b, 0, l);
                s += l;
                // 随机生成的文件的每一段(4M)都不一样
                b = getByte((byte) r.nextInt());
            }
            fos.flush();
            return f;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static byte[] getByte(byte b) {
        byte[] bs = new byte[1024 * 4];

        for (int i = 1; i < 1024 * 4; i++) {
            bs[i] = b;
        }

        bs[10] = (byte) r.nextInt();
        bs[9] = (byte) r.nextInt();
        bs[8] = (byte) r.nextInt();
        bs[7] = (byte) r.nextInt();
        bs[6] = (byte) r.nextInt();
        bs[5] = (byte) r.nextInt();
        bs[4] = (byte) r.nextInt();
        bs[3] = (byte) r.nextInt();
        bs[3] = (byte) r.nextInt();
        bs[1] = (byte) r.nextInt();
        bs[0] = (byte) r.nextInt();

        bs[1024 * 4 - 2] = '\r';
        bs[1024 * 4 - 1] = '\n';
        return bs;
    }


    public static File createFileOld(int kiloSize) throws IOException {
        FileOutputStream fos = null;
        try {
            long size = (long) (1024 * kiloSize);
            File f = File.createTempFile("qiniu_" + kiloSize + "k", "tmp");
            f.createNewFile();
            fos = new FileOutputStream(f);
            byte[] b = getByteOld();
            long s = 0;
            while (s < size) {
                int l = (int) Math.min(b.length, size - s);
                fos.write(b, 0, l);
                s += l;
            }
            fos.flush();
            return f;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static byte[] getByteOld() {
        byte[] b = new byte[1024 * 4];
        b[0] = 'A';
        for (int i = 1; i < 1024 * 4; i++) {
            b[i] = 'b';
        }
        b[1024 * 4 - 2] = '\r';
        b[1024 * 4 - 1] = '\n';
        return b;
    }
}

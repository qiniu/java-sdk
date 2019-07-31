package test.com.qiniu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by bailong on 14/10/11.
 */
public final class TempFile {
    static final Random r = new Random();

    private TempFile() {
    }

    public static void remove(File f) {
        f.delete();
    }

    public static File createFile(long kiloSize) throws IOException {
        FileOutputStream fos = null;
        try {
            long size = 1024 * kiloSize;
            File f = File.createTempFile("qiniu_" + kiloSize + "k", ".tmp");
            f.createNewFile();
            fos = new FileOutputStream(f);
            byte[] b = getByte();
            long s = 0;
            while (s < size) {
                int l = (int) Math.min(b.length, size - s);
                fos.write(b, 0, l);
                s += l;
                // 随机生成的文件的每一段(<4M)都不一样
                b = getByte();
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

    private static byte[] getByte() {
        byte b = (byte) r.nextInt();
        int len = 498 * 4;
        byte[] bs = new byte[len];

        for (int i = 1; i < len; i++) {
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

        bs[len - 2] = '\r';
        bs[len - 1] = '\n';
        return bs;
    }


    public static File createFileOld(long kiloSize) throws IOException {
        FileOutputStream fos = null;
        try {
            long size = 1024 * kiloSize;
            File f = File.createTempFile("qiniu_" + kiloSize + "k", ".tmp");
            f.createNewFile();
            fos = new FileOutputStream(f);
            long s = 0;
            int i = 0;
            while (s < size) {
                byte[] b = getByteOld(i);
                int l = (int) Math.min(b.length, size - s);
                fos.write(b, 0, l);
                s += l;
                i++;
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

    private static byte[] getByteOld(int a) {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 8);
        for (int i = 1; i < 1024; i++) {
            buffer.putInt(a);
        }
        return buffer.array();
    }
}

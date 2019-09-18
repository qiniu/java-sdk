package com.qiniu.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private IOUtils() {

    }

    /**
     * 输入InputSteam，返回byte[].
     * 参考：https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/IOUtils.java<br>
     *
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(final InputStream input) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        }
    }

}

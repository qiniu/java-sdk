package com.qiniu.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class IOUtils {
	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static byte[] toByteArray(final InputStream input) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        }
    }
	
}

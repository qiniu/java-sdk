package test.com.qiniu;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ResCode {
    public static boolean find(int code, int... codes) {
        System.out.println("response code is: " + code + ", possible code is in: " + Arrays.toString(codes));
        for (int i : codes) {
            if (code == i) {
                return true;
            }
        }
        return false;
    }

    public static int[] getPossibleResCode(int... codes) {
        return getPossibleResCode(TestConfig.isTravis(), codes);
    }

    public static int[] getPossibleResCode(boolean isTravis, int... codes) {
        if (isTravis) {
            int[] n = new int[codes.length + 1];
            System.arraycopy(codes, 0, n, 0, codes.length);
            n[codes.length] = -1; // add code -1 for networking failed.
            return n;
        }
        return codes;
    }

    @Test
    public void testAddCode() {
        Assert.assertArrayEquals(new int[]{401, -1}, ResCode.getPossibleResCode(true, 401));
        Assert.assertArrayEquals(new int[]{401}, ResCode.getPossibleResCode(false, 401));
    }


}

package test.com.qiniu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
        return codes;
    }

    @Test
    public void testAddCode() {
        assertArrayEquals(new int[] { 401 }, ResCode.getPossibleResCode(401));
    }

}

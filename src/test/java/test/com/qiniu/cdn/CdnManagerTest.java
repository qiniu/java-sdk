package test.com.qiniu.cdn;

import com.qiniu.cdn.CdnManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CdnManagerTest {
    @Test
    public void testencodePath() {
        try {
            // 空格替换为 %20
            String path = "/ hello/ world";
            String got = CdnManager.encodePath(path);
            String want = "/%20hello/%20world";
            assertEquals(want, got);

            // 半角 + 号替换为 %2B
            path = "/+hello/+world";
            got = CdnManager.encodePath(path);
            want = "/%2Bhello/%2Bworld";

            assertEquals(want, got);

            // % 号替换为 %25
            path = "/%hello/+world";
            got = CdnManager.encodePath(path);
            want = "/%25hello/%2Bworld";
            assertEquals(want, got);


        } catch (Exception e) {
            fail(e.toString());
        }
    }

}

package test.com.qiniu.storage;

import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import org.junit.Test;

public class ZoneTest {

    @Test
    public void zone1() {
        String ak = "<access key>";
        String sk = "<secret key>";
        Auth auth = Auth.create(ak, sk);

//        Configuration.defaultApiHost = "apiserver-sdfrsd-s.qiniubbo.com";
//        Configuration.defaultRsHost = "rs-sdfrsd-s.qiniubbo.com";
        Zone zone = new Zone.Builder()
                .upHttp("http://up-sdfrsd-s.qiniubbo.com")
                .upBackupHttp("http://up-sdfrsd-s.qiniubbo.com")
                .rsHttp("http://rs-sdfrsd-s.qiniubbo.com")
                .rsfHttp("http://rsf-sdfrsd-s.qiniubbo.com")
                .apiHttp("http://apiserver-sdfrsd-s.qiniubbo.com")
                .iovipHttp("http://io-sdfrsd-s.qiniubbo.com").build();

        Zone zone2 = new Zone.Builder(zone)
                .iovipHttp("http://io-sdfrsd-s.qiniubbo.com").build();

        Configuration cfg = new Configuration(Zone.autoZone());
        cfg.zone = zone2;
    }

}

package test.com.qiniu.storage;

import com.qiniu.common.Region;
import com.qiniu.common.Zone;
import org.junit.Test;

import org.junit.Assert;

public class RegionTest {
    @Test
    public void testZone() {
        Zone zone = new Zone.Builder().build();
        Assert.assertNotNull(zone);

        System.out.println("============");
        Zone zone1 = new Zone.Builder(Region.beimei()).build();
        Assert.assertNotNull(zone1);

        System.out.println("============");
        Zone zone2 = Zone.huabei();
        Assert.assertNotNull(zone2);
    }
}

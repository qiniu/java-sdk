package test.com.qiniu.storage;

import com.qiniu.storage.Region;
import com.qiniu.storage.RegionGroup;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegionGroupTest {

    @Test
    @Tag("UnitTest")
    public void testSign() {
        RegionGroup group01 = new RegionGroup();
        group01.addRegion(Region.region1());
        group01.addRegion(Region.region2());

        RegionGroup group02 = (RegionGroup) group01.clone();

        assertNotSame(group01, group02);
    }

}

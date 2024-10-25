package test.com.qiniu.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.Region;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionTest {

    @Test
    @Tag("UnitTest")
    public void testCreateWithRegionId() throws Exception {
        Class regionReqInfoClass = Class.forName("com.qiniu.storage.RegionReqInfo");
        Constructor regionReqInfoConstructor = regionReqInfoClass.getDeclaredConstructor(String.class, String.class);
        regionReqInfoConstructor.setAccessible(true);

        Object info = regionReqInfoConstructor.newInstance("a", "b");
        Region na0 = Region.createWithRegionId("na0");

        // region id
        Method getRegion = Region.class.getDeclaredMethod("getRegion", regionReqInfoClass);
        getRegion.setAccessible(true);
        String regionId = (String) getRegion.invoke(na0, info);
        assertEquals("na0", regionId);

        // up
        // up src
        Method getSrcUpHost = Region.class.getDeclaredMethod("getSrcUpHost", regionReqInfoClass);
        getSrcUpHost.setAccessible(true);
        List<String> srcHosts = (List<String>) getSrcUpHost.invoke(na0, info);
        assertTrue(srcHosts.contains("up-na0.qiniup.com"), "check up src error");

        // up acc
        Method getAccUpHost = Region.class.getDeclaredMethod("getAccUpHost", regionReqInfoClass);
        getAccUpHost.setAccessible(true);
        List<String> accHosts = (List<String>) getAccUpHost.invoke(na0, info);
        assertTrue(accHosts.contains("upload-na0.qiniup.com"), "check up acc error");

        // io
        Method getIoHost = Region.class.getDeclaredMethod("getIovipHost", regionReqInfoClass);
        getIoHost.setAccessible(true);
        String ioHost = (String) getIoHost.invoke(na0, info);
        assertTrue(ioHost.equals("iovip-na0.qiniuio.com"), "check io error");

        // rs
        Method getRsHost = Region.class.getDeclaredMethod("getRsHost", regionReqInfoClass);
        getRsHost.setAccessible(true);
        String rsHost = (String) getRsHost.invoke(na0, info);
        assertTrue(rsHost.equals("rs-na0.qiniuapi.com"), "check rs error");

        // rsf
        Method getRsfHost = Region.class.getDeclaredMethod("getRsfHost", regionReqInfoClass);
        getRsfHost.setAccessible(true);
        String rsfHost = (String) getRsfHost.invoke(na0, info);
        assertTrue(rsfHost.equals("rsf-na0.qiniuapi.com"), "check rsf error");

        // api
        Method getApiHost = Region.class.getDeclaredMethod("getApiHost", regionReqInfoClass);
        getApiHost.setAccessible(true);
        String apiHost = (String) getApiHost.invoke(na0, info);
        assertTrue(apiHost.equals("api-na0.qiniuapi.com"), "check api error");
    }

}

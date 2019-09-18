package test.com.qiniu.streaming;

import com.qiniu.common.QiniuException;
import com.qiniu.streaming.StreamingManager;
import com.qiniu.streaming.model.ActivityRecords;
import com.qiniu.streaming.model.StreamAttribute;
import com.qiniu.streaming.model.StreamListing;
import com.qiniu.util.Auth;
import org.junit.Test;
import test.com.qiniu.TestConfig;

import static org.junit.Assert.*;

/**
 * Created by bailong on 16/9/22
 * Updated by panyuan on 19/3/12
 */
public class StreamingTest {

    private Auth auth = TestConfig.testAuth;
    private String hub = "pilisdktest";
    private String stream = "javasdk";
    private String streamNoExist = "javasdk" + "NoExist";
    private String streamKeyPrefix = "javasdk";
    private StreamingManager manager = new StreamingManager(auth, hub);

    /**
     * 测试获取不存在的流的信息
     * 检测返回状态码是否是612
     */
    @Test
    public void testGetNoExistStream() {
        try {
            manager.attribute(streamNoExist);
            fail("should not exist");
        } catch (QiniuException e) {
            assertEquals(612, e.code());
        }
    }

    /**
     * 测试创建、禁用、启用、获取流信息、列举
     *
     * @throws QiniuException
     */
    @Test
    public void testStreamOperation() throws QiniuException {
        try {
            // 确保流存在 //
            manager.create(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StreamAttribute attr = manager.attribute(stream);
        assertEquals(0, attr.disabledTill);
        assertNotEquals(0, attr.createdAt);

        try {
            manager.create(stream);
            fail("has already existed");
        } catch (QiniuException e) {
            assertEquals(614, e.code());
        }

        manager.disableTill(stream, -1);

        attr = manager.attribute(stream);
        assertEquals(-1, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        manager.enable(stream);
        attr = manager.attribute(stream);
        assertEquals(0, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        long t = System.currentTimeMillis() / 1000 + 3600;
        manager.disableTill(stream, t);
        attr = manager.attribute(stream);
        assertEquals(t, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        manager.enable(stream);
        attr = manager.attribute(stream);
        assertEquals(0, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        try {
            manager.status(stream);
            fail();
        } catch (QiniuException e) {
            assertEquals(619, e.code());
        }

        try {
            manager.saveAs(stream, null, 0, 0);
            fail();
        } catch (QiniuException e) {
            assertEquals(619, e.code());
        }

        ActivityRecords records = manager.history(stream, System.currentTimeMillis() / 1000 - 1000, 0);
        assertEquals(0, records.items.length);

        StreamListing l = manager.listStreams(false, streamKeyPrefix, null);
        String[] keys = l.keys();
        assertEquals(1, keys.length);
        assertEquals("", l.marker);

        l = manager.listStreams(true, streamKeyPrefix, null);
        keys = l.keys();
        assertEquals(0, keys.length);

        StreamingManager.ListIterator it = manager.createStreamListIterator(false, streamKeyPrefix);
        assertTrue(it.hasNext());
        keys = it.next();
        assertEquals(1, keys.length);

        assertFalse(it.hasNext());

        it = manager.createStreamListIterator(true, streamKeyPrefix);
        assertTrue(it.hasNext());
        keys = it.next();
        assertEquals(0, keys.length);

        assertFalse(it.hasNext());
    }

    /**
     * 测试saveas
     * 检测返回状态码是否是404
     *
     * @throws QiniuException
     */
    @Test
    public void testSaveAs() throws QiniuException {
        try {
            manager.saveAs(streamNoExist, "f\"ff.m3u8");
        } catch (QiniuException e) {
            assertEquals(404, e.response.statusCode);
        }
    }

    /**
     * 测试创建流
     * 检测返回状态码是否为614
     *
     * @throws QiniuException
     */
    @Test
    public void testCreate() throws QiniuException {
        try {
            manager.create(stream);
        } catch (QiniuException e) {
            assertEquals(614, e.code());
        }
    }

}

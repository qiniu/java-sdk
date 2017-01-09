package com.qiniu.streaming;

import com.qiniu.TestConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.streaming.model.ActivityRecords;
import com.qiniu.streaming.model.StreamAttribute;
import com.qiniu.streaming.model.StreamListing;
import com.qiniu.streaming.model.StreamStatus;
import com.qiniu.util.Auth;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by bailong on 16/9/22
 */
public class StreamingTest {
    private Auth auth = null;

    {
        try {
            auth = Auth.create(System.getenv("ak"), System.getenv("sk"));
        } catch (Exception e) {
            auth = TestConfig.testAuth;
        }
    }

    private String hub = "pilisdktest";
    private String streamKeyPrefix = "pilijava" + System.currentTimeMillis();
    private StreamingManager manager = new StreamingManager(auth, hub);


    @Test
    public void testGetNoExistStream() {
        try {
            manager.attribute("nnnoexist");
            fail("should not exist");
        } catch (QiniuException e) {
            e.printStackTrace();
            assertEquals(612, e.code());
        }
    }

    // CHECKSTYLE:OFF
    @Test
    public void testStreamOperation() throws QiniuException {
        // CHECKSTYLE:ON
        String streamKey = streamKeyPrefix + "-a";

        manager.create(streamKey);

        StreamAttribute attr = manager.attribute(streamKey);
        assertEquals(0, attr.disabledTill);
        assertNotEquals(0, attr.createdAt);

        try {
            manager.create(streamKey);
            fail("has already existed");
        } catch (QiniuException e) {
            assertEquals(614, e.code());
        }

        manager.disableTill(streamKey, -1);

        attr = manager.attribute(streamKey);
        assertEquals(-1, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        manager.enable(streamKey);
        attr = manager.attribute(streamKey);
        assertEquals(0, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        long t = System.currentTimeMillis() / 1000 + 3600;
        manager.disableTill(streamKey, t);
        attr = manager.attribute(streamKey);
        assertEquals(t, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        manager.enable(streamKey);
        attr = manager.attribute(streamKey);
        assertEquals(0, attr.disabledTill);
        assertNotEquals(0, attr.updatedAt);

        try {
            StreamStatus status = manager.status(streamKey);
            fail();
        } catch (QiniuException e) {
            assertEquals(619, e.code());
        }

        try {
            manager.saveAs(streamKey, null, 0, 0);
            fail();
        } catch (QiniuException e) {
            assertEquals(619, e.code());
        }

        ActivityRecords records = manager.history(streamKey, System.currentTimeMillis() / 1000 - 1000, 0);
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
}

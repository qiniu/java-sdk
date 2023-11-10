package com.qiniu.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class BatchTest {

    @Test
    @Tag("UnitTest")
    public void testBatchCondition() {

        BucketManager.BatchOperations.Condition condition = new BucketManager.BatchOperations.Condition.Builder()
                .build();
        String encodedString = condition.encodedString();
        Assertions.assertNull(encodedString);

        condition = new BucketManager.BatchOperations.Condition.Builder()
                .setHash("hash")
                .build();
        encodedString = condition.encodedString();
        Assertions.assertEquals(encodedString, "aGFzaD1oYXNo");

        condition = new BucketManager.BatchOperations.Condition.Builder()
                .setHash("hash")
                .setPutTime(1993232L)
                .build();
        encodedString = condition.encodedString();
        Assertions.assertEquals(encodedString, "aGFzaD1oYXNoJnB1dFRpbWU9MTk5MzIzMg==");

        condition = new BucketManager.BatchOperations.Condition.Builder()
                .setHash("hash")
                .setMime("application/txt")
                .setPutTime(1993232L)
                .build();
        encodedString = condition.encodedString();
        Assertions.assertEquals(encodedString, "aGFzaD1oYXNoJm1pbWU9YXBwbGljYXRpb24vdHh0JnB1dFRpbWU9MTk5MzIzMg==");

        condition = new BucketManager.BatchOperations.Condition.Builder()
                .setHash("hash")
                .setMime("application/txt")
                .setFileSize(100L)
                .setPutTime(1993232L)
                .build();
        encodedString = condition.encodedString();
        Assertions.assertEquals(encodedString, "aGFzaD1oYXNoJm1pbWU9YXBwbGljYXRpb24vdHh0JmZzaXplPTEwMCZwdXRUaW1lPTE5OTMyMzI=");
    }
}

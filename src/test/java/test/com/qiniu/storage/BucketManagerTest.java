package test.com.qiniu.storage;

import com.qiniu.storage.BucketManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class BucketManagerTest {
    @Test
    @Tag("UnitTest")
    public void testBatchOperationsSize() {
        BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
        Assertions.assertEquals(0, batchOperations.size());

        batchOperations.addDeleteOp("bucket1", "1", "2");
        Assertions.assertEquals(2, batchOperations.size());

        batchOperations.addCopyOp(
                "fromBucket", "fromFileKey",
                "toBucket", "toFileKey"
        );
        Assertions.assertEquals(3, batchOperations.size());

        batchOperations.addRenameOp("fromBucket", "fromFileKey", "toFileKey");
        Assertions.assertEquals(4, batchOperations.size());
    }
}

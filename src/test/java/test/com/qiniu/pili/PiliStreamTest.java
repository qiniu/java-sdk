package test.com.qiniu.pili;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.qiniu.common.QiniuException;
import com.qiniu.pili.PiliStreamManager;
import com.qiniu.pili.PiliStreamModel;
import com.qiniu.util.Auth;

import test.com.qiniu.ResCode;
import test.com.qiniu.TestConfig;

public class PiliStreamTest {
  private PiliStreamManager streamManager;

  /**
   * 初始化
   *
   * @throws Exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    this.streamManager = new PiliStreamManager(
        Auth.create(TestConfig.piliAccessKey, TestConfig.piliSecretKey), TestConfig.piliHTTPhost);
  }

  @Test
  @Tag("UnitTest")
  public void testGetHubList() {
    try {
      PiliStreamModel.GetStreamsListResponse response = streamManager.getStreamsList(TestConfig.piliTestHub, false, "",
          10, "");
      System.out.println("streamManager.getStatUpflow: success: " + response);
      assertNotNull(response);
    } catch (QiniuException e) {
      System.out.println("streamManager.getStatUpflow: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStreamBaseInfo() {
    try {
      PiliStreamModel.GetStreamBaseInfoResponse response = streamManager.getStreamBaseInfo(TestConfig.piliTestHub,
          TestConfig.piliTestStream);
      System.out.println("streamManager.getStreamBaseInfo: success: " + response);
      assertNotNull(response);
    } catch (QiniuException e) {
      System.out.println("streamManager.getStreamBaseInfo: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testStreamDisable() {
    try {
      QiniuException exception = streamManager.streamDisable(TestConfig.piliTestHub, TestConfig.piliTestStream, 0, 0);
      if (exception != null) {
        System.out.println("streamManager.streamDisable ERROR: " + exception.getMessage());
      } else {
        System.out.println("streamManager.streamDisable succeeded.");
      }
    } catch (Exception e) {
      System.out.println("streamManager.streamDisable: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStreamLiveStatus() {
    try {
      PiliStreamModel.GetStreamLiveStatusResponse response = streamManager.getStreamLiveStatus(TestConfig.piliTestHub,
          TestConfig.piliTestStream);
      System.out.println("streamManager.getStreamLiveStatus: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("streamManager.getStreamLiveStatus: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testBatchGetStreamLiveStatus() {
    String[] strs = { TestConfig.piliTestStream };
    try {
      PiliStreamModel.BatchGetStreamLiveStatusResponse response = streamManager
          .batchGetStreamLiveStatus(TestConfig.piliTestHub, strs);
      System.out.println("streamManager.batchGetStreamLiveStatus: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("streamManager.batchGetStreamLiveStatus: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStreamHistory() {

    try {
      PiliStreamModel.GetStreamHistoryResponse response = streamManager.getStreamHistory(0, 0, TestConfig.piliTestHub,
          TestConfig.piliTestStream);
      System.out.println("streamManager.getStreamHistory: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("streamManager.getStreamHistory: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testStreamSnapshot() {
    PiliStreamModel streamModel = new PiliStreamModel();
    PiliStreamModel.StreamSnapshotRequest param = streamModel.new StreamSnapshotRequest();
    param.stream = TestConfig.piliTestStream;
    param.hub = TestConfig.piliTestHub;

    try {
      PiliStreamModel.StreamSnapshotResponse response = streamManager.streamSnapshot(param);
      System.out.println("streamManager.streamSnapshot: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("streamManager.streamSnapshot: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testStreamConverts() {
    String[] strs = {};
    try {
      QiniuException exception = streamManager.streamConverts(TestConfig.piliTestHub, TestConfig.piliTestStream, strs);
      if (exception != null) {
        System.out.println("streamManager.streamConverts ERROR: " + exception.getMessage());
      } else {
        System.out.println("streamManager.streamConverts succeeded.");
      }
    } catch (Exception e) {
      System.out.println("streamManager.streamConverts: Error: ");
      System.out.println(e);
      assertNull(e);
    }
  }
}

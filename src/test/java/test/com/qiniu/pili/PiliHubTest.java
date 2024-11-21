package test.com.qiniu.pili;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import com.qiniu.common.QiniuException;
import com.qiniu.pili.PiliHubManager;
import com.qiniu.pili.PiliHubModel;
import com.qiniu.util.Auth;

import test.com.qiniu.ResCode;
import test.com.qiniu.TestConfig;

public class PiliHubTest {
  private PiliHubManager hubManager;

  /**
   * 初始化
   *
   * @throws Exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    this.hubManager = new PiliHubManager(
        Auth.create(TestConfig.piliAccessKey, TestConfig.piliSecretKey), TestConfig.piliHTTPhost);
  }

  @Test
  @Tag("UnitTest")
  public void testGetHubList() {
    try {
      PiliHubModel.HubListResult response = hubManager.getHubList();
      assertNotNull(response);
    } catch (QiniuException e) {
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetHubInfo() {
    try {
      PiliHubModel.HubInfoResult response = hubManager.getHubInfo(TestConfig.piliTestHub);
      System.out.println("hubManager.getHubInfo: response: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("hubManager.getHubInfo ERROR: response: " + e.getMessage());
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testHubSecurity() {
    try {
      QiniuException exception = hubManager.hubSecurity(TestConfig.piliTestHub, "static", "qiniu_static_publish_key");
      if (exception != null) {
        System.out.println("hubManager.hubSecurity ERROR: " + exception.getMessage());
      } else {
        System.out.println("hubManager.hubSecurity succeeded.");
      }
    } catch (QiniuException e) {
      System.out.println("Error: " + e.getMessage());
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testHubHlsplus() {
    try {
      PiliHubModel model = new PiliHubModel();
      PiliHubModel.HubHlsplusRequest param = model.new HubHlsplusRequest();

      param.hub = TestConfig.piliTestHub;
      param.hlsPlus = true; // 或者 false，根据您的需求设置
      QiniuException exception = hubManager.hubHlsplus(param);
      if (exception != null) {
        System.out.println("hubManager.hubSecurity ERROR: " + exception.getMessage());
      } else {
        System.out.println("hubManager.testHubHlsplus succeeded.");
      }
    } catch (QiniuException e) {
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testHubPersistenceRequest() {
    try {
      PiliHubModel model = new PiliHubModel();
      PiliHubModel.HubPersistenceRequest param = model.new HubPersistenceRequest();

      param.hub = TestConfig.piliTestHub;
      param.liveDataExpireDays = 3;
      param.storageBucket = "";
      QiniuException exception = hubManager.hubPersistence(param);
      if (exception != null) {
        System.out.println("hubManager.testHubPersistenceRequest ERROR: " + exception.getMessage());
      } else {
        System.out.println("hubManager.testHubPersistenceRequest succeeded.");
      }
    } catch (QiniuException e) {
      assertNull(e);
      System.out.println("Error: " + e.getMessage());
    }
  }

  @Test
  @Tag("UnitTest")
  public void testHubSnapshotRequest() {
    try {
      PiliHubModel model = new PiliHubModel();
      PiliHubModel.HubSnapshotRequest param = model.new HubSnapshotRequest();

      param.hub = TestConfig.piliTestHub;
      param.snapshotInterval = 3;

      QiniuException exception = hubManager.hubSnapshot(param);
      if (exception != null) {
        System.out.println("hubManager.testHubSnapshotRequest ERROR: " + exception.getMessage());
      } else {
        System.out.println("hubManager.testHubSnapshotRequest succeeded.");
      }
    } catch (QiniuException e) {
      assertNull(e);
      System.out.println("Error: " + e.getMessage());
    }
  }
}

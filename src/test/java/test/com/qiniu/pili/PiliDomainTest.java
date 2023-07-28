package test.com.qiniu.pili;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.qiniu.common.QiniuException;
import com.qiniu.pili.PiliDomainManager;
import com.qiniu.pili.PiliDomainModel;
import com.qiniu.util.Auth;

import test.com.qiniu.TestConfig;

public class PiliDomainTest {
  private PiliDomainManager domainManager;

  /**
   * 初始化
   *
   * @throws Exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    this.domainManager = new PiliDomainManager(
        Auth.create(TestConfig.piliAccessKey, TestConfig.piliSecretKey), TestConfig.piliHTTPhost);
  }

  @Test
  @Tag("UnitTest")
  public void testGetDomainsList() {
    String hub = "test-hub";
    String expectedErr = "";
    try {
      PiliDomainModel.DomainsListResult result = domainManager.getDomainsList(hub);
      System.out.println(result);
    } catch (QiniuException e) {
      assertEquals(expectedErr, e.getMessage());
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetDomainInfo() {
    String hub = TestConfig.piliTestHub;
    String domain = TestConfig.piliTestDomain;
    String expectedErr = "";

    try {
      PiliDomainModel.DomainInfoResult response = domainManager.getDomainInfo(hub,
          domain);
      assertNotNull(response);
    } catch (QiniuException e) {
      assertEquals(expectedErr, e.getMessage());
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void test_Bind_UnBindDomain() {
    String domain = "testbind-" + "2001" + ".fake.qiniu.com";
    String hub = TestConfig.piliTestHub;
    String type = "liveHls"; // or "live_hls", "live_hdl", "playback_hls"

    try {
      domainManager.bindDomain(hub, domain, type);
    } catch (QiniuException e) {
      assertNotNull(e);
      return;
    }

    try {
      domainManager.unbindDomain(hub, domain);
    } catch (QiniuException e) {
      assertNotNull(e);
      return;
    }
  }

  @Test
  @Tag("UnitTest")
  public void testBindVodDomain() {
    String hub = TestConfig.piliTestHub;
    String vodDomain = TestConfig.piliTestVodDomain;

    try {
      QiniuException exception = domainManager.bindVodDomain(hub, vodDomain);
      if (exception != null) {
        System.out.println("Failed to unbind domain: " + exception.getMessage());
      } else {
        System.out.println("Domain unbind succeeded.");
      }
    } catch (QiniuException e) {
      System.out.println("Error: " + e.getMessage());
      assertNotNull(e);
      return;
    }
  }

  @Test
  @Tag("UnitTest")
  public void testSetDomainCert() {
    String hub = TestConfig.piliTestHub;
    String domain = TestConfig.piliTestDomain;
    String certName = TestConfig.piliTestCertName;

    try {
      QiniuException exception = domainManager.setDomainCert(hub, domain, certName);
      if (exception != null) {
        System.out.println("Failed to setDomainCert domain: " + exception.getMessage());
      } else {
        System.out.println("Domain setDomainCert succeeded.");
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testSetDomainURLRewrite() {
    String hub = TestConfig.piliTestHub;
    String domain = TestConfig.piliTestDomain;
    List<PiliDomainModel.Rules> rules = new ArrayList<>();

    rules.add(new PiliDomainModel.Rules("(.+)/live/(.+)/playlist.m3u8", "${1}/hub/${2}.m3u8"));
    rules.add(new PiliDomainModel.Rules("pattern2", "replacement2"));

    try {
      QiniuException exception = domainManager.setDomainURLRewrite(hub, domain, rules);
      if (exception != null) {
        System.out.println("Failed to setDomainCert domain: " + exception.getMessage());
      } else {
        System.out.println("Domain setDomainCert succeeded.");
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      assertNull(e);
    }
  }
}

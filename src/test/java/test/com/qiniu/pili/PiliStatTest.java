package test.com.qiniu.pili;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.qiniu.common.QiniuException;
import com.qiniu.pili.PiliStatManager;
import com.qiniu.pili.PiliStatModel;
import com.qiniu.util.Auth;

import test.com.qiniu.ResCode;
import test.com.qiniu.TestConfig;

public class PiliStatTest {
  private PiliStatManager statManager;
  private PiliStatModel statModel;
  private PiliStatModel.GetStatCommonRequest commentReq;
  Map<String, String[]> where;
  String select;

  /**
   * 初始化
   *
   * @throws Exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    statManager = new PiliStatManager(
        Auth.create(TestConfig.piliAccessKey, TestConfig.piliSecretKey), TestConfig.piliHTTPhost);
    statModel = new PiliStatModel();
    commentReq = statModel.new GetStatCommonRequest();

    commentReq.begin = "20060102";
    commentReq.end = "20060102";
    commentReq.g = "month";

    where = new HashMap<>();
    where.put("hub", new String[]{TestConfig.piliTestHub});
    where.put("area", new String[]{"!cn", "!hk"});
    select = "flow";
  }

  @Test
  @Tag("UnitTest")
  public void testGetStatUpflow() {
    PiliStatModel.GetStatUpflowRequest param = statModel.new GetStatUpflowRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatResponse[] response = statManager.getStatUpflow(param);
      System.out.println("statManager.getStatUpflow: success: " + response);
      assertNotNull(response);
    } catch (QiniuException e) {
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGroupStatUpflow() {
    PiliStatModel.GroupStatUpflowRequest param = statModel.new GroupStatUpflowRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatGroupResponse[] response = statManager.groupStatUpflow(param);
      System.out.println("statManager.groupStatUpflow: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStatDownflow() {
    PiliStatModel.GetStatDownflowRequest param = statModel.new GetStatDownflowRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatResponse[] response = statManager.getStatDownflow(param);
      System.out.println("statManager.getStatDownflow: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGroupStatDownflow() {
    PiliStatModel.GroupStatDownflowRequest param = statModel.new GroupStatDownflowRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatGroupResponse[] response = statManager.groupStatDownflow(param);
      System.out.println("statManager.groupStatDownflow: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStatCodec() {
    PiliStatModel.GetStatCodecRequest param = statModel.new GetStatCodecRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatResponse[] response = statManager.getStatCodec(param);
      System.out.println("statManager.getStatCodec: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGroupStatCodec() {
    PiliStatModel.GroupStatCodecRequest param = statModel.new GroupStatCodecRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatGroupResponse[] response = statManager.groupStatCodec(param);
      System.out.println("statManager.groupStatCodec: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStatNrop() {
    PiliStatModel.GetStatNropRequest param = statModel.new GetStatNropRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    commentReq.begin = "20210928";
    commentReq.end = "20210930";   
    commentReq.g = "hour"; 
    
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatResponse[] response = statManager.getStatNrop(param);
      System.out.println("statManager.getStatNrop: success: " + response);
      assertNotNull(response);
    } catch (QiniuException e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGroupStatNrop() {
    PiliStatModel.GroupStatCodecRequest param = statModel.new GroupStatCodecRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatGroupResponse[] response = statManager.groupStatNrop(param);
      System.out.println("statManager.groupStatNrop: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStatCasterRequest() {
    PiliStatModel.GetStatCasterRequest param = statModel.new GetStatCasterRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatResponse[] response = statManager.getStatCaster(param);
      System.out.println("statManager.getStatCaster: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGroupStatCaster() {
    PiliStatModel.GroupStatCasterRequest param = statModel.new GroupStatCasterRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatGroupResponse[] response = statManager.groupStatCaster(param);
      System.out.println("statManager.groupStatCaster: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGetStatPub() {
    PiliStatModel.GetStatPubRequest param = statModel.new GetStatPubRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatResponse[] response = statManager.getStatPub(param);
      System.out.println("statManager.getStatPub: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

  @Test
  @Tag("UnitTest")
  public void testGroupStatPub() {
    PiliStatModel.GroupStatPubRequest param = statModel.new GroupStatPubRequest();
    param.commonRequest = statModel.new GetStatCommonRequest();
    param.commonRequest.begin = commentReq.begin;
    param.commonRequest.end = commentReq.end;
    param.commonRequest.g = commentReq.g;
    param.where = where;
    param.select = select;
    try {
      PiliStatModel.StatGroupResponse[] response = statManager.groupStatPub(param);
      System.out.println("statManager.groupStatPub: success: " + response);
      assertNotNull(response);
    } catch (Exception e) {
      System.out.println("statManager.groupStatUpflow: response: ");
      System.out.println(e);
      assertNull(e);
    }
  }

}

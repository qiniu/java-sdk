package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.NameSpaceManager;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.util.Auth;
import test.com.qiniu.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class NameSpaceTest {
    Auth auth = TestConfig.testAuth;
    private NameSpaceManager nameSpaceManager;
    private Response res = null;
    private Response res2 = null;
    private final String namespaceId = "3nm4x1e0xw855";
    private final String name = "" + System.currentTimeMillis();

    @BeforeEach
    public void setUp() throws Exception {
        this.nameSpaceManager = new NameSpaceManager(auth);
    }

/*  @Test
    @Tag("IntegrationTest")
    public void testCreateNameSpace() {
        NameSpace nameSpace = new NameSpace();
        nameSpace.setName(name);
        nameSpace.setAccessType("rtmp");
        nameSpace.setRtmpUrlType(NameSpace.Static);
        nameSpace.setDomains(new String[]{name + ".qnlinking.com"});
        try {
            res = nameSpaceManager.createNameSpace(nameSpace);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
*/

    @Test
    @Tag("IntegrationTest")
    public void testQueryNameSpace() {
        try {
            res = nameSpaceManager.queryNameSpace(namespaceId);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testUpdateNameSpace() {
        PatchOperation[] patchOperation = { new PatchOperation("replace", "recordTemplateApplyAll", true) };
        try {
            res = nameSpaceManager.updateNameSpace(namespaceId, patchOperation);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testListNameSpace() {
        int offset = 0;
        int line = 1;
        String sortBy = "asc:updatedAt";
        try {
            res = nameSpaceManager.listNameSpace(offset, line, sortBy);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

/*  @Test
    @Tag("IntegrationTest")
    public void testDisableNameSpace() {
        try {
            res = nameSpaceManager.disableNameSpace(namespaceId);
            res2 = nameSpaceManager.enableNameSpace(namespaceId);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    @Tag("IntegrationTest")
    public void testEnableNameSpace() {
        try {
            res = nameSpaceManager.enableNameSpace(namespaceId);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
*/

//    @Test
//    @Tag("IntegrationTest")
//    public void testDeleteNameSpace() {
//        try {
//            res = nameSpaceManager.deleteNameSpace("3nm4x1e07mmvz");
//            System.out.println(res.bodyString());
//        } catch (QiniuException e) {
//            e.printStackTrace();
//        } finally {
//            if (res != null) {
//                res.close();
//            }
//        }
//    }

}

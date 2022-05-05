package test.com.qiniu.qvs;

import com.qiniu.http.Response;
import com.qiniu.qvs.TemplateManager;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.BeforeEach;
import test.com.qiniu.TestConfig;

public class TemplateTest {

    Auth auth = TestConfig.testAuth;
    private TemplateManager templateManager;
    private Response res = null;
    private final String templateId = "2xenzwlwgi7mf";
    private String templateName = "" + System.currentTimeMillis();

    @BeforeEach
    public void setUp() throws Exception {
        this.templateManager = new TemplateManager(auth);
    }

 /* @Test
    @Tag("IntegrationTest")
    public void testCreateTemplate() {
        Template template = new Template();
        template.setName(templateName);
        template.setBucket("qiniusdk");
        template.setTemplateType(1);
        template.setJpgOverwriteStatus(true);
        template.setRecordType(2);
        try {
            res = templateManager.createTemplate(template);
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
    public void testQueryTemplate() {
        try {
            res = templateManager.queryTemplate(templateId);
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
    public void testUpdateTemplate() {
        templateName = "" + System.currentTimeMillis();
        PatchOperation[] patchOperation = { new PatchOperation("replace", "name", templateName) };
        try {
            res = templateManager.updateTemplate(templateId, patchOperation);
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
    public void testListTemplate() {
        int offset = 0;
        int line = 1;
        int templateType = 1;
        String match = "test";
        try {
            res = templateManager.listTemplate(offset, line, templateType, match);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }

//    @Test
//    @Tag("IntegrationTest")
//    public void testDeleteTemplate() {
//        try {
//            res = templateManager.deleteTemplate("2xenzwlx661su");
//            System.out.println(res.bodyString());
//        } catch (QiniuException e) {
//            e.printStackTrace();
//        } finally {
//            if (res != null) {
//                res.close();
//            }
//        }
//    }
*/
}

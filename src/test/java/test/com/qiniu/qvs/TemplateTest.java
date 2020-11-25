package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.TemplateManager;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.qvs.model.Template;
import com.qiniu.util.Auth;
import org.junit.Before;
import org.junit.Test;
import test.com.qiniu.TestConfig;

public class TemplateTest {

    Auth auth = TestConfig.testAuth;
    private TemplateManager templateManager;
    private Response res = null;
    private final String templateId = "2akrarsl22iil";

    @Before
    public void setUp() throws Exception {
        this.templateManager = new TemplateManager(auth);
    }

    @Test
    public void testCreateTemplate() {
        Template template = new Template();
        template.setName("testtemplate002");
        template.setBucket("Testforhugo");
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
    public void testUpdateTemplate() {
        PatchOperation[] patchOperation = {new PatchOperation("replace", "name", "testtemplate002")};
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


    @Test
    public void testDeleteTemplate() {
        try {
            res = templateManager.deleteTemplate(templateId);
            System.out.println(res.bodyString());
        } catch (QiniuException e) {
            e.printStackTrace();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
}

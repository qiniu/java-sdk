package test.com.qiniu.qvs;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.qvs.TemplateManager;
import com.qiniu.qvs.model.PatchOperation;
import com.qiniu.qvs.model.Template;
import com.qiniu.util.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test.com.qiniu.TestConfig;

public class TemplateTest {

    Auth auth = TestConfig.testAuth;
    private TemplateManager templateManager;
    private Response res = null;
    private final String templateId = "2xenzwlwgi7mf";

    @BeforeEach
    public void setUp() throws Exception {
        this.templateManager = new TemplateManager(auth);
    }

    @Test
    @Tag("IntegrationTest")
    public void testCreateTemplate() {
        Template template = new Template();
        template.setName("testtemplate003");
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
        PatchOperation[] patchOperation = { new PatchOperation("replace", "name", "testtemplate004") };
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

    @Test
    @Tag("IntegrationTest")
    public void testDeleteTemplate() {
        try {
            res = templateManager.deleteTemplate("3nm4x1e0x1ajc");
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

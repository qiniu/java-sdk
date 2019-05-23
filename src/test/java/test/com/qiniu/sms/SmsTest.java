package test.com.qiniu.sms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.FormSubmitEvent.MethodType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.sms.SmsManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import test.com.qiniu.TestConfig;

public class SmsTest {
	private SmsManager smsManager;
	
	/**
     * 初始化
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.smsManager = new SmsManager(Auth.create(TestConfig.smsAccessKey, TestConfig.smsSecretKey));
        }

    
    @Test
    public void testSendMessage() {
        try {
        	Map<String,String> paramMap = new HashMap();
            Response resp = smsManager.sendMessage("test",new String[]{"10086"},paramMap);
            
        } catch (QiniuException e) {
        	Assert.assertEquals(401, e.code());
        }
    }
    
    @Test
    public void testComposeHeader() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<SmsManager> clazz = SmsManager.class;
		Method declaredMethod = clazz.getDeclaredMethod("composeHeader", new Class[] {String.class,String.class,byte[].class,String.class});
		declaredMethod.setAccessible(true);
		Object invoke = declaredMethod.invoke(this.smsManager, new Object[] {"http://sms.qiniuapi.com",MethodType.GET.toString(),null,Client.DefaultMime});
		declaredMethod.setAccessible(false);
		StringMap headerMap = (StringMap)invoke;
		Assert.assertEquals("application/octet-stream", headerMap.get("Content-Type"));
		Assert.assertEquals("Qiniu test:uwduNrdHyYG9mTUFVBy8xzLg104=", headerMap.get("Authorization"));
    }

}

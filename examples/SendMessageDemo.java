import java.util.HashMap;
import java.util.Map;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.sms.SmsManager;
import com.qiniu.util.Auth;

public class SendMessageDemo {
	public static void main(String args[]) {
		// 设置需要操作的账号的AK和SK
		String ACCESS_KEY = "fv8Pft_71sLxh6AN-EqDt7QSa1J_wt5y3yTGGhCW";
		String SECRET_KEY = "rb3bIUxBKm6aEYGMW_cfpBVMdy-QDPeP_VuObmtV";
		Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);

		// 实例化一个SmsManager对象
		SmsManager smsManager = new SmsManager(auth);

		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("p1",name);
					map.put("p2",time);
					map.put("p3",place);
					map.put("templateId","1386991105744056320");
			Response resp = smsManager.sendMessage("1386991105744056320", new String[] { "15859288526" }, map);
//          Response resp = smsManager.describeSignature("passed", 0, 0);
//          Response resp = smsManager.createSignature("signature", "app",
//                  new String[] { "data:image/gif;base64,xxxxxxxxxx" });
//          Response resp = smsManager.describeTemplate("passed", 0, 0);
//          Response resp = smsManager.createTemplate("name", "template", "notification", "test", "signatureId");
//          Response resp = smsManager.modifyTemplate("templateId", "name", "template", "test", "signatureId");
//          Response resp = smsManager.modifySignature("SignatureId", "signature");
//          Response resp = smsManager.deleteSignature("signatureId");
//          Response resp = smsManager.deleteTemplate("templateId");
			System.out.println(resp.bodyString());

//          SignatureInfo sinfo = smsManager.describeSignatureItems("", 0, 0);
//          System.out.println(sinfo.getItems().get(0).getAuditStatus());
//          TemplateInfo tinfo = smsManager.describeTemplateItems("", 0, 0);
//          System.out.println(tinfo.getItems().get(0).getAuditStatus());


		} catch (QiniuException e) {
			System.out.println(e);
		}

	}
}

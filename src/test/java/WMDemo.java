import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.Client;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.PublishRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;
import com.qiniu.qbox.wm.WMService;
import com.qiniu.qbox.wm.WMTemplate;


public class WMDemo {

	public static void main(String[] args) throws Exception {
		String DEMO_DOMAIN = "http://m1.qbox.me:13019/wmbucket";
		String bucket = "wmbucket";
		String customer = "wmCustomer";
		String key = "test.jpg";
		
		//generate a uptoken
		AuthPolicy wmpolicy = new AuthPolicy(bucket, 3600, customer);
		String wmtoken = wmpolicy.makeAuthTokenString();
		Client conn = new DigestAuthClient();
		RSService rs = new RSService(conn, bucket);
		WMService wms = new WMService(conn);
		String path = RSDemo.class.getClassLoader().getResource("").getPath();
		
		CallRet ret = rs.setProtected(1);
		System.out.println("set protected:");
		if (ret.statusCode == 200) {
			System.out.println("setProtected success!");
		} else {
			System.out.println("result:" + ret.statusCode  + ret.response);			
		}
		
		ret = rs.setSeparator("_");
		System.out.println("set separator:");		
		if (ret.statusCode == 200) {
			System.out.println("setSeparator success!");
		} else {
			System.out.println("result:" + ret.statusCode + ret.response);			
		}
		
		ret = rs.SetStyle("wmsmall.jpg", "imageView/0/w/64/h/64/watermark/0");
		System.out.println("set wmsmall.jpg style:");
		if (ret.statusCode == 200) {
			System.out.println("setStyle wmsmall.jpg success!");
		} else {
			System.out.println("result:" + ret.statusCode + ret.response);			
		}
		
		ret = rs.SetStyle("wmmid.jpg", "imageView/0/w/128/h/128/watermark/1");
		System.out.println("set wmmid.jpg style:");
		if (ret.statusCode == 200) {
			System.out.println("setStyle wmmid.jpg success!");
		} else {
			System.out.println("result:" + ret.statusCode + ret.response);			
		}		
		
		WMTemplate defaultTpl = new WMTemplate("defaultCustomer", 40, 20);
		ret = wms.set("", defaultTpl);
		System.out.println("set defaultCustomer watermark template:");
		if (ret.statusCode == 200) {
			System.out.println("set defaultCustomer watermark success!");
		} else {
			System.out.println("result:" + ret.statusCode + ret.response);
		}
		ret = wms.get("");
		WMTemplate getDefaultTpl = new WMTemplate(ret);
		System.out.println("get defaultCustomer watermark template:");
		if (ret.statusCode == 200) {
			System.out.println("get defaultCustomer watermark success:");
			System.out.println("  text: " + getDefaultTpl.getText() + "\n  dx: " + getDefaultTpl.getDx() + "\n  dy " + getDefaultTpl.getDy());
		} else {
			System.out.println("result:" + ret.statusCode + ret.response);
		}
		

		WMTemplate customerTpl = new WMTemplate(customer, 30, 30);
		ret = wms.set(customer, customerTpl);
		System.out.println("set " + customer + "watermark template:");
		if (ret.statusCode == 200) {
			System.out.println("set " + customer + " watermark success！");
		} else {
			System.out.println("result:" + ret.statusCode + ret.response);
		}
		
		ret = wms.get(customer);
		WMTemplate getCustomerTpl = new WMTemplate(ret);
		System.out.println("get " +customer +" watermark template:");
		if (ret.statusCode == 200) {
			System.out.println("get " + customer +" watermark success:");
			System.out.println("  text: " + getCustomerTpl.getText() + "\n  dx: " + getCustomerTpl.getDx() + "\n  dy: " + getCustomerTpl.getDy());
		} else {
			System.out.println("result:" + ret.statusCode + ret.response);
		}
		
		String url = Config.UP_HOST + "/upload";
        System.out.println("put local file: " + path + key); 
        
		PutFileRet putFileRet = RSClient.putFile(url, bucket, key, "", path + key, "customMeta", "", wmtoken);
		
		if (!putFileRet.ok()) {
			System.out.println("Failed to put file " + path + key + ": " + putFileRet);
		} else {
			System.out.println("Hashcode:"  + putFileRet.getHash());
		}
		
		System.out.println("Publish " + bucket + " as: " + DEMO_DOMAIN);
		PublishRet publishRet = rs.publish(DEMO_DOMAIN);
		System.out.println("Result of publish: " + (publishRet.ok() ? "Succeeded." : "Failed."));
	}
}

import com.qiniu.qbox.Config;
import com.qiniu.qbox.oauth2.AuthException;
import com.qiniu.qbox.oauth2.Client;
import com.qiniu.qbox.oauth2.SimpleClient;
import com.qiniu.qbox.rs.DeleteRet;
import com.qiniu.qbox.rs.DropRet;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.PublishRet;
import com.qiniu.qbox.rs.PutAuthRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSException;
import com.qiniu.qbox.rs.RSService;
import com.qiniu.qbox.rs.StatRet;


public class RSDemo {

	public static void main(String[] args) throws AuthException, RSException {
		
		Client conn = new SimpleClient();
		try {
			conn.exchangeByPassword("test@qbox.net", "test");
		} catch (AuthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String tblName = "tblName";
		String key = "RSDemo.class";
		
		String path = RSDemo.class.getClassLoader().getResource("").getPath();
        System.out.println("Test to put local file: " + path + key);   
		
		RSService rs = new RSService(conn, tblName);
		PutAuthRet putAuthRet = rs.putAuth();
		System.out.println("Result of putAuth: " + putAuthRet.getUrl());
		
		System.out.println("Putting file: " + path + key);
		PutFileRet putFileRet = RSClient.putFile(putAuthRet.getUrl(), tblName, key, "", path + key, "", null);
		if (!putFileRet.ok()) {
			System.out.println("Failed to put file " + path + key + ": " + putFileRet.getResult());
			return;
		}

		try {
			StatRet statRet = rs.stat(key);
			if (!statRet.ok()) {
				System.out.println("Failed to stat " + key + ": " + statRet.getResult());
			} else {
				System.out.println("Result of stat() for " + key);
				System.out.println("  Hash: " + statRet.getHash());
				System.out.println("  Fsize: " + String.valueOf(statRet.getFsize()));
				System.out.println("  MimeType: " + statRet.getMimeType());
				System.out.println("  PutTime: " + String.valueOf(statRet.getPutTime()));
			}

			GetRet getRet = rs.get(key, key);
			System.out.println("Result of get() for " + key);
			if (!getRet.ok()) {
				System.out.println("Failed to get " + key + ": " + getRet.getResult());
			} else {
				System.out.println("  Hash: " + getRet.getHash());
				System.out.println("  Fsize: " + String.valueOf(getRet.getFsize()));
				System.out.println("  MimeType: " + getRet.getMimeType());
				System.out.println("  URL: " + getRet.getUrl());
			}
			
			GetRet getIfNotModifiedRet = rs.getIfNotModified(key, key, getRet.getHash());
			System.out.println("Result of getIfNotModified() for " + key);
			if (!getIfNotModifiedRet.ok()) {
				System.out.println("  Did not find " + key + ", or, the entity has been changed.");
			} else {
				System.out.println("  Hash: " + getIfNotModifiedRet.getHash());
				System.out.println("  Fsize: " + String.valueOf(getIfNotModifiedRet.getFsize()));
				System.out.println("  MimeType: " + getIfNotModifiedRet.getMimeType());
				System.out.println("  URL: " + getIfNotModifiedRet.getUrl());
			}
			
			System.out.println("Publish " + tblName + " as: " + Config.DEMO_DOMAIN + "/" + tblName);
			PublishRet publishRet = rs.publish(Config.DEMO_DOMAIN + "/" + tblName);
			System.out.println("Result of publish: " + (publishRet.ok() ? "Succeeded." : "Failed."));

			System.out.println("Unpublish " + tblName + " as: " + Config.DEMO_DOMAIN + "/" + tblName);
			PublishRet unpublishRet = rs.unpublish(Config.DEMO_DOMAIN + "/" + tblName);
			System.out.println("Result of unpublish: " + (unpublishRet.ok() ? "Succeeded." : "Failed."));

			System.out.println("Delete " + key);
			DeleteRet deleteRet = rs.delete(key);
			System.out.println("Result of delete: " + (deleteRet.ok() ? "Succeeded." : "Failed."));
			
			System.out.println("Drop table " + tblName);
			DropRet dropRet = rs.drop();
			System.out.println("Result of drop: " + (dropRet.ok() ? "Succeeded." : "Failed."));
			
		} catch (Exception e) {
			System.out.println("Failed testing " + key + " with reason:" + e.getMessage());
		}
	}
}

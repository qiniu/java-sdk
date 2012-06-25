import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.OAuth2Client;
import com.qiniu.qbox.auth.UpTokenClient;
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
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;
import com.qiniu.qbox.up.UpClient;


public class RSDemo {
	
	public static void main(String[] args) throws RSException {
		
		OAuth2Client conn = new OAuth2Client();
		conn.exchangeByPassword("test@qbox.net", "test");
		
		String tblName = "tblName";
		String key = "CPP_SDK.rar"; // "RSDemo.class";
		
		String path = RSDemo.class.getClassLoader().getResource("").getPath();
        System.out.println("Test to put local file: " + path + key);   
		
		RSService rs = new RSService(conn, tblName);
		PutAuthRet putAuthRet = rs.putAuth();
		System.out.println("Put URL: " + putAuthRet.getUrl());
		
		HashMap<String, String> callbackParams = new HashMap<String, String>();
		callbackParams.put("key", key);
		
		System.out.println("Delete " + key);
		DeleteRet deleteRet = rs.delete(key);
		System.out.println("Result of delete: " + (deleteRet.ok() ? "Succeeded." : "Failed."));
		
//		System.out.println("Putting file: " + path + key);
//		PutFileRet putFileRet = RSClient.putFile(
//				putAuthRet.getUrl(), tblName, key, "", path + key, "CustomData", callbackParams);
//		if (!putFileRet.ok()) {
//			System.out.println("Failed to put file " + path + key + ": " + putFileRet);
//			return;
//		}
		
		// Resumable Put
		
		System.out.println("Resumably putting file: " + path + key);
		// test@qbox.net
		// AccessKey: RLT1NBD08g3kih5-0v8Yi6nX6cBhesa2Dju4P7mT 
		// SecretKey: k6uZoSDAdKBXQcNYG3UOm4bP3spDVkTg-9hWHIKm 
		byte[] accessKey = "RLT1NBD08g3kih5-0v8Yi6nX6cBhesa2Dju4P7mT".getBytes();
		byte[] secretKey = "k6uZoSDAdKBXQcNYG3UOm4bP3spDVkTg-9hWHIKm".getBytes();
		
		AuthPolicy policy = new AuthPolicy("tblName", "", "", System.currentTimeMillis() / 1000 + 3600);
		
		UpTokenClient upTokenClient = new UpTokenClient(accessKey, secretKey, policy);
		
		UpClient upClient = new UpClient(upTokenClient);
		
		try {
			
			RandomAccessFile f = new RandomAccessFile(path + key, "r");

			long fsize = f.length();
			int blockCount = (int)(fsize + UpClient.BLOCK_SIZE - 1) / UpClient.BLOCK_SIZE;
			
			String[] checksums = new String[blockCount];
			BlockProgress[] progresses = new BlockProgress[blockCount];
			
			Notifier notif = new Notifier();
			
			PutFileRet putFileRet = RSClient.resumablePutFile(upClient, 
					checksums, progresses, 
					(ProgressNotifier)notif, (BlockProgressNotifier)notif, 
					tblName + ":" + key, "", f, fsize, "CustomMeta", "");
			
			if (putFileRet.ok()) {
				System.out.println("Successfully put file resumably: " + putFileRet.getHash());
			} else {
				System.out.println("Failed to put file resumably: " + putFileRet.getException());
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			StatRet statRet = rs.stat(key);
			if (!statRet.ok()) {
				System.out.println("Failed to stat " + key + ": " + statRet);
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
				System.out.println("Failed to get " + key + ": " + getRet);
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
			deleteRet = rs.delete(key);
			System.out.println("Result of delete: " + (deleteRet.ok() ? "Succeeded." : "Failed."));
			
			System.out.println("Drop table " + tblName);
			DropRet dropRet = rs.drop();
			System.out.println("Result of drop: " + (dropRet.ok() ? "Succeeded." : "Failed."));
			
		} catch (Exception e) {
			System.out.println("Failed testing " + key + " with reason:" + e.getMessage());
		}
	}
}

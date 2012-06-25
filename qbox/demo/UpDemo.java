import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.UpTokenClient;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;
import com.qiniu.qbox.up.UpClient;
import com.qiniu.qbox.up.UpService;


public class UpDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String tblName = "tblName";
		String key = "RSDemo.class";
		
		String path = RSDemo.class.getClassLoader().getResource("").getPath();
		
		System.out.println("Resumably putting file: " + path + key);
		// test@qbox.net
		// AccessKey: RLT1NBD08g3kih5-0v8Yi6nX6cBhesa2Dju4P7mT 
		// SecretKey: k6uZoSDAdKBXQcNYG3UOm4bP3spDVkTg-9hWHIKm 
		byte[] accessKey = "RLT1NBD08g3kih5-0v8Yi6nX6cBhesa2Dju4P7mT".getBytes();
		byte[] secretKey = "k6uZoSDAdKBXQcNYG3UOm4bP3spDVkTg-9hWHIKm".getBytes();
		
		AuthPolicy policy = new AuthPolicy("tblName", "", "", System.currentTimeMillis() / 1000 + 3600);
		
		UpTokenClient upTokenClient = new UpTokenClient(accessKey, secretKey, policy);
		
		UpService upClient = new UpService(upTokenClient);
		
		try {
			
			RandomAccessFile f = new RandomAccessFile(path + key, "r");

			long fsize = f.length();
			long blockCount = UpService.blockCount(fsize);
			
			String[] checksums = new String[(int)blockCount];
			BlockProgress[] progresses = new BlockProgress[(int)blockCount];
			
			Notifier notif = new Notifier();
			
			PutFileRet putFileRet = UpClient.resumablePutFile(upClient, 
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

	}

}

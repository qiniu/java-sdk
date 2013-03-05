import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.json.JSONObject;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.UpTokenClient;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;
import com.qiniu.qbox.up.UpService;

public class ResumablePutDemo {

	public static void readProgress(String file, String[] checksums,
			BlockProgress[] progresses, int blockCount) throws Exception {
		File fi = new File(file);
		if (!fi.exists()) {
			return;
		}
		FileReader f = new FileReader(file);
		BufferedReader is = new BufferedReader(f);

		for (;;) {
			String line = is.readLine();
			if (line == null)
				break;

			JSONObject o = new JSONObject(line);

			Object block = o.get("block");

			if (block == null) {
				// error ...
				break;
			}
			int blockIdx = (Integer) block;
			if (blockIdx < 0 || blockIdx >= blockCount) {
				// error ...
				break;
			} else {

			}

			Object checksum = null;
			if (o.has("checksum")) {
				checksum = o.get("checksum");
			}

			if (checksum != null) {
				checksums[blockIdx] = (String) checksum;
				continue;
			}

			JSONObject progress = null;
			if (o.has("progress")) {
				progress = (JSONObject) o.get("progress");
			}

			if (progress != null) {
				BlockProgress bp = new BlockProgress();
				bp.context = progress.getString("context");
				bp.offset = progress.getInt("offset");
				bp.restSize = progress.getInt("restSize");
				bp.host = progress.getString("host");
				progresses[blockIdx] = bp;

				continue;
			}
			break; // error ...
		}
	}

	public static void main(String[] args) throws Exception {
		Config.ACCESS_KEY = "<Please apply your access key>";
		Config.SECRET_KEY = "<Dont send your secret key to anyone>";

		String bucketName = "bucket";
		String key = "upload.txt";
		String path = System.getProperty("user.dir") ;
		String inputFile = path + "/" + key;
		
		AuthPolicy policy = new AuthPolicy(bucketName, 3600);
		String token = policy.makeAuthTokenString();

		UpTokenClient upTokenClient = new UpTokenClient(token);
		UpService upClient = new UpService(upTokenClient);

		RandomAccessFile f = null ;
		try {
			f = new RandomAccessFile(inputFile, "r");

			long fsize = f.length();
			int blockCount = UpService.blockCount(fsize);
			String progressFile = inputFile + ".progress" + fsize;
			String[] checksums = new String[(int) blockCount];
			BlockProgress[] progresses = new BlockProgress[(int) blockCount];

			readProgress(progressFile, checksums, progresses, blockCount);

			ResumableNotifier notif = new ResumableNotifier(progressFile);

			PutFileRet putFileRet = RSClient.resumablePutFile(upClient,
					checksums, progresses, (ProgressNotifier) notif,
					(BlockProgressNotifier) notif, bucketName, key, "", f,
					fsize, "CustomMeta", "");

			if (putFileRet.ok()) {
				System.out.println("Successfully put file resumably: "+ putFileRet.getHash());
				File progress = new File(progressFile) ;
				progress.delete() ;
			} else {
				System.out.println("Failed to put file resumably: " + putFileRet);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (f != null) {
					f.close() ;
					f = null ;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

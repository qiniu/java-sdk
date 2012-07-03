import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.json.JSONObject;

import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;


public class ResumableNotifier implements ProgressNotifier, BlockProgressNotifier {

	private PrintStream os;
	
	public ResumableNotifier(String progressFile) throws Exception {
		OutputStream out = new FileOutputStream(progressFile, true);
		this.os = new PrintStream(out, true);
	}
	
	@Override
	public void notify(int blockIndex, String checksum) {
	
		try {
			HashMap<String, Object> doc = new HashMap<String, Object>();
			doc.put("block", blockIndex);
			doc.put("checksum", checksum);
			String json = JSONObject.valueToString(doc);
			os.println(json);
		} catch (Exception e) {
			// nothing to do;
		}
	}

	@Override
	public void notify(int blockIndex, BlockProgress progress) {

		try {
			HashMap<String, Object> doc = new HashMap<String, Object>();
			doc.put("block", blockIndex);
			doc.put("progress", progress);
			String json = JSONObject.valueToString(doc);
			os.println(json);
		} catch (Exception e) {
			// nothing to do;
		}
	}
}

package com.qiniu.testing;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.qiniu.api.resumable.io.BlockProgress;
import com.qiniu.api.resumable.io.BlockProgressNotifier;
import com.qiniu.api.resumable.io.ProgressNotifier;

public class ResumableNotifier implements ProgressNotifier,
		BlockProgressNotifier {

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
			e.printStackTrace();
		}
	}

	@Override
	public void notify(int blockIndex, BlockProgress progress) {

		try {
			HashMap<String, Object> doc = new HashMap<String, Object>();
			doc.put("block", blockIndex);

			Map<String, String> map = new HashMap<String, String>();
			map.put("context", progress.context);
			map.put("offset", progress.offset + "");
			map.put("restSize", progress.restSize + "");
			map.put("host", progress.host);
			doc.put("progress", map);

			String json = JSONObject.valueToString(doc);
			os.println(json);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
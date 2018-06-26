import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;

public class VideoPulpTest {

	public static void main(String[] args) throws QiniuException, UnsupportedEncodingException {
		//构建OpParamsLabels
		OpParamsLabels opParamsLabels = new OpParamsLabels("1", 1, 0.5f);
		//构建List<OpParamsLabels>
		List<OpParamsLabels> opParamsLabelsArray = new ArrayList<>();
		opParamsLabelsArray.add(opParamsLabels);
		//构建Terminate
		Map<String, Integer> map = new HashMap<>();
		map.put("1", 2);
		Terminate terminate = new Terminate(1, map);
		//构建OpParams
		OpParams opParams = new OpParams(opParamsLabelsArray, terminate);
		//构建OpChild
		OpChild opChild = new OpChild("pulp", "", opParams);
		//构建List<OpChild>
		List<OpChild> opChildArray = new ArrayList<>();
		opChildArray.add(opChild);
		//构建Save
		Save save = new Save("gebo", "");
		//构建Vframe
		Vframe vframe = new Vframe(0, 2);
		//构建Params
		Params params = new Params(false, vframe, save, "");
		//构建Data
		Data data = new Data("http://p9w3dsbag.bkt.clouddn.com/meier.mp4");
		//构建PulpParams
		PulpParams 	pulpParams = new PulpParams(data, params, opChildArray);
		VideoPulp vp = new VideoPulp("GWRaeafsFAfJ7luwiG83rHddEHJ6u76pWI5EDDzX", "BLlscza4aH9dVKyTEf3EnrsYo6gMdkEo2_-L2Pgy", "gebo", pulpParams);
		Response response = vp.videoPulp();
		System.out.println(response.toString());

	}

}

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
/**
 * 视频鉴黄
 * @author gebo
 *
 */
public class VideoPulp {

	private Client client;
	private Auth auth;
	public String ak;
	public String sk;
	public String vid;
	public PulpParams pulpParams;

	public VideoPulp(String ak, String sk, String vid, PulpParams pulpParams) {
		super();
		this.ak = ak;
		this.sk = sk;
		this.vid = vid;
		this.pulpParams = pulpParams;
		this.client = new Client();
		this.auth = Auth.create(ak, sk);
	}

	public Response videoPulp() throws QiniuException, UnsupportedEncodingException {
		Response response = null;
		Gson gson = new Gson();
		String requestBody = gson.toJson(pulpParams);
		System.out.println(requestBody);
		// 创建请求Url
		String Url = "http://argus.atlab.ai/v1/video/" + vid;
		// 创建请求头 包含七牛鉴权
		StringMap headers = auth.authorizationV2(Url, "POST", requestBody.getBytes("UTF-8"), Client.JsonMime);
		try {
			// 发送请求
			response = client.post(Url, requestBody.getBytes("UTF-8"), headers, Client.JsonMime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public String getAk() {
		return ak;
	}

	public void setAk(String ak) {
		this.ak = ak;
	}

	public String getSk() {
		return sk;
	}

	public void setSk(String sk) {
		this.sk = sk;
	}

	public String getVid() {
		return vid;
	}

	public void setVid(String vid) {
		this.vid = vid;
	}

	public PulpParams getPulpParams() {
		return pulpParams;
	}

	public void setPulpParams(PulpParams pulpParams) {
		this.pulpParams = pulpParams;
	}
	
	

}

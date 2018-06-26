/**
 * construct List<OpChild> in Pulpparams
 * gebo
 *
 */
public class OpChild {

	public String op; // 视频检测执行的命令，支持多种视频检测操作。目前，视频鉴黄的命令就是pulp
	public String hookURL; // 单个命令的回调地址
	public OpParams params;

	public OpChild() {
		super();
	}

	public OpChild(String op, String hookURL, OpParams params) {
		super();
		this.op = op;
		this.hookURL = hookURL;
		this.params = params;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getHookURL() {
		return hookURL;
	}

	public void setHookURL(String hookURL) {
		this.hookURL = hookURL;
	}

	public OpParams getParams() {
		return params;
	}

	public void setParams(OpParams params) {
		this.params = params;
	}

}

import java.util.List;
/**
 * parameters for construct request body
 * 
 * -参数构建层次
 * --PulpParams
 * 
 * ---Data
 * 
 * ---Params
 * ----Vframe
 * ----Save
 * 
 * ---List<OpChild>
 * ----OpChild
 * -----OpParams
 * ------List<OpParamsLabels>
 * -------OpParamsLabels
 * ------Terminate
 *
 */
public class PulpParams {
	public Data data; // 视频地址 格式是 {"uri":"http://xxx"}
	public Params params; // params
	public List<OpChild> ops; // ops

	public PulpParams() {
		super();
	}

	public PulpParams(Data data, Params params, List<OpChild> ops) {
		super();
		this.data = data;
		this.params = params;
		this.ops = ops;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}

	public List<OpChild> getOps() {
		return ops;
	}

	public void setOps(List<OpChild> ops) {
		this.ops = ops;
	}

}

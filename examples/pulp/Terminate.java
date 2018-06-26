import java.util.Map;

/**
 * construct Terminate in OpParams
 * 
 * @author gebo
 *
 */
public class Terminate {

	public Integer mode; // 视频检测命令提前停止处理的参数。1表示按帧计数；2表示按片段计数
	public Map<String, Integer> labels; // 第一个String参数：视频检测命令返回的label。例如，视频鉴黄的命令pulp的label有0色情,
										// 1性感, 2正常。
										// 第二个Integer参数:设置该类别的最大个数，达到该值则处理过程退出

	public Terminate() {
		super();
	}

	public Terminate(Integer mode, Map<String, Integer> labels) {
		super();
		this.mode = mode;
		this.labels = labels;
	}

	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public Map<String, Integer> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, Integer> labels) {
		this.labels = labels;
	}

}

/**
 * construct List<OpParamsLabels> in OpParams
 * 
 * @author gebo
 *
 */
public class OpParamsLabels {

	public String label; // 对某个命令返回label进行过滤，与ops.op.params.labels.select、ops.op.params.labels.score一起使用。例如，视频鉴黄的命令pulp的label有0色情,
							// 1性感, 2正常。
	public Integer select; // 对ops.op.params.labels.label中设置的label,设置过滤条件，1表示忽略不选，2表示只选该类别。
	public Float score; // 过滤返回label结果的置信度参数，当ops.op.params.labels.select=1时表示忽略不选小于该设置的结果，当select=2时表示只选大于等于该设置的结果

	public OpParamsLabels() {
		super();
	}

	public OpParamsLabels(String label, Integer select, Float score) {
		super();
		this.label = label;
		this.select = select;
		this.score = score;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getSelect() {
		return select;
	}

	public void setSelect(Integer select) {
		this.select = select;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

}

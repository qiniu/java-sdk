/**
 * construct Vframe in Params
 * 
 * @author gebo
 *
 */
public class Vframe {
	public Integer mode; // 截帧逻辑，可选值为[0,
							// 1]。0表示每隔固定时间截一帧，固定时间在vframe.interval中设定；1表示截关键帧。不填表示取默认值1。
	public Integer interval; // 当params.vframe.mode取0时，用来设置每隔多长时间截一帧，单位s,
							// 不填则取默认值5s

	public Vframe() {
		super();
	}

	public Vframe(Integer mode, Integer interval) {
		super();
		this.mode = mode;
		this.interval = interval;
	}

	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

}

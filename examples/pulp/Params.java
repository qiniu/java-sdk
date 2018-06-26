/**
 * construct Params in Pulpparams
 * 
 * @author gebo
 *
 */
public class Params {

	public Boolean async; // True是异步处理，False是同步处理，不填则取默认值False
	public Vframe vframe;
	public Save save;
	public String hookURL; // 视频检测结束后的回调地址

	public Params() {
		super();
	}

	public Params(Boolean async, Vframe vframe, Save save, String hookURL) {
		super();
		this.async = async;
		this.vframe = vframe;
		this.save = save;
		this.hookURL = hookURL;
	}

	public Boolean getAsync() {
		return async;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}

	public Vframe getVframe() {
		return vframe;
	}

	public void setVframe(Vframe vframe) {
		this.vframe = vframe;
	}

	public Save getSave() {
		return save;
	}

	public void setSave(Save save) {
		this.save = save;
	}

	public String getHookURL() {
		return hookURL;
	}

	public void setHookURL(String hookURL) {
		this.hookURL = hookURL;
	}

}

/**
 * construct Save in Pulpparams
 * 
 * @author gebo
 *
 */
public class Save {

	public String bucket; // 保存截帧图片的Bucket名称
	public String prefix; // 截帧图片名称的前缀，图片名称的格式为<prefix>/<video_id>/<op>/<offset>

	public Save() {
		super();
	}

	public Save(String bucket, String prefix) {
		super();
		this.bucket = bucket;
		this.prefix = prefix;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}

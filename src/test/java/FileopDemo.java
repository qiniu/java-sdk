import java.util.HashMap;
import java.util.Map;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.Fileop;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.ImageInfoRet;
import com.qiniu.qbox.rs.PutAuthRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;

public class FileopDemo {

	public static void main(String[] args) throws Exception {
		// Config.ACCESS_KEY = "<Please apply your access key>";
		// Config.SECRET_KEY = "<Dont send your secret key to anyone>";
		Config.ACCESS_KEY = "AB4dwG3RXyN9aaxQImbhUGK482uHFaTe2IgDszsX";
		Config.SECRET_KEY = "F3PC1FvnspkjgcRI73fTZKx8ijyOEg4WsGt04KEi";

		DigestAuthClient conn = new DigestAuthClient();
		String bucketName = "bucket";
		String key = "upload.jpg";
		String path = System.getProperty("user.dir");
		System.out.println("Test to put local image: " + path + "/" + key + "\n");

		RSService rs = new RSService(conn, bucketName);
		PutAuthRet putAuthRet = rs.putAuth();
		String putUrl = putAuthRet.getUrl();
		System.out.println("Put URL: " + putUrl);

		Map<String, String> callbackParams = new HashMap<String, String>();
		callbackParams.put("key", key);

		PutFileRet putFileRet = RSClient.putFile(putAuthRet.getUrl(),
				bucketName, key, "", key, "CustomData", callbackParams);
		if (!putFileRet.ok()) {
			System.out.println("Failed to put file " + path + "/" + key + ": " + putFileRet);
			return;
		} else {
			System.out.println("Put file " + path + "/" + key + " successfully.\n");
		}

		GetRet getRet = rs.get(key, key);
		if (!getRet.ok()) {
			System.out.println("RS get failed : " + getRet) ;
			return ;
		}
		String imgDownloadUrl = getRet.getUrl();
		System.out.println("Image Download Url : " + imgDownloadUrl + "\n");

		Fileop fp = new Fileop();
		// get the image info from the specified url
		ImageInfoRet imgInfoRet = rs.imageInfo(fp.getImageInfoURL(imgDownloadUrl));
		if (imgInfoRet.ok()) {
			System.out.println("Resulst of imageInfo() : ");
			System.out.println("format     : " + imgInfoRet.getFormat());
			System.out.println("width      : " + imgInfoRet.getWidth());
			System.out.println("height     : " + imgInfoRet.getHeight());
			System.out.println("colorModel : " + imgInfoRet.getColorMode()); 
			System.out.println() ;
		} else {
			System.out.println("Fileop getImageInfo failed : " + imgInfoRet) ;
		}
		
		// get the exif info from the specified image url
		CallRet imgExRet = rs.imageEXIF(fp.getImageExifURL(imgDownloadUrl));
		if (imgExRet.ok()) {
			System.out.println("Result of imageEXIF()  : ");
			System.out.println(imgExRet.getResponse());
			System.out.println() ;
		} else {
			System.out.println("Fileop getImgExif failed : " + imgExRet) ;
		}

		// get image preview url
		String imgPreviewUrl = fp.getImagePreviewURL(imgDownloadUrl, 1);
		System.out.println("imgPreviewUrl : " + imgPreviewUrl + "\n");

		// get image view url
		Map<String, String> imgViewOpts = new HashMap<String, String>();
		imgViewOpts.put("mode", "1");
		imgViewOpts.put("w", "100");
		imgViewOpts.put("h", "200");
		imgViewOpts.put("q", "1");
		imgViewOpts.put("format", "jpg");
		imgViewOpts.put("sharpen", "100");
		String imgViewUrl = fp.getImageViewURL(imgDownloadUrl, imgViewOpts);
		System.out.println("image view url : " + imgViewUrl + "\n");
		
		// get image mogrify url
		Map<String, String> opts = new HashMap<String, String>();
		opts.put("thumbnail", "!120x120r");
		opts.put("gravity", "center");
		opts.put("crop", "!120x120a0a0");
		opts.put("quality", "85");
		opts.put("rotate", "45");
		opts.put("format", "jpg");
		opts.put("auto_orient", "True");
		String mogrifyPreviewUrl = fp.getImageMogrifyURL(imgDownloadUrl, opts);
		System.out.println("ImageMogrifyPreviewUrl : " + mogrifyPreviewUrl + "\n");

		
		CallRet imgSaveAsRet = rs.imageMogrifySaveAs("test", key,
				imgDownloadUrl, opts);
		if (imgSaveAsRet.ok()) {
			System.out.println("ImageMogrSaveAs successfully!");
		} else {
			System.out.println("ImageMogrSaveAs fail : " + imgSaveAsRet);
		}
	}
}
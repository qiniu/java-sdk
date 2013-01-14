import java.util.HashMap;
import java.util.Map;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.rs.Fileop;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.ImageInfoRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;

public class FileopDemo {

	public static void main(String[] args) throws Exception {
		Config.ACCESS_KEY = "<Please apply your access key>";
		Config.SECRET_KEY = "<Dont send your secret key to anyone>";
				
		String bucketName = "bucket";
		String key = "upload.jpg";
		String path = System.getProperty("user.dir");
		System.out.println("Test to put local image: " + path + "/" + key + "\n");

		Map<String, String> callbackParams = new HashMap<String, String>();
		callbackParams.put("key", key);

		AuthPolicy policy = new AuthPolicy(bucketName, 3600);
		String token = policy.makeAuthTokenString();
		PutFileRet putRet = RSClient.putFileWithToken(token, bucketName, key, path+"/"+key, "", "", "", "2") ;
		if (putRet.ok()) {
			System.out.println("Upload " + path+"/"+key + " with token successfully!") ;
		} else {
			System.out.println("Upload " + path+"/"+key + " with token failed!") ;
			return ;
		}
		
		DigestAuthClient conn = new DigestAuthClient();
		RSService rs = new RSService(conn, bucketName);
		GetRet getRet = rs.get(key, key);
		if (!getRet.ok()) {
			System.out.println("RS get failed : " + getRet) ;
			return ;
		}
		
		String imgDownloadUrl = getRet.getUrl();
		System.out.println("Image Download Url : " + imgDownloadUrl + "\n");

		// get the image info from the specified url
		ImageInfoRet imgInfoRet = rs.imageInfo(Fileop.getImageInfoURL(imgDownloadUrl));
		if (imgInfoRet.ok()) {
			System.out.println("Resulst of imageInfo() : ");
			System.out.println("format     : " + imgInfoRet.getFormat());
			System.out.println("width      : " + imgInfoRet.getWidth());
			System.out.println("height     : " + imgInfoRet.getHeight());
			System.out.println("colorModel : " + imgInfoRet.getColorMode()); 
			System.out.println() ;
		} else {
			System.out.println("Fileop getImageInfo failed : " + imgInfoRet) ;
			return ;
		}
		
		// get the exif info from the specified image url
		CallRet imgExRet = rs.imageEXIF(Fileop.getImageExifURL(imgDownloadUrl));
		if (imgExRet.ok()) {
			System.out.println("Result of imageEXIF()  : ");
			System.out.println(imgExRet.getResponse());
			System.out.println() ;
		} else {
			System.out.println("Fileop getImgExif failed or has no exif data. " + imgExRet) ;
		}

		// get image view url
		Map<String, String> imgViewOpts = new HashMap<String, String>();
		imgViewOpts.put("mode", "1");
		imgViewOpts.put("w", "100");
		imgViewOpts.put("h", "200");
		imgViewOpts.put("q", "1");
		imgViewOpts.put("format", "jpg");
		imgViewOpts.put("sharpen", "100");
		String imgViewUrl = Fileop.getImageViewURL(imgDownloadUrl, imgViewOpts);
		System.out.println("imageView url : " + imgViewUrl + "\n");
		
		// get image mogrify url
		Map<String, String> opts = new HashMap<String, String>();
		opts.put("thumbnail", "!120x120r");
		opts.put("gravity", "center");
		opts.put("crop", "!120x120a0a0");
		opts.put("quality", "85");
		opts.put("rotate", "45");
		opts.put("format", "jpg");
		opts.put("auto_orient", "True");
		String mogrifyPreviewUrl = Fileop.getImageMogrifyURL(imgDownloadUrl, opts);
		System.out.println("ImageMogrifyUrl : " + mogrifyPreviewUrl + "\n");
		
		CallRet imgSaveAsRet = rs.imageMogrifySaveAs("test", key,
				imgDownloadUrl, opts);
		if (imgSaveAsRet.ok()) {
			System.out.println("ImageMogrSaveAs successfully!");
		} else {
			System.out.println("ImageMogrSaveAs fail : " + imgSaveAsRet);
		}
	}
}
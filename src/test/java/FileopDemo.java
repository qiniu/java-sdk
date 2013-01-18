import java.util.HashMap;
import java.util.Map;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.auth.AuthPolicy;
import com.qiniu.qbox.auth.CallRet;
import com.qiniu.qbox.auth.DigestAuthClient;
import com.qiniu.qbox.fileop.ImageExif;
import com.qiniu.qbox.fileop.ImageInfo;
import com.qiniu.qbox.fileop.ImageInfoRet;
import com.qiniu.qbox.fileop.ImageMogrify;
import com.qiniu.qbox.fileop.ImageView;
import com.qiniu.qbox.rs.GetRet;
import com.qiniu.qbox.rs.PutFileRet;
import com.qiniu.qbox.rs.RSClient;
import com.qiniu.qbox.rs.RSService;


public class FileopDemo {

	public static void main(String[] args) throws Exception {
		Config.ACCESS_KEY = "<Please apply your access key>";
		Config.SECRET_KEY = "<Do not send your secret key to anyone>";

		String bucketName = "bucket";
		String key = "upload.jpg";
		String path = System.getProperty("user.dir");
		System.out.println("Test to put local image: " + path + "/" + key + "\n");

		// upload an image to the qiniu cloud platform
		Map<String, String> callbackParams = new HashMap<String, String>();
		callbackParams.put("key", key);
		AuthPolicy policy = new AuthPolicy(bucketName, 3600);
		String token = policy.makeAuthTokenString();
		PutFileRet putRet = RSClient.putFileWithToken(token, bucketName, key, path+"/"+key, "", "", "", "") ;
		if (putRet.ok()) {
			System.out.println("Upload " + path+"/"+key + " with token successfully!") ;
		} else {
			System.out.println("Upload " + path+"/"+key + " with token failed! " + putRet) ;
			return ;
		}
		
		// get image download url
		DigestAuthClient conn = new DigestAuthClient();
		RSService rs = new RSService(conn, bucketName);
		GetRet getRet = rs.get(key, key);
		if (!getRet.ok()) {
			System.out.println("RS get failed : " + getRet) ;
			return ;
		}
		String imageUrl = getRet.getUrl();
		System.out.println("Image Download Url : " + imageUrl + "\n");
		
		// imageInfo demo
		ImageInfo imgInfo = new ImageInfo(imageUrl, conn) ;
		ImageInfoRet imgInfoRet = imgInfo.call() ;
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
		
		// imageExif demo
		ImageExif imgExif = new ImageExif(imageUrl, conn) ;
		CallRet imgExifRet = imgExif.call() ;
		if (imgExifRet.ok()) {
			System.out.println("Result of imageEXIF()  : ");
			System.out.println(imgExifRet.getResponse());
			System.out.println() ;
		} else {
			System.out.println("Fileop getImgExif failed or has no exif data. " + imgExifRet) ;
		}
		
		// imageView demo
		Map<String, String> imgViewOpts = new HashMap<String, String>() ;
		imgViewOpts.put("mode", "1");
		imgViewOpts.put("w", "100");
		imgViewOpts.put("h", "200");
		imgViewOpts.put("q", "1");
		imgViewOpts.put("format", "jpg");
		imgViewOpts.put("sharpen", "100");
		ImageView imgView = new ImageView(imgViewOpts) ;
		String imgViewUrl = imgView.makeURL(imageUrl) ;
		System.out.println("imageView url : " + imgViewUrl) ;
		
		// imageMogr demo
		Map<String, String> imgMogrOpts = new HashMap<String, String>() ;
		imgMogrOpts.put("thumbnail", "!120x120r");
		imgMogrOpts.put("gravity", "center");
		imgMogrOpts.put("crop", "!120x120a0a0");
		imgMogrOpts.put("quality", "85");
		imgMogrOpts.put("rotate", "45");
		imgMogrOpts.put("format", "jpg");
		imgMogrOpts.put("auto_orient", "True");
		ImageMogrify imgMogrify = new ImageMogrify(imgMogrOpts) ;
		String imgMogrUrl = imgMogrify.makeURL(imageUrl) ;
		System.out.println("imageMogrify url : " + imgMogrUrl) ;
	}
}

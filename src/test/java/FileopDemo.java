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
		ImageInfo imgInfo = new ImageInfo(imageUrl) ;
		ImageInfoRet imgInfoRet = imgInfo.call() ;
		if (imgInfoRet.ok()) {
			System.out.println("Resulst of imageInfo() : ");
			System.out.println("format     : " + imgInfoRet.format);
			System.out.println("width      : " + imgInfoRet.width);
			System.out.println("height     : " + imgInfoRet.height);
			System.out.println("colorModel : " + imgInfoRet.colorMode); 
			System.out.println() ;
		} else {
			System.out.println("Fileop getImageInfo failed : " + imgInfoRet) ;
			return ;
		}
		
		// imageExif demo
		ImageExif imgExif = new ImageExif(imageUrl) ;
		CallRet imgExifRet = imgExif.call() ;
		if (imgExifRet.ok()) {
			System.out.println("Result of imageEXIF()  : ");
			System.out.println(imgExifRet.getResponse());
			System.out.println() ;
		} else {
			System.out.println("Fileop getImgExif failed or has no exif data. " + imgExifRet) ;
		}
		
		// imageView demo
		ImageView imgView = new ImageView(imageUrl) ;
		imgView.mode = 1 ;
		imgView.width = 100 ;
		imgView.height = 200 ;
		imgView.quality = 1 ;
		imgView.format = "jpg" ;
		imgView.sharpen = 100 ;
		System.out.println("imageView url : " + imgView.makeURL()) ;
		CallRet imgViewRet = imgView.call() ;
		System.out.println("Result of imageView: " + (imgViewRet.ok() ? "Succeeded." : imgViewRet)) ;
		
		// imageMogr demo
		ImageMogrify imgMogr = new ImageMogrify(imageUrl) ;
		imgMogr.thumbnail = "!120x120r" ;
		imgMogr.gravity = "center" ;
		imgMogr.crop = "!120x120a0a0" ;
		imgMogr.quality = 85 ;
		imgMogr.rotate = 45 ;
		imgMogr.format = "jpg" ;
		imgMogr.autoOrient = true ;
		System.out.println("imageMogrify url : " + imgMogr.makeURL()) ;
		CallRet imgMogrRet = imgMogr.call() ;
		System.out.println("Result of imageMogr: " + (imgMogrRet.ok() ? "Succeeded." : imgMogrRet)) ;
	}
}

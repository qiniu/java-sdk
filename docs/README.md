---
title: Java SDK | 七牛云存储
---

# Java SDK 使用指南


此SDK适用于Java 6及以上版本。

SDK下载地址：[https://github.com/qiniu/java-sdk/tags](https://github.com/qiniu/java-sdk/tags)


**环境准备**

**应用接入**

- [获取Access Key 和 Secret Key](#acc-appkey)
- [签名认证](#acc-auth)

**云存储接口**

- [新建资源表](#rs-NewService)
- [获得上传授权](#rs-PutAuth)
- [上传文件](#rs-PutFile)
- [获取已上传文件信息](#rs-Stat)
- [下载文件](#rs-Get)
- [发布公开资源](#rs-Publish)
- [取消资源发布](#rs-Unpublish)
- [删除已上传的文件](#rs-Delete)
- [删除整张资源表](#rs-Drop)

## 环境准备

需要在Eclipse工程中，导入七牛云存储的 SDK。目前，七牛云存储的 SDK 依赖于一下第三方包：

- commons-codec-1.6.jar
- commons-logging-1.1.1.jar
- fluent-hc-4.2.jar
- httpclient-4.2.jar
- httpclient-cache-4.2.jar
- httpcore-4.2.1.jar
- httpcore-4.2.jar
- httpcore-ab-4.2.1.jar
- httpcore-nio-4.2.1.jar
- httpmime-4.2.jar

七牛云存储 SDK 中的 qbox/lib 目录默认已经包含这些第三方包，您直接使用就行。但是，也有可能因为你本地编译环境问题，需要重新载入这些包。

## 应用接入

<a name="acc-appkey"></a>

### 1. 获取Access Key 和 Secret Key

要接入七牛云存储，您需要拥有一对有效的 Access Key 和 Secret Key 用来进行签名认证。可以通过如下步骤获得：

1. [开通七牛开发者帐号](https://dev.qiniutek.com/signup)
2. [登录七牛开发者自助平台，查看 Access Key 和 Secret Key](https://dev.qiniutek.com/account/keys) 。

<a name="acc-auth"></a>

### 2. 签名认证

首先，到 [https://github.com/qiniu/java-sdk/tags](https://github.com/qiniu/java-sdk/tags) 下载SDK源码。

然后，将SDK导入到您的 Eclipse 项目中，并编辑当前工程目录下QBox.config文件，确保其包含您从七牛开发者平台所获取的 [Access Key 和 Secret Key](#acc-appkey)：

    ACCESS_KEY	: "<Please apply your access key>";
	SECRET_KEY	: "<Dont send your secret key to anyone>";



在完成 Access Key 和 Secret Key 配置后，为了正常使用该 SDK 提供的功能需要根据配置文件进行初始化，您还需要使用你获得的 Access Key 和 Secret Key 向七牛云存储服务器发出认证请求：

	Config.init("QBox.config"); // 此处通过传入配置文件完成初始化，您可以将配置文件存放任何位置，初始化时候要注意传入路径的正确性。
	DigestAuthClient conn = new DigestAuthClient();

请求成功后得到的 conn 即可用于您正常使用七牛云存储的一系列功能，接下来将一一介绍。

## 云存储接口

<a name="rs-NewService"></a>

### 1. 新建资源表

新建资源表的意义在于，您可以将所有上传的资源分布式加密存储在七牛云存储服务端后还能保持相应的完整映射索引。

    // 通过传入配置文件，完成初始化工作
    Config.init("QBox.config") ;

    // 首先定义资源表名
    String bucketName = "bucketName";
    
    // 然后获得签名认证
    DigestAuthClient conn = new DigestAuthClient();
    
    // 签名认证完成后，即可使用该认证来新建资源表
    RSService rs = new RSService(conn, bucketName);


<a name="rs-PutAuth"></a>

### 2. 获得上传授权

建完资源表，在上传文件并和该资源表建立关联之前，还需要取得上传授权。所谓上传授权，就是获得一个可匿名直传的且离客户端应用程序最近的一个云存储节点的临时有效URL。

要取得上传授权，只需调用已经实例化好的资源表对象的 putAuth() 方法。实例代码如下：

    // 获得上传授权之前需要通过签名认证的方式来实例化一个资源表对象
    Config.init("QBox.config") ;
    String bucketName = "bucketName";
    DigestAuthClient conn = new DigestAuthClient();
    RSService rs = new RSService(conn, bucketName);
    
    // 然后，调用该资源表对象的 putAuth() 方法来获得上传授权
    PutAuthRet putAuthRet = rs.putAuth();


如果请求成功，putAuthRet 会包含 url 和 expires_in 两个字段。url 字段对应的值为匿名上传的临时URL，expires_in 对应的值则是该临时URL的有效期。

<a name="rs-PutFile"></a>

### 3. 上传文件

七牛云存储上传文件的方式分为直传和断点续传两种。

##### 1.文件上传
Java SDK 目前支持从本地上传某个文件，用户可以根据需要选择是直传还是断点续传。  
要以直传的方式上传文件，用户只需获得相关认证就可以上传文件了。  
如果上传成功，得到的 putFileRet 会包含对应的 hash 值，否则返回对应的错误。

示例代码如下：
   
    // 获得认证
    Config.init("QBox.config") ;
    String bucketName = "bucketName";
    AuthPolicy policy = new AuthPolicy(bucketName, 3600);
    String token = policy.makeAuthTokenString();
    UpTokenClient upTokenClient = new UpTokenClient(token);
    UpService upClient = new UpService(upTokenClient);

    // 上传文件
    String key = "README.md";
    String path = RSDemo.class.getClassLoader().getResource("").getPath();		
    RandomAccessFile f = new RandomAccessFile(path + key, "r");
    long fsize = f.length();
    long blockCount = UpService.blockCount(fsize);
    String[] checksums = new String[(int)blockCount];
    BlockProgress[] progresses = new BlockProgress[(int)blockCount];
    // 由于此处的 Notifer 未提供任何的"持久化"功能，所以此处相当于文件直传
    Notifier notif = new Notifier();

    PutFileRet putFileRet = RSClient.resumablePutFile(upClient, 
		checksums, progresses, 
		(ProgressNotifier)notif, (BlockProgressNotifier)notif, 
		bucketName, key, "", f, fsize, "CustomMeta", "");

	


##### 2. 断点续上传

客户端在上传文件的时候也可以根据需求选择断点续上传的方式，此处所说的断点上传是指用户在某次上传过程中出现错误，再重新上传的时候只需要从上次上传失败处上传即可。用户可以根据通过修改配置文件改变上传块（Config文件中的PUT_CHUNK_SIZE对应的值）的大小来适应用户所处的网络环境。为了提供一个简单的接口，我们将上传进度持久化的相关工作内置在了 SDK 中，当然用户也可以根据需要自己实现文件上传进度的持久化工作。

如果上传成功，得到的 putFileRet 会包含对应的 hash 值，否则返回对应的错误。

示例代码如下：  

    Config.init("/home/wangjinlei/QBox.config") ;
	String bucketName = "bucketName";
	String key = "RSDemo.class";
				
	AuthPolicy policy = new AuthPolicy("bucketName", 3600);
	String token = policy.makeAuthTokenString();
	UpTokenClient upTokenClient = new UpTokenClient(token);
	UpService upClient = new UpService(upTokenClient);
	PutFileRet  putFileRet = null ;
	
	putFileRet = RSClient.resumablePutFile(upClient,
				bucketName, key, "", inputFile, "CustomMeta", "", "");



<a name="rs-Stat"></a>

### 4. 获取已上传文件信息

您可以调用资源表对象的 Stat() 方法并传入一个 Key（类似ID）来获取指定文件的相关信息。

	// 实例化一个资源表对象，并获得一个相应的授权认证
    Config.init("QBox.config") ;
	String bucketName = "bucketName";
	DigestAuthClient conn = new DigestAuthClient();
    RSService rs = new RSService(conn, bucketName);
    
    // 获取资源表中特定文件信息
    StatRet statRet = rs.stat(key);

如果请求成功，得到的 statRet 数组将会包含如下几个字段：

    hash: <FileETag>
    fsize: <FileSize>
    putTime: <PutTime>
    mimeType: <MimeType>


<a name="rs-Get"></a>

### 5. 下载文件

要下载一个文件，首先需要取得下载授权，所谓下载授权，就是取得一个临时合法有效的下载链接，只需调用资源表对象的 Get() 方法并传入相应的 文件ID 和下载要保存的文件名 作为参数即可。示例代码如下：

	// 实例化一个资源表对象，并获得一个下载已上传文件信息的授权认证
    Config.init("QBox.config") ;
	String bucketName = "bucketName";
	DigestAuthClient conn = new DigestAuthClient();
    RSService rs = new RSService(conn, bucketName);
    
    // 下载资源表中的特定文件
    GetRet getRet = rs.get(key, key);


注意，这并不会直接将文件下载并命名为一个 example.jpg 的文件。当请求执行成功，Get() 方法返回的 getRet 变量将会包含如下字段：

    url: <DownloadURL> # 获取文件内容的实际下载地址
    hash: <FileETag>
    fsize: <FileSize>
    mimeType: <MimeType>
    expires:<Seconds> ＃下载url的实际生命周期，精确到秒

这里所说的断点续传指断点续下载，所谓断点续下载，就是已经下载的部分不用下载，只下载基于某个“游标”之后的那部分文件内容。相对于资源表对象的 Get() 方法，调用断点续下载方法 GetIfNotModified() 需额外再传入一个 $baseVersion 的参数作为下载的内容起点。示例代码如下：

    // 以断点续下载的方式下载资源表中的某个文件
    GetRet getIfNotModifiedRet = rs.getIfNotModified(key, key, getRet.getHash());

GetIfNotModified() 方法返回的结果包含的字段同 Get() 方法一致。

<a name="rs-Publish"></a>

### 6. 发布公开资源

使用七牛云存储提供的资源发布功能，您可以将一个资源表里边的所有文件以静态链接可访问的方式公开发布到您自己的域名下。

要公开发布一个资源表里边的所有文件，只需调用改资源表对象的 Publish() 方法并传入 域名 作为参数即可。如下示例：

	// 实例化一个资源表对象，并获得一个发布公开资源的授权认证
    Config.init("QBox.config") ;
	String bucketName = "bucketName";
	DigestAuthClient conn = new DigestAuthClient();
    RSService rs = new RSService(conn, bucketName);
    String DEMO_DOMAIN = "http://test.dn.qbox.me";
    
    // 公开发布某个资源表
    PublishRet publishRet = rs.publish(DEMO_DOMAIN + "/" + bucketName);

<a name="rs-Unpublish"></a>

### 7. 取消资源发布

调用资源表对象的 Unpublish() 方法可取消该资源表内所有文件的静态外链。

    // 实例化一个资源表对象，并获得一个取消发布公开资源的授权认证
    Config.init("QBox.config") ;
	String bucketName = "bucketName";
    String DEMO_DOMAIN = "http://iovip.qbox.me/test";
	DigestAuthClient conn = new DigestAuthClient();
    RSService rs = new RSService(conn, bucketName);
    
    // 取消公开发布某个资源表
    PublishRet unpublishRet = rs.unpublish(DEMO_DOMAIN + "/" + bucketName);

<a name="rs-Delete"></a>

### 8. 删除已上传的文件

要删除指定的文件，只需调用资源表对象的 Delete() 方法并传入 文件ID（key）作为参数即可。如下示例代码：

    // 实例化一个资源表对象，并获得一个删除资源表中特定文件的授权认证
    Config.init("QBox.config") ;
	String bucketName = "bucketName";
	DigestAuthClient conn = new DigestAuthClient();
    RSService rs = new RSService(conn, bucketName);
    
    // 删除资源表中的某个文件
    DeleteRet deleteRet = rs.delete(key);


<a name="rs-Drop"></a>

### 9. 删除整张资源表

要删除整个资源表及该表里边的所有文件，可以调用资源表对象的 Drop() 方法。
需慎重，这会删除整个表及其所有文件

    // 实例化一个资源表对象，并获得一个删除整张资源表的授权认证
    Config.init("QBox.config") ;
	String bucketName = "bucketName";
	DigestAuthClient conn = new DigestAuthClient();
    RSService rs = new RSService(conn, bucketName);
    
    // 删除资源表中的某个文件
    DropRet dropRet = rs.drop();

##图像处理接口
### 1. 查看图片属性信息

SDK 提供的 com.qiniu.qbox.rs.RSService 中的 imageInfo 方法，可以基于一张存储于七牛云存储服务器上的图片，针对其下载链接来获取该张图片的属性信息。  
参数 ：  
imgUrl  
必须，字符串类型（String），图片的下载链接，需是 com.qiniu.qbox.rs.RSService 中 get 方法返回结果中 url 字段的值，且文件本身必须是图片。

返回值：  
如果请求成功得到的返回结果 imgInfoRet 包含如下的字段：  
  
    "format"	       // 原始图片类型
    "width"      	   // 原始图片宽度，单位像素
    "height"     	   // 原始图片高度，单位像素
    "colorModel" 	   // 原始图片着色模式
示例程序如下：  

    Config.init("QBox.config") ;  
    DigestAuthClient conn = new DigestAuthClient();
    String bucketName = "testPhotos";
    String key = "dao.jpg";
    
    RSService rs = new RSService(conn, bucketName);
    PutAuthRet putAuthRet = rs.putAuth();
    String putUrl = putAuthRet.getUrl();
    Map<String, String> callbackParams = new HashMap<String, String>();
    callbackParams.put("key", key);
    PutFileRet putFileRet = RSClient.putFile(putAuthRet.getUrl(),
    		bucketName, key, "", key, "CustomData", callbackParams);
    GetRet getRet = rs.get(key, key);
    // 得到图片的下载url
    String imgDownloadUrl = getRet.getUrl() ;
    
    FileOp fp = new FileOp() ;
    ImageInfoRet imgInfoRet = rs.imageInfo(fp.getImageInfoURL(imgDownloadUrl)) ;

### 2. 获取图片EXIF信息

RSservice 中的 imageEXIF 方法，可以基于一张存储于七牛云存储服务器上的原始图片图片，取到该图片的 EXIF 信息。 
  
参数：  
imgUrl  
必须，字符串类型（String），图片的下载链接，需是 com.qiniu.qbox.rs.RSService 中 get 方法返回结果中 url 字段的值，且文件本身必须是图片。

返回值：  
如果请求参数 url 所代表的图片没有 EXIF 信息，返回结果中的响应字段为 false。否则，返回一个包含 EXIF 信息的 Hash 结构。

示例代码：

    CallRet imgExRet = rs.imageEXIF(fp.getImageEXIFURL(imgDownloadUrl)) ;


### 3. 获取指定规格的缩略图预览地址

FileOp 中的 getImageViewURL 方法，可以基于一张存储于七牛云存储服务器上的图片，针对其下载链接，以及指定的缩略图规格类型，来获取该张图片的缩略图地址。 

参数：  
imgUrl  
必须，字符串类型（String），图片的下载链接，需是 com.qiniu.qbox.rs.RSService 中 get 方法返回结果中 url 字段的值，且文件本身必须是图片。

opts 
必须，Hash Map 格式的图像处理参数。  

返回值：  
返回一个字符串类型的缩略图 URL

示例代码：
    
    Map<String, String> imgViewOpts = new HashMap<String, String>() ;
	imgViewOpts.put("mode", "1") ;
	imgViewOpts.put("w", "100") ;
	imgViewOpts.put("h", "100") ;
	imgViewOpts.put("q", "1") ;
	imgViewOpts.put("format", "jpg") ;
	imgViewOpts.put("sharpen", "10") ;
	String imgViewUrl = fp.getImageViewURL(imgDownloadUrl, imgViewOpts) ;
	System.out.println("image view url : " + imgViewUrl) ;
  

### 4. 高级图像处理（缩略、裁剪、旋转、转化）

FileOp 中的 getImageMogrifyPreviewURL 方法支持将一个存储在七牛云存储的图片进行缩略、裁剪、旋转和格式转化处理，该方法返回一个可以直接预览缩略图的URL。

参数：  
imgUrl  
必须，字符串类型（String），图片的下载链接，需是 com.qiniu.qbox.rs.RSService 中 get 方法返回结果中 url 字段的值，且文件本身必须是图片。

opts 
必须，Hash Map 格式的图像处理参数。  
具体规格如下：
    
    thumbnail    <ImageSizeGeometry> 
    gravity      <GravityType>=NorthWest, North, NorthEast, West, Center, East, SouthWest, South, SouthEast
    crop         <ImageSizeAndOffsetGeometry>
    quality      <ImageQuality> 
    rotate       <RotateDegree> 
    format       <DestinationImageFormat> =jpg, gif, png, tif, etc.
    auto_orient  <TrueOrFalse>

返回值：  
返回一个可以预览最终缩略图的URL，String 类型。

示例代码：
    
    Map<String, String> opts = new HashMap<String, String>() ;
    opts.put("thumbnail", "!120x120r") ;
    opts.put("gravity", "center") ;
    opts.put("crop", "!120x120a0a0") ;
    opts.put("quality", "85") ;
    opts.put("rotate", "45") ;
    opts.put("format", "jpg") ;
    opts.put("auto_orient", "True") ;
    String mogrifyPreviewUrl = fp.getImageMogrifyPreviewURL(imgDownloadUrl, opts) ;
### 5. 高级图像处理（缩略、裁剪、旋转、转化）并持久化存储处理结果

RSService 中的 imageMogrifySaveAs 方法支持将一个存储在七牛云存储的图片进行缩略、裁剪、旋转和格式转化处理，并且将处理后的缩略图作为一个新文件持久化存储到七牛云存储服务器上，这样就可以供后续直接使用而不用每次都传入参数进行图像处理。

参数：  
targetBucketName  
必须，字符串类型（string），指定最终缩略图要存放的 bucket 。  
  
targetKey  
必须，字符串类型（string），指定最终缩略图存放在云存储服务端的唯一文件ID。

srcImgUrl  
必须，字符串类型（string），指定原始图片的下载链接，可以根据 rs.get() 获取到。

opts  
必须，Hash Map 格式的图像处理参数。  
具体规格如下：
    
    thumbnail    <ImageSizeGeometry> 
    gravity      <GravityType>=NorthWest, North, NorthEast, West, Center, East, SouthWest, South, SouthEast
    crop         <ImageSizeAndOffsetGeometry>
    quality      <ImageQuality> 
    rotate       <RotateDegree> 
    format       <DestinationImageFormat> =jpg, gif, png, tif, etc.
    auto_orient  <TrueOrFalse>


返回值：  
如果请求失败，返回错误信息；否则，返回如下一个 Hash 类型的结构： 
 
    {"hash":"FiA388M_-D3Gt-RXBYl4J3U1c96a"}

示例代码：
    
    CallRet imgSaveAsRet = rs.imageMogrifySaveAs("testTarget", key, imgDownloadUrl, opts) ;
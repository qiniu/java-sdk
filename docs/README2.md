---
title: Java SDK | 七牛云存储
---

# Java SDK 使用指南

此SDK适用于Java 6及以上版本。基于 [七牛云存储官方API](/v3/api/) 构建。使用此 SDK 构建您的网络应用程序，能让您以非常便捷地方式将数据安全地存储到七牛云存储上。无论您的网络应用是一个网站程序，还是包括从云端（服务端程序）到终端（手持设备应用）的架构的服务或应用，通过七牛云存储及其 SDK，都能让您应用程序的终端用户高速上传和下载，同时也让您的服务端更加轻盈。

SDK下载地址：[https://github.com/qiniu/java-sdk/tags](https://github.com/qiniu/java-sdk/tags)

**目录**

- [环境准备](#env_preparation)
- [接入](#turn-on)
    - [配置密钥（AccessKey / SecretKey）](#establish_connection!)
- [使用](#Usage)
    - [文件上传](#upload)
        - [生成上传授权凭证（uploadToken）](#generate-upload-token)
        - [上传文件](#upload-server-side)
        - [断点续上传](#resumable-upload)
    - [文件下载](#download)
        - [公有资源下载](#download-public-files)
        - [私有资源下载](#download-private-files)
            - [生成下载授权凭证（downloadToken）](#download-token)
        - [高级特性](#other-download-features)
            - [断点续下载](#resumable-download)
            - [自定义 404 NotFound](#upload-file-for-not-found)
    - [文件管理](#file-management)
        - [查看单个文件属性信息](#stat)
        - [复制单个文件](#copy)
        - [移动单个文件](#move)
        - [删除单个文件](#delete)
        - [批量操作](#batch)
            - [批量获取文件属性信息](#batch-get)
            - [批量复制文件](#batch-copy)
            - [批量移动文件](#batch-move)
            - [批量删除文件](#batch-delete)
    - [云处理](#cloud-processing)
        - [图像](#image-processing)
            - [查看图片属性信息](#image-info)
            - [查看图片EXIF信息](#image-exif)
            - [图像在线处理（缩略、裁剪、旋转、转化）](#image-mogrify-for-preview)
            - [图像在线处理（缩略、裁剪、旋转、转化）后并持久化存储](#image-mogrify-for-save-as)
        - 音频(TODO)
        - 视频(TODO)
- [贡献代码](#Contributing)
- [许可证](#License)


<a name="env_preparation"></a>

## 环境准备

安装 Maven 的插件：[The Maven Integration for Eclipse](http://www.eclipse.org/m2e/)

添加依赖

	<dependency>
		<groupId>com.qiniu</groupId>
		<artifactId>sdk</artifactId>
		<version>2.4.2</version>
	</dependency>


<a name="turn-on"></a>

## 接入

<a name="establish_connection!"></a>

### 配置密钥（AccessKey / SecretKey）

要接入七牛云存储，您需要拥有一对有效的 `Access Key` 和 `Secret Key` 用来进行签名认证。可以通过如下步骤获得：

1. [开通七牛开发者帐号](https://dev.qiniutek.com/signup)
2. [登录七牛开发者自助平台，查看 Access Key 和 Secret Key](https://dev.qiniutek.com/account/keys) 。

在获取到 `Access Key` 和 `Secret Key` 之后，编辑 `com.qiniu.qbox` 包下的 `Config.java` 文件，确保其包含您从七牛开发者平台所获取的 `Access Key` 和 `Secret Key`：

    public static String ACCESS_KEY = "<Please apply your access key>";
    public static String SECRET_KEY = "<Please apply your secret key>";

<a name="Usage"></a>

## 使用

<a name="upload"></a>

### 文件上传

**注意**：如果您只是想要上传已存在您电脑本地或者是服务器上的文件到七牛云存储，可以直接使用七牛提供的 [qrsync](/v3/tools/qrsync/) 上传工具。如果是需要通过您的网站或是移动应用(App)上传文件，则可以接入使用此 SDK，详情参考如下文档说明。

<a name="generate-upload-token"></a>

#### 生成上传授权凭证（uploadToken）

要上传一个文件，首先需要调用 SDK 提供的 `com.qiniu.qbox.auth` 包下的`PutPolicy`这个类来获取一个经过授权用于临时匿名上传的 `upload_token`——经过数字签名的一组数据信息，该 `upload_token` 作为文件上传流中 `multipart/form-data` 的一部分进行传输。 

示例代码如下： 

	String bucketName = "imageBucket";  
	long expiry = 3600; // an hour  
	PutPolicy upPolicy = new PutPolicy(bucketName, expiry);   
	String upToken = upPolicy.token();   

更多详细信息请参见[生成上传授权](http://docs.qiniutek.com/v3/api/io/#upload-token)

#### 服务端上传文件

通过 `com.qiniu.qbox.rs` 包下 `RSClient` 类的 `putFileWithToken` 方法可在客户方的业务服务器上直接往七牛云存储上传文件。

方法签名如下：

    public static PutFileRet putFileWithToken(String upToken, 
                                             String bucketName,  
                                             String key,   
                                             String localFile,   
                                             String mimeType,   
                                             String customMeta,  
                                             Object callbackParam,   
                                             String rotate) throws Exception

**参数**

    upToken:必须，字符此类型。调用PutPolicy的token()方法生成。
    bucketName:必须，字符此类型，空间名称。
    key:必须，字符串类型，若把 Bucket 理解为关系性数据库的某个表，那么 key 类似数据库里边某个表的主键ID，需给每一个文件一个UUID用于进行标示。
    localFile:必须，本地文件的绝对路径。
    mimeType:可选，文件的 mime-type 值。如若不传入，缺省使用 application/octet-stream 代替之。
    customMeta:可选，文件备注。
    callbackParam:可选，文件上传成功后，七牛云存储向客户方业务服务器发送的回调参数。
    rotate:可选，可选，上传图片时专用，可针对图片上传后进行旋转。该参数值为 0 ：表示根据图像EXIF信息自动旋转；值为 1 : 右转90度；值为 2 :右转180度；值为 3 : 右转270度。

**返回值**

    如果上次成功，我们可以得到上次文件对应的hash值，否则会返回相应的错误信息。

示例代码如下：

	String key = "upload.jpg" ;
	String dir = System.getProperty("user.dir") ;
	String absFilePath = dir + "/" + key ;

	String bucketName = "bucket" ;
	PutPolicy policy = new PutPolicy(bucketName, 3600);
	String uptoken = policy.token();
	
	PutFileRet putRet = RSClient.putFileWithToken(uptoken, bucketName, key, absFilePath, "", "", "", "2") ;


<a name="resumable-upload"></a>

##### 断点续上传

用户在上传文件的时候也可以根据需求选择断点续上传的方式，此处所说的断点上传是指用户在某次上传过程中出现故障（比如断网，断电等异常情况）导致上传失败，再重新上传的时候只需要从上次上传失败处上传即可。用户可以根据具体应用的需求通过修改配置文件改变上传块（`com.qiniu.qbox` 包下的 `Config` 文件中的 `PUT_CHUNK_SIZE` 对应的值）的大小来适应用户所处的网络环境。具体的示例代码可以参见我们在SDK中提供的 `ResumableGUIPutDemo` 以及 `ResumablePutDemo` 两个例子。

<a name="download"></a>

### 文件下载


#### 公有资源下载

    [GET] http://<bucket>.qiniudn.com/<key>

或者，

    [GET] http://<绑定域名>/<key>

绑定域名可以是自定义域名，可以在 [七牛云存储开发者自助网站](https://dev.qiniutek.com/buckets) 进行域名绑定操作。

注意，尖括号不是必需，代表替换项。

<a name="download-private-files"></a>

#### 私有资源下载

私有资源只能通过临时下载授权凭证(downloadToken)下载，下载链接格式如下：

    [GET] http://<bucket>.qiniudn.com/<key>?token=<downloadToken>

或者，

    [GET] http://<绑定域名>/<key>?token=<downloadToken>

<a name="download-token"></a>

##### 生成下载授权凭证（downloadToken）

`<downloadToken>` 可以使用 SDK 提供的 `com.qiniu.qbox.auth` 包下的 `GetPolicy` 类提供的方法生成：

示例代码如下:

    GetPolicy getPolicy = new GetPolicy(scope);
    String downloadToken = getPolicy.token();
    
关于参数的具体详解，请参见 [私有资源下载](http://docs.qiniutek.com/v3/api/io/#private-download)



#### 高级特性

<a name="resumable-download"></a>

##### 断点续下载

七牛云存储支持标准的断点续下载，参考：[云存储API之断点续下载](/v3/api/io/#download-by-range-bytes)

<a name="upload-file-for-not-found"></a>

##### 自定义 404 NotFound

您可以上传一个应对 HTTP 404 出错处理的文件，当用户访问一个不存在的文件时，即可使用您上传的“自定义404文件”代替之。要这么做，您只须使用JAVA_SDK中的上传文件函数上传一个 `key` 为固定字符串类型的值 `errno-404` 即可。

除了使用 SDK 提供的方法，同样也可以借助七牛云存储提供的命令行辅助工具 [qboxrsctl](/v3/tools/qboxrsctl/) 达到同样的目的：

    qboxrsctl put <Bucket> <Key> <LocalFile>

将其中的 `<Key>` 换作  `errno-404` 即可。

注意，每个 `<Bucket>` 里边有且只有一个 `errno-404` 文件，上传多个，最后的那一个会覆盖前面所有的。


<a name="file-management"></a>

### 文件管理

文件管理包括对存储在七牛云存储上的文件进行查看、复制、移动和删除处理。

<a name="stat"></a>

#### 查看单个文件属性信息

您可以调用资源表对象的 stat() 方法并传入一个 Key（类似ID）来获取指定文件的相关信息。

示例代码如下 :

	// 实例化一个资源表对象，并获得一个相应的授权认证
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


<a name="copy"></a>

#### 复制单个文件

要将一个文件从一个bucket复制到另一个bucket，可以通过使用 `RSService` 中 `copy` 方法来实现，其方法签名如下：  

    public CallRet copy(String entryUriSrc, String entryUriDest)
    

**参数**  

    entryUriSrc : 由源bucket以及key拼接而成   
    entryUriDest : 由目标bucket和key拼接而成 

**返回值**  

    如果请求成功, callRet.ok()为true；否则为false


<a name="move"></a>

#### 移动单个文件

要将一个文件从一个bucket移动到另一个bucket，可以通过使用 `RSService` 中 `move` 方法来实现，其方法签名如下：
   
    public CallRet move(String entryUriSrc, String entryUriDest) 

**参数**  

    entryUriSrc : 由源bucket以及key拼接而成   
    entryUriDest : 由目标bucket和key拼接而成 

**返回值**  

    如果请求成功，callRet.ok()为true；否则为false。
    需要补充说明的是，执行完 `move` 操作之后被移动的文件会从源bucket中移除。
 
<a name="delete"></a>

### 删除单个文件

要删除指定的文件，只需调用资源表对象的 delete() 方法并传入 文件ID（key）作为参数即可。

方法签名如下：

    public DeleteRet delete(String key) throws Exception 

**参数**

    所要删除文件对应的key。    

**返回值**

    如果删除成功，则deleteRet.ok()为true，否则为false。
     


如下示例代码：

	// 实例化一个资源表对象，并获得一个删除资源表中特定文件的授权认证
	String bucketName = "bucketName";
	DigestAuthClient conn = new DigestAuthClient();
	RSService rs = new RSService(conn, bucketName);

	// 删除资源表中的某个文件
	DeleteRet deleteRet = rs.delete(key);


<a name="batch"></a>

### 批量操作

为了更高效的处理文件，我们还提供了批量文件处理操作，现支持的操作有 `batchStat`, `batchDelete`, `batchCopy`, `batchMove`,下面分别进行阐述：
   
<a name="batch-stat"></a>

#### 批量获取文件属性信息

方法签名如下：

    public BatchStatRet batchStat(List<String> entryUris)

**参数**
    
    和stat方法类似，不同的是batchStat接受一个包含所有请求的entryUri的列表  

**返回值**
    
    如果操作成功，BatchStatRet对象的方法ok()为true，而且该对象所包含一个含有所有请求entryUri对应的stat结果。


<a name="batch-copy"></a>
#### 批量复制文件

方法签名如下：
    
    public BatchCallRet batchCopy(List<EntryUriPair> entryUriPairs)     

**参数**

    每一个`EntryUriPair`对象包含一对字符串，分别是源文件对应的entryUri以及目标文件对应的entryUri。

**返回值**
  
    BatchCallRet 会包含一个 `CallRet` 对象的列表, 每个CallRet对象对应一个key经过处理后的结果。

<a name="batch-move"></a>

#### 批量移动文件

方法签名如下：
    
    public BatchCallRet batchMove(List<EntryUriPair> entryUriPairs)
   
**参数**

同 `batchCopy`

**返回值**

    BatchCallRet 会包含一个 `callRet` 对象的列表, 每个callRet对象对应一个key经过处理后的结果。

    需要说明的是经过 `batchMove` 操作后所有的源文件都将被从源bucket中清除。


<a name="batch-delete"></a>

#### 批量删除文件

方法签名如下：

    public BatchCallRet batchDelete(List<String> entryUris)
   
**参数**
    
    一个包含所有要删除文件对应的entryUri列表

**返回值**

    BatchCallRet 会包含一个 `callRet` 对象的列表, 每个callRet对象对应一个key经过处理后的结果。

    关于批量处理更详细的信息，[请点击七牛云存储批量处理api](http://docs.qiniutek.com/v3/api/io/#batch)

<a name="cloud-processing"></a>

### 云处理

<a name="image-processing"></a>

#### 图像

<a name="image-info"></a>

##### 查看图片属性信息

使用 SDK 提供的 `com.qiniu.qbox.fileop` 包下的 `ImageInfo` 类可以基于一张存储于七牛云存储服务器上的图片，针对其下载链接来获取该张图片的属性信息。
首先，您要获得该图片的下载链接，请参见[查看单个文件属性信息](#stat)

示例代码如下：
 
    String imageUrl = "Your image url on the qiniu server" ;
    ImageInfoRet imgInfoRet = ImageInfo.call(imageUrl) ;

如果请求失败，返回相应的错误信息；否则，返回如下一个 Hash 类型的结构：

    {
        "format"     => "jpeg",    // 原始图片类型 
        "width"      => 640,       // 原始图片宽度，单位像素  
        "height"     => 425,       // 原始图片高度，单位像素 
        "colorModel" => "ycbcr"    // 原始图片着色模式 
    } 


<a name="image-exif"></a>

##### 查看图片EXIF信息

使用 SDK 提供的 `com.qiniu.qbox.fileop` 包下的 `ImageInfo` 类可以基于一张存储于七牛云存储服务器上的原始图片图片，取到该图片的 EXIF 信息。

示例代码如下：

    String imageUrl = "Your image url on the qiniu server" ;
    CallRet imageExifRet = ImageExif.call(imageUrl) ;     

如果参数 `imageUrl` 所代表的图片没有 EXIF 信息 `imageExifRet.ok()` 为 `false`。否则，返回一个包含 EXIF 信息的 Hash 结构。


<a name="image-mogrify-for-preview"></a>

##### 图像在线处理（缩略、裁剪、旋转、转化）

使用 SDK 提供的 `com.qiniu.qbox.fileop` 包下的 `ImageView` 类将一个存储在七牛云存储的图片进行缩略、裁剪、旋转和格式转化处理，该方法返回一个可以直接预览缩略图的URL。

示例代码如下：

    String imageUrl = "Your image url on the qiniu server" ;
    ImageMogrify imgMogr = new ImageMogrify() ;
    imgMogr.thumbnail = "!120x120r" ;
    imgMogr.gravity = "center" ;
    imgMogr.crop = "!120x120a0a0" ;
    imgMogr.quality = 85 ;
    imgMogr.rotate = 45 ;
    imgMogr.format = "jpg" ;
    imgMogr.autoOrient = true ;
    String imgMogrRequestUrl = imgMogr.makeRequest(imageUrl) ;

关于参数的具体说明请参见：[高级图像处理接口(缩略、裁剪、旋转、转化)](https://github.com/v3/api/foimg/#fo-imageMogr)


<a name="Contributing"></a>
## 贡献代码

1. Fork
2. 创建您的特性分支 (`git checkout -b my-new-feature`)
3. 提交您的改动 (`git commit -am 'Added some feature'`)
4. 将您的修改记录提交到远程 `git` 仓库 (`git push origin my-new-feature`)
5. 然后到 github 网站的该 `git` 远程仓库的 `my-new-feature` 分支下发起 Pull Request

<a name="License"></a>
## 许可证

Copyright (c) 2013 qiniutek.com

基于 MIT 协议发布:

* [www.opensource.org/licenses/MIT](http://www.opensource.org/licenses/MIT)


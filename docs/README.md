---
title: Java SDK | 七牛云存储
---

# Java SDK 使用指南

此SDK适用于Java 6及以上版本。基于 [七牛云存储官方API](http://docs.qiniu.com) 构建。使用此 SDK 构建您的网络应用程序，能让您以非常便捷地方式将数据安全地存储到七牛云存储上。无论您的网络应用是一个网站程序，还是包括从云端（服务端程序）到终端（手持设备应用）的架构的服务或应用，通过七牛云存储及其 SDK，都能让您应用程序的终端用户高速上传和下载，同时也让您的服务端更加轻盈。

SDK下载地址：[https://github.com/qiniu/java-sdk/tags](https://github.com/qiniu/java-sdk/tags)

目录
----
- [环境准备](#env_preparation)
- [初始化](#setup)
	- [配置密钥](#setup-key)
- [上传下载接口](#get-and-put-api)
	- [上传流程](#io-put-flow)
	- [生成上传授权uptoken](#make-uptoken)
	- [上传代码](#upload-code)
	- [断点续上传、分块并行上传](#resumable-io-put)
	- [上传策略](#io-put-policy)
	- [公有资源下载](#public-download)
	- [私有资源下载](#private-download)
- [资源管理接口](#rs-api)
	- [查看单个文件属性信息](#rs-stat)
	- [复制单个文件](#rs-copy)
	- [移动单个文件](#rs-move)
	- [删除单个文件](#rs-delete)
	- [批量操作](#batch)
		- [批量获取文件属性信息](#batch-stat)
		- [批量复制文件](#batch-copy)
		- [批量移动文件](#batch-move)
		- [批量删除文件](#batch-delete)
- [数据处理接口](#fop-api)
	- [图像](#fop-image)
		- [查看图像属性](#fop-image-info)
		- [查看图片EXIF信息](#fop-exif)
		- [生成图片预览](#fop-image-view)
- [高级资源管理接口](#rsf-api)
	- [批量获得文件列表](#rsf-listPrefix)
- [贡献代码](#contribution)
- [许可证](#license)

----

<a name="env_preparation"></a>

## 1. 环境准备

安装 Maven 的插件：[The Maven Integration for Eclipse](http://www.eclipse.org/m2e/)

添加依赖

	<dependency>
		<groupId>com.qiniu</groupId>
		<artifactId>sdk</artifactId>
		<version>6.0.0</version>
	</dependency>


<a name="setup"></a>
## 2.初始化
<a name="setup-key"></a>

### 2.1 配置密钥

要接入七牛云存储，您需要拥有一对有效的 Access Key 和 Secret Key 用来进行签名认证。可以通过如下步骤获得：

1. [开通七牛开发者帐号](https://portal.qiniu.com/signup)
2. [登录七牛开发者自助平台，查看 Access Key 和 Secret Key](https://portal.qiniu.com/setting/key) 。

在获取到 Access Key 和 Secret Key 之后，您可以按照如下方式进行密钥配置：

```{java}
import com.qiniu.api.config.Config;

public class Init {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
	}
}
```

<a name="get-and-put-api"></a>

## 3. 上传下载接口

为了尽可能地改善终端用户的上传体验，七牛云存储首创了客户端直传功能。

一般云存储的上传流程是：

    客户端（终端用户） => 业务服务器 => 云存储服务

这样通过用户自己的业务服务器中转上传至云存储服务端。这种方式存在一些不足：

1. 多了一次中转的上传过程，同数据存放在用户的业务服务器中相比，会相对慢一些；
1. 增加了用户业务服务器的负载，消耗了带宽，占用了磁盘，降低了服务能力；
1. 增加了用户的流量消耗，来自终端用户的上传数据进入业务服务器，然后再次上传至云存储服务，净增一倍流量。

因此，七牛云存储引入了客户端直传的模式，将整个上传过程调整为：

    客户端（终端用户） => 七牛 => 业务服务器

客户端（终端用户）直接上传到七牛的服务器。通过DNS智能解析，七牛会选择到离终端用户最近的ISP服务商节点，速度会相比数据存放在用户自己的业务服务器上的方式更快。而且，七牛云存储可以在用户文件上传成功以后，替用户的客户端向用户的业务服务器发送反馈信息，减少用户的客户端同业务服务器之间的交互。详情请参考[上传策略](#io-put-policy)

**注意**：如果您只是想要将您电脑上，或者是服务器上的文件上传到七牛云存储，可以直接使用七牛提供的 [qrsync](http://docs.qiniu.com/tools/qrsync.html) 上传工具，而无需额外开发。

文件上传有两种方式：普通方式，即一次性上传整个文件；断点续上传，即将文件分割成若干小块，分别上传，然后在七牛云存储服务端重新合并成一个文件。一般情况下，用户可以采用普通上传。如果文件较大，或者网络条件不佳，那么可以使用断点续上传，提高上传的速度和成功率。


<a name="io-put-flow"></a>

### 3.1 上传流程

在七牛云存储中，整个上传流程大体分为这样几步：

1. 业务服务器颁发 [uptoken（上传授权凭证）](#make-uptoken)给客户端（终端用户）
1. 客户端凭借 [uptoken](#make-uptoken) 上传文件到七牛
1. 在七牛获得完整数据后，根据用户请求的设定执行以下操作：

	a. 如果用户设定了[returnUrl](#io-put-policy)，七牛云存储将反馈一个指向returnUrl的HTTP 301，驱动客户端执行跳转；
	
	b. 如果用户设定了[callbackUrl](#io-put-policy)，七牛云存储将向callbackUrl指定的地址发起一个HTTP 请求回调业务服务器，同时向业务服务器发送数据。发送的数据内容由[callbackBody](#io-put-policy)指定。业务服务器完成回调的处理后，可以在HTTP Response中放入数据，七牛云存储会响应客户端，并将业务服务器反馈的数据发送给客户端；
	
	c. 如果两者都没有设置，七牛云存储根据[returnBody](#io-put-policy)的设定向客户端发送反馈信息。

需要注意的是，回调到业务服务器的过程是可选的，它取决于业务服务器颁发的 [uptoken](#make-uptoken)。如果没有回调，七牛会返回一些标准的信息（比如文件的 hash）给客户端。如果上传发生在业务服务器，以上流程可以自然简化为：

1. 业务服务器生成 uptoken（不设置回调，自己回调到自己这里没有意义）
1. 凭借 [uptoken](#make-uptoken) 上传文件到七牛
1. 善后工作，比如保存相关的一些信息


<a name="make-uptoken"></a>

### 3.2 生成上传授权uptoken

uptoken是一个字符串，作为http协议Header的一部分（Authorization字段）发送到我们七牛的服务端，表示这个http请求是经过用户授权的。

```{java}
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.PutPolicy;

public class Uptoken {

	public static void main(String[] args) throws Exception {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		// 请确保该bucket已经存在
		String bucketName = "Your bucket name";
		PutPolicy putPolicy = new PutPolicy(bucketName);
		String uptoken = putPolicy.token(mac);
	}
}

```

<a name="upload-code"></a>

### 3.3 上传代码

上传本地文件。如果用户从自己的计算机或服务器上传文件，可以直接使用七牛云存储提供的[qrsync](http://docs.qiniu.com/tools/qrsync.html)工具。用户也可以自行编写上传程序。

上传程序大体步骤如下：

1. 设置AccessKey和SecretKey；
1. 创建Mac对象；
1. 创建PutPolicy对象；
1. 生成UploadToken；
1. 创建PutExtra对象；
1. 调用put或putFile方法上传文件；

具体代码如下：

```{java}
import java.io.File;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.rs.PutPolicy;

public class UploadFile {
	
	public static void main(String[] args) throws Exception {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		// 请确保该bucket已经存在
		String bucketName = "<Your bucket name>";
		PutPolicy putPolicy = new PutPolicy(bucketName);
		String uptoken = putPolicy.token(mac);
		PutExtra extra = new PutExtra();
		String key = "<key>";
		String localFile = "<local file path>";
		PutRet ret = IoApi.putFile(uptoken, key, localFile, extra);
	}
}

```

<a name="resumable-io-put"></a>

### 3.4 断点续上传、分块并行上传

与普通上传类似：
```{java}
	private void uploadFile() throws AuthException, JSONException{
		PutPolicy p = new PutPolicy(bucketName);
		p.returnBody = "{\"key\": $(key), \"hash\": $(etag),\"mimeType\": $(mimeType)}";
		String upToken = p.token(mac);
		PutRet ret = ResumeableIoApi.put(file, upToken, key, mimeType);
	}
	
	private void uploadStream() throws AuthException, JSONException, FileNotFoundException{
		PutPolicy p = new PutPolicy(bucketName);
		String upToken = p.token(mac);
		FileInputStream fis = new FileInputStream(file);
		PutRet ret = ResumeableIoApi.put(fis, upToken, key, mimeType);
	}

```
key，mimeType 可为null。

<a name="io-put-policy"></a>

### 3.5 上传策略

[uptoken](http://docs.qiniu.com/api/put.html#uploadToken) 实际上是用 AccessKey/SecretKey 进行数字签名的上传策略(`rs.PutPolicy`)，它控制则整个上传流程的行为。让我们快速过一遍你都能够决策啥：

* `expires` 指定 [uptoken](http://docs.qiniu.com/api/put.html#uploadToken) 有效时长。单位：秒（s），默认1小时，3600秒。deadline = System.currentTimeMillis() / 1000 + this.expires，不直接指定deadline。一个 [uptoken](http://docs.qiniu.com/api/put.html#uploadToken) 可以被用于多次上传（只要它还没有过期）。

关于上传策略更完整的说明，请参考 [uptoken](http://docs.qiniu.com/api/put.html#uploadToken)。

### 3.6 文件下载

七牛云存储上的资源下载分为 公有资源下载 和 私有资源下载 。

私有（private）是 Bucket（空间）的一个属性，一个私有 Bucket 中的资源为私有资源，私有资源不可匿名下载。

新创建的空间（Bucket）缺省为私有，也可以将某个 Bucket 设为公有，公有 Bucket 中的资源为公有资源，公有资源可以匿名下载。

<a name="public-download"></a>

### 3.7 公有资源下载

如果在给bucket绑定了域名的话，可以通过以下地址访问。

	[GET] http://<domain>/<key>

其中\<domain\>是bucket所对应的域名。七牛云存储为每一个bucket提供一个默认域名。默认域名可以到[七牛云存储开发者平台](https://portal.qiniu.com/)中，空间设置的域名设置一节查询。用户也可以将自有的域名绑定到bucket上，通过自有域名访问七牛云存储。

**注意： key必须采用utf8编码，如使用非utf8编码访问七牛云存储将反馈错误**

<a name="private-download"></a>

### 3.8 私有资源下载

私有资源必须通过临时下载授权凭证(downloadToken)下载，如下：

	[GET] http://<domain>/<key>?token=<downloadToken>

注意，尖括号不是必需，代表替换项。  

`downloadToken` 可以使用 SDK 提供的如下方法生成：

```{java}
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.GetPolicy;
import com.qiniu.api.rs.URLUtils;

public class DownloadFile {

	public static void main(String[] args) throws Exception {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		String baseUrl = URLUtils.makeBaseUrl("<domain>", "<key>");
		GetPolicy getPolicy = new GetPolicy();
		String downloadUrl = getPolicy.makeRequest(baseUrl, mac);
	}
}
```

<a name="rs-api"></a>

## 4. 资源管理接口

文件管理包括对存储在七牛云存储上的文件进行查看、复制、移动和删除处理。  

<a name="rs-stat"></a>

### 4.1 查看单个文件属性信息

```{java}
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.Entry;
import com.qiniu.api.rs.RSClient;

public class Stat {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient client = new RSClient(mac);
		Entry statRet = client.stat("<bucketName>", "<key>");
	}
}
```


<a name="rs-copy"></a>

### 4.2 复制单个文件

```{java}
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.RSClient;

public class Copy {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient client = new RSClient(mac);
		client.copy("<bucketSrc>", "<keySrc>", "<bucketDest>", "<keyDest>");
	}
}
```

<a name="rs-move"></a>

### 4.3 移动单个文件

```{java}
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.RSClient;

public class Move {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient client = new RSClient(mac);
		client.move("<bucketSrc>", "<keySrc>", "<bucketDest>", "<keyDest>");
	}
}

```

<a name="rs-delete"></a>

### 4.4 删除单个文件

```{java}
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.RSClient;

public class Delete {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient client = new RSClient(mac);
		client.delete("<bucketName>", "<key>");
	}
}

```


<a name="batch"></a>

### 4.5 批量操作

当您需要一次性进行多个操作时, 可以使用批量操作.

<a name="batch-stat"></a>

#### 4.5.1 批量获取文件属性信息

```{java}

import java.util.ArrayList;
import java.util.List;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.BatchStatRet;
import com.qiniu.api.rs.EntryPath;
import com.qiniu.api.rs.RSClient;

public class BatchStat {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		
		RSClient rs = new RSClient(mac);
		List<EntryPath> entries = new ArrayList<EntryPath>();

		EntryPath e1 = new EntryPath();
		e1.bucket = "<bucketName>";
		e1.key = "<key1>";
		entries.add(e1);

		EntryPath e2 = new EntryPath();
		e2.bucket = "<bucketName>";
		e2.key = "<key2>";
		entries.add(e2);

		BatchStatRet bsRet = rs.batchStat(entries);
	}
}
```


<a name="batch-copy"></a>

#### 4.5.2 批量复制文件

```{java}

import java.util.ArrayList;
import java.util.List;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.BatchCallRet;
import com.qiniu.api.rs.EntryPath;
import com.qiniu.api.rs.EntryPathPair;
import com.qiniu.api.rs.RSClient;

public class BatchCopy {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient rs = new RSClient(mac);
		List<EntryPathPair> entries = new ArrayList<EntryPathPair>();

		EntryPathPair pair1 = new EntryPathPair();

		EntryPath src = new EntryPath();
		src.bucket = "<srcBucket>";
		src.key = "<key1>";

		EntryPath dest = new EntryPath();
		dest.bucket = "<destBucket>";
		dest.key = "<key1>";

		pair1.src = src;
		pair1.dest = dest;

		EntryPathPair pair2 = new EntryPathPair();

		EntryPath src2 = new EntryPath();
		src2.bucket = "<srcBucket>";
		src2.key = "<key2>";

		EntryPath dest2 = new EntryPath();
		dest2.bucket = "<destBucket>";
		dest2.key = "<key2>";

		pair2.src = src2;
		pair2.dest = dest2;

		entries.add(pair1);
		entries.add(pair2);

		BatchCallRet ret = rs.batchCopy(entries);
	}
}
```

<a name="batch-move"></a>

#### 4.5.3 批量移动文件

```{java}

import java.util.ArrayList;
import java.util.List;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.BatchCallRet;
import com.qiniu.api.rs.EntryPath;
import com.qiniu.api.rs.EntryPathPair;
import com.qiniu.api.rs.RSClient;

public class BatchMove {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient rs = new RSClient(mac);
		List<EntryPathPair> entries = new ArrayList<EntryPathPair>();
		
		EntryPathPair pair1 = new EntryPathPair();
		
		EntryPath src = new EntryPath();
		src.bucket = "<srcBucket>";
		src.key = "<key1>";
		
		EntryPath dest = new EntryPath();
		dest.bucket = "<destBucket>";
		dest.key = "<key1>";
		
		pair1.src = src;
		pair1.dest = dest;
		
		EntryPathPair pair2 = new EntryPathPair();
		
		EntryPath src2 = new EntryPath();
		src2.bucket = "<srcBucket>";
		src2.key =  "<key2>";
		
		EntryPath dest2 = new EntryPath();
		dest2.bucket = "<destBucket>";
		dest2.key = "<key2>";
		
		pair2.src = src2;
		pair2.dest = dest2;
		
		entries.add(pair1);
		entries.add(pair2);
		
		BatchCallRet ret = rs.batchMove(entries);
	}
}   
```

<a name="batch-delete"></a>

#### 4.5.4 批量删除文件

```{java}
import java.util.ArrayList;
import java.util.List;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.BatchCallRet;
import com.qiniu.api.rs.EntryPath;
import com.qiniu.api.rs.RSClient;

public class BatchDelete {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		RSClient rs = new RSClient(mac);
		List<EntryPath> entries = new ArrayList<EntryPath>();

		EntryPath e1 = new EntryPath();
		e1.bucket = "<bucketName>";
		e1.key = "<key1>";
		entries.add(e1);

		EntryPath e2 = new EntryPath();
		e2.bucket = "<bucketName>";
		e2.key = "<key2>";
		entries.add(e2);

		BatchCallRet bret = rs.batchDelete(entries);
	}
}

```

参阅: `rs.EntryPath`, `rs.Client.BatchDelete`

<a name="batch-advanced"></a>

#### 4.5.5 高级批量操作

批量操作不仅仅支持同时进行多个相同类型的操作, 同时也支持不同的操作.

```{java}
to do!
```

<a name="fop-api"></a>

## 5. 数据处理接口

七牛支持在云端对图像, 视频, 音频等富媒体进行个性化处理

<a name="fop-image"></a>

### 5.1 图像

<a name="fop-image-info"></a>

### 5.1.1 查看图像属性

```{java}
import com.qiniu.api.fop.ImageInfo;
import com.qiniu.api.fop.ImageInfoRet;

public class FopImageInfo {

	public static void main(String[] args) {
		String url = "<domain>" + "/" + "<key>";
		ImageInfoRet ret = ImageInfo.call(url);
	}
}
```

参阅: `fop.ImageInfoRet`, `fop.ImageInfo`

<a name="fop-exif"></a>

### 5.1.2 查看图片EXIF信息

```{java}
import com.qiniu.api.fop.ExifRet;
import com.qiniu.api.fop.ImageExif;

public class FopImageExif {

	public static void main(String[] args) {
		String url = "<domain>" + "/" + "<key>";
		ExifRet ret = ImageExif.call(url);
	}
}
```

<a name="fop-image-view"></a>

### 5.1.3 生成图片预览

```{java}
import com.qiniu.api.fop.ImageView;
import com.qiniu.api.net.CallRet;

public class FopImageView {

	public static void main(String[] args) {
		String url = "http://domain/key";
		ImageView iv = new ImageView();
		iv.mode = 1 ;
		iv.width = 100 ;
		iv.height = 200 ;
		iv.quality = 1 ;
		iv.format = "jpg" ;
		CallRet ret = iv.call(url);
	}
}
```

<a name="rsf-api"></a>

## 6. 高级资源管理接口(rsf)

<a name="rsf-listPrefix"></a>

批量获取文件列表

```{java}
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rsf.RSFClient;

public class ListPrefix {

	public static void main(String[] args) {
		Config.ACCESS_KEY = "<YOUR APP ACCESS_KEY>";
		Config.SECRET_KEY = "<YOUR APP SECRET_KEY>";
		Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		
		RSFClient client = new RSFClient(mac);
		String marker = "";
			
		List<ListItem> all = new ArrayList<ListItem>();
		ListPrefixRet ret = null;
		while (true) {
			ret = client.listPrifix(bucketName, "<prifix>", marker, 10);
			marker = ret.marker;
			all.addAll(ret.results);
			if (!ret.ok()) {
				// no more items or error occurs
				break;
			}
		}
		if (ret.exception.getClass() != RSFEofException.class) {
			// error handler
		} 
	}
}
```

<a name="contribution"></a>
## 7. 贡献代码

1. Fork
2. 创建您的特性分支 (`git checkout -b my-new-feature`)
3. 提交您的改动 (`git commit -am 'Added some feature'`)
4. 将您的修改记录提交到远程 `git` 仓库 (`git push origin my-new-feature`)
5. 然后到 github 网站的该 `git` 远程仓库的 `my-new-feature` 分支下发起 Pull Request

<a name="license"></a>
## 8. 许可证

Copyright (c) 2013 qiniu.com

基于 MIT 协议发布:

* [www.opensource.org/licenses/MIT](http://www.opensource.org/licenses/MIT)


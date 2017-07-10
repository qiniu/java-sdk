# Qiniu Resource Storage SDK for Java
[![@qiniu on weibo](http://img.shields.io/badge/weibo-%40qiniutek-blue.svg)](http://weibo.com/qiniutek)
[![Software License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](LICENSE)
[![Build Status](https://travis-ci.org/qiniu/java-sdk.svg)](https://travis-ci.org/qiniu/java-sdk)
[![Latest Stable Version](https://img.shields.io/maven-central/v/com.qiniu/qiniu-java-sdk.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.qiniu%22%20AND%20a%3A%22qiniu-java-sdk%22)
## 安装

下载 [the latest JAR][1] 或者 通过 Maven:
```xml
<dependency>
  <groupId>com.qiniu</groupId>
  <artifactId>qiniu-java-sdk</artifactId>
  <version>[7.0.0, 7.2.99]</version>
</dependency>
```
或者 Gradle:
```groovy
compile 'com.qiniu:qiniu-java-sdk:7.1.+'
```
7.0.x 版本的jdk 6.0 不能直接使用mvn上的okhttp, 需要另外下载，[代码][2], [okhttp.jar][3], [okio.jar][4]
7.1.x, 7.2.x 版本 jdk6.0 支持 后面会做处理，暂时只支持7及以上, 原因同前。

## 运行环境

| Qiniu SDK版本 | Java 版本 |
|:--------------------:|:---------------------------:|
|          7.2.x         |  7+ |
|          7.1.x         |  7+ |
|          7.0.x         |  6+ |
|          6.x         |  6+ |

## 使用方法

### 上传
```Java
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.http.Response;
...
    UploadManager uploadManager = new UploadManager()
    Auth auth = Auth.create(accessKey, secretKey);
    String token = auth.uploadToken(bucketName);
    Response r = upManager.put("hello world".getBytes(), "yourkey", token);
...
```

## 测试

``` bash
$ ./gradlew build
```

## 生成Eclipse工程文件
``` bash
$ ./gradlew gen_eclipse
```

## 常见问题

- QiniuExeption保留了请求响应的信息，失败情况下会抛出此异常，可以提交给我们排查问题。
- API 的使用 demo 可以参考 [单元测试](https://github.com/qiniu/java-sdk/blob/master/src/test)。

## 代码贡献

详情参考[代码提交指南](https://github.com/qiniu/java-sdk/blob/master/CONTRIBUTING.md)。

## 贡献记录

- [所有贡献者](https://github.com/qiniu/java-sdk/contributors)

## 联系我们

- 如果需要帮助，请提交工单（在portal右侧点击咨询和建议提交工单，或者直接向 support@qiniu.com 发送邮件）
- 如果有什么问题，可以到问答社区提问，[问答社区](http://qiniu.segmentfault.com/)
- 更详细的文档，见[官方文档站](http://developer.qiniu.com/)
- 如果发现了bug， 欢迎提交 [issue](https://github.com/qiniu/java-sdk/issues)
- 如果有功能需求，欢迎提交 [issue](https://github.com/qiniu/java-sdk/issues)
- 如果要提交代码，欢迎提交 pull request
- 欢迎关注我们的[微信](http://www.qiniu.com/#weixin) [微博](http://weibo.com/qiniutek)，及时获取动态信息。

## 代码许可

The MIT License (MIT).详情见 [License文件](https://github.com/qiniu/java-sdk/blob/master/LICENSE).

[1]: https://search.maven.org/remote_content?g=com.qiniu&a=qiniu-java-sdk&v=LATEST
[2]: https://github.com/Nextpeer/okhttp
[3]: https://raw.githubusercontent.com/qiniu/java-sdk/master/libs/okhttp-2.3.0-SNAPSHOT.jar
[4]: https://raw.githubusercontent.com/qiniu/java-sdk/master/libs/okio-1.3.0-SNAPSHOT.jar

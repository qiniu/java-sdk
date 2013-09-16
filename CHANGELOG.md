## CHANGE LOG

v6.0.5

2013-09-12 issue [#80](https://github.com/qiniu/java-sdk/pull/80)

增加私有资源fop的接口，包括exif,imageInfo,ImageView

v6.0.4

2013-08-21 issue [#77](https://github.com/qiniu/java-sdk/pull/77)

添加ListPrefix

### v6.0.3

2013-08-5 issue [#76](https://github.com/qiniu/java-sdk/pull/76)

Bug fix，编码强制UTF-8修复

### v6.0.1

2013-08-5 issue [#74](https://github.com/qiniu/java-sdk/pull/74)

Bug fix，增加PutPolicy类的 callbackBody字段到PutPolicy的Json格式中


### v6.0.0

2013-07-01 issue [#64](https://github.com/qiniu/java-sdk/pull/64)

- 遵循 [sdkspec v6.0.2](https://github.com/qiniu/sdkspec/tree/v6.0.2)
    - 暂不支持断点续传


### v3.0.0

2013-06-05 issue [#61](https://github.com/qiniu/java-sdk/pull/61)

- PutPolicy: add member - escape, asyncOps, returnBody


### v2.5.1

2013-03-25 issue [#54](https://github.com/qiniu/java-sdk/pull/54)

- 修正 HttpClient 类的不正确使用：补漏。


### v2.5.0

2013-03-23 issue [#46](https://github.com/qiniu/java-sdk/pull/46)

- 修正帮助文档关于 AccessKey/SecretKey 配置描述的问题。
- 修正 HttpClient 类的不正确使用。


### v2.4.2

2013-03-11 issue [#39](https://github.com/qiniu/java-sdk/pull/39)

- RSService 类增加批量操作方法：`batchStat`, `batchCopy`, `batchMove`, `batchDelete`。
- RSService 类增加文件操作方法：`copy`, `move`, 以及`buckets`。
- 修复断点续传 `mkblk` 返回的 `host` 字段未持久化的问题。
- 增加以 `downloadtoken` 方式下载私有资源, 并将生成 `token` 的方式做成统一的接口。
- 增加 `GetRet` 的 `expiry` 字段，基于此用户可以控制 `url` 有效期。


### v2.4.1

2013-01-22 issue [#29](https://github.com/qiniu/java-sdk/pull/29)

- 修复 `Fileop` 相关 `api` 的bug。


### v2.4.0

2013-01-14 issue [#27](https://github.com/qiniu/java-sdk/pull/27)

- 增加创建 `bucket`对应的方法以及 `Fileop` 相关的类，包括 `ImageInfo`,`ImageExif`,`ImageMogrify`,`ImageView`。
- 增加 `travis` 支持。
- 修复由于 `host` 作用域过大，导致线程安全问题的 `bug`。
- 增加以 `uptoken` 的方式上传文件。


### v2.3.1

- 回滚到 `v2.2.6`

### v2.3.0

- 合并新提交的 `sdk`

### v2.2.6

- 增加 `encodeParams` 支持

### v2.2.5

- 修复因 `base64` 编码而导致和 `android` 不兼容的问题

### v2.2.4

- 为 `JAVA-SDK` 增加 `Maven` 支持

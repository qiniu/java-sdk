## CHANGE LOG

### v2.4.2

issue [#39](https://github.com/qiniu/java-sdk/pull/39) 

- 更新`README`文档

- RSService 类增加批量操作方法：`batchStat`, `batchCopy`, `batchMove`, `batchDelete`。

- RSService 类增加文件操作方法：`copy`, `move`, 以及`buckets`。

- 修复断点续传 `mkblk` 返回的 `host` 字段未持久化的问题。

- 增加以 `downloadtoken` 方式下载私有资源, 并将生成 `token` 的方式做成统一的接口。

- 增加 `GetRet` 的 `expiry` 字段，基于此用户可以控制 `url` 有效期。

### v2.4.1

issue [#29](https://github.com/qiniu/java-sdk/pull/29)

- 修复 `Fileop` 相关 `api` 的bug。


### v2.4.0

issue [#27](https://github.com/qiniu/java-sdk/pull/27) 

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

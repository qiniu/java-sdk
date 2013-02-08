## CHANGE LOG

### v2.4.1

`Add Feature` [#35](https://github.com/qiniu/java-sdk/pull/35) 增加以 `downloadtoken` 方式下载私有资源, 并将生成token的方式做成统一的接口

- `com.qiniu.qbox.GetPolicy`
- `com.qiniu.qbox.PutPolicy`

更多详细信息,请参见[私有资源下载](http://docs.qiniutek.com/v3/api/io/#private-download)

`Add Feature` [#31](https://github.com/qiniu/java-sdk/pull/31) 增加 `up` 服务的断点续传的单元测试 


`Add Feature` [#34](https://github.com/qiniu/java-sdk/pull/34) 增加 `GetRet` 的 `expiry` 字段，基于此用户可以控制 `url` 有效期


`Add Feature` [#32](https://github.com/qiniu/java-sdk/pull/32) 增加 `rs` 服务相关的单元测试

- `com.qiniu.qbox.test.RSTest`



`Add Feature` [#30](https://github.com/qiniu/java-sdk/pull/34) 增加 `Fileop` 相关的 `junit` 单元测试

- `com.qiniu.qbox.testing.FileopTest`

`Bug Fix` [#29](https://github.com/qiniu/java-sdk/pull/29) 修复 `Fileop` 相关 `api` 的bug


### v2.4.0

 `Add Feature` [#27](https://github.com/qiniu/java-sdk/pull/27) 增加创建 `bucket`对应的方法以及 `Fileop` 相关的类，包括

- `com.qiniu.qbox.fileop.ImageInfo` 
- `com.qiniu.qbox.fileop.ImageInfoRet` 
- `com.qiniu.qbox.fileop.ImageExif`
- `com.qiniu.qbox.fileop.ImageView`
- `com.qiniu.qbox.fileop.ImageMogrify`

更多详细信息，请参见[七牛图像处理接口](http://docs.qiniutek.com/v3/api/foimg/)

`Add Feature` [#25](https://github.com/qiniu/java-sdk/pull/25) 增加 `travis` 支持

`Bug Fix` [#22](https://github.com/qiniu/java-sdk/pull/22) 修复由于 `host` 作用域过大，导致线程安全问题的 `bug`

`Add Feature` [#20](https://github.com/qiniu/java-sdk/pull/34) 增加以 `uptoken` 的方式上传文件

更多详细信息，请参见[认证授权](http://docs.qiniutek.com/v3/api/auth/)

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

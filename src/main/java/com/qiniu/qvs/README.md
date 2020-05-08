# QVS Cloud Server-Side Library for Java

## Features

- 空间管理
    - [x] 创建空间: createNameSpace(NameSpace nameSpace)
	- [x] 删除空间: deleteNameSpace(String namespaceId)
	- [x] 更新空间: updateNameSpace(String namespaceId, PatchOperation[] patchOperation)
	- [x] 查询空间信息: queryNameSpace(String namespaceId)
	- [x] 获取空间列表: listNameSpace(int offset, int line, String sortBy)
	- [x] 禁用空间: disableNameSpace(String namespaceId)
    - [x] 启用空间: enableNameSpace(String namespaceId)
    
- 流管理
    - [x] 创建流: createStream(String namespaceId, Stream stream)
    - [x] 删除流: deleteStream(String namespaceId, String streamId)
    - [x] 查询流信息: queryStream(String namespaceId, String streamId)
    - [x] 更新流: updateStream(String namespaceId, String streamId, PatchOperation[] patchOperation)
    - [x] 获取流列表: listStream(String namespaceId, int offset, int line, int qtype, String prefix, String sortBy)
    - [x] 获取流地址
        - [x] 动态模式: dynamicPublishPlayURL(String namespaceId, String streamId, DynamicLiveRoute dynamicLiveRoute)
        - [x] 静态模式: staticPublishPlayURL(String namespaceId, String streamId, StaticLiveRoute staticLiveRoute)
    - [x] 禁用流: disableStream(String namespaceId, String streamId) 
    - [x] 启用流: enableStream(String namespaceId, String streamId)
    - [x] 查询推流记录: queryStreamPubHistories(String namespaceId, String streamId, int start, int end, int offset, int line) 

- 模板管理
    - [x] 创建模板: createTemplate(Template template)
    - [x] 删除模板: deleteTemplate(String templateId)
    - [x] 更新模板: updateTemplate(String templateId, PatchOperation[] patchOperation)
    - [x] 查询模板信息: queryTemplate(String templateId)
    - [x] 获取模板列表: listTemplate(int offset, int line, int templateType, String match)

- 录制管理相关接口
    - [x] 查询录制记录: queryStreamRecordHistories(String namespaceId, String streamId, int start, int end, int line, String marker)
    - [x] 获取截图列表: streamsSnapshots(String namespaceId, String streamId, int start, int end, int type, int line, String marker)
    - [x] 获取直播封面截图: queryStreamCover(String namespaceId, String streamId)

## Usage

### Init

```go
String accessKey = "<QINIU ACCESS KEY>"; // 替换成自己 Qiniu 账号的 AccessKey.
String secretKey = "<QINIU SECRET KEY>"; // 替换成自己 Qiniu 账号的 SecretKey.
Auth auth = Auth.create(accessKey, secretKey);
NameSpaceManager nameSpaceManager = new NameSpaceManager(auth);
// StreamManager streamManager = new StreamManager(auth);
// TemplateManager templateManager = new TemplateManager(auth);
```


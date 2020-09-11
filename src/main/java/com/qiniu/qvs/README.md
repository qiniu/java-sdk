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
    - [x] 停用流: stopStream(String namespaceId, String streamId)
    
- 设备管理
    - [x] 创建设备: createDevice(String namespaceId, Device device) 
	- [x] 删除设备: deleteDevice(String namespaceId, String gbId)
	- [x] 更新设备: updateDevice(String namespaceId, String gbId, PatchOperation[] patchOperation)
	- [x] 查询设备信息: queryDevice(String namespaceId, String gbId)
	- [x] 获取设备列表: listDevice(String namespaceId, int offset, int line, String prefix, String state, int qtype)
	- [x] 获取通道列表: listChannels(String namespaceId, String gbId, String prefix)
	- [x] 启动设备拉流: startDevice(String namespaceId, String gbId, String channels)
    - [x] 停止设备拉流: stopDevice(String namespaceId, String gbId, String channels)

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

## Contents


- [Usage](#usage)

  - [Init](#Init)

  - [空间管理](#空间管理)

    - [创建空间](#创建空间)
    - [查询空间](#查询空间)
    - [更新空间](#更新空间)
    - [获取空间列表](#获取空间列表)
    - [禁用空间](#禁用空间)
    - [启用空间](#启用空间)
    - [删除空间](#删除空间)

  - [流管理](#流管理)

    * [创建流](#创建流)
    * [查询流](#查询流)
    * [更新流](#更新流)
    * [获取流列表](#获取流列表)
    * [静态模式获取流地址](#静态模式获取流地址)
    * [动态模式获取流地址](#动态模式获取流地址)
    * [查询推流历史记录](#查询推流历史记录)
    * [禁用流](#禁用流)
    * [启用流](#启用流)
    * [删除流](#删除流)
    * [停用流](#停用流)
  
- [设备管理](#设备管理)
  
    - [创建设备](#创建设备)
    - [删除设备](#删除设备)
    - [查询设备](#查询设备)
    - [更新设备](#更新设备)
    - [获取设备列表](#获取设备列表)
    - [获取通道列表](#获取通道列表)
    - [启动设备拉流](#启动设备拉流)
    - [停止设备拉流](#停止设备拉流)
  
  - [模板管理](#模板管理)
  
    * [创建模板](#创建模板)
    * [查询模板](#查询模板)
    * [更新模板](#更新模板)
    * [获取模板列表](#获取模板列表)
    * [删除模板](#删除模板)
  
  - [录制管理](#录制管理)
  
    - [查询录制记录](#查询录制记录)
    - [查询流封面](#查询流封面)
    - [获取截图列表](#获取截图列表)
  
    

## Usage

### Init

```java
String accessKey = "<QINIU ACCESS KEY>"; // 替换成自己 Qiniu 账号的 AccessKey.
String secretKey = "<QINIU SECRET KEY>"; // 替换成自己 Qiniu 账号的 SecretKey.
Auth auth = Auth.create(accessKey, secretKey);
NameSpaceManager nameSpaceManager = new NameSpaceManager(auth);
// StreamManager streamManager = new StreamManager(auth);
// TemplateManager templateManager = new TemplateManager(auth);
```

### 空间管理

#### 创建空间

```
 // 创建空间
 NameSpace nameSpace = new NameSpace();
 nameSpace.setName("hugo");
 nameSpace.setAccessType("rtmp");
 nameSpace.setRTMPURLType(1);
 nameSpace.setDomains(new String[]{"qtest.com"});

 nameSpaceManager.createNameSpace(nameSpace);
```

#### 查询空间

```
// 查询空间
String namespaceId = "2akrarsj8zp0w";

res = nameSpaceManager.queryNameSpace(namespaceId);
```

#### 更新空间

```
// 更新空间
PatchOperation[] patchOperation = {new PatchOperation("replace", "recordTemplateApplyAll", true)};

nameSpaceManager.updateNameSpace(namespaceId, patchOperation);
```

#### 获取空间列表

```
// 获取空间列表
int offset = 0;
int line = 1;
String sortBy = "asc:updatedAt";

nameSpaceManager.listNameSpace(offset, line, sortBy);
```

#### 禁用空间

```
// 禁用空间
nameSpaceManager.disableNameSpace(namespaceId);
```

#### 启用空间

```
// 启用空间
nameSpaceManager.enableNameSpace(namespaceId);
```

#### 删除空间

```
// 删除空间
nameSpaceManager.deleteNameSpace(namespaceId);
```



### 流管理

#### 创建流

```
// 创建流
Stream stream = new Stream("teststream004");
String namespaceId = "2akrarsj8zp0w";

streamManager.createStream(namespaceId, stream);
```

#### 查询流

```
// 查询流
streamManager.queryStream(namespaceId, stream.getStreamID());
```

#### 更新流

```
// 更新流
PatchOperation[] patchOperation = {new PatchOperation("replace", "desc", "test")};
streamManager.updateStream(namespaceId, stream.getStreamID(), patchOperation);
```

#### 获取流列表

```
// 获取流列表
int offset = 0;
int line = 1;
int qtype = 0;
String prefix = "test";
String sortBy = "desc:updatedAt";

streamManager.listStream(namespaceId, offset, line, qtype, prefix, sortBy);
```

#### 静态模式获取流地址

```
// 静态模式获取流地址
StaticLiveRoute staticLiveRoute = new StaticLiveRoute("qvs-publish.qtest.com", "publishRtmp", 3600);

streamManager.staticPublishPlayURL(namespaceId, stream.getStreamID(), staticLiveRoute);
```

#### 动态模式获取流地址

```
// 动态模式获取流地址
DynamicLiveRoute dynamicLiveRoute = new DynamicLiveRoute("127.0.0.1", "127.0.0.1", 0);

streamManager.dynamicPublishPlayURL(namespaceId, stream.getStreamID(), dynamicLiveRoute);
```

#### 查询推流历史记录

```
//  查询推流历史记录

streamManager.queryStreamPubHistories(namespaceId, stream.getStreamID(), start, end, offset, line);
```

#### 禁用流

```
// 禁用流
streamManager.disableStream(namespaceId, stream.getStreamID());
```

#### 启用流

```
// 启用流
streamManager.enableStream(namespaceId, stream.getStreamID());
```

#### 删除流

```
// 删除流
streamManager.deleteStream(namespaceId, stream.getStreamID());
```

#### 停用流

```
// 停用流
streamManager.stopStream(namespaceId, stream.getStreamID());
```



### 设备管理

#### 创建设备

```
// 创建设备
DeviceManager deviceManager = new DeviceManager(auth);
Device device = new Device();
device.setUsername("admin");
device.setPassword("QQQNNN111");

String namespaceId = "3nm4x0v0h6vjr";
deviceManager.createDevice(namespaceId, device);
```

#### 删除设备

```
deviceManager.deleteDevice(namespaceId, device.getGbId());
```

#### 查询设备

```
// 查询设备
device.setGbId("31011500991320000056");

deviceManager.queryDevice(namespaceId, device.getGbId());
```

#### 更新设备

```
// 更新设备
PatchOperation[] patchOperation = {new PatchOperation("replace", "name", "GBTEST")};

deviceManager.updateDevice(namespaceId, device.getGbId(), patchOperation);
```

#### 获取设备列表

```
// 获取设备列表
int offset = 0;
int line = 3;
int qtype = 0;
String prefix = "310";
String state = "notReg";

deviceManager.listDevice(namespaceId, offset, line, prefix, state, qtype);
```

#### 获取通道列表

```
// 禁用空间
deviceManager.listChannels(namespaceId, device.getGbId(), prefix);
```

#### 启动设备拉流

```
// 启动设备拉流
deviceManager.startDevice(namespaceId, device.getGbId(), "31011500991320000056");
```

#### 停止设备拉流

```
// 停止设备拉流
deviceManager.stopDevice(namespaceId, device.getGbId(), "31011500991320000056");
```



### 模板管理

#### 创建模板

```
// 创建模板
Template template = new Template();
template.setName("testtemplate001");
template.setBucket("Testforhugo");
template.setTemplateType(1);
template.setJpgOverwriteStatus(true);
template.setRecordType(2);

templateManager.createTemplate(template);
```

#### 查询模板

```
// 查询模板
String templateId = "2akrarsl22iil";
templateManager.queryTemplate(templateId);
```

#### 更新模板

```
// 更新模板
PatchOperation[] patchOperation = {new PatchOperation("replace", "name","testtemplate002")};
templateManager.updateTemplate(templateId, patchOperation);
```

#### 获取模板列表

```
// 获取模板列表
int offset = 0;
int line = 1;
int templateType = 1;
String match = "test";

templateManager.listTemplate(offset, line, templateType, match);
```

#### 删除模板

```
// 删除模板
templateManager.deleteTemplate(templateId);
```

### 录制管理

#### 查询录制记录

```
// 查询录制记录
int start = 1587975463;
int end = 1587976463;
String maker = "";

streamManager.queryStreamRecordHistories(namespaceId, stream.getStreamID(), start, end, line, maker);
```

#### 查询流封面

```
// 查询流封面 
streamManager.queryStreamCover(namespaceId, stream.getStreamID());
```

#### 获取截图列表

```
//  获取截图列表
streamManager.streamsSnapshots(namespaceId, stream.getStreamID(), start, end, 0, line, maker);
```
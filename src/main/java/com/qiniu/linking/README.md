# 七牛社会化监控JAVA SDK

## Features

- 设备管理
	- [x] 新建设备: deviceManager.createDevice(appid,deviceName)
	- [x] 获取设备列表:deviceManager.listDevice(appid,prefix,marker,limit,online)
	- [x] 查询设备: deviceManager.getDevice(appid,deviceName)
	- [x] 更新设备信息: deviceManager.updateDevice(appid,deviceName,operations)
	- [x] 添加dak: deviceManager.addDeviceKey(appid,deviceName)
	- [x] 通过dak查询设备: deviceManager.getDeviceByAccessKey(dak)
	- [x] 删除dak: deviceManager.deleteDeviceKey(appid,deviceName,dak)
	- [x] 获取设备的dak列表: deviceManager.queryDeviceKey(appid,deviceName1)
	- [x] 将dak移到另外的设备上去: deviceManager.cloneDeviceKey(appid,fromDeviceName,toDeviceName,cleanSelfKeys,deleteDevice,dak)
- dtoken生成
	- [x] 生成获取设备状态的token: auth.generateLinkingDeviceStatusTokenWithExpires(appid,deviceName,expires,actions)
	- [x] 生成获取视频的token: auth.generateLinkingDeviceVodTokenWithExpires(appid,deviceName,expires)
	- [x] 生成具有多种功能的token: auth.generateLinkingDeviceTokenWithExpires(appid,deviceName,expires)

## Contents


- [Usage](#usage)
	- [设备管理](#设备管理)
	        - [新建设备](#新建设备)
		- [获取设备列表](#获取设备列表)
		- [查询设备](#查询设备)
		- [更新设备信息](#更新设备信息)
		- [添加dak](#添加dak)
		- [通过dak查询设备](#通过dak查询设备)
		- [删除dak](#删除dak)
		- [获取设备的dak列表](#获取设备的dak列表)
		- [将dak移到另外的设备上去](#将dak移到另外的设备上去)
	- [dtoken](#dtoken)
		- [Instantiate a Pili Hub object](#instantiate-a-pili-hub-object)
		- [Create a new Stream](#create-a-new-stream)


## Usage

### Init

```java
Auth auth = Auth.create(testAk,testSk);
LinkingDeviceManager deviceManager = new LinkingDeviceManager(auth);
```

### 设备管理

####新建设备

```java
deviceManager.createDevice(appid,deviceName);
```
####获取设备列表

```java
DeviceListing deviceslist = deviceManager.listDevice(appid,prefix,marker,limit,online)
Device[] devices =  deviceslist.items;
String marker =  deviceslist.marker;
```
####查询设备

```java
Device device = deviceManager.getDevice(appid,deviceName);
```
####更新设备信息

```java
PatchOperation[] operations={new PatchOperation("replace","segmentExpireDays",9)};
Device device= deviceManager.updateDevice(appid,deviceName,operations);
//返回更新后的设备
```
####添加dak

```java
DeviceKey[] keys = deviceManager.addDeviceKey(appid,deviceName);
```
####通过dak查询设备

```java
Device device = deviceManager.getDeviceByAccessKey(dak);
```
####删除dak

```java
deviceManager.deleteDeviceKey(appid,deviceName,dak);
```
####获取设备的dak列表

```java
DeviceKey[] keys = deviceManager.queryDeviceKey(appid,deviceName);
```
####将dak移到另外的设备上去

```java
deviceManager.cloneDeviceKey(appid,fromDeviceName,toDeviceName2,true, false,dak)
```


###dtoken

####生成具有多种功能的token:
```java
String[] = new
String token = auth.generateLinkingDeviceTokenWithExpires(appid,deviceName,expires,actions);
```


####生成获取视频的token: 
```java
String token = auth.generateLinkingDeviceVodTokenWithExpires(appid,deviceName,expires)
```
####生成获取设备状态的token:
```java
String[] actions = new String[]{Auth.DTOKEN_ACTION_STATUS,Auth.DTOKEN_ACTION_VOD,Auth.DTOKEN_ACTION_TUTK};
String token =  auth.generateLinkingDeviceStatusTokenWithExpires(appid,deviceName,expires)
```
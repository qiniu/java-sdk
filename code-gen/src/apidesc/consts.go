package apidesc

import (
	"net/http"
)

type MethodName string

const (
	MethodNameGET    MethodName = http.MethodGet
	MethodNamePOST   MethodName = http.MethodPost
	MethodNamePUT    MethodName = http.MethodPut
	MethodNameDELETE MethodName = http.MethodDelete
)

type ServiceName string

const (
	ServiceNameUp     ServiceName = "up"
	ServiceNameIo     ServiceName = "io"
	ServiceNameRs     ServiceName = "rs"
	ServiceNameRsf    ServiceName = "rsf"
	ServiceNameApi    ServiceName = "api"
	ServiceNameBucket ServiceName = "uc"
)

// StringLikeType 类字符串参数类型
type StringLikeType = string

const (
	StringLikeTypeString  StringLikeType = "string"  // 字符串
	StringLikeTypeInteger StringLikeType = "integer" // 整型数字
	StringLikeTypeFloat   StringLikeType = "float"   // 浮点型数字
	StringLikeTypeBoolean StringLikeType = "boolean" // 布尔值
)

type MultipartFormDataType = string

const (
	MultipartFormDataTypeString      MultipartFormDataType = "string" // 字符串
	MultipartFormDataTypeInteger     MultipartFormDataType = "integer"
	MultipartFormDataTypeUploadToken MultipartFormDataType = "upload_token"
	MultipartFormDataTypeBinaryData  MultipartFormDataType = "binary_data"
)

type OptionalType string

const (
	OptionalTypeRequired  OptionalType = ""          // 用户必须传值
	OptionalTypeOmitEmpty OptionalType = "omitempty" // 如果用户不传值，则该字段省略
	OptionalTypeKeepEmpty OptionalType = "keepempty" // 即使用户不传值，也会发送空值
	OptionalTypeNullable  OptionalType = "nullable"  // 如果用户不传值，则该字段省略，但如果用户传值，即使是空值也会发送
)

type Authorization string

const (
	AuthorizationNone    Authorization = ""
	AuthorizationQbox    Authorization = "Qbox"
	AuthorizationQiniu   Authorization = "qiniu"
	AuthorizationUpToken Authorization = "UploadToken"
)

type Idempotent string

const (
	IdempotentDefault Idempotent = "default" // 默认幂等性（根据 HTTP 方法判定）
	IdempotentAlways  Idempotent = "always"  // 总是幂等
	IdempotentNever   Idempotent = "never"   // 总是不幂等
)

type EncodeType string

const (
	EncodeTypeNone                EncodeType = "none"
	EncodeTypeUrlSafeBase64       EncodeType = "url_safe_base64"         // 需要进行编码
	EncodeTypeUrlSafeBase64OrNone EncodeType = "url_safe_base64_or_none" // 不仅需要编码，即使路径参数的值是 None 也要编码。该选项暗示了 nullable
)

type ServiceBucketType string

const (
	ServiceBucketTypeNone        ServiceBucketType = ""             //
	ServiceBucketTypePlainText   ServiceBucketType = "plain_text"   // 该值为存储空间名称
	ServiceBucketTypeEntry       ServiceBucketType = "entry"        // 该值格式为 UrlSafeBase64("$bucket:$key")
	ServiceBucketTypeUploadToken ServiceBucketType = "upload_token" // 该值为上传凭证，内部包含存储空间信息
)

type ServiceObjectType string

const (
	ServiceObjectTypeNone      ServiceObjectType = ""           //
	ServiceObjectTypePlainText ServiceObjectType = "plain_text" // 该值为对象名称
	ServiceObjectTypeEntry     ServiceObjectType = "entry"      // 该值格式为 UrlSafeBase64("$bucket:$key")
)

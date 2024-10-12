package apidesc

import (
	"fmt"

	"gopkg.in/yaml.v3"
)

type NamedPathParam struct {
	Documentation      string             `yaml:"documentation,omitempty"`         // URL 路径参数文档
	PathSegment        string             `yaml:"path_segment,omitempty"`          // URL 路径段落，如果为空，则表示参数直接追加在 URL 路径末尾
	FieldName          string             `yaml:"field_name,omitempty"`            // URL 路径参数名称
	FieldCamelCaseName string             `yaml:"field_camel_case_name,omitempty"` // URL 路径参数驼峰命名
	FieldSnakeCaseName string             `yaml:"field_snake_case_name,omitempty"` // URL 路径参数下划线命名
	Type               *StringLikeType    `yaml:"type,omitempty"`                  // URL 路径参数类型
	Encode             *EncodeType        `yaml:"encode,omitempty"`                // URL 路径参数编码方式，如果为空，表示直接转码成字符串
	ServiceBucket      *ServiceBucketType `yaml:"service_bucket,omitempty"`        // URL 路径参数是否是空间名称，如果为空，则表示不是，如果不为空，则填写格式
	ServiceObject      *ServiceObjectType `yaml:"service_object,omitempty"`        // URL 路径参数是否是对象名称，如果为空，则表示不是，如果不为空，则填写格式
	Optional           *OptionalType      `yaml:"optional,omitempty"`              // URL 路径参数是否可选，如果为空，则表示必填
}

type FreePathParams struct {
	FieldName          string      `yaml:"field_name,omitempty"`            // URL 路径参数名称
	FieldCamelCaseName string      `yaml:"field_camel_case_name,omitempty"` // URL 路径参数驼峰命名
	FieldSnakeCaseName string      `yaml:"field_snake_case_name,omitempty"` // URL 路径参数下划线命名
	Documentation      string      `yaml:"documentation,omitempty"`         // URL 路径参数文档
	EncodeParamKey     *EncodeType `yaml:"encode_param_key"`                // URL 路径参数键编码方式，如果为空，表示直接转码成字符串
	EncodeParamValue   *EncodeType `yaml:"encode_param_value"`              // URL 路径参数值编码方式，如果为空，表示直接转码成字符串
}

type PathParams struct {
	Named []NamedPathParam `yaml:"named,omitempty"` // URL 路径有名参数列表
	Free  *FreePathParams  `yaml:"free,omitempty"`  // URL 路径自由参数列表
}

type RequestBody struct {
	Json              *JsonType                    `yaml:"json,omitempty"`                // JSON 类型
	FormUrlencoded    *FormUrlencodedRequestStruct `yaml:"form_urlencoded,omitempty"`     // URL 编码表单调用（无法上传二进制数据）
	MultipartFormData *MultipartFormFields         `yaml:"multipart_form_data,omitempty"` // 复合表单调用（可以上传二进制数据）
	BinaryData        bool                         `yaml:"binary_data,omitempty"`         // 二进制数据
}

func (body *RequestBody) UnmarshalYAML(value *yaml.Node) error {
	switch value.ShortTag() {
	case "!!str":
		switch value.Value {
		case "binary_data":
			body.BinaryData = true
		default:
			return fmt.Errorf("unknown request body type: %s", value.Value)
		}
		return nil
	case "!!map":
		switch value.Content[0].Value {
		case "json":
			return value.Content[1].Decode(&body.Json)
		case "form_urlencoded":
			return value.Content[1].Decode(&body.FormUrlencoded)
		case "multipart_form_data":
			return value.Content[1].Decode(&body.MultipartFormData)
		default:
			return fmt.Errorf("unknown request body type: %s", value.Content[0].Value)
		}
	default:
		return fmt.Errorf("unknown request body type: %s", value.ShortTag())
	}
}

type ApiRequestDescription struct {
	PathParams           *PathParams    `yaml:"path_params,omitempty"`   // URL 路径参数列表
	HeaderNames          HeaderNames    `yaml:"header_names,omitempty"`  // HTTP 头参数列表
	QueryNames           QueryNames     `yaml:"query_names,omitempty"`   // URL 查询参数列表
	Body                 *RequestBody   `yaml:"body,omitempty"`          // 请求体
	Authorization        *Authorization `yaml:"authorization,omitempty"` // 鉴权参数
	Idempotent           *Idempotent    `yaml:"idempotent,omitempty"`    // 幂等性
	responseTypeRequired bool           `yaml:"response_type_required"`  //
}

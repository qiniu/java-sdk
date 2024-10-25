package apidesc

import (
	"fmt"

	"gopkg.in/yaml.v3"
)

type (
	JsonType struct {
		String    bool        `yaml:"string,omitempty"`     // 字符串
		Integer   bool        `yaml:"integer,omitempty"`    // 整型数字
		Float     bool        `yaml:"float,omitempty"`      // 浮点型数字
		Boolean   bool        `yaml:"boolean,omitempty"`    // 布尔值
		Array     *JsonArray  `yaml:"array,omitempty"`      // 数组
		Struct    *JsonStruct `yaml:"struct,omitempty"`     // 结构体
		Any       bool        `yaml:"any,omitempty"`        // 任意数据结构
		StringMap bool        `yaml:"string_map,omitempty"` // 任意字符串映射结构
	}

	JsonArray struct {
		Documentation string    `yaml:"documentation,omitempty"`   // JSON 数组参数文档
		Name          string    `yaml:"name,omitempty"`            // JSON 数组名称
		CamelCaseName string    `yaml:"camel_case_name,omitempty"` // JSON 数组驼峰命名
		SnakeCaseName string    `yaml:"snake_case_name,omitempty"` // JSON 数组下划线命名
		Type          *JsonType `yaml:"type,omitempty"`            // JSON 数组类型
	}

	JsonStruct struct {
		Documentation string      `yaml:"documentation,omitempty"`   // JSON 结构体参数文档
		Name          string      `yaml:"name,omitempty"`            // JSON 结构体名称
		CamelCaseName string      `yaml:"camel_case_name,omitempty"` // JSON 结构体驼峰命名
		SnakeCaseName string      `yaml:"snake_case_name,omitempty"` // JSON 结构体下划线命名
		Fields        []JsonField `yaml:"fields,omitempty"`          // JSON 字段列表
	}

	JsonField struct {
		Documentation      string             `yaml:"documentation,omitempty"`         // JSON 字段参数文档
		Key                string             `yaml:"key,omitempty"`                   // JSON 字段参数名称
		FieldName          string             `yaml:"field_name,omitempty"`            // JSON 字段名称
		FieldCamelCaseName string             `yaml:"field_camel_case_name,omitempty"` // JSON 字段驼峰命名
		FieldSnakeCaseName string             `yaml:"field_snake_case_name,omitempty"` // JSON 字段下划线命名
		Type               JsonType           `yaml:"type,omitempty"`                  // JSON 字段类型
		Optional           *OptionalType      `yaml:"optional,omitempty"`              // JSON 字段是否可选，如果为空，则表示必填
		ServiceBucket      *ServiceBucketType `yaml:"service_bucket,omitempty"`        // JSON 字段是否是空间名称，如果为空，则表示不是，如果不为空，则填写格式
		ServiceObject      *ServiceObjectType `yaml:"service_object,omitempty"`        // JSON 字段是否是对象名称，如果为空，则表示不是，如果不为空，则填写格式
	}
)

func (jsonType *JsonType) UnmarshalYAML(value *yaml.Node) error {
	switch value.ShortTag() {
	case "!!str":
		switch value.Value {
		case "string":
			jsonType.String = true
		case "integer":
			jsonType.Integer = true
		case "float":
			jsonType.Float = true
		case "boolean":
			jsonType.Boolean = true
		case "any":
			jsonType.Any = true
		case "string_map":
			jsonType.StringMap = true
		default:
			return fmt.Errorf("unknown json type: %s", value.Value)
		}
		return nil
	case "!!map":
		switch value.Content[0].Value {
		case "array":
			return value.Content[1].Decode(&jsonType.Array)
		case "struct":
			return value.Content[1].Decode(&jsonType.Struct)
		default:
			return fmt.Errorf("unknown json type: %s", value.Content[0].Value)
		}
	default:
		return fmt.Errorf("unknown json type: %s", value.ShortTag())
	}
}

package apidesc

import (
	"fmt"

	"gopkg.in/yaml.v3"
)

type ResponseBody struct {
	Json             *JsonType `yaml:"json,omitempty"`             // JSON 类型
	BinaryDataStream bool      `yaml:"binaryDataStream,omitempty"` // 二进制数据
}

func (body *ResponseBody) UnmarshalYAML(value *yaml.Node) error {
	switch value.ShortTag() {
	case "!!str":
		switch value.Value {
		case "binary_data_stream":
			body.BinaryDataStream = true
			return nil
		default:
			return fmt.Errorf("unknown response body type: %s", value.Value)
		}
	case "!!map":
		switch value.Content[0].Value {
		case "json":
			return value.Content[1].Decode(&body.Json)
		default:
			return fmt.Errorf("unknown response body type: %s", value.Content[0].Value)
		}
	default:
		return fmt.Errorf("unknown response body type: %s", value.ShortTag())
	}
}

package apidesc

type HeaderName struct {
	HeaderName         string        `yaml:"header_name,omitempty"`           // HTTP 头名称
	Documentation      string        `yaml:"documentation,omitempty"`         // HTTP 头参数文档
	FieldName          string        `yaml:"field_name,omitempty"`            // HTTP 头参数名称
	FieldCamelCaseName string        `yaml:"field_camel_case_name,omitempty"` // HTTP 头参数驼峰命名
	FieldSnakeCaseName string        `yaml:"field_snake_case_name,omitempty"` // HTTP 头参数下划线命名
	Optional           *OptionalType `yaml:"optional,omitempty"`              // HTTP 头参数是否可选，如果为空，则表示必填
}

type HeaderNames []HeaderName

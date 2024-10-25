package apidesc

type QueryName struct {
	FieldName          string             `yaml:"field_name,omitempty"`            // 参数名称
	FieldCamelCaseName string             `yaml:"field_camel_case_name,omitempty"` // URL 路径参数驼峰命名
	FieldSnakeCaseName string             `yaml:"field_snake_case_name,omitempty"` // URL 路径参数下划线命名
	QueryName          string             `yaml:"query_name,omitempty"`            // URL 查询参数名称
	Documentation      string             `yaml:"documentation,omitempty"`         // URL 查询参数文档
	Multiple           bool               `yaml:"multiple,omitempty"`              // URL 查询参数是否可以有多个值
	QueryType          *StringLikeType    `yaml:"query_type,omitempty"`            // URL 查询参数类型
	ServiceBucket      *ServiceBucketType `yaml:"service_bucket,omitempty"`        // URL 查询参数是否是空间名称，如果为空，则表示不是，如果不为空，则填写格式
	ServiceObject      *ServiceObjectType `yaml:"service_object,omitempty"`        // URL 查询参数是否是对象名称，如果为空，则表示不是，如果不为空，则填写格式
	Optional           *OptionalType      `yaml:"optional,omitempty"`              // URL 查询参数是否可选，如果为空，则表示必填
}

type QueryNames []QueryName

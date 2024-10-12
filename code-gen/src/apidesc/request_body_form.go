package apidesc

type FormUrlencodedRequestStruct struct {
	Fields []FormUrlencodedRequestField `yaml:"fields,omitempty"` // URL 编码表单字段列表
}

type FormUrlencodedRequestField struct {
	FieldName          string             `yaml:"field_name,omitempty"`            // URL 编码表单字段名称
	FieldCamelCaseName string             `yaml:"field_camel_case_name,omitempty"` // URL 编码表单字段驼峰命名
	FieldSnakeCaseName string             `yaml:"field_snake_case_name,omitempty"` // URL 编码表单字段下划线命名
	Key                string             `yaml:"key,omitempty"`                   // URL 编码表单参数名称
	Documentation      string             `yaml:"documentation,omitempty"`         // URL 编码表单参数文档
	Type               *StringLikeType    `yaml:"type,omitempty"`                  // URL 编码表单参数类型
	Multiple           bool               `yaml:"multiple,omitempty"`              // URL 编码表单参数是否可以有多个值
	Optional           *OptionalType      `yaml:"optional,omitempty"`              // URL 编码表单参数是否可选，如果为空，则表示必填
	ServiceBucket      *ServiceBucketType `yaml:"service_bucket,omitempty"`        // URL 编码表单参数是否是空间名称，如果为空，则表示不是，如果不为空，则填写格式
	ServiceObject      *ServiceObjectType `yaml:"service_object,omitempty"`        // URL 编码表单参数是否是对象名称，如果为空，则表示不是，如果不为空，则填写格式
}

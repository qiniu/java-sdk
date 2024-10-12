package apidesc

type MultipartFormFields struct {
	Named []NamedMultipartFormField `yaml:"named_fields,omitempty"`
	Free  *FreeMultipartFormFields  `yaml:"free_fields,omitempty"`
}

type NamedMultipartFormField struct {
	FieldName          string                 `yaml:"field_name,omitempty"`
	FieldCamelCaseName string                 `yaml:"field_camel_case_name,omitempty"`
	FieldSnakeCaseName string                 `yaml:"field_snake_case_name,omitempty"`
	Key                string                 `yaml:"key,omitempty"`
	Type               *MultipartFormDataType `yaml:"type,omitempty"`
	Documentation      string                 `yaml:"documentation,omitempty"`
	ServiceBucket      *ServiceBucketType     `yaml:"service_bucket,omitempty"`
	ServiceObject      *ServiceObjectType     `yaml:"service_object,omitempty"`
	Optional           *OptionalType          `yaml:"optional,omitempty"`
}

type FreeMultipartFormFields struct {
	FieldName          string `yaml:"field_name,omitempty"`
	FieldCamelCaseName string `yaml:"field_camel_case_name,omitempty"`
	FieldSnakeCaseName string `yaml:"field_snake_case_name,omitempty"`
	Documentation      string `yaml:"documentation,omitempty"`
}

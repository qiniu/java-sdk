package lang

type FieldType struct {
	FieldType        string
	ClassName        string //  for class
	ContentKeyType   string //  for map，转化后的
	ContentValueType string //  for map and array，转化后的
}

type Language interface {
	PackageName(name string) string
	Indentation() string
	Annotation(info string) string
	ApiFileName(name string) string
	ClassName(name string, isPrivate bool) string
	FieldName(name string, isPrivate bool) string
	FieldTypeDesc(fieldType FieldType) string
	FieldDefaultValue(fieldType FieldType, isOptional bool) string
	FieldGetFunctionName(name string, isPrivate bool) string
	FieldSetFunctionName(name string, isPrivate bool) string
	FunctionName(name string, isPrivate bool) string
}

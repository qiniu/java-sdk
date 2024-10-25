package java

import (
	"fmt"

	"github.com/iancoleman/strcase"

	"github.com/YangSen-qn/code-gen/src/generator/internal/data"
	"github.com/YangSen-qn/code-gen/src/generator/lang"
	"github.com/YangSen-qn/code-gen/src/utils"
)

func Java() lang.Language {
	return &java{}
}

type java struct {
}

func (j *java) PackageName(name string) string {
	return strcase.ToLowerCamel(name)
}

func (j *java) Indentation() string {
	return "    "
}

func (j *java) Annotation(info string) string {
	if newInfo, err := utils.TextLineMapper(info, func(line string, lineNumber int, totalLine int) (t string, stop bool) {
		return " * " + line, false
	}); err != nil {
		return info
	} else {
		return fmt.Sprintf(`
/**
 %s
 */`, newInfo)
	}

}

func (j *java) ApiFileName(name string) string {
	return "Api" + strcase.ToCamel(name) + ".java"
}

func (j *java) ClassName(name string, isPrivate bool) string {
	return "Api" + strcase.ToCamel(name)
}

func (j *java) FunctionName(name string, isPrivate bool) string {
	return strcase.ToLowerCamel(name)
}

func (j *java) FieldName(name string, isPrivate bool) string {
	if name == "private" {
		name = "isPrivate"
	}
	return strcase.ToLowerCamel(name)
}

func (j *java) FieldDefaultValue(fieldType lang.FieldType, isOptional bool) string {
	switch fieldType.FieldType {
	case data.FieldTypeInt:
		if isOptional {
			return "null"
		} else {
			return "0"
		}
	case data.FieldTypeLong:
		if isOptional {
			return "null"
		} else {
			return "0l"
		}
	case data.FieldTypeFloat:
		if isOptional {
			return "null"
		} else {
			return "0"
		}
	case data.FieldTypeBool:
		if isOptional {
			return "null"
		} else {
			return "false"
		}
	case data.FieldTypeString:
		if isOptional {
			return "null"
		} else {
			return "\"\""
		}
	case data.FieldTypeArray:
		if isOptional {
			return "null"
		} else {
			return fmt.Sprintf("new %s[]()", strcase.ToCamel(fieldType.ContentValueType))
		}
	case data.FieldTypeMap:
		if isOptional {
			return "null"
		} else {
			return fmt.Sprintf("new HashMap<%s,%s>()", strcase.ToCamel(fieldType.ContentKeyType), strcase.ToCamel(fieldType.ContentValueType))
		}
	case data.FieldTypeClass:
		if isOptional {
			return "null"
		} else {
			return fmt.Sprintf("new %s()", strcase.ToCamel(fieldType.ContentKeyType))
		}
	case data.FieldTypeBinaryData:
		if isOptional {
			return ""
		} else {
			return "new byte[]()"
		}
	}

	panic(fmt.Sprintf("unknown field type: %+v", fieldType))
}

func (j *java) FieldGetFunctionName(name string, isPrivate bool) string {
	return "get" + strcase.ToCamel(name)
}

func (j *java) FieldSetFunctionName(name string, isPrivate bool) string {
	return "set" + strcase.ToCamel(name)
}

func (j *java) FieldTypeDesc(fieldType lang.FieldType) string {
	switch fieldType.FieldType {
	case data.FieldTypeInt:
		return "Integer"
	case data.FieldTypeLong:
		return "Long"
	case data.FieldTypeFloat:
		return "Float"
	case data.FieldTypeBool:
		return "Boolean"
	case data.FieldTypeString:
		return "String"
	case data.FieldTypeArray:
		return fmt.Sprintf("%s[]", strcase.ToCamel(fieldType.ContentValueType))
	case data.FieldTypeMap:
		return fmt.Sprintf("Map<%s,%s>", strcase.ToCamel(fieldType.ContentKeyType), strcase.ToCamel(fieldType.ContentValueType))
	case data.FieldTypeClass:
		return strcase.ToCamel(fieldType.ClassName)
	case data.FieldTypeAny:
		return "Object"
	case data.FieldTypeBinaryData:
		return "byte[]"
	}

	panic(fmt.Sprintf("unknown field type: %+v", fieldType))
}

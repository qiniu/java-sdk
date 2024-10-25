package data

import (
	"fmt"

	"github.com/YangSen-qn/code-gen/src/apidesc"
	"github.com/YangSen-qn/code-gen/src/generator/lang"
)

const (
	defaultFieldName = "data"
)

type Field struct {
	Document         string `json:"document"`
	Name             string `json:"name"`
	Type             string `json:"type"` // 参考：parseJsonTypeToField
	PublicGetFunc    bool   `json:"public_get_func"`
	GetFuncName      string `json:"get_func_name"`
	PublicSetFunc    bool   `json:"public_set_func"`
	SetFuncName      string `json:"set_func_name"`
	Key              string `json:"key"`
	KeyOptional      bool   `json:"key_optional"`   // true: value 无值，则 Key 和 Value 忽略，如果为 false, Key 必传，Value 看实际情况
	ValueOptional    bool   `json:"value_optional"` // Key 为 false 时生效，如果为 true, Value 可忽略
	Default          string `json:"default"`
	Encode           string `json:"encode"`
	EncodeDefault    string `json:"encode_default"` // Encode 模式，Value 为空，如果此值不为空，则使用此值作为编码后的值
	KeyEncode        string `json:"key_encode"`
	KeyEncodeDefault string `json:"key_encode_default"` // KeyEncode Encode 模式，Value 为空，如果此值不为空，则使用此值作为编码后的值
	Deprecated       bool   `json:"deprecated"`

	typeInfo lang.FieldType //
}

func (f *Field) IsMap() bool {
	return f.typeInfo.FieldType == FieldTypeMap
}

func (f *Field) ContentValueType() string {
	return f.typeInfo.ContentValueType
}

type Class struct {
	Document  string   `json:"document"`
	ClassName string   `json:"class_name"`
	IsInner   bool     `json:"is_inner"`
	Fields    []*Field `json:"fields"`
}

func parseJsonTypeToField(l lang.Language, t *apidesc.JsonType) (f Field, classes []Class) {
	f = Field{
		Document:      "数据信息",
		Name:          defaultFieldName,
		PublicGetFunc: false,
		GetFuncName:   "",
		PublicSetFunc: false,
		SetFuncName:   "",
		Key:           "",
		Type:          "",
		KeyOptional:   false,
		ValueOptional: false,
		Default:       "null",
		Encode:        "",
		Deprecated:    false,
	}
	if t.String {
		f.typeInfo = lang.FieldType{
			FieldType: FieldTypeString,
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		return
	} else if t.Integer {
		f.typeInfo = lang.FieldType{
			FieldType: FieldTypeInt,
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		return
	} else if t.Float {
		f.typeInfo = lang.FieldType{
			FieldType: FieldTypeFloat,
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		return
	} else if t.Boolean {
		f.typeInfo = lang.FieldType{
			FieldType: FieldTypeBool,
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		return
	} else if t.Array != nil {
		ff, cc := parseJsonTypeToField(l, t.Array.Type)
		if len(cc) > 0 {
			classes = append(classes, cc...)
		}
		f.Name = l.FieldName(t.Array.Name, false)
		f.Document = t.Array.Documentation
		f.typeInfo = lang.FieldType{
			FieldType:        FieldTypeArray,
			ContentValueType: ff.Type,
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		return
	} else if t.Struct != nil {
		f.Name = defaultFieldName
		f.Document = t.Struct.Documentation
		f.typeInfo = lang.FieldType{
			FieldType: FieldTypeClass,
			ClassName: t.Struct.Name,
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		t.Struct.Name = f.Type

		// 解析 Field
		classFields := make([]*Field, 0)
		for _, field := range t.Struct.Fields {
			ff, cc := parseJsonTypeToField(l, &field.Type)
			if len(cc) > 0 {
				classes = append(classes, cc...)
			}
			ff.Document = field.Documentation
			// TODO: 字段是否私有，一般是公开的，但需要确认
			ff.Name = l.FieldName(field.FieldName, false)
			ff.Key = field.Key
			ff.KeyOptional, ff.ValueOptional = parseOptional(nil, field.Optional)
			ff.GetFuncName = l.FieldGetFunctionName(field.FieldName, false)
			ff.SetFuncName = l.FieldSetFunctionName(field.FieldName, false)
			// TODO: 默认值
			ff.Default = "null"
			classFields = append(classFields, &ff)
		}

		classes = append(classes, Class{
			Document:  t.Struct.Documentation,
			ClassName: t.Struct.Name,
			IsInner:   true,
			Fields:    classFields,
		})

		return
	} else if t.Any {
		f.Key = defaultFieldName
		f.typeInfo = lang.FieldType{
			FieldType: FieldTypeAny,
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		return
	} else if t.StringMap {
		f.Key = defaultFieldName
		f.typeInfo = lang.FieldType{
			FieldType: FieldTypeMap,
			ContentKeyType: l.FieldTypeDesc(lang.FieldType{
				FieldType: FieldTypeString,
			}),
			ContentValueType: l.FieldTypeDesc(lang.FieldType{
				FieldType: FieldTypeAny,
			}),
		}
		f.Type = l.FieldTypeDesc(f.typeInfo)
		return
	}

	panic(fmt.Sprintf("json type not found:%+v", t))
}

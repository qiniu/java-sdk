package data

import (
	"fmt"

	"github.com/YangSen-qn/code-gen/src/apidesc"
	"github.com/YangSen-qn/code-gen/src/generator/lang"
)

const (
	FieldInPath  = "path"
	FieldInQuery = "query"
	FieldInHead  = "head"
	FieldInBody  = "body"
)

const (
	FieldTypeInt        = "int"
	FieldTypeInteger    = "integer"
	FieldTypeLong       = "long"
	FieldTypeFloat      = "float"
	FieldTypeBool       = "bool"
	FieldTypeString     = "string"
	FieldTypeArray      = "array"
	FieldTypeMap        = "map"
	FieldTypeAny        = "any"
	FieldTypeClass      = "class"
	FieldTypeBinaryData = "binary"
)

var fieldTypeMapper = map[string]string{
	apidesc.StringLikeTypeInteger:            FieldTypeInt,
	apidesc.StringLikeTypeBoolean:            FieldTypeBool,
	apidesc.StringLikeTypeFloat:              FieldTypeFloat,
	apidesc.StringLikeTypeString:             FieldTypeString,
	apidesc.MultipartFormDataTypeUploadToken: FieldTypeString,
	apidesc.MultipartFormDataTypeBinaryData:  FieldTypeBinaryData,
}

func transformFileType(fieldName string, t *apidesc.StringLikeType) lang.FieldType {
	if t == nil {
		panic(fmt.Sprintf("field %s type is nil", fieldName))
	}

	fieldType, ok := fieldTypeMapper[*t]
	if !ok {
		panic(fmt.Sprintf("field %s type is not support", *t))
	}

	newFieldType := lang.FieldType{
		FieldType:        fieldType,
		ContentKeyType:   "",
		ContentValueType: "",
	}

	// 大小转换成 long，其他是 int
	if fieldType == FieldTypeInt && fieldName == "fsize" {
		newFieldType.FieldType = FieldTypeLong
	}

	return newFieldType
}

type Body struct {
	Type     string
	IsBinary bool
}

type Request struct {
	Document            string                        `json:"document"`
	PackageName         string                        `json:"package_name"`
	ClassName           string                        `json:"class_name"`
	Method              string                        `json:"method"`
	Desc                apidesc.ApiRequestDescription `json:"desc"`
	Language            lang.Language                 `json:"-"`
	BasicPath           string                        `json:"-"`
	PathSuffix          string                        `json:"-"`
	Authorization       string                        `json:"-"`
	RequireFields       []Field                       `json:"-"`
	OptionsFields       []Field                       `json:"-"`
	PathFields          []Field                       `json:"-"`
	QueryFields         []Field                       `json:"-"`
	HeadFields          []Field                       `json:"-"`
	BodyFields          []Field                       `json:"-"` // json 或 表单
	Classes             []Class                       `json:"-"`
	IsBodyBinary        bool                          `json:"-"`
	IsBodyJson          bool                          `json:"-"`
	IsBodyForm          bool                          `json:"-"`
	IsBodyMultiPartFrom bool                          `json:"-"`
	HasEncodeFields     bool                          `json:"-"`
}

func (r *Request) JsonBodyField() *Field {
	if !r.IsBodyJson || len(r.BodyFields) == 0 {
		return nil
	}

	return &r.BodyFields[0]
}

func (r *Request) Setup() *Request {
	r.getFieldsFromPath()
	r.getFieldsFromQuery()
	r.getFieldsFromHeader()
	r.getFieldsFromBody()

	for _, class := range r.Classes {
		for _, field := range class.Fields {
			field.PublicSetFunc = true
		}
	}

	if authorization := r.Desc.Authorization; authorization != nil {
		r.Authorization = string(*authorization)
	}

	return r
}

func (r *Request) getFieldsFromPath() {
	hasEncodeFields := false

	if path := r.Desc.PathParams; path != nil {
		for _, f := range path.Named {
			kOptional, vOptional := parseOptional(f.Encode, f.Optional)
			fieldType := transformFileType(f.FieldName, f.Type)
			encode, defaultEncode := parseEncode(f.Encode)
			field := Field{Document: f.Documentation,
				Name:          r.Language.FieldName(f.FieldName, true),
				GetFuncName:   r.Language.FieldGetFunctionName(f.FieldName, true),
				SetFuncName:   r.Language.FieldSetFunctionName(f.FieldName, !vOptional),
				Key:           f.PathSegment,
				Type:          r.Language.FieldTypeDesc(fieldType),
				typeInfo:      fieldType,
				KeyOptional:   kOptional,
				ValueOptional: vOptional,
				Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
				Encode:        encode,
				EncodeDefault: defaultEncode,
				Deprecated:    false,
			}

			if len(field.Encode) > 0 {
				hasEncodeFields = true
			}

			r.PathFields = append(r.PathFields, field)

			if vOptional {
				r.OptionsFields = append(r.OptionsFields, field)
			} else {
				r.RequireFields = append(r.RequireFields, field)
			}
		}

		if f := path.Free; f != nil {
			fieldType := lang.FieldType{
				FieldType: FieldTypeMap,
				ContentKeyType: r.Language.FieldTypeDesc(lang.FieldType{
					FieldType: FieldTypeString,
				}),
				ContentValueType: r.Language.FieldTypeDesc(lang.FieldType{
					FieldType: FieldTypeString,
				}),
			}

			field := Field{
				Document:      f.Documentation,
				Name:          r.Language.FieldName(f.FieldName, true),
				GetFuncName:   r.Language.FieldGetFunctionName(f.FieldName, true),
				SetFuncName:   r.Language.FieldSetFunctionName(f.FieldName, true),
				Key:           "",
				Type:          r.Language.FieldTypeDesc(fieldType),
				typeInfo:      fieldType,
				ValueOptional: true,
				Default:       r.Language.FieldDefaultValue(fieldType, false),
				Encode:        "",
				Deprecated:    false,
			}
			field.KeyEncode, field.KeyEncodeDefault = parseEncode(f.EncodeParamKey)
			field.Encode, field.EncodeDefault = parseEncode(f.EncodeParamValue)

			r.PathFields = append(r.PathFields, field)
			r.OptionsFields = append(r.OptionsFields, field)
		}
	}

	r.HasEncodeFields = r.HasEncodeFields || hasEncodeFields
}

func (r *Request) getFieldsFromQuery() {
	if queryItems := r.Desc.QueryNames; queryItems != nil {
		for _, f := range queryItems {
			fieldType := transformFileType(f.FieldName, f.QueryType)
			kOptional, vOptional := parseOptional(nil, f.Optional)
			field := Field{
				Document:      f.Documentation,
				Name:          r.Language.FieldName(f.FieldName, true),
				GetFuncName:   r.Language.FieldGetFunctionName(f.FieldName, true),
				SetFuncName:   r.Language.FieldSetFunctionName(f.FieldName, !vOptional),
				Key:           f.QueryName,
				Type:          r.Language.FieldTypeDesc(fieldType),
				typeInfo:      fieldType,
				Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
				KeyOptional:   kOptional,
				ValueOptional: vOptional,
				Deprecated:    false,
			}

			r.QueryFields = append(r.QueryFields, field)

			if vOptional {
				r.OptionsFields = append(r.OptionsFields, field)
			} else {
				r.RequireFields = append(r.RequireFields, field)
			}
		}
	}
}

func (r *Request) getFieldsFromHeader() {
	if headers := r.Desc.HeaderNames; headers != nil {
		for _, f := range headers {
			fieldType := lang.FieldType{
				FieldType: FieldTypeString,
			}
			kOptional, vOptional := parseOptional(nil, f.Optional)
			field := Field{
				Document:      f.Documentation,
				Name:          r.Language.FieldName(f.FieldName, true),
				GetFuncName:   r.Language.FieldGetFunctionName(f.FieldName, true),
				SetFuncName:   r.Language.FieldSetFunctionName(f.FieldName, !vOptional),
				Key:           f.HeaderName,
				Type:          r.Language.FieldTypeDesc(fieldType),
				typeInfo:      fieldType,
				Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
				KeyOptional:   kOptional,
				ValueOptional: vOptional,
				Deprecated:    false,
			}

			r.HeadFields = append(r.HeadFields, field)

			if vOptional {
				r.OptionsFields = append(r.OptionsFields, field)
			} else {
				r.RequireFields = append(r.RequireFields, field)
			}
		}
	}
}

func (r *Request) getFieldsFromBody() {
	body := r.Desc.Body
	if body == nil {
		return
	}

	if body.BinaryData {
		r.IsBodyBinary = true
		kOptional, vOptional := false, false
		fieldType := lang.FieldType{
			FieldType: FieldTypeBinaryData,
		}
		fieldName := "data"
		field := Field{
			Document:      "请求数据",
			Name:          r.Language.FieldName(fieldName, true),
			GetFuncName:   r.Language.FieldGetFunctionName(fieldName, true),
			SetFuncName:   r.Language.FieldSetFunctionName(fieldName, !vOptional),
			Key:           fieldName,
			Type:          r.Language.FieldTypeDesc(fieldType),
			typeInfo:      fieldType,
			Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
			KeyOptional:   kOptional,
			ValueOptional: vOptional,
			Deprecated:    false,
		}

		r.BodyFields = append(r.BodyFields, field)

		if vOptional {
			r.OptionsFields = append(r.OptionsFields, field)
		} else {
			r.RequireFields = append(r.RequireFields, field)
		}
		return
	}

	if data := body.FormUrlencoded; data != nil {
		if len(data.Fields) == 0 {
			return
		}

		for _, f := range data.Fields {
			kOptional, vOptional := parseOptional(nil, f.Optional)
			fieldType := transformFileType(f.FieldName, f.Type)
			field := Field{
				Document:      f.Documentation,
				Name:          r.Language.FieldName(f.FieldName, true),
				GetFuncName:   r.Language.FieldGetFunctionName(f.FieldName, true),
				SetFuncName:   r.Language.FieldSetFunctionName(f.FieldName, !vOptional),
				Key:           f.Key,
				Type:          r.Language.FieldTypeDesc(fieldType),
				typeInfo:      fieldType,
				Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
				KeyOptional:   kOptional,
				ValueOptional: vOptional,
				Deprecated:    false,
			}

			r.BodyFields = append(r.BodyFields, field)

			if vOptional {
				r.OptionsFields = append(r.OptionsFields, field)
			} else {
				r.RequireFields = append(r.RequireFields, field)
			}
		}

		r.IsBodyForm = true
		return
	}

	if data := body.MultipartFormData; data != nil {
		if nData := data.Named; nData != nil {
			for _, f := range nData {
				kOptional, vOptional := parseOptional(nil, f.Optional)
				fieldType := transformFileType(f.FieldName, f.Type)
				field := Field{
					Document:      f.Documentation,
					Name:          r.Language.FieldName(f.FieldName, true),
					GetFuncName:   r.Language.FieldGetFunctionName(f.FieldName, true),
					SetFuncName:   r.Language.FieldSetFunctionName(f.FieldName, !vOptional),
					Key:           f.Key,
					Type:          r.Language.FieldTypeDesc(fieldType),
					typeInfo:      fieldType,
					Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
					KeyOptional:   kOptional,
					ValueOptional: vOptional,
					Deprecated:    false,
				}

				r.BodyFields = append(r.BodyFields, field)

				if vOptional {
					r.OptionsFields = append(r.OptionsFields, field)
				} else {
					r.RequireFields = append(r.RequireFields, field)
				}
			}
		}

		if fData := data.Free; fData != nil {
			fieldType := lang.FieldType{
				FieldType:        FieldTypeMap,
				ContentKeyType:   FieldTypeString,
				ContentValueType: FieldTypeString,
			}
			vOptional := false
			field := Field{
				Document:      fData.Documentation,
				Name:          r.Language.FieldName(fData.FieldName, true),
				GetFuncName:   r.Language.FieldGetFunctionName(fData.FieldName, true),
				SetFuncName:   r.Language.FieldSetFunctionName(fData.FieldName, !vOptional),
				Key:           fData.FieldName,
				Type:          r.Language.FieldTypeDesc(fieldType),
				typeInfo:      fieldType,
				Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
				KeyOptional:   false,
				ValueOptional: vOptional,
				Deprecated:    false,
			}

			r.BodyFields = append(r.BodyFields, field)

			if vOptional {
				r.OptionsFields = append(r.OptionsFields, field)
			} else {
				r.RequireFields = append(r.RequireFields, field)
			}
		}

		r.IsBodyMultiPartFrom = true
		return
	}

	if data := body.Json; data != nil {
		field, classes := parseJsonTypeToField(r.Language, data)
		r.BodyFields = append(r.BodyFields, field)

		if field.ValueOptional {
			r.OptionsFields = append(r.OptionsFields, field)
		} else {
			r.RequireFields = append(r.RequireFields, field)
		}

		r.Classes = classes
		r.IsBodyJson = true
		return
	}
}

func parseEncode(encode *apidesc.EncodeType) (string, string) {
	if encode == nil || *encode == apidesc.EncodeTypeNone {
		return "", ""
	}

	if *encode == apidesc.EncodeTypeUrlSafeBase64 {
		return string(*encode), ""
	} else if *encode == apidesc.EncodeTypeUrlSafeBase64OrNone {
		return string(*encode), "~"
	}

	panic(fmt.Errorf("unknown encode type: %s", *encode))
}

func parseOptional(encode *apidesc.EncodeType, optional *apidesc.OptionalType) (keyOptional, valueOptional bool) {
	encodeType, encodeDefault := parseEncode(encode)
	if optional == nil || len(*optional) == 0 {
		keyOptional = false
		valueOptional = len(encodeType) > 0 && len(encodeDefault) > 0 // encode 有默认值，此 value 可以不传
	} else if *optional == apidesc.OptionalTypeOmitEmpty {
		// 如果用户不传值，则该字段省略
		keyOptional = true
		valueOptional = true
	} else if *optional == apidesc.OptionalTypeNullable {
		// 如果用户不传值，则该字段省略，但如果用户传值，即使是空值也会发送
		keyOptional = true
		valueOptional = true
	} else if *optional == apidesc.OptionalTypeKeepEmpty {
		// 即使用户不传值，也会发送空值
		keyOptional = false
		valueOptional = true
	} else {
		keyOptional = false
		valueOptional = len(encodeType) > 0 && len(encodeDefault) > 0 // encode 有默认值，此 value 可以不传
	}
	return
}

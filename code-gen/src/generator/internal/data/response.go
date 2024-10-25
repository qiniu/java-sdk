package data

import (
	"github.com/YangSen-qn/code-gen/src/apidesc"
	"github.com/YangSen-qn/code-gen/src/generator/lang"
)

type Response struct {
	Document     string                         `json:"document"`
	PackageName  string                         `json:"package_name"`
	ClassName    string                         `json:"class_name"`
	Desc         apidesc.ApiResponseDescription `json:"desc"`
	Language     lang.Language                  `json:"-"`
	HeadFields   []Field                        `json:"-"`
	BodyField    Field                          `json:"-"`
	IsStreamBody bool                           `json:"-"`
	IsBodyJson   bool                           `json:"-"`
	Classes      []Class                        `json:"-"`
}

func (r *Response) Setup() *Response {
	r.getFieldsFromHeader()
	r.getFieldsFromBody()

	for _, class := range r.Classes {
		if class.ClassName == "" {
			class.ClassName = "ResponseData"
		}

		for _, field := range class.Fields {
			field.PublicGetFunc = true
		}
	}
	return r
}

func (r *Response) getFieldsFromHeader() {
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
				Default:       r.Language.FieldDefaultValue(fieldType, vOptional),
				KeyOptional:   kOptional,
				ValueOptional: vOptional,
				Deprecated:    false,
			}

			r.HeadFields = append(r.HeadFields, field)
		}
	}
}

func (r *Response) getFieldsFromBody() {
	body := r.Desc.Body
	if body == nil {
		return
	}

	if body.BinaryDataStream {
		r.IsStreamBody = true
		return
	}

	if data := body.Json; data != nil {
		field, classes := parseJsonTypeToField(r.Language, data)
		r.BodyField = field
		r.BodyField.PublicGetFunc = true
		r.BodyField.GetFuncName = r.Language.FieldGetFunctionName(field.Name, false)

		r.Classes = classes
		r.IsBodyJson = true
		return
	}
}

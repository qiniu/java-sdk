package data

import (
	"github.com/YangSen-qn/code-gen/src/generator/lang"
	"github.com/YangSen-qn/code-gen/src/utils"
)

type Api struct {
	PackageName  string
	Document     string
	ClassName    string
	RequestCode  string
	ResponseCode string
	UseEncode    bool
	Request      *Request
	Response     *Response
	Language     lang.Language
}

func (a *Api) HasOtherClasses() bool {
	return len(a.Request.Classes) > 0 || len(a.Response.Classes) > 0
}

func (a *Api) HasMapFields() bool {
	for _, f := range a.Request.RequireFields {
		if f.IsMap() {
			return true
		}
	}

	for _, f := range a.Request.OptionsFields {
		if f.IsMap() {
			return true
		}
	}

	for _, class := range a.Request.Classes {
		for _, field := range class.Fields {
			if field.IsMap() {
				return true
			}

		}
	}

	for _, class := range a.Response.Classes {
		for _, field := range class.Fields {
			if field.IsMap() {
				return true
			}
		}
	}

	return false
}

func (a *Api) IsFromRequestBody() bool {
	return a.Request.IsBodyForm || a.Request.IsBodyMultiPartFrom
}

func (a *Api) IsJsonRequestBody() bool {
	return a.Request.IsBodyJson
}

func (a *Api) IsJsonResponseBody() bool {
	return a.Response.IsBodyJson
}

func (a *Api) AddIndentation(info string) string {
	if newInfo, err := utils.TextLineMapper(info, func(line string, lineNumber int, totalLine int) (t string, stop bool) {
		return a.Language.Indentation() + line, false
	}); err != nil {
		return info
	} else {
		return newInfo
	}
}

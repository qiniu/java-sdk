package utils

import (
	"text/template"
)

func TemplateFunctions() template.FuncMap {
	return template.FuncMap{
		"add": Add,
		"sub": Sub,
		"mul": Mul,
		"div": Div,
	}
}

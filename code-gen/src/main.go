package main

import (
	"fmt"

	"github.com/YangSen-qn/code-gen/src/apidesc"
	"github.com/YangSen-qn/code-gen/src/generator"
	"github.com/YangSen-qn/code-gen/src/generator/lang/java"
	"github.com/YangSen-qn/code-gen/src/parser"
)

func main() {

	apiDescDir := "api-specs"

	gen := generator.New(generator.Config{
		Language:      java.Java(),
		TemplateDir:   "temp/java",
		OutputDir:     "../src/main/java/com/qiniu",
		PackagePrefix: "com.qiniu.",
	})

	if e := parser.Parser(apiDescDir, func(desc *apidesc.ApiDetailedDescription) error {
		fmt.Println(desc)
		return gen.Generate(desc)
	}); e != nil {
		fmt.Printf("error: %s", e)
	}
}

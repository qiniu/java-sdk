package generator

import (
	"bytes"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"text/template"

	"github.com/YangSen-qn/code-gen/src/apidesc"
	"github.com/YangSen-qn/code-gen/src/generator/internal/data"
	"github.com/YangSen-qn/code-gen/src/utils"
)

type Generator interface {
	Generate(desc *apidesc.ApiDetailedDescription) error
}

func New(cfg Config) Generator {
	return &generator{
		Config: cfg,
	}
}

type generator struct {
	Config

	api     data.Api
	funcMap template.FuncMap
}

func (g *generator) Generate(desc *apidesc.ApiDetailedDescription) error {
	pkgName := g.Language.PackageName(desc.PackageName)
	apiFileName := g.Language.ApiFileName(desc.Name)
	outputPath := filepath.Join(g.OutputDir, pkgName, "apis", apiFileName)
	outputDir := filepath.Dir(outputPath)
	pkgName = pkgName + ".apis"
	if exist, err := utils.ExistDir(outputDir); err != nil {
		return err
	} else if !exist {
		if e := os.MkdirAll(outputDir, 0755); e != nil {
			return e
		}
	}

	g.api = data.Api{
		PackageName: g.PackagePrefix + pkgName,
		Document:    g.Language.Annotation(desc.Documentation),
		ClassName:   g.Language.ClassName(desc.Name, false),
		Language:    g.Language,
	}

	g.funcMap = template.FuncMap{
		"addIndentation":    g.api.AddIndentation,
		"generateClassCode": g.generateClassCode,
	}

	requestData := &data.Request{
		PackageName: g.PackagePrefix + pkgName,
		Document:    g.Language.Annotation(desc.Documentation),
		ClassName:   g.Language.ClassName(desc.Name, false),
		Method:      strings.ToUpper(string(desc.Method)),
		BasicPath:   strings.TrimPrefix(desc.BasePath, "/"),   // 去除首部的 /
		PathSuffix:  strings.TrimPrefix(desc.PathSuffix, "/"), // 去除首部的 /
		Desc:        desc.Request,
		Language:    g.Language,
	}
	requestData.Setup()
	requestCode := g.generateRequestCode(requestData)

	responseData := &data.Response{
		PackageName: g.PackagePrefix + pkgName,
		Document:    g.Language.Annotation(desc.Documentation),
		ClassName:   g.Language.ClassName(desc.Name, false),
		Desc:        desc.Response,
		Language:    g.Language,
	}
	responseData.Setup()
	responseCode := g.generateResponseCode(responseData)

	apiCode := g.generateApiCode(&data.Api{
		PackageName:  g.PackagePrefix + pkgName,
		Document:     g.Language.Annotation(desc.Documentation),
		ClassName:    g.Language.ClassName(desc.Name, false),
		RequestCode:  requestCode,
		ResponseCode: responseCode,
		Language:     g.Language,
		UseEncode:    requestData.HasEncodeFields,
		Request:      requestData,
		Response:     responseData,
	})

	f, err := os.Create(outputPath)
	if err != nil {
		return err
	}
	defer f.Close()

	_, err = f.WriteString(apiCode)
	return err
}

func (g *generator) generateRequestCode(info any) string {
	code, err := g.generateCode(info, g.apiRequestTemplatePath()...)
	if err != nil {
		panic(fmt.Sprintf("generateRequestCode error:%v ", err))
	}
	return code
}

func (g *generator) generateResponseCode(info any) string {
	code, err := g.generateCode(info, g.apiResponseTemplatePath()...)
	if err != nil {
		panic(fmt.Sprintf("generateResponseCode error:%v ", err))
	}
	return code
}

func (g *generator) generateClassCode(info any) string {
	code, err := g.generateCode(info, g.apiClassTemplatePath()...)
	if err != nil {
		panic(fmt.Sprintf("generateClass error:%v ", err))
	}
	return code
}

func (g *generator) generateApiCode(info any) string {
	code, err := g.generateCode(info, g.apiTemplatePath())
	if err != nil {
		panic(fmt.Sprintf("generateApiCode error:%v ", err))
	}
	return code
}

func (g *generator) generateCode(data any, templatePaths ...string) (string, error) {
	if len(templatePaths) == 0 {
		return "", fmt.Errorf("no template path")
	}

	allFuncs := utils.TemplateFunctions()
	for name, fn := range g.funcMap {
		allFuncs[name] = fn
	}

	name := filepath.Base(templatePaths[0])
	t, err := template.New(name).Funcs(allFuncs).ParseFiles(templatePaths...)
	if err != nil {
		return "", err
	}

	w := bytes.NewBuffer([]byte{})
	if err = t.Execute(w, data); err != nil {
		return "", err
	}

	return w.String(), nil
}

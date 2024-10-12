package generator

import (
	"path/filepath"

	"github.com/YangSen-qn/code-gen/src/generator/lang"
)

type Config struct {
	Language      lang.Language //
	TemplateDir   string        // 模版路径
	OutputDir     string        // 输出路径
	PackagePrefix string        // 包名前缀
}

func (c Config) apiTemplatePath() string {
	return c.templatePath(TemplateFileNameApi)
}

func (c Config) apiClassTemplatePath() []string {
	templateNames := []string{
		TemplateFileNameCreateClass,
	}
	paths := make([]string, 0)
	for _, name := range templateNames {
		paths = append(paths, c.templatePath(name))
	}
	return paths
}

func (c Config) apiRequestTemplatePath() []string {
	templateNames := []string{
		TemplateFileNameRequest,
		TemplateFileNameCreateClass,
	}
	paths := make([]string, 0)
	for _, name := range templateNames {
		paths = append(paths, c.templatePath(name))
	}
	return paths
}

func (c Config) apiResponseTemplatePath() []string {
	templateNames := []string{
		TemplateFileNameResponse,
	}
	paths := make([]string, 0)
	for _, name := range templateNames {
		paths = append(paths, c.templatePath(name))
	}
	return paths
}

func (c Config) templatePath(name string) string {
	path := name

	if len(c.TemplateDir) > 0 {
		path = filepath.Join(c.TemplateDir, path)
	}

	return path
}

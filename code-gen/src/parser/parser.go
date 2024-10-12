package parser

import (
	"fmt"
	"io/fs"
	"os"
	"path/filepath"
	"strings"

	"gopkg.in/yaml.v3"

	"github.com/YangSen-qn/code-gen/src/apidesc"
	"github.com/YangSen-qn/code-gen/src/utils"
)

func isApiSpecFile(path string) bool {
	return strings.HasSuffix(path, ".yml")
}

type ApiDescHandler func(desc *apidesc.ApiDetailedDescription) error

func Parser(apiDir string, handler ApiDescHandler) error {
	if handler == nil {
		return nil
	}

	return filepath.WalkDir(apiDir, func(path string, d fs.DirEntry, err error) error {
		if err != nil {
			return err
		}

		// 检查
		apiPath := path
		if exist, e := utils.ExistFile(apiPath); e != nil {
			return e
		} else if !exist {
			return nil
		}

		if !isApiSpecFile(apiPath) {
			return nil
		}

		descFile, err := os.Open(apiPath)
		if err != nil {
			return err
		}
		defer descFile.Close()

		pkgName := strings.TrimPrefix(path, apiDir)
		pkgName = strings.TrimPrefix(pkgName, "/")
		pkgName = strings.TrimSuffix(pkgName, d.Name())
		pkgName = strings.TrimSuffix(pkgName, "/")

		desc := apidesc.ApiDetailedDescription{
			PackageName: pkgName,
			Name:        strings.TrimSuffix(d.Name(), ".yml"),
		}
		decoder := yaml.NewDecoder(descFile)
		decoder.KnownFields(true)
		if err = decoder.Decode(&desc); err != nil {
			return fmt.Errorf("parse api:%s error:%s", apiPath, err)
		}

		return handler(&desc)
	})
}

package apidesc

type ApiDetailedDescription struct {
	Documentation string                 `yaml:"documentation,omitempty"`   // API 文档
	PackageName   string                 `yaml:"package_name,omitempty"`    // 包名，文件路径中提取
	Name          string                 `yaml:"name,omitempty"`            // API 名称，文件名
	CamelCaseName string                 `yaml:"camel_case_name,omitempty"` // 驼峰命名
	SnakeCaseName string                 `yaml:"snake_case_name,omitempty"` // 蛇形命名：特指下划线命名
	Method        MethodName             `yaml:"method,omitempty"`          // HTTP 方法
	ServiceNames  []ServiceName          `yaml:"service_names,omitempty"`   // 七牛服务名称
	Command       string                 `yaml:"command,omitempty"`         // URL 查询命令
	BasePath      string                 `yaml:"base_path,omitempty"`       // URL 基础路径
	PathSuffix    string                 `yaml:"path_suffix,omitempty"`     // URL 路径后缀
	Request       ApiRequestDescription  `yaml:"request,omitempty"`         // 请求参数
	Response      ApiResponseDescription `yaml:"response,omitempty"`        // 响应参数
}

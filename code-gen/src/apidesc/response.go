package apidesc

type ApiResponseDescription struct {
	HeaderNames HeaderNames   `yaml:"header_names,omitempty"` // HTTP 头参数列表
	Body        *ResponseBody `yaml:"body,omitempty"`         // 响应体
}

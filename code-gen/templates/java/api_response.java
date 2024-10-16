/**
 * 响应信息
 */
public static class Response extends Api.Response {
    {{- if .IsBodyJson}}

    /**
     * {{.BodyField.Document}}
     */
    private {{.BodyField.Type}} {{.BodyField.Name}};
    {{- end}}

    protected Response(com.qiniu.http.Response response) throws QiniuException {
        super(response);

        {{- if .IsBodyJson}}

        this.{{.BodyField.Name}} = Json.decode(response.bodyString(), {{.BodyField.Type}}.class);
        {{- end}}
    }

    {{- if .IsBodyJson}}

    /**
     * 响应信息
     *
     * @return {{.BodyField.Type}}
     */
    public {{.BodyField.Type}} {{.BodyField.GetFuncName}}() {
        return this.{{.BodyField.Name}};
    }
    {{- end}}
    {{/* 创建 class */}}
    {{- $classList := .Classes}}{{if $classList}}
    {{- range $i, $classInfo := $classList}}
    {{- $classCode := generateClassCode $classInfo }}{{ addIndentation $classCode }}
    {{- end}}
    {{- end}}
}
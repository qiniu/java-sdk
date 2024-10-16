{{- $classInfo := .}}
/**
  * {{.Document}}
  */
public{{if .IsInner}} static{{end}} final class {{ .ClassName }} {
    {{- $fieldList := .Fields}}{{if $fieldList}}
    {{- range $i, $field := $fieldList}}

    /**
     * {{$field.Document}}
     */
    @SerializedName("{{$field.Key}}")
    private {{$field.Type}} {{$field.Name}};
    {{- end}}
    {{- end}}

    {{- $fieldList := .Fields}}{{if $fieldList}}
    {{- range $i, $field := $fieldList}}
    {{- if $field.PublicGetFunc}}

    /**
     * 获取变量值
     * {{$field.Document}}
     *
     * @return {{$field.Name}}
     */
    public {{$field.Type}} {{$field.GetFuncName}}() {
        return this.{{$field.Name}};
    }
    {{- end}}
    {{- if $field.PublicSetFunc}}

    /**
     * 设置变量值
     *
     * @param {{$field.Name}} {{$field.Document}}
     * @return Request
     */
    public {{ $classInfo.ClassName }} {{$field.SetFuncName}}({{$field.Type}} {{$field.Name}}) {
        this.{{$field.Name}} = {{$field.Name}};
        return this;
    }
    {{- end}}
    {{- end}}
    {{- end}}
}
/**
 * 请求信息
 */
public static class Request extends Api.Request {
    {{- /* 必选属性 */}}
    {{- $fields := .RequireFields}}{{range $i, $field := $fields}}

    /**
     * {{$field.Document}}
     */
    private {{$field.Type}} {{$field.Name}};
    {{- end}}

    {{- /* 可选属性 */}}
    {{- $fields := .OptionsFields}}{{range $i, $field := $fields}}

    /**
     * {{$field.Document}}
     */
    private {{$field.Type}} {{$field.Name}} = {{$field.Default}};
    {{- end}}

    /**
     * 请求构造函数
     *
     * @param urlPrefix 请求 scheme + host 【可选】
     *                  若为空则会直接从 HostProvider 中获取
    {{- $fields := .RequireFields}}{{range $i, $field := $fields}}
     * @param {{$field.Name}} {{$field.Document}} 【必须】
    {{- end}}
     */
    public Request(String urlPrefix{{$fields := .RequireFields}}{{range $i, $field := $fields}}, {{$field.Type}} {{$field.Name}}{{- end}}) {
        super(urlPrefix);
        this.setMethod(MethodType.{{.Method}});

        {{- /* 鉴权方式 */}}
        {{- if eq .Authorization  "qiniu"}}
        this.setAuthType(AuthTypeQiniu);
        {{- else if eq .Authorization  ""}}
        {{- end}}

        {{- $fields := .RequireFields}}{{range $i, $field := $fields}}
        this.{{$field.Name}} = {{$field.Name}};
        {{- end}}
    }

    {{- /* 可选属性配置函数 */}}
    {{- $fields := .OptionsFields}}{{range $i, $field := $fields}}

    /**
     * 设置参数【可选】
     *
     * @param {{$field.Name}} {{$field.Document}}
     * @return Request
     */
    public Request {{$field.SetFuncName}}({{$field.Type}} {{$field.Name}}) {
        this.{{$field.Name}} = {{$field.Name}};
        return this;
    }
    {{- end}}

    @Override
    protected void prepareToRequest() throws QiniuException {
        {{- $fields := .RequireFields}}{{if $fields}}{{range $i, $field := $fields}}
        if (this.{{$field.Name}} == null) {
            throw new QiniuException(new NullPointerException("{{$field.Name}} can't empty"));
        }
        {{- end}}
        {{- end}}

        super.prepareToRequest();
    }

    @Override
    protected void buildPath() throws QiniuException {
        {{- /* 原始路径 */}}
        {{- if len .BasicPath }}
        addPathSegment("{{.BasicPath}}");
        {{- end }}

        {{- $fields := .PathFields}}{{if $fields}}
        {{- range $i, $field := $fields}}
        {{- if not $field.IsMap}}

            {{- if $field.KeyOptional}}
            {{- /* Key 可选，如果 Key 对应的 Value 不存在，则此部分省略 */}}
        if (this.{{$field.Name}} != null) {
                {{- if len $field.Key}}
            addPathSegment("{{$field.Key}}");
                {{- end}}
            addPathSegment({{ if $field.Encode }}UrlSafeBase64.encodeToString(this.{{$field.Name}}){{ else }}this.{{$field.Name}}{{end}});
        }
            {{- else}}

                {{- /* Key 有值情况必选，Value 根据实际处理 */}}
                {{- if len $field.Key}}
        addPathSegment("{{$field.Key}}");
                {{- end}}

                {{- /* 看 Value 是否可选 */}}
                {{- if $field.ValueOptional }}
                {{- /* Value 可选，如果 Value 无值则使用默认值，如果默认值也没有，则使用 EncodeDefault*/}}
        {{$field.Type}} value = this.{{$field.Name}};
                    {{- if and ($field.Default) (ne $field.Default "null")}}
        if (value == null) {
            value = {{$field.Default}};
        }
                    {{- end}}
                    {{- if $field.Encode }}
        if (value != null) {
            value = UrlSafeBase64.encodeToString(value);
        }{{- if len $field.EncodeDefault }} else {
            {{- /* 如果有默认值 EncodeDefault 就使用 */}}
            value = "{{$field.EncodeDefault}}";
        }{{- end}}
                    {{- end}}
        if (value != null) {
            addPathSegment(value);
        }
                {{- else}}
                {{- /* Value 必选，值会提前检测，一定会有值 */}}
        addPathSegment({{ if $field.Encode }}UrlSafeBase64.encodeToString(this.{{$field.Name}}){{ else }}this.{{$field.Name}}{{end}});
                {{- end}}
            {{- end}}

        {{- else}}

        for (String key : this.{{$field.Name}}.keySet()) {
            {{- if $field.KeyOptional}}

            {{- /* Key 可选，如果 Key 对应的 Value 不存在，则此部分省略 */}}
            if (this.{{$field.Name}} != null) {
                {{- if len $field.Key}}
                addPathSegment("{{$field.Key}}");
                {{- end}}
                addPathSegment({{ if $field.Encode }}UrlSafeBase64.encodeToString(this.{{$field.Name}}){{ else }}this.{{$field.Name}}{{end}});
            }

            {{- else}}

                {{- /* Key 有值情况必选，Value 根据实际处理 */}}
            addPathSegment({{ if $field.KeyEncode }}UrlSafeBase64.encodeToString(key){{ else }}key{{end}});

                {{- /* 看 Value 是否可选 */}}
                {{- if $field.ValueOptional }}
                {{- /* Value 可选，如果 Value 无值则使用默认值，如果默认值也没有，则使用 EncodeDefault*/}}
            {{$field.ContentValueType}} valueP = this.{{$field.Name}}.get(key);
                    {{- if $field.Encode }}
            if (valueP != null) {
                valueP = UrlSafeBase64.encodeToString(valueP);
            }{{- if len $field.EncodeDefault }} else {
                {{- /* 如果有默认值 EncodeDefault 就使用 */}}
                valueP = "{{$field.EncodeDefault}}";
            }{{- end}}
                    {{- end}}
            if (valueP != null) {
                addPathSegment(valueP);
            }
                {{- else}}
            {{- /* Value 必选，值会提前检测，一定会有值 */}}
            addPathSegment({{ if $field.Encode }}UrlSafeBase64.encodeToString(this.{{$field.Name}}){{ else }}this.{{$field.Name}}{{end}});
                {{- end}}
            {{- end}}
        }

        {{- end}}
        {{- end}}
        {{- end}}

        {{- if len .PathSuffix }}
        addPathSegment("{{.PathSuffix}}");
        {{- end}}

         {{- /* super */}}
        super.buildPath();
    }

    @Override
    protected void buildQuery() throws QiniuException {
        {{- $fields := .QueryFields}}{{if $fields}}
            {{range $i, $field := $fields}}
                {{- if $field.KeyOptional}}
        if (this.{{$field.Name}} != null) {
            addQueryPair("{{$field.Key}}", this.{{$field.Name}});
        }
                {{- else}}
        addQueryPair("{{$field.Key}}", this.{{$field.Name}});
                {{- end}}
            {{- end}}
        {{- end}}

        super.buildQuery();
    }

    @Override
    protected void buildHeader() throws QiniuException {
        {{- $fields := .HeadFields}}{{if $fields}}
            {{range $i, $field := $fields}}
                {{- if $field.KeyOptional}}
        if (this.{{$field.Name}} != null) {
            addHeaderField("{{$field.Key}}", this.{{$field.Name}});
        }
                {{- else}}
        addHeaderField("{{$field.Key}}", this.{{$field.Name}});
                {{- end}}
            {{- end}}
        {{- end}}

        super.buildHeader();
    }

    @Override
    protected void buildBodyInfo() throws QiniuException {
        {{- /* 构建 Binary */}}
        {{- if .IsBodyBinary}}
        this.setBody(data, 0, data.length, null);

        {{- /* 构建表单 */}}
        {{- else if .IsBodyForm }}
        StringMap fields = new StringMap();
            {{- $fields := .BodyFields}}{{if $fields}}
                {{- range $i, $field := $fields}}
                    {{- if $field.KeyOptional}}
        if (this.{{$field.Name}} != null) {
            fields.put("{{$field.Key}}", this.{{$field.Name}});
        }
                    {{- else}}
        fields.put("{{$field.Key}}", this.{{$field.Name}});
                    {{- end}}
                {{- end}}
            {{- end}}
        this.setFormBody(fields);

        {{- /* TODO: 构建 MultiPartFrom */}}
        {{- else if .IsBodyMultiPartFrom }}
         StringMap fields = new StringMap();
             {{- $fields := .BodyFields}}{{if $fields}}
                 {{- range $i, $field := $fields}}
                     {{- if $field.KeyOptional}}
         if (this.{{$field.Name}} != null) {
             fields.put("{{$field.Key}}", this.{{$field.Name}});
         }
                     {{- else}}
         fields.put("{{$field.Key}}", this.{{$field.Name}});
                     {{- end}}
                 {{- end}}
             {{- end}}
         this.setMultipartBody("", "", fields, (byte[])null, null);

        {{- /* 构建 Json */}}
        {{- else if .IsBodyJson }}
        {{- $jsonBodyField := .JsonBodyField }}
        byte[] body = Json.encode(this.{{$jsonBodyField.Name}}).getBytes(Constants.UTF_8);
        this.setBody(body, 0, body.length, Client.JsonMime);
        {{- end}}

        super.buildBodyInfo();
    }
    {{/* 创建 class */}}
    {{- $classList := .Classes}}{{if $classList}}
    {{- range $i, $classInfo := $classList}}
    {{- $classCode := generateClassCode $classInfo }}{{ addIndentation $classCode }}
    {{- end}}
    {{- end}}
}
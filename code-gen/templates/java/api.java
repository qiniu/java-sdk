package {{ .PackageName }};
{{ if .HasOtherClasses}}
import com.google.gson.annotations.SerializedName;
{{- end}}
{{- if .IsJsonRequestBody }}
import com.qiniu.common.Constants;
{{- end}}
import com.qiniu.common.QiniuException;
import com.qiniu.http.Client;
import com.qiniu.http.MethodType;
{{- if .UseEncode}}
import com.qiniu.util.UrlSafeBase64;
{{- end}}
import com.qiniu.storage.Api;
{{- if or .IsJsonRequestBody .IsJsonResponseBody }}
import com.qiniu.util.Json;
{{- end}}
{{- if .IsFromRequestBody }}
import com.qiniu.util.StringMap;
{{- end}}
{{ if .HasMapFields }}
import java.util.HashMap;
import java.util.Map;
{{- end}}
{{ .Document }}
public class {{ .ClassName }} extends Api {

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     */
    public {{ .ClassName }}(Client client) {
        super(client);
    }

    /**
     * api 构建函数
     *
     * @param client 请求 Client
     * @param config 请求流程的配置信息
     **/
    public {{ .ClassName }}(Client client, Config config) {
        super(client, config);
    }

    /**
     * 发起请求
     *
     * @param request 请求对象【必须】
     * @return 响应对象
     * @throws QiniuException 请求异常
     */
    public Response request(Request request) throws QiniuException {
        return new Response(requestWithInterceptor(request));
    }

{{ .AddIndentation .RequestCode }}

{{ .AddIndentation .ResponseCode }}
}

package com.xyb.xfxh.interceptor;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.xyb.xfxh.common.Constants;
import com.xyb.xfxh.session.Configuration;
import lombok.SneakyThrows;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;




import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**

 * @author 云深不知处
 * @description 自定义拦截器
 */
public class OpenAiInterceptor implements Interceptor {

    /** OpenAi apiKey 需要在官网申请 */
    private String apiKeyBySystem;
    /** 访问授权接口的认证 Token */


    private Configuration configuration;


    public OpenAiInterceptor(String apiKeyBySystem, Configuration configuration) {
        this.apiKeyBySystem = apiKeyBySystem;
        this.configuration = configuration;
    }

    @SneakyThrows
    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        // 获得请求信息
        Request original = chain.request();
        // 拿到请求头中用户传递的Key
        String apiKeyByUser = original.header("apiKey");
        String apiKey = Constants.NULL.equals(apiKeyByUser) ? apiKeyBySystem : apiKeyByUser;


        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // 拼接
        HttpUrl url = original.url();
        URL url1 = new URL(url.toString());


        String preStr = "host: " + original.url().host() + "\n" +
                "date: " + date + "\n" +
                "GET " + url1.getPath() + " HTTP/1.1";
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(configuration.getApiSecret().getBytes(StandardCharsets.UTF_8), "hmacsha256");
        System.out.println(configuration.getApiSecret());
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // 拼接
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", configuration.getApiKey(), "hmac-sha256", "host date request-line", sha);
        String encodeToString = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        System.out.println(encodeToString);
        // 构建request
        Request request = new Request.Builder()
                .url(original.url())
                .header(Header.AUTHORIZATION.getValue(), "authorization " + encodeToString)
                .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                .method(original.method(), original.body())
                .build();

        // 返回执行结果
        return chain.proceed(request);
    }

}
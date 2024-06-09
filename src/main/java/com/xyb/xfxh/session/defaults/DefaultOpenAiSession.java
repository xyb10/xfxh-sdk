package com.xyb.xfxh.session.defaults;

import cn.hutool.http.ContentType;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyb.xfxh.IOpenAiApi;
import com.xyb.xfxh.dto.RequestDTO;
import com.xyb.xfxh.session.Configuration;
import com.xyb.xfxh.session.OpenAiSession;
import com.xyb.xfxh.util.AuthUtil;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author 云深不知处
 */
public class DefaultOpenAiSession implements OpenAiSession {

    /** 配置信息 */
    private final Configuration configuration;

    private final EventSource.Factory factory;


    private final IOpenAiApi openAiApi;

    private static final String V = "v3.1/chat";

    public DefaultOpenAiSession(Configuration configuration) {
        this.configuration = configuration;
        this.openAiApi = configuration.getOpenAiApi();
        this.factory = configuration.createRequestFactory();
    }

    @Override
    public WebSocket completions(RequestDTO chatCompletionRequest,  WebSocketListener listener) throws Exception {
        return this.completions(null, null, chatCompletionRequest,  listener);
    }

    @Override
    public WebSocket completions(String apiHostByUser, String apiKeyByUser, RequestDTO chatCompletionRequest,  WebSocketListener listener) throws Exception {
        // 动态设置 Host、Key，便于用户传递自己的信息
        String apiHost = apiHostByUser == null ? configuration.getApiHost() : apiHostByUser;
        String apiKey = apiKeyByUser == null ? configuration.getApiKey() : apiKeyByUser;
        // 构建请求信息
        String key = AuthUtil.getKey(apiKey, configuration);
        Request request = new Request.Builder()
                // 这里的url需注意，需要提前处理好key，具体请前往讯飞开发平台查看开发文档
                // 参考格式：wss://spark-api.xf-yun.com/v1.1/chat?authorization=YXBpX2tleT0iYWRkZDIyNzJiNmQ4YjdjOGFiZGQ3OTUzMTQyMGNhM2IiLCBhbGdvcml0aG09ImhtYWMtc2hhMjU2IiwgaGVhZGVycz0iaG9zdCBkYXRlIHJlcXVlc3QtbGluZSIsIHNpZ25hdHVyZT0iejVnSGR1M3B4VlY0QURNeWs0Njd3T1dEUTlxNkJRelIzbmZNVGpjL0RhUT0i&date=Fri%2C+05+May+2023+10%3A43%3A39+GMT&host=spark-api.xf-yun.com
                .url(key)
                .build();
        // 建立 wss 连接
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        // 发送请求
        webSocket.send(JSONObject.toJSONString(chatCompletionRequest));

        // 返回结果信息
        return webSocket;
    }
}

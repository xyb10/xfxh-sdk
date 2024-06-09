此项目封装了讯飞星火大模型，方便开发者接入使用，基于springboot工程，开箱即用
注意：首先去讯飞开发平台官网注册https://www.xfyun.cn/，拿到appId、key、secret，并且需要使用加密算法以得到token,同时使用的是websocket协议，不过此sdk已全部处理好，只管拿去使用即可
下面是测试示例
```
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
```

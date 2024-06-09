package com.xyb.xfxh.session;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import com.xyb.xfxh.IOpenAiApi;
import lombok.*;

import okhttp3.OkHttpClient;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;



/**

 * @author 云深不知处
 * @description 配置信息
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {

    @Getter
    @Setter
    private IOpenAiApi openAiApi;

    @Getter
    @Setter
    private OkHttpClient okHttpClient;

    @Getter
    @NotNull
    private String appId;

    @Getter
    @NotNull
    private String apiKey;

    @Getter
    private String apiHost;

    @Getter
//    @NotNull
    private String apiSecret;

    public EventSource.Factory createRequestFactory() {
        return EventSources.createFactory(okHttpClient);
    }

}

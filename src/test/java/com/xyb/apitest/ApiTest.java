package com.xyb.apitest;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.xyb.xfxh.dto.MsgDTO;
import com.xyb.xfxh.dto.RequestDTO;
import com.xyb.xfxh.dto.ResponseDTO;
import com.xyb.xfxh.session.Configuration;
import com.xyb.xfxh.session.OpenAiSession;
import com.xyb.xfxh.session.OpenAiSessionFactory;
import com.xyb.xfxh.session.defaults.DefaultOpenAiSessionFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;


@Slf4j
public class ApiTest {
    private OpenAiSession openAiSession;

    private StringBuilder answer = new StringBuilder();

    @Before
    public void test_OpenAiSessionFactory() {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setAppId("appId");
        configuration.setApiHost("https://spark-api.xf-yun.com/");
        configuration.setApiKey("key");
        configuration.setApiSecret("secret");
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
    }


    /**
     * 【常用对话模式，推荐使用此模型进行测试】
     * 此对话模型 V3.0 接近于官网体验 & 流式应答
     */
    @Test
    public void test_chat_completions_stream_channel() throws Exception {
        RequestDTO chatCompletion = RequestDTO
                .builder()
                .header(RequestDTO.HeaderDTO.builder().appId("").uid("111").build())
                .parameter(RequestDTO.ParameterDTO.builder().chat(RequestDTO.ParameterDTO.ChatDTO.builder().domain("generalv3").maxTokens(2048).temperature(0.5F).build()).build())
                .payload(RequestDTO.PayloadDTO.builder().message(RequestDTO.PayloadDTO.MessageDTO.builder().text(Collections.singletonList(MsgDTO.builder().role("user").content("你是谁").index(1).build())).build()).build()).build();


        // 3. 发起请求
        WebSocket webSocket = openAiSession.completions(chatCompletion, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                log.info("连接成功");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                // 将大模型回复的 JSON 文本转为 ResponseDTO 对象
                ResponseDTO responseData = JSONObject.parseObject(text, ResponseDTO.class);
                // 如果响应数据中的 header 的 code 值不为 0，则表示响应错误
                if (responseData.getHeader().getCode() != 0) {
                    // 日志记录
                    log.error("发生错误，错误码为：" + responseData.getHeader().getCode() + "; " + "信息：" + responseData.getHeader().getMessage());
                    return;
                }
                // 将回答进行拼接
                for (MsgDTO msgDTO : responseData.getPayload().getChoices().getText()) {
//                    apiTest.answer.append(msgDTO.getContent());
                    log.info("text:"+msgDTO.getContent());

                }

               /* // 对最后一个文本结果进行处理
                if (2 == responseData.getHeader().getStatus()) {
                    wsCloseFlag = true;
                }*/

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                log.error("error:"+response.message());
            }
        });
        // 等待
        new CountDownLatch(1).await();
    }
}

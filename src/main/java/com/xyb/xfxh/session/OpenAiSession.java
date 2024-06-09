package com.xyb.xfxh.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xyb.xfxh.dto.RequestDTO;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface OpenAiSession {
    /**
     * 星火认知大模型
     * @param requestDTO
     * @param
     * @return
     */
    WebSocket completions(RequestDTO requestDTO,  WebSocketListener listener) throws Exception;


    /**
     * 星火认知大模型， 用自己的数据
     * @param requestDTO
     * @param
     * @return
     */
    WebSocket completions(String apiHost, String apiKey, RequestDTO requestDTO,  WebSocketListener listener) throws Exception;

}

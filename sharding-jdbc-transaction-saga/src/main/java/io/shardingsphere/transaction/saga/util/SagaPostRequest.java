package io.shardingsphere.transaction.saga.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Slf4j
public class SagaPostRequest {
    
    private static final SagaPostRequest INSTANCE = new SagaPostRequest();
    
    private final String ip = "127.0.0.1";
    
    private final int port = 8083;
    
    private OkHttpClient okHttpClient;
    
    public void SagaPostRequest() {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.retryOnConnectionFailure(true);
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            builder.proxy(proxy);
            okHttpClient = builder.build();
        } catch (final Exception ex) {
            log.error("SagaPostRequest() Error: " + ex);
        }
    }
    
    public static SagaPostRequest getInstance() {
        return INSTANCE;
    }
    
    private String request(String body) {
        String url = "127.0.0.1:8083/requests/";
        String result = "";
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.header("Connection", "close");
        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
        Request request = builder.post(RequestBody.create(mediaType, body)).build();
        log.info("Post: " + url);
        try (Response response = getOkHttpClient().newCall(request).execute()) {
            byte[] contentByte = response.body().bytes();
            result = new String(contentByte);
        } catch (final Exception ex) {
            log.error("Post: error: ", ex);
        }
        return result;
    }
}

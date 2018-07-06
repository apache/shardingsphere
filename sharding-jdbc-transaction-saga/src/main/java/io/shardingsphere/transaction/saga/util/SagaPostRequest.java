package io.shardingsphere.transaction.saga.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@Getter
@Setter
public class SagaPostRequest {
    private static Logger logger = LoggerFactory.getLogger(SagaPostRequest.class);
    private static SagaPostRequest instance = new SagaPostRequest();
    private String ip;
    private int port;
    private String username;
    private String password;
    private OkHttpClient okHttpClient;
    
    public static SagaPostRequest getInstance() {
        return instance;
    }
    
    public void initOkHttpClient() {
        // 初始化OkHttpClient, 一个实例的请求共享连接池
        Proxy proxy = null;
        if (ip != null && port != 0) {
            InetSocketAddress addr = new InetSocketAddress(ip, port);
            proxy = new Proxy(Proxy.Type.HTTP, addr);
        }
        
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.retryOnConnectionFailure(true);
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.writeTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);
            if (proxy != null) {
                builder.proxy(proxy);
            }
            okHttpClient = builder.build();
        } catch (Exception e) {
            logger.error("initOkHttpClient() Error : " + e);
        }
    }
    
    private Map<String, Map<String, String>> request(String url, Map<String, String> header, String body, String bodyCharset) throws IOException {
        Map<String, Map<String, String>> result = new HashMap<>();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.header("Connection", "close");
        Response response = null;
        MediaType mediaType;
        
        // 添加headers
        for (Map.Entry<String, String> hd : header.entrySet()) {
            builder.addHeader(hd.getKey(), hd.getValue());
        }
        
        if (StringUtils.isEmpty(body)) {
            // 没有body get请求
            Request request = builder.build();
            try {
                logger.info("Get: " + url);
                response = getOkHttpClient().newCall(request).execute();
            } catch (Exception e) {
                logger.error("Get: error: ", e);
                if (response != null)
                    response.close();
                throw new IOException();
            }
        } else {
            // 有请求body体 post请求
            String code = bodyCharset;
            if (StringUtils.isBlank(code)) {
                code = "utf-8";
            }
            
            JSONObject json = null;
            try {
                json = JSON.parseObject(body);
            } catch (Exception e) {
                // logger.error("JSON.parseObject(body) error!");
            }
            
            if (json != null) {
                mediaType = MediaType.parse("application/json; charset=" + code);
            } else {
                mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=" + code);
            }
            
            Request request = builder.post(RequestBody.create(mediaType, body)).build();
            try {
                logger.info("Post: " + url);
                response = getOkHttpClient().newCall(request).execute();
            } catch (Exception e) {
                logger.error("Post: error: ", e);
                if (response != null)
                    response.close();
                throw new IOException();
            }
        }
        
        if (response != null) {
            if (!(response.code() >= 200 && response.code() < 300)) {
                logger.error("HTTP Status Code: " + response.code());
                response.close();
                throw new IOException();
            }
            
            byte[] contentByte = response.body().bytes();
            String contentString = new String(contentByte);
            
            // 添加content
            Map<String, String> content = new HashMap<>();
            content.put("content", contentString);
            result.put("content", content);
            // 添加headers
            Map<String, String> hdrs = new HashMap<>();
            Headers resHeaders = response.headers();
            Set<String> keys = resHeaders.names();
            for (String key : keys) {
                for (String value : resHeaders.values(key)) {
                    hdrs.put(key, value);
                }
            }
            result.put("headers", hdrs);
        }
        return result;
    }
}

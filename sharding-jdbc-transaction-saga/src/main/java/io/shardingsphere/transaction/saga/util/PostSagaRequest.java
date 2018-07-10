/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.saga.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * Post saga request.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
@Slf4j
public final class PostSagaRequest {
    
    private static final PostSagaRequest INSTANCE = new PostSagaRequest();
    
    private final String ip = "127.0.0.1";
    
    private final int port = 8083;
    
    private OkHttpClient okHttpClient;
    
    private PostSagaRequest() {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(true);
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.proxy(proxy);
        okHttpClient = builder.build();
    }
    
    /**
     * Get post saga request instance.
     *
     * @return post saga request instance
     */
    public static PostSagaRequest getInstance() {
        return INSTANCE;
    }
    
    private String request(final String body) {
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
        } catch (final IOException ex) {
            log.error("Post: error: ", ex);
        }
        return result;
    }
}

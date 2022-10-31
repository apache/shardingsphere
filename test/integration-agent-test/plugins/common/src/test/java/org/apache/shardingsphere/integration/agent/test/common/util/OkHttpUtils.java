/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.integration.agent.test.common.util;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

/**
 * Ok http utils.
 */
public final class OkHttpUtils {
    
    private static final OkHttpUtils OK_HTTP_UTILS = new OkHttpUtils();
    
    private static final Gson GSON = new Gson();
    
    private final OkHttpClient client;
    
    private OkHttpUtils() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
        client = builder.build();
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static OkHttpUtils getInstance() {
        return OK_HTTP_UTILS;
    }
    
    /**
     * Get response json and transform to class bean.
     *
     * @param <T> type parameter
     * @param url url
     * @param clazz clazz
     * @return type parameter class bean
     * @throws IOException the IOException
     */
    public <T> T get(final String url, final Class<T> clazz) throws IOException {
        return GSON.fromJson(get(url), clazz);
    }
    
    /**
     * Get response json.
     *
     * @param url url
     * @return response json
     * @throws IOException the IOException
     */
    public String get(final String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        assertNotNull(response.body());
        return response.body().string();
    }
}

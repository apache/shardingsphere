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

package org.apache.shardingsphere.integration.scaling.test.mysql.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.scaling.web.entity.ResponseContent;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

/**
 * Ok http utils.
 */
public final class ScalingUtil {
    
    private static final ScalingUtil OK_HTTP_UTILS = new ScalingUtil();
    
    private static final Gson GSON = new Gson();
    
    private final OkHttpClient client;
    
    private final String scalingUrl;
    
    private ScalingUtil() {
        scalingUrl = IntegrationTestEnvironment.getInstance().getEngineEnvProps().getProperty("scaling.url");
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
    public static ScalingUtil getInstance() {
        return OK_HTTP_UTILS;
    }
    
    private <T> T get(final String url, final Type type) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        assertNotNull(response.body());
        String result = response.body().string();
        return GSON.fromJson(result, type);
    }
    
    private <T> T post(final String url, final String body, final Type type) throws IOException {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = client.newCall(request).execute();
        assertNotNull(response.body());
        String result = response.body().string();
        return GSON.fromJson(result, type);
    }
    
    /**
     * Start job.
     *
     * @param configuration configuration
     * @return response
     * @throws IOException io exception
     */
    public ResponseContent<String> startJob(final String configuration) throws IOException {
        return getInstance().post(scalingUrl + "/scaling/job/start", configuration, new TypeToken<ResponseContent<String>>() {
        
        }.getType());
    }
    
    /**
     * Get job list.
     *
     * @return job list
     * @throws IOException io exception
     */
    public ResponseContent<String> getJobList() throws IOException {
        return getInstance().get(scalingUrl + "/scaling/job/list", new TypeToken<ResponseContent<Object[]>>() {
        
        }.getType());
    }
}

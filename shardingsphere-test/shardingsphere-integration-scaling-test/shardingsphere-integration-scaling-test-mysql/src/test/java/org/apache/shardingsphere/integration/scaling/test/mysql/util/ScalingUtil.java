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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import groovy.lang.Tuple2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.IntegrationTestEnvironment;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

/**
 * Ok http utils.
 */
public final class ScalingUtil {
    
    private static final ScalingUtil OK_HTTP_UTILS = new ScalingUtil();
    
    private static final JsonParser JSON_PARSER = new JsonParser();
    
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
    
    private JsonElement get(final String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        assertNotNull(response.body());
        String result = response.body().string();
        return JSON_PARSER.parse(result);
    }
    
    private JsonElement post(final String url, final String body) throws IOException {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response = client.newCall(request).execute();
        assertNotNull(response.body());
        String result = response.body().string();
        return JSON_PARSER.parse(result);
    }
    
    /**
     * Start job.
     *
     * @param configuration configuration
     * @return result
     * @throws IOException io exception
     */
    public Tuple2<Boolean, String> startJob(final String configuration) throws IOException {
        JsonObject response = getInstance().post(scalingUrl + "/scaling/job/start", configuration).getAsJsonObject();
        return new Tuple2<>(response.get("success").getAsBoolean(), response.get("model").getAsString());
    }
    
    /**
     * Get job status.
     *
     * @param jobId job id
     * @return job status
     */
    public String getJobStatus(final String jobId) {
        try {
            JsonElement response = getInstance().get(scalingUrl + "/scaling/job/progress/" + jobId);
            return response.getAsJsonObject().getAsJsonObject("model").getAsJsonObject("0").get("status").getAsString();
            //CHECKSTYLE:OFF
        } catch (Exception ignored) {
            //CHECKSTYLE:ON
        }
        return null;
    }
    
    /**
     * Check job.
     *
     * @param jobId job id
     * @return check result
     * @throws IOException io exception
     */
    public Map<String, Tuple2<Boolean, Boolean>> getJobCheckResult(final String jobId) throws IOException {
        JsonElement response = getInstance().get(scalingUrl + "/scaling/job/check/" + jobId);
        return response.getAsJsonObject().getAsJsonObject("model").getAsJsonObject().entrySet().stream().collect(
                Collectors.toMap(entry -> entry.getKey(), entry -> createTaskResult(entry)));
    }
    
    private Tuple2<Boolean, Boolean> createTaskResult(final Map.Entry<String, JsonElement> entry) {
        return new Tuple2<>(entry.getValue().getAsJsonObject().get("countValid").getAsBoolean(),
                entry.getValue().getAsJsonObject().get("dataValid").getAsBoolean());
    }
    
    /**
     * Get job list.
     *
     * @return result
     * @throws IOException io exception
     */
    public JsonElement getJobList() throws IOException {
        return getInstance().get(scalingUrl + "/scaling/job/list");
    }
}

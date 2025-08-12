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

package org.apache.shardingsphere.test.e2e.agent.engine.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Agent E2E HTTP utility class.
 */
public final class AgentE2EHttpUtils {
    
    private static final AgentE2EHttpUtils OK_HTTP_UTILS = new AgentE2EHttpUtils();
    
    private final OkHttpClient client;
    
    private AgentE2EHttpUtils() {
        client = new OkHttpClient.Builder().connectTimeout(10L, TimeUnit.SECONDS).readTimeout(10L, TimeUnit.SECONDS).writeTimeout(10L, TimeUnit.SECONDS).build();
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static AgentE2EHttpUtils getInstance() {
        return OK_HTTP_UTILS;
    }
    
    /**
     * Query response.
     *
     * @param url query URL
     * @return response
     * @throws IOException IO exception
     */
    public String query(final String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            assertNotNull(response.body());
            return response.body().string();
        }
    }
}

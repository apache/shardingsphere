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

package org.apache.shardingsphere.test.e2e.mcp.runtime.programmatic;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("isEnabled")
class HttpTransportSecurityE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final String ACCESS_TOKEN = "test-access-token";
    
    private static final String ALLOWED_ORIGIN = "https://gateway.example.test";
    
    private static final String AUTHORIZATION_SERVER = "https://auth.example.test";
    
    private boolean remoteBinding;
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Override
    protected HttpTransportConfiguration createHttpTransportConfiguration(final boolean enabled) {
        return remoteBinding
                ? new HttpTransportConfiguration(enabled, "0.0.0.0", true, ACCESS_TOKEN, 0, getHttpEndpointPath(), List.of(ALLOWED_ORIGIN), List.of(AUTHORIZATION_SERVER), List.of("mcp.read"), "",
                        new OAuthIntrospectionConfiguration())
                : super.createHttpTransportConfiguration(enabled);
    }
    
    @Test
    void assertAcceptInitializeWithIpv6LoopbackOrigin() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Origin", "http://[::1]:8080"), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectInitializeWithInvalidOrigin() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Origin", "https://evil.example.com"), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(403));
    }
    
    @Test
    void assertAcceptInitializeWithAllowedRemoteOrigin() throws IOException, InterruptedException {
        launchRemoteHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createRemoteHeaders(ALLOWED_ORIGIN), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectInitializeWithUnlistedRemoteOrigin() throws IOException, InterruptedException {
        launchRemoteHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createRemoteHeaders("https://evil.example.test"), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(403));
    }
    
    @Test
    void assertRejectInitializeWithoutRemoteOrigin() throws IOException, InterruptedException {
        launchRemoteHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createRemoteHeaders(""), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(403));
    }
    
    @Test
    void assertRejectInitializeWithMalformedRemoteOrigin() throws IOException, InterruptedException {
        launchRemoteHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createRemoteHeaders("://bad-origin"), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(403));
    }
    
    @Test
    void assertRejectInitializeWithLoopbackOriginForRemoteBinding() throws IOException, InterruptedException {
        launchRemoteHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, createRemoteHeaders("http://127.0.0.1:8080"), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(403));
    }
    
    private void launchRemoteHttpTransport() throws IOException {
        remoteBinding = true;
        launchHttpTransport();
    }
    
    private Map<String, String> createRemoteHeaders(final String origin) {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("Authorization", createAuthorizationHeaderValue(ACCESS_TOKEN));
        if (!origin.isEmpty()) {
            result.put("Origin", origin);
        }
        return result;
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        return new LinkedHashMap<>(MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-security"));
    }
}

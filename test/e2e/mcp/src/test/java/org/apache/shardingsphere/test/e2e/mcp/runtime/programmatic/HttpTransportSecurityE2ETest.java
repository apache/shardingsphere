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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HttpTransportSecurityE2ETest extends AbstractHttpProtocolOnlyE2ETest {
    
    private boolean remoteBinding;
    
    @Override
    protected HttpTransportConfiguration createHttpTransportConfiguration() {
        return remoteBinding ? new HttpTransportConfiguration("0.0.0.0", 0, getHttpEndpointPath()) : super.createHttpTransportConfiguration();
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
    void assertAcceptInitializeWithoutOriginForRemoteBinding() throws IOException, InterruptedException {
        launchRemoteHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of(), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectInitializeWithPresentRemoteOrigin() throws IOException, InterruptedException {
        launchRemoteHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Origin", "https://gateway.example.test"), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(403));
    }
    
    private void launchRemoteHttpTransport() throws IOException {
        remoteBinding = true;
        launchHttpTransport();
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        return new LinkedHashMap<>(MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-security"));
    }
}

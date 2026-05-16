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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("isEnabled")
class HttpTransportOAuthIntrospectionE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final String CLIENT_ID = "foo_client";
    
    private static final String CLIENT_SECRET = "foo_secret";
    
    private static final String AUTHORIZATION_SERVER = "https://auth.example.test";
    
    private static final String PROTECTED_RESOURCE = "https://gateway.example.test/mcp";
    
    private final Map<String, Map<String, Object>> introspectionResponses = new ConcurrentHashMap<>(3, 1F);
    
    private HttpServer introspectionServer;
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @AfterEach
    void stopIntrospectionServer() {
        if (null != introspectionServer) {
            introspectionServer.stop(0);
            introspectionServer = null;
        }
    }
    
    @Override
    protected HttpTransportConfiguration createHttpTransportConfiguration(final boolean enabled) {
        return new HttpTransportConfiguration(enabled, "127.0.0.1", false, "", 0, getHttpEndpointPath(), Collections.emptyList(), List.of(AUTHORIZATION_SERVER), List.of("mcp.read"),
                PROTECTED_RESOURCE, new OAuthIntrospectionConfiguration(getIntrospectionEndpoint(), CLIENT_ID, CLIENT_SECRET, AUTHORIZATION_SERVER, 30000L));
    }
    
    @Test
    void assertAcceptInitializeWithOAuthToken() throws IOException, InterruptedException {
        startIntrospectionServer();
        introspectionResponses.put("valid-token", createIntrospectionResponse(PROTECTED_RESOURCE, "mcp.read"));
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Authorization", createAuthorizationHeaderValue("valid-token")), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectInitializeWithUnexpectedResourceToken() throws IOException, InterruptedException {
        startIntrospectionServer();
        introspectionResponses.put("wrong-resource-token", createIntrospectionResponse("https://gateway.example.test/other", "mcp.read"));
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Authorization", createAuthorizationHeaderValue("wrong-resource-token")),
                createInitializeRequestParams());
        assertThat(actual.statusCode(), is(401));
        assertThat(actual.headers().firstValue("WWW-Authenticate").orElse(""), containsString("error=\"invalid_token\""));
    }
    
    @Test
    void assertRejectFollowUpRequestWithInsufficientScope() throws IOException, InterruptedException {
        startIntrospectionServer();
        introspectionResponses.put("valid-token", createIntrospectionResponse(PROTECTED_RESOURCE, "mcp.read"));
        introspectionResponses.put("missing-scope-token", createIntrospectionResponse(PROTECTED_RESOURCE, "mcp.write"));
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSessionWithOAuthToken(httpClient, "valid-token");
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, createAuthorizedSessionHeaders(sessionId, "missing-scope-token"));
        assertThat(actual.statusCode(), is(403));
        assertThat(actual.headers().firstValue("WWW-Authenticate").orElse(""), containsString("error=\"insufficient_scope\""));
    }
    
    private void startIntrospectionServer() throws IOException {
        introspectionServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        introspectionServer.createContext("/introspect", this::handleIntrospectionRequest);
        introspectionServer.start();
    }
    
    private void handleIntrospectionRequest(final HttpExchange exchange) throws IOException {
        try {
            if (!"POST".equals(exchange.getRequestMethod()) || !getExpectedBasicAuthorization().equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                exchange.sendResponseHeaders(401, -1L);
                return;
            }
            String token = getFormValue(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), "token");
            Map<String, Object> response = introspectionResponses.getOrDefault(token, Map.of("active", false));
            byte[] responseBody = JsonUtils.toJsonString(response).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
        } finally {
            exchange.close();
        }
    }
    
    private String getFormValue(final String requestBody, final String name) {
        for (String each : requestBody.split("&")) {
            int separatorIndex = each.indexOf('=');
            if (separatorIndex > 0 && name.equals(URLDecoder.decode(each.substring(0, separatorIndex), StandardCharsets.UTF_8))) {
                return URLDecoder.decode(each.substring(separatorIndex + 1), StandardCharsets.UTF_8);
            }
        }
        return "";
    }
    
    private String getExpectedBasicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));
    }
    
    private String getIntrospectionEndpoint() {
        return null == introspectionServer ? "http://127.0.0.1:0/introspect" : "http://127.0.0.1:" + introspectionServer.getAddress().getPort() + "/introspect";
    }
    
    private Map<String, Object> createIntrospectionResponse(final String resource, final String scope) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("active", true);
        result.put("iss", AUTHORIZATION_SERVER);
        result.put("aud", List.of(resource));
        result.put("exp", 4102444800L);
        result.put("nbf", 1L);
        result.put("scope", scope);
        return result;
    }
    
    private String initializeSessionWithOAuthToken(final HttpClient httpClient, final String accessToken) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Authorization", createAuthorizationHeaderValue(accessToken)), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    private Map<String, String> createAuthorizedSessionHeaders(final String sessionId, final String accessToken) {
        Map<String, String> result = new LinkedHashMap<>(createSessionHeaders(sessionId));
        result.put("Authorization", createAuthorizationHeaderValue(accessToken));
        return result;
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        return new LinkedHashMap<>(MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-oauth"));
    }
}

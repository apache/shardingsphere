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
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("isEnabled")
class HttpTransportAccessTokenE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final String ACCESS_TOKEN = "test-access-token";
    
    private static final String AUTHORIZATION_SERVER = "https://auth.example.test";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Override
    protected HttpTransportConfiguration createHttpTransportConfiguration(final boolean enabled) {
        return new HttpTransportConfiguration(enabled, "127.0.0.1", false, ACCESS_TOKEN, 0, getHttpEndpointPath(), List.of(AUTHORIZATION_SERVER), List.of("mcp.read"), "");
    }
    
    @Test
    void assertAcceptInitializeWithAccessToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Authorization", createAuthorizationHeaderValue(ACCESS_TOKEN)), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        assertFalse(actual.headers().firstValue("MCP-Session-Id").orElse("").isEmpty());
    }
    
    @Test
    void assertRejectInitializeWithoutAccessToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient);
        assertThat(actual.statusCode(), is(401));
        assertProtectedResourceMetadataChallenge(actual);
    }
    
    @Test
    void assertRejectInitializeWithWrongAccessToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Authorization", createAuthorizationHeaderValue("wrong-token")), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(401));
        assertProtectedResourceMetadataChallenge(actual);
    }
    
    @Test
    void assertRejectInitializeWithQueryAccessToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequestWithQueryAccessToken(httpClient);
        assertThat(actual.statusCode(), is(401));
        assertProtectedResourceMetadataChallenge(actual);
    }
    
    @Test
    void assertReadProtectedResourceMetadata() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = httpClient.send(HttpRequest.newBuilder(getProtectedResourceMetadataUri()).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualBody = parseJsonBody(actual.body());
        assertThat(actualBody.get("resource"), is(getEndpointUri().toString()));
        assertThat(actualBody.get("authorization_servers"), is(List.of(AUTHORIZATION_SERVER)));
        assertThat(actualBody.get("scopes_supported"), is(List.of("mcp.read")));
        assertThat(actualBody.get("bearer_methods_supported"), is(List.of("header")));
    }
    
    @Test
    void assertRejectFollowUpRequestWithoutAccessTokenBeforeSessionValidation() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        initializeSessionWithAccessToken(httpClient, ACCESS_TOKEN);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, Map.of("MCP-Session-Id", "missing-session", "MCP-Protocol-Version", getProtocolVersion()));
        assertThat(actual.statusCode(), is(401));
        assertProtectedResourceMetadataChallenge(actual);
    }
    
    @Test
    void assertRejectFollowUpRequestWithWrongAccessToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSessionWithAccessToken(httpClient, ACCESS_TOKEN);
        Map<String, String> headers = createAuthorizedSessionHeaders(sessionId, "wrong-token");
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, headers);
        assertThat(actual.statusCode(), is(401));
        assertProtectedResourceMetadataChallenge(actual);
    }
    
    @Test
    void assertCloseSessionWithAccessToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSessionWithAccessToken(httpClient, ACCESS_TOKEN);
        HttpResponse<String> actual = sendDeleteRequest(httpClient, createAuthorizedSessionHeaders(sessionId, ACCESS_TOKEN));
        assertThat(actual.statusCode(), is(200));
    }
    
    private String initializeSessionWithAccessToken(final HttpClient httpClient, final String accessToken) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient, Map.of("Authorization", createAuthorizationHeaderValue(accessToken)), createInitializeRequestParams());
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    private HttpResponse<String> sendInitializeRequestWithQueryAccessToken(final HttpClient httpClient) throws IOException, InterruptedException {
        URI endpointUri = URI.create(getEndpointUri() + "?access_token=" + ACCESS_TOKEN);
        HttpRequest request = MCPHttpTransportTestSupport.createJsonRequestBuilder(endpointUri)
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody("init-query-token", "initialize", createInitializeRequestParams())))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    private Map<String, String> createAuthorizedSessionHeaders(final String sessionId, final String accessToken) {
        Map<String, String> result = new LinkedHashMap<>(createSessionHeaders(sessionId));
        result.put("Authorization", createAuthorizationHeaderValue(accessToken));
        return result;
    }
    
    private void assertProtectedResourceMetadataChallenge(final HttpResponse<String> actual) throws IOException {
        assertThat(actual.headers().firstValue("WWW-Authenticate").orElse(""), containsString("resource_metadata=\"" + getProtectedResourceMetadataUri() + "\""));
    }
    
    private URI getProtectedResourceMetadataUri() throws IOException {
        return getEndpointUri().resolve("/.well-known/oauth-protected-resource" + getHttpEndpointPath());
    }
    
    private Map<String, Object> createInitializeRequestParams() {
        return new LinkedHashMap<>(MCPHttpTransportTestSupport.createInitializeRequestParams("mcp-e2e-auth"));
    }
}

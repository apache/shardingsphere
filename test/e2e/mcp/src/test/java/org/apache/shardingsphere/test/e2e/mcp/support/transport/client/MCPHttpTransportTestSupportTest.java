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

package org.apache.shardingsphere.test.e2e.mcp.support.transport.client;

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPHttpTransportTestSupportTest {
    
    @Test
    void assertCreateJsonRequestBuilder() {
        HttpRequest actual = MCPHttpTransportTestSupport.createJsonRequestBuilder(URI.create("http://127.0.0.1:8080/mcp")).build();
        assertThat(actual.uri(), is(URI.create("http://127.0.0.1:8080/mcp")));
        assertThat(actual.headers().firstValue("Content-Type").orElse(""), is("application/json"));
        assertThat(actual.headers().firstValue("Accept").orElse(""), is("application/json, text/event-stream"));
    }
    
    @Test
    void assertCreateSessionRequestBuilder() {
        HttpRequest actual = MCPHttpTransportTestSupport.createSessionRequestBuilder(URI.create("http://127.0.0.1:8080/mcp"), "session", "protocol").build();
        assertThat(actual.headers().firstValue("MCP-Session-Id").orElse(""), is("session"));
        assertThat(actual.headers().firstValue("MCP-Protocol-Version").orElse(""), is("protocol"));
    }
    
    @Test
    void assertCreateSessionHeaders() {
        assertThat(MCPHttpTransportTestSupport.createSessionHeaders("session", "protocol"), is(Map.of("MCP-Session-Id", "session", "MCP-Protocol-Version", "protocol")));
    }
    
    @Test
    void assertSendDeleteRequest() throws IOException, InterruptedException {
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse<String> expected = mockHttpResponse();
        HttpClient httpClient = mockHttpClient(expected, requestCaptor);
        HttpResponse<String> actual = MCPHttpTransportTestSupport.sendDeleteRequest(httpClient, URI.create("http://127.0.0.1:8080/mcp"), Map.of("MCP-Session-Id", "session"));
        assertThat(actual, is(expected));
        assertThat(requestCaptor.getValue().method(), is("DELETE"));
        assertThat(requestCaptor.getValue().headers().firstValue("MCP-Session-Id").orElse(""), is("session"));
    }
    
    @Test
    void assertSendRawPostRequest() throws IOException, InterruptedException {
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse<String> expected = mockHttpResponse();
        HttpClient httpClient = mockHttpClient(expected, requestCaptor);
        HttpResponse<String> actual = MCPHttpTransportTestSupport.sendRawPostRequest(httpClient, URI.create("http://127.0.0.1:8080/mcp"), Map.of("MCP-Session-Id", "session"), "{}");
        assertThat(actual, is(expected));
        assertThat(requestCaptor.getValue().method(), is("POST"));
        assertThat(requestCaptor.getValue().headers().firstValue("Content-Type").orElse(""), is("application/json"));
        assertThat(requestCaptor.getValue().headers().firstValue("MCP-Session-Id").orElse(""), is("session"));
    }
    
    @Test
    void assertOpenEventStream() throws IOException, InterruptedException {
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse<String> expected = mockHttpResponse();
        HttpClient httpClient = mockHttpClient(expected, requestCaptor);
        HttpResponse<String> actual = MCPHttpTransportTestSupport.openEventStream(httpClient, URI.create("http://127.0.0.1:8080/mcp"), Map.of("MCP-Session-Id", "session"));
        assertThat(actual, is(expected));
        assertThat(requestCaptor.getValue().method(), is("GET"));
        assertThat(requestCaptor.getValue().headers().firstValue("MCP-Session-Id").orElse(""), is("session"));
    }
    
    @Test
    void assertSendJsonRpcRequest() throws IOException, InterruptedException {
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse<String> expected = mockHttpResponse();
        HttpClient httpClient = mockHttpClient(expected, requestCaptor);
        HttpResponse<String> actual = MCPHttpTransportTestSupport.sendJsonRpcRequest(
                httpClient, URI.create("http://127.0.0.1:8080/mcp"), Map.of("MCP-Session-Id", "session"), "id", "tools/list", Map.of());
        assertThat(actual, is(expected));
        assertThat(requestCaptor.getValue().method(), is("POST"));
        assertThat(requestCaptor.getValue().headers().firstValue("Accept").orElse(""), is("application/json, text/event-stream"));
    }
    
    @Test
    void assertCreateInitializeRequestParams() {
        assertThat(MCPHttpTransportTestSupport.createInitializeRequestParams("client"),
                is(MCPInteractionProtocolSupport.createInitializeRequestParams("client")));
    }
    
    @Test
    void assertCreateJsonRpcRequestBody() {
        assertThat(MCPInteractionPayloads.parseJsonPayload(MCPHttpTransportTestSupport.createJsonRpcRequestBody("id", "tools/list", Map.of())),
                is(Map.of("jsonrpc", "2.0", "id", "id", "method", "tools/list", "params", Map.of())));
    }
    
    @Test
    void assertCreateJsonRpcNotificationBody() {
        assertThat(MCPInteractionPayloads.parseJsonPayload(MCPHttpTransportTestSupport.createJsonRpcNotificationBody("notifications/initialized", Map.of())),
                is(Map.of("jsonrpc", "2.0", "method", "notifications/initialized", "params", Map.of())));
    }
    
    private HttpClient mockHttpClient(final HttpResponse<String> response, final ArgumentCaptor<HttpRequest> requestCaptor) throws IOException, InterruptedException {
        HttpClient result = mock(HttpClient.class);
        when(result.send(requestCaptor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockHttpResponse() {
        return mock(HttpResponse.class);
    }
}

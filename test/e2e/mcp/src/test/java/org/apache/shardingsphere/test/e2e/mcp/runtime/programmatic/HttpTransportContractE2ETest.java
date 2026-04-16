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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTransportContractE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    @Test
    void assertInitializeSessionAndProtocolHeaders() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> actual = sendInitializeRequest(httpClient);
        assertThat(actual.statusCode(), is(200));
        assertTrue(actual.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertThat(actual.headers().firstValue("MCP-Protocol-Version").orElse(""), is(getProtocolVersion()));
        String actualSessionId = actual.headers().firstValue("MCP-Session-Id").orElse("");
        assertFalse(actualSessionId.isEmpty());
        Map<String, Object> actualPayload = parseJsonBody(actual.body());
        Map<String, Object> actualResult = castToMap(actualPayload.get("result"));
        assertThat(String.valueOf(actualPayload.get("jsonrpc")), is("2.0"));
        assertThat(String.valueOf(actualResult.get("protocolVersion")), is(getProtocolVersion()));
    }
    
    @Test
    void assertAcceptFollowUpRequestWithLowercaseSessionHeaders() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = MCPHttpTransportTestSupport.createJsonRequestBuilder(getEndpointUri())
                .header("mcp-session-id", sessionId)
                .header("mcp-protocol-version", getProtocolVersion())
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                        "resource-1", "resources/read", Map.of("uri", "shardingsphere://capabilities"))))
                .build();
        HttpResponse<String> actual = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(200));
        assertThat(getFirstResourcePayload(actual.body()).get("supportedTools"), is(List.of("search_metadata", "execute_query")));
    }
    
    @Test
    void assertRejectFollowUpRequestWithoutProtocolHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpRequest request = MCPHttpTransportTestSupport.createJsonRequestBuilder(getEndpointUri())
                .header("MCP-Session-Id", sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                        "resource-1", "resources/read", Map.of("uri", "shardingsphere://capabilities"))))
                .build();
        HttpResponse<String> actual = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(400));
        assertThat(String.valueOf(parseJsonBody(actual.body()).get("message")), is("MCP-Protocol-Version header is required."));
    }
    
    private Map<String, Object> parseJsonBody(final String responseBody) {
        return MCPInteractionPayloads.parseJsonPayload(responseBody);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
}

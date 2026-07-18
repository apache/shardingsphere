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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StreamableHttpMCPServerWireTest {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Test
    void assertInitializeRejectsUnsupportedContentType() throws IOException, InterruptedException {
        StreamableHttpMCPServer server = createServer();
        server.start();
        try {
            HttpResponse<String> actual = HttpClient.newHttpClient().send(createInitializeRequest(server.getLocalPort(), "text/plain"), HttpResponse.BodyHandlers.ofString());
            assertThat(actual.statusCode(), is(415));
        } finally {
            server.stop();
        }
    }
    
    @Test
    void assertInitializeRejectsRemoteOriginWithCategory() throws IOException, InterruptedException {
        StreamableHttpMCPServer server = createServer();
        server.start();
        try {
            HttpResponse<String> actual = HttpClient.newHttpClient().send(createInitializeRequest(server.getLocalPort(), "application/json", "http://example.com"),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(actual.statusCode(), is(403));
            Map<?, ?> actualPayload = OBJECT_MAPPER.readValue(actual.body(), Map.class);
            assertFalse(actualPayload.containsKey("id"));
            Map<?, ?> actualError = (Map<?, ?>) actualPayload.get("error");
            Map<?, ?> actualData = (Map<?, ?>) actualError.get("data");
            Map<?, ?> actualRecovery = (Map<?, ?>) actualData.get("recovery");
            assertThat(actualRecovery.get("category"), is("origin_not_allowed"));
        } finally {
            server.stop();
        }
    }
    
    private StreamableHttpMCPServer createServer() {
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()),
                MCPTransportType.HTTP);
        return new StreamableHttpMCPServer(new HttpTransportConfiguration("127.0.0.1", 0, "/mcp"), runtimeContext);
    }
    
    private HttpRequest createInitializeRequest(final int port, final String contentType) throws JsonProcessingException {
        return createInitializeRequest(port, contentType, "");
    }
    
    private HttpRequest createInitializeRequest(final int port, final String contentType, final String origin) throws JsonProcessingException {
        HttpRequest.Builder result = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/mcp"))
                .header("Content-Type", contentType)
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(createInitializePayload())));
        if (!origin.isEmpty()) {
            result.header("Origin", origin);
        }
        return result.build();
    }
    
    private Map<String, Object> createInitializePayload() {
        return Map.of(
                "jsonrpc", "2.0",
                "id", "init-1",
                "method", "initialize",
                "params", Map.of(
                        "protocolVersion", MCPTransportConstants.PROTOCOL_VERSION,
                        "capabilities", Map.of(),
                        "clientInfo", Map.of("name", "mcp-http-wire-test", "version", "test")));
    }
}

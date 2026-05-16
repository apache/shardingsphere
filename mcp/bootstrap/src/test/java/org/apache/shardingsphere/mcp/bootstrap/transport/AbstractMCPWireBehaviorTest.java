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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract MCP wire behavior test.
 */
public abstract class AbstractMCPWireBehaviorTest {
    
    private static final String JSONRPC_VERSION = "2.0";
    
    private static final String INITIALIZE_REQUEST_ID = "init-1";
    
    private static final String CLIENT_NAME = "mcp-bootstrap-wire";
    
    private static final String ENDPOINT_PATH = "/mcp";
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String ACCEPT_HEADER = "application/json, text/event-stream";
    
    private static final int PIPE_SIZE = 65536;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    protected static Stream<Arguments> transports() {
        return Stream.of(
                Arguments.of("Streamable HTTP", (MCPWireClientFactory) AbstractMCPWireBehaviorTest::createHttpWireClient),
                Arguments.of("STDIO", (MCPWireClientFactory) AbstractMCPWireBehaviorTest::createStdioWireClient));
    }
    
    protected void assertJsonRpcErrorWithoutResult(final Map<String, Object> actual, final String requestId) {
        assertThat(String.valueOf(actual.get("jsonrpc")), is(JSONRPC_VERSION));
        assertThat(String.valueOf(actual.get("id")), is(requestId));
        assertTrue(actual.containsKey("error"));
        assertFalse(actual.containsKey("result"));
        Map<String, Object> error = getMap(actual.get("error"));
        assertThat(error.get("code"), isA(Number.class));
        assertFalse(String.valueOf(error.get("message")).isBlank());
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getMap(final Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }
    
    private static MCPWireClient createHttpWireClient() throws Exception {
        HttpTransportConfiguration config =
                new HttpTransportConfiguration(true, "127.0.0.1", false, "", 0, ENDPOINT_PATH, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "",
                        new OAuthIntrospectionConfiguration());
        StreamableHttpMCPServer server = new StreamableHttpMCPServer(config, createRuntimeContext("http"));
        server.start();
        HttpWireClient result = new HttpWireClient(server);
        boolean opened = false;
        try {
            result.open();
            opened = true;
            return result;
        } finally {
            if (!opened) {
                server.stop();
            }
        }
    }
    
    private static MCPWireClient createStdioWireClient() throws Exception {
        StdioWireClient result = new StdioWireClient();
        boolean opened = false;
        try {
            result.open();
            opened = true;
            return result;
        } finally {
            if (!opened) {
                closeStdioWireClient(result);
            }
        }
    }
    
    private static void closeStdioWireClient(final StdioWireClient client) {
        try {
            client.close();
        } catch (final IOException ignored) {
        }
    }
    
    private static MCPRuntimeContext createRuntimeContext(final String activeTransport) {
        return new MCPRuntimeContext(new MCPSessionManager(Collections.emptyMap()), new MCPDatabaseCapabilityProvider(Collections.emptyMap()), activeTransport);
    }
    
    private static Map<String, Object> createInitializeRequestParams() {
        return Map.of(
                "protocolVersion", MCPTransportConstants.PROTOCOL_VERSION,
                "capabilities", Map.of(),
                "clientInfo", Map.of("name", CLIENT_NAME, "version", "test"));
    }
    
    private static Map<String, Object> createRequest(final String requestId, final String method, final Map<String, Object> params) {
        return Map.of("jsonrpc", JSONRPC_VERSION, "id", requestId, "method", method, "params", params);
    }
    
    private static Map<String, Object> createNotification(final String method) {
        return Map.of("jsonrpc", JSONRPC_VERSION, "method", method, "params", Map.of());
    }
    
    private static String toJson(final Map<String, Object> payload) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(payload);
    }
    
    private static Map<String, Object> parseJsonPayload(final String body) throws IOException {
        String actualBody = body.trim();
        if (actualBody.contains("data:")) {
            return parseServerSentEventPayload(actualBody);
        }
        return OBJECT_MAPPER.readValue(actualBody, new TypeReference<>() {
        });
    }
    
    private static Map<String, Object> parseServerSentEventPayload(final String body) throws IOException {
        for (String each : body.split("\\R")) {
            if (each.startsWith("data:")) {
                return parseJsonPayload(each.substring("data:".length()).trim());
            }
        }
        throw new IllegalStateException("MCP HTTP response does not contain an SSE data frame.");
    }
    
    private static String getErrorMessage(final Map<String, Object> payload) {
        return Objects.toString(getStaticMap(payload.get("error")).get("message"), "unknown error");
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getStaticMap(final Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }
    
    @FunctionalInterface
    protected interface MCPWireClientFactory {
        
        MCPWireClient create() throws Exception;
    }
    
    protected interface MCPWireClient extends AutoCloseable {
        
        Map<String, Object> sendRawRequest(String requestId, String method, Map<String, Object> params) throws Exception;
    }
    
    private static final class HttpWireClient implements MCPWireClient {
        
        private final StreamableHttpMCPServer server;
        
        private final HttpClient httpClient = HttpClient.newHttpClient();
        
        private String sessionId;
        
        private String protocolVersion;
        
        private HttpWireClient(final StreamableHttpMCPServer server) {
            this.server = server;
        }
        
        private void open() throws IOException, InterruptedException {
            HttpResponse<String> initializeResponse = sendInitializeRequest();
            if (200 != initializeResponse.statusCode()) {
                throw new IllegalStateException("MCP HTTP initialize failed with status " + initializeResponse.statusCode() + ".");
            }
            Map<String, Object> initializePayload = parseJsonPayload(initializeResponse.body());
            if (initializePayload.containsKey("error")) {
                throw new IllegalStateException("MCP HTTP initialize failed: " + getErrorMessage(initializePayload) + ".");
            }
            sessionId = initializeResponse.headers().firstValue("MCP-Session-Id")
                    .orElseThrow(() -> new IllegalStateException("MCP HTTP initialize response does not contain MCP-Session-Id."));
            protocolVersion = initializeResponse.headers().firstValue("MCP-Protocol-Version").orElse(MCPTransportConstants.PROTOCOL_VERSION);
            sendInitializedNotification();
        }
        
        private HttpResponse<String> sendInitializeRequest() throws IOException, InterruptedException {
            return httpClient.send(createRequestBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(createRequest(INITIALIZE_REQUEST_ID, "initialize", createInitializeRequestParams()))))
                    .build(), HttpResponse.BodyHandlers.ofString());
        }
        
        private void sendInitializedNotification() throws IOException, InterruptedException {
            HttpResponse<String> response = httpClient.send(createSessionRequestBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(createNotification("notifications/initialized")))).build(), HttpResponse.BodyHandlers.ofString());
            if (202 != response.statusCode()) {
                throw new IllegalStateException("MCP HTTP initialized notification failed with status " + response.statusCode() + ".");
            }
        }
        
        @Override
        public Map<String, Object> sendRawRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException, InterruptedException {
            HttpResponse<String> response = httpClient.send(createSessionRequestBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(createRequest(requestId, method, params)))).build(), HttpResponse.BodyHandlers.ofString());
            if (200 != response.statusCode()) {
                throw new IllegalStateException("MCP HTTP request failed with status " + response.statusCode() + ".");
            }
            return parseJsonPayload(response.body());
        }
        
        private HttpRequest.Builder createRequestBuilder() {
            return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.getLocalPort() + ENDPOINT_PATH))
                    .header("Content-Type", JSON_CONTENT_TYPE)
                    .header("Accept", ACCEPT_HEADER);
        }
        
        private HttpRequest.Builder createSessionRequestBuilder() {
            return createRequestBuilder()
                    .header("MCP-Session-Id", sessionId)
                    .header("MCP-Protocol-Version", protocolVersion);
        }
        
        @Override
        public void close() throws IOException, InterruptedException {
            try {
                if (null != sessionId) {
                    httpClient.send(createSessionRequestBuilder().DELETE().build(), HttpResponse.BodyHandlers.ofString());
                }
            } finally {
                server.stop();
            }
        }
        
    }
    
    private static final class StdioWireClient implements MCPWireClient {
        
        private final McpJsonMapper jsonMapper = MCPTransportJsonMapperFactory.create();
        
        private McpSyncServer server;
        
        private BufferedWriter writer;
        
        private BufferedReader reader;
        
        private void open() throws IOException {
            PipedOutputStream clientOutput = new PipedOutputStream();
            PipedInputStream serverInput = new PipedInputStream(clientOutput, PIPE_SIZE);
            PipedOutputStream serverOutput = new PipedOutputStream();
            PipedInputStream clientInput = new PipedInputStream(serverOutput, PIPE_SIZE);
            writer = new BufferedWriter(new OutputStreamWriter(clientOutput, StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(clientInput, StandardCharsets.UTF_8));
            server = new MCPSyncServerFactory(createRuntimeContext("stdio"), jsonMapper)
                    .create(new StdioServerTransportProvider(jsonMapper, serverInput, serverOutput));
            initializeSession();
        }
        
        private void initializeSession() throws IOException {
            Map<String, Object> initializePayload = sendRawRequest(INITIALIZE_REQUEST_ID, "initialize", createInitializeRequestParams());
            if (initializePayload.containsKey("error")) {
                throw new IllegalStateException("MCP STDIO initialize failed: " + getErrorMessage(initializePayload) + ".");
            }
            writePayload(createNotification("notifications/initialized"));
        }
        
        @Override
        public Map<String, Object> sendRawRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException {
            writePayload(createRequest(requestId, method, params));
            return readResponse(requestId);
        }
        
        private void writePayload(final Map<String, Object> payload) throws IOException {
            writer.write(toJson(payload));
            writer.newLine();
            writer.flush();
        }
        
        private Map<String, Object> readResponse(final String requestId) throws IOException {
            String line;
            while (null != (line = reader.readLine())) {
                if (line.isBlank()) {
                    continue;
                }
                Map<String, Object> result = parseJsonPayload(line);
                if (requestId.equals(String.valueOf(result.get("id")))) {
                    return result;
                }
            }
            throw new IllegalStateException("MCP STDIO response was not returned.");
        }
        
        @Override
        public void close() throws IOException {
            IOException closeException = closeResource(writer);
            if (null != server) {
                server.closeGracefully();
            }
            IOException readerCloseException = closeResource(reader);
            if (null != closeException) {
                if (null != readerCloseException) {
                    closeException.addSuppressed(readerCloseException);
                }
                throw closeException;
            }
            if (null != readerCloseException) {
                throw readerCloseException;
            }
        }
        
        private IOException closeResource(final Closeable closeable) {
            try {
                if (null != closeable) {
                    closeable.close();
                }
                return null;
            } catch (final IOException ex) {
                return ex;
            }
        }
    }
}

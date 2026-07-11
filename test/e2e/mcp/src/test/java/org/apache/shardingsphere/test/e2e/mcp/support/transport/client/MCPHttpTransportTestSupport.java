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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP transport support for MCP E2E tests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPHttpTransportTestSupport {
    
    private static final String CONTENT_TYPE = "application/json";
    
    private static final String ACCEPT = "application/json, text/event-stream";
    
    /**
     * Create a JSON request builder for the given MCP endpoint.
     *
     * @param endpointUri MCP endpoint URI
     * @return JSON request builder
     */
    public static HttpRequest.Builder createJsonRequestBuilder(final URI endpointUri) {
        return HttpRequest.newBuilder(endpointUri)
                .header("Content-Type", CONTENT_TYPE)
                .header("Accept", ACCEPT);
    }
    
    /**
     * Create a session-bound JSON request builder for the given MCP endpoint.
     *
     * @param endpointUri MCP endpoint URI
     * @param sessionId MCP session id
     * @param protocolVersion MCP protocol version
     * @return session-bound JSON request builder
     */
    public static HttpRequest.Builder createSessionRequestBuilder(final URI endpointUri, final String sessionId, final String protocolVersion) {
        return createJsonRequestBuilder(endpointUri)
                .header("MCP-Session-Id", sessionId)
                .header("MCP-Protocol-Version", protocolVersion);
    }
    
    /**
     * Create session headers.
     *
     * @param sessionId MCP session identifier
     * @param protocolVersion MCP protocol version
     * @return session headers
     */
    public static Map<String, String> createSessionHeaders(final String sessionId, final String protocolVersion) {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("MCP-Session-Id", sessionId);
        result.put("MCP-Protocol-Version", protocolVersion);
        return result;
    }
    
    /**
     * Send a DELETE request.
     *
     * @param httpClient HTTP client
     * @param endpointUri MCP endpoint URI
     * @param headers request headers
     * @return HTTP response
     * @throws IOException I/O exception
     * @throws InterruptedException interrupted exception
     */
    public static HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final URI endpointUri, final Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpointUri).DELETE();
        applyHeaders(requestBuilder, headers);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Send a raw POST request.
     *
     * @param httpClient HTTP client
     * @param endpointUri MCP endpoint URI
     * @param headers request headers
     * @param requestBody request body
     * @return HTTP response
     * @throws IOException I/O exception
     * @throws InterruptedException interrupted exception
     */
    public static HttpResponse<String> sendRawPostRequest(final HttpClient httpClient, final URI endpointUri, final Map<String, String> headers,
                                                          final String requestBody) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = createJsonRequestBuilder(endpointUri).POST(HttpRequest.BodyPublishers.ofString(requestBody));
        applyHeaders(requestBuilder, headers);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Open an event stream.
     *
     * @param httpClient HTTP client
     * @param endpointUri MCP endpoint URI
     * @param headers request headers
     * @return HTTP response
     * @throws IOException I/O exception
     * @throws InterruptedException interrupted exception
     */
    public static HttpResponse<String> openEventStream(final HttpClient httpClient, final URI endpointUri, final Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpointUri).GET();
        applyHeaders(requestBuilder, headers);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
    
    /**
     * Open an event stream without waiting for the whole response body.
     *
     * @param httpClient HTTP client
     * @param endpointUri MCP endpoint URI
     * @param headers request headers
     * @return HTTP response
     * @throws IOException I/O exception
     * @throws InterruptedException interrupted exception
     */
    public static HttpResponse<InputStream> openEventStreamInputStream(final HttpClient httpClient, final URI endpointUri,
                                                                       final Map<String, String> headers) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpointUri).GET();
        applyHeaders(requestBuilder, headers);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
    }
    
    /**
     * Send a JSON-RPC request.
     *
     * @param httpClient HTTP client
     * @param endpointUri MCP endpoint URI
     * @param headers request headers
     * @param requestId request id
     * @param method method name
     * @param params request parameters
     * @return HTTP response
     * @throws IOException I/O exception
     * @throws InterruptedException interrupted exception
     */
    public static HttpResponse<String> sendJsonRpcRequest(final HttpClient httpClient, final URI endpointUri, final Map<String, String> headers, final String requestId,
                                                          final String method, final Map<String, Object> params) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, endpointUri, headers, MCPInteractionProtocolSupport.createJsonRpcRequestBody(requestId, method, params));
    }
    
    private static void applyHeaders(final HttpRequest.Builder requestBuilder, final Map<String, String> headers) {
        headers.forEach(requestBuilder::setHeader);
    }
}

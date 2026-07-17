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
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPHttpInteractionClientTest {
    
    private static final URI ENDPOINT_URI = URI.create("http://127.0.0.1:8080/mcp");
    
    @Test
    void assertOpen() throws IOException, InterruptedException {
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.addResponse(200, Map.of("MCP-Session-Id", List.of("session"), "MCP-Protocol-Version", List.of("protocol")), "{\"result\":{\"serverInfo\":{\"name\":\"test\"}}}");
        httpClient.addResponse(202, Map.of(), "");
        MCPHttpInteractionClient client = new MCPHttpInteractionClient(ENDPOINT_URI, httpClient);
        client.open();
        assertThat(client.getInitializePayload(), is(Map.of("result", Map.of("serverInfo", Map.of("name", "test")))));
        assertThat(httpClient.requests.size(), is(2));
        assertThat(httpClient.requests.get(0).method(), is("POST"));
        assertThat(httpClient.requests.get(1).headers().firstValue("MCP-Session-Id").orElse(""), is("session"));
        assertThat(httpClient.requests.get(1).headers().firstValue("MCP-Protocol-Version").orElse(""), is("protocol"));
    }
    
    @Test
    void assertOpenWithErrorStatus() {
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.addResponse(500, Map.of(), "{}");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new MCPHttpInteractionClient(ENDPOINT_URI, httpClient).open());
        assertThat(actual.getMessage(), is("Failed to initialize MCP session."));
    }
    
    @Test
    void assertOpenWithJsonRpcError() {
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.addResponse(200, Map.of("MCP-Session-Id", List.of("session")), "{\"error\":{\"message\":\"denied\"}}");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new MCPHttpInteractionClient(ENDPOINT_URI, httpClient).open());
        assertThat(actual.getMessage(), is("Failed to initialize MCP session: denied"));
    }
    
    @Test
    void assertOpenWithMissingSessionHeader() {
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.addResponse(200, Map.of(), "{\"result\":{}}");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new MCPHttpInteractionClient(ENDPOINT_URI, httpClient).open());
        assertThat(actual.getMessage(), is("MCP initialize response does not contain MCP-Session-Id header."));
    }
    
    @Test
    void assertOpenWithInvalidNotificationResponse() {
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.addResponse(200, Map.of("MCP-Session-Id", List.of("session")), "{\"result\":{}}");
        httpClient.addResponse(202, Map.of(), "{}");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new MCPHttpInteractionClient(ENDPOINT_URI, httpClient).open());
        assertThat(actual.getMessage(), is("MCP notification response body must be empty."));
    }
    
    @Test
    void assertGetInitializePayloadBeforeOpen() {
        assertThat(new MCPHttpInteractionClient(ENDPOINT_URI, new FakeHttpClient()).getInitializePayload(), is(Map.of()));
    }
    
    @Test
    void assertClose() throws IOException, InterruptedException {
        FakeHttpClient httpClient = new FakeHttpClient();
        httpClient.addResponse(200, Map.of("MCP-Session-Id", List.of("session")), "{\"result\":{}}");
        httpClient.addResponse(202, Map.of(), "");
        httpClient.addResponse(200, Map.of(), "");
        MCPHttpInteractionClient client = new MCPHttpInteractionClient(ENDPOINT_URI, httpClient);
        client.open();
        client.close();
        assertThat(httpClient.requests.get(2).method(), is("DELETE"));
        assertThat(httpClient.requests.get(2).headers().firstValue("MCP-Session-Id").orElse(""), is("session"));
        assertThat(client.getInitializePayload(), is(Map.of()));
    }
    
    @Test
    void assertCloseBeforeOpen() throws IOException, InterruptedException {
        FakeHttpClient httpClient = new FakeHttpClient();
        new MCPHttpInteractionClient(ENDPOINT_URI, httpClient).close();
        assertThat(httpClient.requests, is(List.of()));
    }
    
    private static final class FakeHttpClient extends HttpClient {
        
        private final Deque<QueuedResponse> responses = new ArrayDeque<>();
        
        private final List<HttpRequest> requests = new LinkedList<>();
        
        private void addResponse(final int statusCode, final Map<String, List<String>> headers, final String body) {
            responses.addLast(new QueuedResponse(statusCode, headers, body));
        }
        
        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }
        
        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }
        
        @Override
        public HttpClient.Redirect followRedirects() {
            return HttpClient.Redirect.NEVER;
        }
        
        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }
        
        @Override
        public SSLContext sslContext() {
            return null;
        }
        
        @Override
        public SSLParameters sslParameters() {
            return null;
        }
        
        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }
        
        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
        
        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(final HttpRequest request, final HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
            requests.add(request);
            if (responses.isEmpty()) {
                throw new IOException("No queued HTTP response.");
            }
            QueuedResponse response = responses.removeFirst();
            return (HttpResponse<T>) new StringHttpResponse(request, response.statusCode(), response.headers(), response.body());
        }
        
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request, final HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(final HttpRequest request, final HttpResponse.BodyHandler<T> responseBodyHandler,
                                                                final HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }
    }
    
    private record QueuedResponse(int statusCode, Map<String, List<String>> headers, String body) {
    }
    
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class StringHttpResponse implements HttpResponse<String> {
        
        private final HttpRequest request;
        
        private final int statusCode;
        
        private final Map<String, List<String>> rawHeaders;
        
        private final String body;
        
        @Override
        public int statusCode() {
            return statusCode;
        }
        
        @Override
        public HttpRequest request() {
            return request;
        }
        
        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }
        
        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(rawHeaders, (key, value) -> true);
        }
        
        @Override
        public String body() {
            return body;
        }
        
        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }
        
        @Override
        public URI uri() {
            return request.uri();
        }
        
        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }
}

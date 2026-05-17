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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.authorization;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpOAuthTokenIntrospectorTest {
    
    private HttpServer introspectionServer;
    
    @AfterEach
    void stopIntrospectionServer() {
        if (null != introspectionServer) {
            introspectionServer.stop(0);
            introspectionServer = null;
        }
    }
    
    @Test
    void assertIntrospectWithMalformedResponse() throws IOException {
        startIntrospectionServer("not-json");
        HttpOAuthTokenIntrospector introspector = new HttpOAuthTokenIntrospector(new OAuthIntrospectionConfiguration(createIntrospectionEndpoint().toString(), "foo_client", "foo_secret", "", 0L));
        IOException actual = assertThrows(IOException.class, () -> introspector.introspect("foo_token"));
        assertThat(actual.getMessage(), is("OAuth introspection endpoint returned invalid JSON."));
    }
    
    private void startIntrospectionServer(final String responseBody) throws IOException {
        introspectionServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        introspectionServer.createContext("/introspect", exchange -> handleIntrospectionRequest(exchange, responseBody));
        introspectionServer.start();
    }
    
    private void handleIntrospectionRequest(final HttpExchange exchange, final String responseBody) throws IOException {
        try {
            byte[] actualResponseBody = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, actualResponseBody.length);
            exchange.getResponseBody().write(actualResponseBody);
        } finally {
            exchange.close();
        }
    }
    
    private URI createIntrospectionEndpoint() {
        return URI.create("http://127.0.0.1:" + introspectionServer.getAddress().getPort() + "/introspect");
    }
}

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

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthProtectedResourceMetadataServletTest {
    
    @Test
    void assertDoGet() throws IOException {
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        new OAuthProtectedResourceMetadataServlet(
                new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 18088, "/mcp", Collections.emptyList(), List.of("https://auth.example.test"), List.of("mcp.read"), "",
                        new OAuthIntrospectionConfiguration()))
                .doGet(createRequest(), response);
        Map<String, Object> actual = JsonUtils.fromJsonString(responseBody.toString(), new TypeReference<>() {
        });
        assertThat(actual.get("resource"), is("http://127.0.0.1:18088/mcp"));
        assertThat(actual.get("authorization_servers"), is(List.of("https://auth.example.test")));
        assertThat(actual.get("scopes_supported"), is(List.of("mcp.read")));
        assertThat(actual.get("bearer_methods_supported"), is(List.of("header")));
    }
    
    @Test
    void assertDoGetWithConfiguredProtectedResource() throws IOException {
        StringWriter responseBody = new StringWriter();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        new OAuthProtectedResourceMetadataServlet(new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 18088, "/mcp", Collections.emptyList(), List.of("https://auth.example.test"),
                List.of(), "https://gateway.example.test/mcp",
                new OAuthIntrospectionConfiguration())).doGet(createRequest(), response);
        Map<String, Object> actual = JsonUtils.fromJsonString(responseBody.toString(), new TypeReference<>() {
        });
        assertThat(actual.get("resource"), is("https://gateway.example.test/mcp"));
    }
    
    @Test
    void assertDoGetWhenProtectedResourceMetadataIsDisabled() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        new OAuthProtectedResourceMetadataServlet(
                new HttpTransportConfiguration(true, "127.0.0.1", false, "", 18088, "/mcp", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "",
                        new OAuthIntrospectionConfiguration()))
                .doGet(createRequest(), response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    
    private HttpServletRequest createRequest() {
        HttpServletRequest result = mock(HttpServletRequest.class);
        when(result.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:18088/.well-known/oauth-protected-resource/mcp"));
        when(result.getRequestURI()).thenReturn("/.well-known/oauth-protected-resource/mcp");
        return result;
    }
}

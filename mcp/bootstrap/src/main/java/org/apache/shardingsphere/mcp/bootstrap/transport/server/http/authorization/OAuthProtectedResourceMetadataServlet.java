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

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OAuth protected resource metadata servlet.
 */
@RequiredArgsConstructor
public final class OAuthProtectedResourceMetadataServlet extends HttpServlet {
    
    public static final String WELL_KNOWN_ROOT_PATH = "/.well-known/oauth-protected-resource";
    
    private static final long serialVersionUID = -3968753819870750606L;
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private final HttpTransportConfiguration config;
    
    /**
     * Create endpoint-scoped well-known path.
     *
     * @param endpointPath MCP endpoint path
     * @return endpoint-scoped well-known path
     */
    public static String createEndpointWellKnownPath(final String endpointPath) {
        String actualEndpointPath = Objects.toString(endpointPath, "").trim();
        return WELL_KNOWN_ROOT_PATH + (actualEndpointPath.startsWith("/") ? actualEndpointPath : "/" + actualEndpointPath);
    }
    
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(JSON_CONTENT_TYPE);
        if (!config.isProtectedResourceMetadataEnabled()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.getWriter().write(JsonUtils.toJsonString(createPayload(request)));
    }
    
    private Map<String, Object> createPayload(final HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("resource", resolveResource(request));
        result.put("authorization_servers", config.getAuthorizationServers());
        if (!config.getScopesSupported().isEmpty()) {
            result.put("scopes_supported", config.getScopesSupported());
        }
        result.put("bearer_methods_supported", List.of("header"));
        return result;
    }
    
    private String resolveResource(final HttpServletRequest request) {
        return config.getProtectedResource().isEmpty() ? HttpAuthorizationUriUtils.createAbsoluteUri(request, config.getEndpointPath()) : config.getProtectedResource();
    }
}

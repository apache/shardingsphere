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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

final class HttpOAuthTokenIntrospector implements OAuthTokenIntrospector {
    
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5L);
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private final HttpClient httpClient;
    
    private final URI endpoint;
    
    private final String basicAuthorization;
    
    HttpOAuthTokenIntrospector(final OAuthIntrospectionConfiguration config) {
        httpClient = HttpClient.newHttpClient();
        endpoint = URI.create(config.getEndpoint());
        basicAuthorization = "Basic " + Base64.getEncoder().encodeToString((config.getClientId() + ":" + config.getClientSecret()).getBytes(StandardCharsets.UTF_8));
    }
    
    @Override
    public Map<String, Object> introspect(final String token) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(REQUEST_TIMEOUT)
                .header("Authorization", basicAuthorization).header("Content-Type", "application/x-www-form-urlencoded").header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(token))).build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("OAuth introspection endpoint returned HTTP " + response.statusCode() + ".");
            }
            return parseResponseBody(response.body());
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("OAuth introspection request was interrupted.", ex);
        }
    }
    
    private Map<String, Object> parseResponseBody(final String responseBody) throws IOException {
        try {
            return JSON_MAPPER.readValue(responseBody, new TypeReference<>() {
            });
        } catch (final JsonProcessingException ex) {
            throw new IOException("OAuth introspection endpoint returned invalid JSON.", ex);
        }
    }
    
    private String createRequestBody(final String token) {
        return "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8) + "&token_type_hint=access_token";
    }
}

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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

final class OAuthTokenValidator {
    
    private final OAuthTokenIntrospector introspector;
    
    private final List<String> expectedIssuers;
    
    private final Set<String> requiredScopes;
    
    private final String protectedResource;
    
    private final String endpointPath;
    
    private final long cacheTtlMillis;
    
    private final LongSupplier currentTimeMillisSupplier;
    
    private final Map<String, CachedValidationResult> cache;
    
    OAuthTokenValidator(final HttpTransportConfiguration config) {
        introspector = new HttpOAuthTokenIntrospector(config.getOauthIntrospection());
        expectedIssuers = config.getOauthIntrospection().getExpectedIssuer().isEmpty()
                ? config.getAuthorizationServers()
                : List.of(config.getOauthIntrospection().getExpectedIssuer());
        requiredScopes = config.getScopesSupported().stream().collect(Collectors.toSet());
        protectedResource = config.getProtectedResource();
        endpointPath = config.getEndpointPath();
        cacheTtlMillis = config.getOauthIntrospection().getCacheTtlMillis();
        currentTimeMillisSupplier = System::currentTimeMillis;
        cache = new ConcurrentHashMap<>();
    }
    
    OAuthTokenValidationResult validate(final String token, final HttpServletRequest request) {
        CachedValidationResult cachedResult = cache.get(token);
        if (null != cachedResult && cachedResult.expiresAtMillis > currentTimeMillisSupplier.getAsLong()) {
            return cachedResult.result;
        }
        cache.remove(token);
        try {
            Map<String, Object> introspectionResponse = introspector.introspect(token);
            OAuthTokenValidationResult result = validateIntrospectionResponse(introspectionResponse, getExpectedResource(request));
            cacheSuccessfulResult(token, result, introspectionResponse);
            return result;
        } catch (final IOException ex) {
            return OAuthTokenValidationResult.invalidToken();
        }
    }
    
    private OAuthTokenValidationResult validateIntrospectionResponse(final Map<String, Object> introspectionResponse, final String expectedResource) {
        if (!isActive(introspectionResponse) || !isExpectedIssuer(introspectionResponse.get("iss")) || !isValidTimeWindow(introspectionResponse)
                || !matchesResource(introspectionResponse, expectedResource)) {
            return OAuthTokenValidationResult.invalidToken();
        }
        return hasRequiredScopes(introspectionResponse.get("scope")) ? OAuthTokenValidationResult.valid() : OAuthTokenValidationResult.insufficientScope();
    }
    
    private boolean isActive(final Map<String, Object> introspectionResponse) {
        Object active = introspectionResponse.get("active");
        return Boolean.TRUE.equals(active) || "true".equalsIgnoreCase(Objects.toString(active, ""));
    }
    
    private boolean isExpectedIssuer(final Object issuer) {
        return expectedIssuers.contains(Objects.toString(issuer, "").trim());
    }
    
    private boolean isValidTimeWindow(final Map<String, Object> introspectionResponse) {
        Long expiration = getEpochSeconds(introspectionResponse.get("exp"));
        if (null == expiration || currentEpochSeconds() >= expiration) {
            return false;
        }
        Long notBefore = getEpochSeconds(introspectionResponse.get("nbf"));
        return null == notBefore || currentEpochSeconds() >= notBefore;
    }
    
    private long currentEpochSeconds() {
        return currentTimeMillisSupplier.getAsLong() / 1000L;
    }
    
    private Long getEpochSeconds(final Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            String text = Objects.toString(value, "").trim();
            return text.isEmpty() ? null : Long.parseLong(text);
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }
    
    private boolean matchesResource(final Map<String, Object> introspectionResponse, final String expectedResource) {
        return matchesTextOrCollection(introspectionResponse.get("aud"), expectedResource) || matchesTextOrCollection(introspectionResponse.get("resource"), expectedResource)
                || matchesTextOrCollection(introspectionResponse.get("resources"), expectedResource);
    }
    
    private boolean matchesTextOrCollection(final Object value, final String expected) {
        return value instanceof Collection<?> ? ((Collection<?>) value).stream().anyMatch(each -> expected.equals(Objects.toString(each, "").trim()))
                : expected.equals(Objects.toString(value, "").trim());
    }
    
    private boolean hasRequiredScopes(final Object scope) {
        if (requiredScopes.isEmpty()) {
            return true;
        }
        Set<String> actualScopes = scope instanceof Collection<?> ? ((Collection<?>) scope).stream().map(each -> Objects.toString(each, "").trim()).filter(each -> !each.isEmpty())
                .collect(Collectors.toSet()) : splitScopeText(scope);
        return actualScopes.containsAll(requiredScopes);
    }
    
    private Set<String> splitScopeText(final Object scope) {
        String text = Objects.toString(scope, "").trim();
        return text.isEmpty() ? Collections.emptySet() : Arrays.stream(text.split("\\s+")).collect(Collectors.toSet());
    }
    
    private String getExpectedResource(final HttpServletRequest request) {
        return protectedResource.isEmpty() ? HttpAuthorizationUriUtils.createAbsoluteUri(request, endpointPath) : protectedResource;
    }
    
    private void cacheSuccessfulResult(final String token, final OAuthTokenValidationResult result, final Map<String, Object> introspectionResponse) {
        if (!result.isValid() || 0L == cacheTtlMillis) {
            return;
        }
        long now = currentTimeMillisSupplier.getAsLong();
        long tokenExpiresAtMillis = getEpochSeconds(introspectionResponse.get("exp")) * 1000L;
        long expiresAtMillis = Math.min(now + cacheTtlMillis, tokenExpiresAtMillis);
        if (expiresAtMillis > now) {
            cache.put(token, new CachedValidationResult(result, expiresAtMillis));
        }
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class CachedValidationResult {
        
        private final OAuthTokenValidationResult result;
        
        private final long expiresAtMillis;
        
    }
}

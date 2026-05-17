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
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class OAuthTokenValidatorTest {
    
    private static final long NOW_MILLIS = 1800000000000L;
    
    private static final long NOW_SECONDS = NOW_MILLIS / 1000L;
    
    @Test
    void assertValidateWithValidToken() {
        OAuthTokenValidationResult actual = createValidator(validResponse(), 0L).validate("foo_token", mock(HttpServletRequest.class));
        assertTrue(actual.isValid());
    }
    
    @Test
    void assertValidateWithInactiveToken() {
        OAuthTokenValidationResult actual = createValidator(response(false, "https://auth.example.test", resource(), NOW_SECONDS + 60L, NOW_SECONDS - 1L, "mcp.read"), 0L)
                .validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("invalid_token"));
        assertThat(actual.getStatusCode(), is(401));
    }
    
    @Test
    void assertValidateWithUnexpectedIssuer() {
        OAuthTokenValidationResult actual = createValidator(response(true, "https://other.example.test", resource(), NOW_SECONDS + 60L, NOW_SECONDS - 1L, "mcp.read"), 0L)
                .validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("invalid_token"));
    }
    
    @Test
    void assertValidateWithUnexpectedResource() {
        OAuthTokenValidationResult actual = createValidator(response(true, "https://auth.example.test", "https://gateway.example.test/other", NOW_SECONDS + 60L, NOW_SECONDS - 1L,
                "mcp.read"), 0L).validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("invalid_token"));
    }
    
    @Test
    void assertValidateWithExpiredToken() {
        OAuthTokenValidationResult actual = createValidator(response(true, "https://auth.example.test", resource(), NOW_SECONDS - 1L, NOW_SECONDS - 10L, "mcp.read"), 0L)
                .validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("invalid_token"));
    }
    
    @Test
    void assertValidateWithNotBeforeToken() {
        OAuthTokenValidationResult actual = createValidator(response(true, "https://auth.example.test", resource(), NOW_SECONDS + 60L, NOW_SECONDS + 10L, "mcp.read"), 0L)
                .validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("invalid_token"));
    }
    
    @Test
    void assertValidateWithInsufficientScope() {
        OAuthTokenValidationResult actual = createValidator(response(true, "https://auth.example.test", resource(), NOW_SECONDS + 60L, NOW_SECONDS - 1L, "mcp.write"), 0L)
                .validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("insufficient_scope"));
        assertThat(actual.getStatusCode(), is(403));
    }
    
    @Test
    void assertValidateWithDuplicateScopeText() {
        OAuthTokenValidationResult actual = createValidator(response(true, "https://auth.example.test", resource(), NOW_SECONDS + 60L, NOW_SECONDS - 1L,
                "mcp.read mcp.read"), 0L).validate("foo_token", mock(HttpServletRequest.class));
        assertTrue(actual.isValid());
    }
    
    @Test
    void assertValidateWithIntrospectionFailure() {
        OAuthTokenValidationResult actual = createValidator(new FailingOAuthTokenIntrospector(), 0L).validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("invalid_token"));
    }
    
    @Test
    void assertValidateWithCachedSuccess() {
        CountingOAuthTokenIntrospector introspector = new CountingOAuthTokenIntrospector(validResponse());
        OAuthTokenValidator validator = createValidator(introspector, 30000L);
        assertTrue(validator.validate("foo_token", mock(HttpServletRequest.class)).isValid());
        assertTrue(validator.validate("foo_token", mock(HttpServletRequest.class)).isValid());
        assertThat(introspector.getCount(), is(1));
    }
    
    @Test
    void assertValidateRefreshesCacheAfterTokenExpiration() {
        AtomicLong currentTimeMillis = new AtomicLong(NOW_MILLIS);
        CountingOAuthTokenIntrospector introspector = new CountingOAuthTokenIntrospector(response(true, "https://auth.example.test", resource(), NOW_SECONDS + 1L,
                NOW_SECONDS - 1L, "mcp.read"));
        OAuthTokenValidator validator = createValidator(introspector, 30000L, currentTimeMillis::get);
        assertTrue(validator.validate("foo_token", mock(HttpServletRequest.class)).isValid());
        currentTimeMillis.addAndGet(2000L);
        OAuthTokenValidationResult actual = validator.validate("foo_token", mock(HttpServletRequest.class));
        assertFalse(actual.isValid());
        assertThat(actual.getError(), is("invalid_token"));
        assertThat(introspector.getCount(), is(2));
    }
    
    private OAuthTokenValidator createValidator(final Map<String, Object> response, final long cacheTtlMillis) {
        return createValidator(new CountingOAuthTokenIntrospector(response), cacheTtlMillis);
    }
    
    private OAuthTokenValidator createValidator(final OAuthTokenIntrospector introspector, final long cacheTtlMillis) {
        return createValidator(introspector, cacheTtlMillis, () -> NOW_MILLIS);
    }
    
    private OAuthTokenValidator createValidator(final OAuthTokenIntrospector introspector, final long cacheTtlMillis, final LongSupplier currentTimeMillisSupplier) {
        OAuthTokenValidator result = new OAuthTokenValidator(createConfig(cacheTtlMillis));
        try {
            setField(result, "introspector", introspector);
            setField(result, "currentTimeMillisSupplier", currentTimeMillisSupplier);
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private HttpTransportConfiguration createConfig(final long cacheTtlMillis) {
        return new HttpTransportConfiguration(true, "127.0.0.1", false, "", 18088, "/mcp", Collections.emptyList(), List.of("https://auth.example.test"), List.of("mcp.read"), resource(),
                new OAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "", cacheTtlMillis));
    }
    
    private Map<String, Object> validResponse() {
        return response(true, "https://auth.example.test", resource(), NOW_SECONDS + 60L, NOW_SECONDS - 1L, "mcp.read mcp.write");
    }
    
    private Map<String, Object> response(final boolean active, final String issuer, final String audience, final long expiration, final long notBefore, final String scope) {
        return Map.of("active", active, "iss", issuer, "aud", List.of(audience), "exp", expiration, "nbf", notBefore, "scope", scope);
    }
    
    private String resource() {
        return "https://gateway.example.test/mcp";
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class CountingOAuthTokenIntrospector implements OAuthTokenIntrospector {
        
        private final Map<String, Object> response;
        
        private int count;
        
        @Override
        public Map<String, Object> introspect(final String token) {
            count++;
            return response;
        }
        
        private int getCount() {
            return count;
        }
    }
    
    private static final class FailingOAuthTokenIntrospector implements OAuthTokenIntrospector {
        
        @Override
        public Map<String, Object> introspect(final String token) throws IOException {
            throw new IOException("failed");
        }
    }
}

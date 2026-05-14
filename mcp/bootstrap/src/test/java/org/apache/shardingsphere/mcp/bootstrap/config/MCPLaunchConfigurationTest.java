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

package org.apache.shardingsphere.mcp.bootstrap.config;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPLaunchConfigurationTest {
    
    @Test
    void assertValidateWhenHttpTransportEnabled() {
        assertDoesNotThrow(createLaunchConfiguration(true, false)::validate);
    }
    
    @Test
    void assertValidateWhenStdioTransportEnabled() {
        assertDoesNotThrow(createLaunchConfiguration(false, true)::validate);
    }
    
    @Test
    void assertValidateWhenBothTransportsEnabled() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, createLaunchConfiguration(true, true)::validate);
        assertThat(actual.getMessage(), is("HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport."));
    }
    
    @Test
    void assertValidateWhenBothTransportsDisabled() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, createLaunchConfiguration(false, false)::validate);
        assertThat(actual.getMessage(), is("Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    @Test
    void assertValidateWhenRemoteHttpIsNotExplicitlyAllowed() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, createLaunchConfiguration(true, false, "0.0.0.0", false, "")::validate);
        assertThat(actual.getMessage(), is("Property `transport.http.allowRemoteAccess` must be true when `transport.http.bindHost` is not loopback."));
    }
    
    @Test
    void assertValidateWhenRemoteHttpAccessTokenIsMissing() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                createLaunchConfiguration(true, false, "0.0.0.0", true, "", Collections.singleton("https://gateway.example.test"))::validate);
        assertThat(actual.getMessage(), is("HTTP authorization must be configured when remote HTTP access is enabled."));
    }
    
    @Test
    void assertValidateWhenRemoteHttpAllowedOriginsAreMissing() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(new HttpTransportConfiguration(true, "0.0.0.0", true, "token", 0, "/mcp", Collections.emptyList(),
                        Collections.singleton("https://auth.example.test"), Collections.emptyList(), ""),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.allowedOrigins` must not be empty when remote HTTP access is enabled."));
    }
    
    @Test
    void assertValidateWhenRemoteHttpAllowedOriginIsMalformed() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(new HttpTransportConfiguration(true, "0.0.0.0", true, "token", 0, "/mcp",
                        Collections.singleton("https://gateway.example.test/path"), Collections.singleton("https://auth.example.test"), Collections.emptyList(), ""),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.allowedOrigins` must use valid HTTP or HTTPS origins."));
    }
    
    @Test
    void assertValidateWhenHttpAuthorizationServersAreMissing() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 0, "/mcp"),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.authorizationServers` must not be empty when HTTP authorization is enabled."));
    }
    
    @Test
    void assertValidateWhenHttpAuthorizationServerIsNotHttps() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(
                        new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 0, "/mcp", Collections.singleton("http://auth.example.test"), Collections.emptyList(), ""),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled."));
    }
    
    @Test
    void assertValidateWhenHttpAuthorizationServerIsMalformed() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(
                        new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 0, "/mcp", Collections.singleton("https://"), Collections.emptyList(), ""),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled."));
    }
    
    @Test
    void assertValidateWhenHttpAuthorizationServerHasFragment() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(
                        new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 0, "/mcp", Collections.singleton("https://auth.example.test#fragment"),
                                Collections.emptyList(), ""),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled."));
    }
    
    @Test
    void assertValidateWhenHttpAuthorizationMetadataIsConfigured() {
        assertDoesNotThrow(() -> new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 0, "/mcp", Collections.singleton("https://auth.example.test"), Collections.singleton("mcp.read"), ""),
                new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
    }
    
    @Test
    void assertValidateWhenRemoteHttpOAuthIntrospectionIsConfigured() {
        assertDoesNotThrow(() -> new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true, "0.0.0.0", true, "", 0, "/mcp", Collections.singleton("https://gateway.example.test"),
                        Collections.singleton("https://auth.example.test"), Collections.singleton("mcp.read"), "",
                        createOAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "", 30000L)),
                new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
    }
    
    @Test
    void assertValidateWhenLocalOAuthIntrospectionEndpointUsesLoopbackHttp() {
        assertDoesNotThrow(() -> new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", false, "", 0, "/mcp", Collections.singleton("https://auth.example.test"), Collections.singleton("mcp.read"), "",
                        createOAuthIntrospectionConfiguration("http://127.0.0.1:19090/introspect", "foo_client", "foo_secret", "https://auth.example.test", 0L)),
                new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
    }
    
    @Test
    void assertValidateWhenAccessTokenAndOAuthIntrospectionAreBothConfigured() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(
                        new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 0, "/mcp", Collections.singleton("https://auth.example.test"),
                                Collections.emptyList(), "", createOAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "", 0L)),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Properties `transport.http.accessToken` and `transport.http.oauthIntrospection.endpoint` cannot both be configured."));
    }
    
    @Test
    void assertValidateWhenOAuthIntrospectionIsInvalid() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(
                        new HttpTransportConfiguration(true, "127.0.0.1", false, "", 0, "/mcp", Collections.singleton("https://auth.example.test"),
                                Collections.emptyList(), "", createOAuthIntrospectionConfiguration("http://auth.example.test/introspect", "foo_client", "foo_secret", "", 0L)),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.oauthIntrospection` must include a valid endpoint, clientId, clientSecret, and non-negative cacheTtlMillis."));
    }
    
    @Test
    void assertValidateWhenOAuthIntrospectionExpectedIssuerIsInvalid() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new MCPLaunchConfiguration(
                        new HttpTransportConfiguration(true, "127.0.0.1", false, "", 0, "/mcp", Collections.singleton("https://auth.example.test"),
                                Collections.emptyList(), "", createOAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "http://auth.example.test", 0L)),
                        new StdioTransportConfiguration(false), Collections.emptyMap()).validate());
        assertThat(actual.getMessage(), is("Property `transport.http.oauthIntrospection` must include a valid endpoint, clientId, clientSecret, and non-negative cacheTtlMillis."));
    }
    
    @Test
    void assertValidateWhenDisabledRemoteHttpDoesNotRequireAccessToken() {
        assertDoesNotThrow(createLaunchConfiguration(false, true, "0.0.0.0", false, "")::validate);
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled, final boolean stdioEnabled) {
        return createLaunchConfiguration(httpEnabled, stdioEnabled, "127.0.0.1", false, "");
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled, final boolean stdioEnabled,
                                                             final String bindHost, final boolean allowRemoteAccess, final String accessToken) {
        return createLaunchConfiguration(httpEnabled, stdioEnabled, bindHost, allowRemoteAccess, accessToken, Collections.emptyList());
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled, final boolean stdioEnabled, final String bindHost,
                                                             final boolean allowRemoteAccess, final String accessToken, final Collection<String> allowedOrigins) {
        return new MCPLaunchConfiguration(new HttpTransportConfiguration(httpEnabled, bindHost, allowRemoteAccess, accessToken, 0, "/mcp", allowedOrigins,
                Collections.singleton("https://auth.example.test"), Collections.emptyList(), ""),
                new StdioTransportConfiguration(stdioEnabled), Collections.emptyMap());
    }
    
    private OAuthIntrospectionConfiguration createOAuthIntrospectionConfiguration(final String endpoint, final String clientId, final String clientSecret,
                                                                                  final String expectedIssuer, final long cacheTtlMillis) {
        return new OAuthIntrospectionConfiguration(endpoint, clientId, clientSecret, expectedIssuer, cacheTtlMillis);
    }
}

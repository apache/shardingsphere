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

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlOAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlStdioTransportConfiguration;
import org.apache.shardingsphere.mcp.support.yaml.MCPYamlConfigurationValidator;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPLaunchConfigurationTest {

    @Test
    void assertValidateWhenHttpTransportEnabled() {
        assertDoesNotThrow(() -> validate(createYamlConfig(true, false)));
    }

    @Test
    void assertValidateWhenStdioTransportEnabled() {
        assertDoesNotThrow(() -> validate(createYamlConfig(false, true)));
    }

    @Test
    void assertValidateWhenBothTransportsEnabled() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(createYamlConfig(true, true)));
        assertThat(actual.getMessage(), is("HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport."));
    }

    @Test
    void assertValidateWhenBothTransportsDisabled() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(createYamlConfig(false, false)));
        assertThat(actual.getMessage(), is("Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }

    @Test
    void assertValidateWhenHttpTransportMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(false, true);
        yamlConfig.getTransport().setHttp(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http` is required."));
    }

    @Test
    void assertValidateWhenStdioTransportMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.getTransport().setStdio(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.stdio` is required."));
    }

    @Test
    void assertValidateWhenDatabasesMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.setRuntimeDatabases(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` is required."));
    }

    @Test
    void assertValidateWhenDatabasesEmpty() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.setRuntimeDatabases(Collections.emptyMap());
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `runtimeDatabases` is required."));
    }

    @Test
    void assertValidateWhenHttpBindHostMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.getTransport().getHttp().setBindHost(null);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.bindHost` is required."));
    }

    @Test
    void assertValidateWhenHttpEndpointPathMissingLeadingSlash() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.getTransport().getHttp().setEndpointPath("mcp");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.endpointPath` must start with '/'."));
    }

    @Test
    void assertValidateWhenRemoteHttpIsNotExplicitlyAllowed() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "0.0.0.0", false, "");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.allowRemoteAccess` must be true when `transport.http.bindHost` is not loopback."));
    }

    @Test
    void assertValidateWhenRemoteHttpAccessTokenIsMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "0.0.0.0", true, "");
        yamlConfig.getTransport().getHttp().setAllowedOrigins(Collections.singletonList("https://gateway.example.test"));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(),
                is("MCP launch configuration property `transport.http.accessToken` or `transport.http.oauthIntrospection.endpoint` must be configured when remote HTTP access is enabled."));
    }

    @Test
    void assertValidateWhenRemoteHttpAllowedOriginsAreMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "0.0.0.0", true, "token");
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.allowedOrigins` must not be empty when remote HTTP access is enabled."));
    }

    @Test
    void assertValidateWhenRemoteHttpAllowedOriginIsMalformed() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "0.0.0.0", true, "token");
        yamlConfig.getTransport().getHttp().setAllowedOrigins(Collections.singletonList("https://gateway.example.test/path"));
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.allowedOrigins` must use valid HTTP or HTTPS origins."));
    }

    @Test
    void assertValidateWhenHttpAuthorizationServersAreMissing() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "127.0.0.1", false, "token");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.authorizationServers` must not be empty when HTTP authorization is enabled."));
    }

    @Test
    void assertValidateWhenHttpAuthorizationServerIsNotHttps() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "127.0.0.1", false, "token");
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("http://auth.example.test"));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled."));
    }

    @Test
    void assertValidateWhenHttpAuthorizationServerIsMalformed() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "127.0.0.1", false, "token");
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://"));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled."));
    }

    @Test
    void assertValidateWhenHttpAuthorizationServerHasFragment() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "127.0.0.1", false, "token");
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test#fragment"));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled."));
    }

    @Test
    void assertValidateWhenHttpAuthorizationMetadataIsConfigured() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "127.0.0.1", false, "token");
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        yamlConfig.getTransport().getHttp().setScopesSupported(Collections.singletonList("mcp.read"));
        assertDoesNotThrow(() -> validate(yamlConfig));
    }

    @Test
    void assertValidateWhenRemoteHttpOAuthIntrospectionIsConfigured() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "0.0.0.0", true, "");
        yamlConfig.getTransport().getHttp().setAllowedOrigins(Collections.singletonList("https://gateway.example.test"));
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        yamlConfig.getTransport().getHttp().setScopesSupported(Collections.singletonList("mcp.read"));
        yamlConfig.getTransport().getHttp().setOauthIntrospection(createYamlOAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "", 30000L));
        assertDoesNotThrow(() -> validate(yamlConfig));
    }

    @Test
    void assertValidateWhenLocalOAuthIntrospectionEndpointUsesLoopbackHttp() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        yamlConfig.getTransport().getHttp().setScopesSupported(Collections.singletonList("mcp.read"));
        yamlConfig.getTransport().getHttp().setOauthIntrospection(createYamlOAuthIntrospectionConfiguration("http://127.0.0.1:19090/introspect", "foo_client", "foo_secret",
                "https://auth.example.test", 0L));
        assertDoesNotThrow(() -> validate(yamlConfig));
    }

    @Test
    void assertValidateWhenAccessTokenAndOAuthIntrospectionAreBothConfigured() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false, "127.0.0.1", false, "token");
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        yamlConfig.getTransport().getHttp().setOauthIntrospection(createYamlOAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "", 0L));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(), is("MCP launch configuration property `transport.http.accessToken` cannot be configured with `transport.http.oauthIntrospection.endpoint`."));
    }

    @Test
    void assertValidateWhenOAuthIntrospectionIsInvalid() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        yamlConfig.getTransport().getHttp().setOauthIntrospection(createYamlOAuthIntrospectionConfiguration("http://auth.example.test/introspect", "foo_client", "foo_secret", "", 0L));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(),
                is("MCP launch configuration property `transport.http.oauthIntrospection` must include a valid endpoint, clientId, clientSecret, and non-negative cacheTtlMillis."));
    }

    @Test
    void assertValidateWhenOAuthIntrospectionExpectedIssuerIsInvalid() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig(true, false);
        yamlConfig.getTransport().getHttp().setAuthorizationServers(Collections.singletonList("https://auth.example.test"));
        yamlConfig.getTransport().getHttp().setOauthIntrospection(createYamlOAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret",
                "http://auth.example.test", 0L));
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> validate(yamlConfig));
        assertThat(actual.getMessage(),
                is("MCP launch configuration property `transport.http.oauthIntrospection` must include a valid endpoint, clientId, clientSecret, and non-negative cacheTtlMillis."));
    }

    @Test
    void assertValidateWhenDisabledRemoteHttpDoesNotRequireAccessToken() {
        assertDoesNotThrow(() -> validate(createYamlConfig(false, true, "0.0.0.0", false, "")));
    }

    private void validate(final YamlMCPLaunchConfiguration yamlConfig) {
        MCPYamlConfigurationValidator.validate(yamlConfig, "MCP launch configuration");
    }

    private YamlMCPLaunchConfiguration createYamlConfig(final boolean httpEnabled, final boolean stdioEnabled) {
        return createYamlConfig(httpEnabled, stdioEnabled, "127.0.0.1", false, "");
    }

    private YamlMCPLaunchConfiguration createYamlConfig(final boolean httpEnabled, final boolean stdioEnabled, final String bindHost, final boolean allowRemoteAccess, final String accessToken) {
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(createYamlTransportConfiguration(httpEnabled, stdioEnabled, bindHost, allowRemoteAccess, accessToken));
        result.setRuntimeDatabases(createRuntimeDatabases());
        return result;
    }

    private YamlMCPTransportConfiguration createYamlTransportConfiguration(final boolean httpEnabled, final boolean stdioEnabled, final String bindHost, final boolean allowRemoteAccess,
                                                                          final String accessToken) {
        YamlMCPTransportConfiguration result = new YamlMCPTransportConfiguration();
        result.setHttp(createYamlHttpTransportConfiguration(httpEnabled, bindHost, allowRemoteAccess, accessToken));
        result.setStdio(createYamlStdioTransportConfiguration(stdioEnabled));
        return result;
    }

    private YamlHttpTransportConfiguration createYamlHttpTransportConfiguration(final boolean enabled, final String bindHost, final boolean allowRemoteAccess, final String accessToken) {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setEnabled(enabled);
        result.setBindHost(bindHost);
        result.setAllowRemoteAccess(allowRemoteAccess);
        result.setAccessToken(accessToken);
        result.setPort(0);
        result.setEndpointPath("/mcp");
        return result;
    }

    private YamlStdioTransportConfiguration createYamlStdioTransportConfiguration(final boolean enabled) {
        YamlStdioTransportConfiguration result = new YamlStdioTransportConfiguration();
        result.setEnabled(enabled);
        return result;
    }

    private YamlOAuthIntrospectionConfiguration createYamlOAuthIntrospectionConfiguration(final String endpoint, final String clientId, final String clientSecret,
                                                                                         final String expectedIssuer, final Long cacheTtlMillis) {
        YamlOAuthIntrospectionConfiguration result = new YamlOAuthIntrospectionConfiguration();
        result.setEndpoint(endpoint);
        result.setClientId(clientId);
        result.setClientSecret(clientSecret);
        result.setExpectedIssuer(expectedIssuer);
        result.setCacheTtlMillis(cacheTtlMillis);
        return result;
    }

    private Map<String, Map<String, Object>> createRuntimeDatabases() {
        return Collections.singletonMap("logic_db", Map.of(
                "databaseType", "H2",
                "jdbcUrl", "jdbc:h2:mem:logic",
                "username", "",
                "password", "",
                "driverClassName", ""));
    }
}

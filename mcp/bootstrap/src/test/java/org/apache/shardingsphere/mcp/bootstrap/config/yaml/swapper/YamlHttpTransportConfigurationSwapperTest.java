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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlOAuthIntrospectionConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlHttpTransportConfigurationSwapperTest {
    
    private final YamlHttpTransportConfigurationSwapper swapper = new YamlHttpTransportConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("127.0.0.1", false, "", 18088, "/mcp"));
        assertTrue(actual.isEnabled());
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertFalse(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is(""));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
        assertThat(actual.getAllowedOrigins(), is(List.of()));
        assertThat(actual.getAuthorizationServers(), is(List.of()));
        assertThat(actual.getScopesSupported(), is(List.of()));
        assertThat(actual.getProtectedResource(), is(""));
        assertThat(actual.getOauthIntrospection().getEndpoint(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithOmittedEnabled() {
        YamlHttpTransportConfiguration yamlConfig = new YamlHttpTransportConfiguration();
        yamlConfig.setBindHost("127.0.0.1");
        yamlConfig.setPort(18088);
        yamlConfig.setEndpointPath("/mcp");
        HttpTransportConfiguration actual = swapper.swapToObject(yamlConfig);
        assertFalse(actual.isEnabled());
    }
    
    @Test
    void assertSwapToObjectWithMissingBindHost() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig(null, false, "", 18088, "/mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `bindHost` is required."));
    }
    
    @Test
    void assertSwapToObjectWithLocalhostBindHost() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("localhost", false, "", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("localhost"));
    }
    
    @Test
    void assertSwapToObjectWithRemoteBindHost() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", false, "", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertFalse(actual.isAllowRemoteAccess());
    }
    
    @Test
    void assertSwapToObjectWithAllowedRemoteAccess() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", true, "foo_token", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertTrue(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is("foo_token"));
    }
    
    @Test
    void assertSwapToObjectWithAccessTokenEnvironmentPlaceholder() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", true, "${MCP_ACCESS_TOKEN}", 18088, "/mcp"),
                Map.of("MCP_ACCESS_TOKEN", "foo_token"));
        assertThat(actual.getAccessToken(), is("foo_token"));
    }
    
    @Test
    void assertSwapToObjectWithAllowedOrigins() {
        YamlHttpTransportConfiguration yamlConfig = createYamlConfig("0.0.0.0", true, "foo_token", 18088, "/mcp");
        yamlConfig.setAllowedOrigins(List.of(" https://gateway.example.test ", "", "${MCP_ALLOWED_ORIGIN}"));
        HttpTransportConfiguration actual = swapper.swapToObject(yamlConfig, Map.of("MCP_ALLOWED_ORIGIN", "https://console.example.test"));
        assertThat(actual.getAllowedOrigins(), is(List.of("https://gateway.example.test", "https://console.example.test")));
    }
    
    @Test
    void assertSwapToObjectWithAuthorizationMetadata() {
        YamlHttpTransportConfiguration yamlConfig = createYamlConfig("127.0.0.1", false, "foo_token", 18088, "/mcp");
        yamlConfig.setAuthorizationServers(List.of(" https://auth.example.test ", ""));
        yamlConfig.setScopesSupported(List.of("mcp.read", "mcp.write"));
        yamlConfig.setProtectedResource("${MCP_RESOURCE}");
        HttpTransportConfiguration actual = swapper.swapToObject(yamlConfig, Map.of("MCP_RESOURCE", "https://gateway.example.test/mcp"));
        assertThat(actual.getAuthorizationServers(), is(List.of("https://auth.example.test")));
        assertThat(actual.getScopesSupported(), is(List.of("mcp.read", "mcp.write")));
        assertThat(actual.getProtectedResource(), is("https://gateway.example.test/mcp"));
    }
    
    @Test
    void assertSwapToObjectWithOAuthIntrospection() {
        YamlHttpTransportConfiguration yamlConfig = createYamlConfig("0.0.0.0", true, "", 18088, "/mcp");
        yamlConfig.setAuthorizationServers(List.of("https://auth.example.test"));
        yamlConfig.setScopesSupported(List.of("mcp.read"));
        yamlConfig.setOauthIntrospection(createYamlOAuthIntrospectionConfiguration("${MCP_INTROSPECTION_ENDPOINT}", "${MCP_CLIENT_ID}", "${MCP_CLIENT_SECRET}",
                "${MCP_EXPECTED_ISSUER}", 30000L));
        HttpTransportConfiguration actual = swapper.swapToObject(yamlConfig, Map.of(
                "MCP_INTROSPECTION_ENDPOINT", "https://auth.example.test/introspect",
                "MCP_CLIENT_ID", "foo_client",
                "MCP_CLIENT_SECRET", "foo_secret",
                "MCP_EXPECTED_ISSUER", "https://auth.example.test"));
        assertTrue(actual.getOauthIntrospection().isEnabled());
        assertThat(actual.getOauthIntrospection().getEndpoint(), is("https://auth.example.test/introspect"));
        assertThat(actual.getOauthIntrospection().getClientId(), is("foo_client"));
        assertThat(actual.getOauthIntrospection().getClientSecret(), is("foo_secret"));
        assertThat(actual.getOauthIntrospection().getExpectedIssuer(), is("https://auth.example.test"));
        assertThat(actual.getOauthIntrospection().getCacheTtlMillis(), is(30000L));
    }
    
    @Test
    void assertSwapToObjectWithMissingAccessTokenEnvironmentPlaceholder() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig("0.0.0.0", true, "${MCP_ACCESS_TOKEN}", 18088, "/mcp"), Map.of()));
        assertThat(actual.getMessage(), is("Environment variable `MCP_ACCESS_TOKEN` referenced by property `transport.http.accessToken` is not set."));
    }
    
    @Test
    void assertSwapToObjectWithAllowedRemoteAccessAndMissingAccessToken() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", true, "", 18088, "/mcp"));
        assertTrue(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithAllowedRemoteAccessAndBlankAccessToken() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", true, "   ", 18088, "/mcp"));
        assertThat(actual.getAccessToken(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithDisabledRemoteHttpWithoutAccessToken() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig(false, "0.0.0.0", false, "", 18088, "/mcp"));
        assertFalse(actual.isEnabled());
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertFalse(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithNegativePort() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig("127.0.0.1", false, "", -1, "/mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `port` must be zero or positive."));
    }
    
    @Test
    void assertSwapToObjectWithEndpointPathMissingLeadingSlash() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig("127.0.0.1", false, "", 18088, "gateway")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `endpointPath` must start with '/'."));
    }
    
    @Test
    void assertSwapToObjectWithMissingPort() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig("127.0.0.1", false, "", null, "/mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `port` is required."));
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(null));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration cannot be null."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlHttpTransportConfiguration actual = swapper.swapToYamlConfiguration(new HttpTransportConfiguration(true, "127.0.0.1", false, "token", 18088, "/mcp",
                List.of("https://gateway.example.test"), List.of("https://auth.example.test"), List.of("mcp.read"), "https://gateway.example.test/mcp", new OAuthIntrospectionConfiguration()));
        assertTrue(actual.isEnabled());
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertFalse(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is("token"));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
        assertThat(actual.getAllowedOrigins(), is(List.of("https://gateway.example.test")));
        assertThat(actual.getAuthorizationServers(), is(List.of("https://auth.example.test")));
        assertThat(actual.getScopesSupported(), is(List.of("mcp.read")));
        assertThat(actual.getProtectedResource(), is("https://gateway.example.test/mcp"));
        assertThat(actual.getOauthIntrospection().getEndpoint(), is(""));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithOAuthIntrospection() {
        HttpTransportConfiguration data = new HttpTransportConfiguration(true, "127.0.0.1", false, "", 18088, "/mcp", Collections.emptyList(), List.of("https://auth.example.test"),
                List.of("mcp.read"), "https://gateway.example.test/mcp",
                new OAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "https://auth.example.test", 30000L));
        YamlHttpTransportConfiguration actual = swapper.swapToYamlConfiguration(data);
        assertThat(actual.getOauthIntrospection().getEndpoint(), is("https://auth.example.test/introspect"));
        assertThat(actual.getOauthIntrospection().getClientId(), is("foo_client"));
        assertThat(actual.getOauthIntrospection().getClientSecret(), is("foo_secret"));
        assertThat(actual.getOauthIntrospection().getExpectedIssuer(), is("https://auth.example.test"));
        assertThat(actual.getOauthIntrospection().getCacheTtlMillis(), is(30000L));
    }
    
    private YamlHttpTransportConfiguration createYamlConfig(final String bindHost, final boolean allowRemoteAccess, final String accessToken, final Integer port, final String endpointPath) {
        return createYamlConfig(true, bindHost, allowRemoteAccess, accessToken, port, endpointPath);
    }
    
    private YamlHttpTransportConfiguration createYamlConfig(final boolean enabled, final String bindHost, final boolean allowRemoteAccess,
                                                            final String accessToken, final Integer port, final String endpointPath) {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setEnabled(enabled);
        result.setBindHost(bindHost);
        result.setAllowRemoteAccess(allowRemoteAccess);
        result.setAccessToken(accessToken);
        result.setPort(port);
        result.setEndpointPath(endpointPath);
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
}

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

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlOAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.support.yaml.MCPYamlConfigurationValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * YAML HTTP transport configuration swapper.
 */
public final class YamlHttpTransportConfigurationSwapper implements YamlConfigurationSwapper<YamlHttpTransportConfiguration, HttpTransportConfiguration> {
    
    @Override
    public YamlHttpTransportConfiguration swapToYamlConfiguration(final HttpTransportConfiguration data) {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setEnabled(data.isEnabled());
        result.setBindHost(data.getBindHost());
        result.setAllowRemoteAccess(data.isAllowRemoteAccess());
        result.setAccessToken(data.getAccessToken());
        result.setPort(data.getPort());
        result.setEndpointPath(data.getEndpointPath());
        result.setAllowedOrigins(data.getAllowedOrigins());
        result.setAuthorizationServers(data.getAuthorizationServers());
        result.setScopesSupported(data.getScopesSupported());
        result.setProtectedResource(data.getProtectedResource());
        result.setOauthIntrospection(swapOAuthIntrospectionToYamlConfiguration(data.getOauthIntrospection()));
        return result;
    }
    
    private YamlOAuthIntrospectionConfiguration swapOAuthIntrospectionToYamlConfiguration(final OAuthIntrospectionConfiguration data) {
        YamlOAuthIntrospectionConfiguration result = new YamlOAuthIntrospectionConfiguration();
        result.setEndpoint(data.getEndpoint());
        result.setClientId(data.getClientId());
        result.setClientSecret(data.getClientSecret());
        result.setExpectedIssuer(data.getExpectedIssuer());
        result.setCacheTtlMillis(data.getCacheTtlMillis());
        return result;
    }
    
    @Override
    public HttpTransportConfiguration swapToObject(final YamlHttpTransportConfiguration yamlConfig) {
        return swapToObject(yamlConfig, System.getenv());
    }
    
    HttpTransportConfiguration swapToObject(final YamlHttpTransportConfiguration yamlConfig, final Map<String, String> environment) {
        MCPYamlConfigurationValidator.validate(yamlConfig, "MCP HTTP transport configuration");
        boolean allowRemoteAccess = yamlConfig.isAllowRemoteAccess();
        String accessToken = resolveAccessToken(yamlConfig.getAccessToken(), environment);
        return new HttpTransportConfiguration(yamlConfig.isEnabled(), yamlConfig.getBindHost(), allowRemoteAccess, accessToken, yamlConfig.getPort(), yamlConfig.getEndpointPath(),
                resolveTextList(yamlConfig.getAllowedOrigins(), "transport.http.allowedOrigins", environment),
                resolveTextList(yamlConfig.getAuthorizationServers(), "transport.http.authorizationServers", environment),
                resolveTextList(yamlConfig.getScopesSupported(), "transport.http.scopesSupported", environment),
                Objects.toString(YamlEnvironmentPlaceholderUtils.resolve(yamlConfig.getProtectedResource(), "transport.http.protectedResource", environment), "").trim(),
                swapOAuthIntrospectionToObject(yamlConfig.getOauthIntrospection(), environment));
    }
    
    private OAuthIntrospectionConfiguration swapOAuthIntrospectionToObject(final YamlOAuthIntrospectionConfiguration yamlConfig, final Map<String, String> environment) {
        if (null == yamlConfig) {
            return new OAuthIntrospectionConfiguration();
        }
        return new OAuthIntrospectionConfiguration(resolveOptionalText(yamlConfig.getEndpoint(), "transport.http.oauthIntrospection.endpoint", environment),
                resolveOptionalText(yamlConfig.getClientId(), "transport.http.oauthIntrospection.clientId", environment),
                resolveOptionalText(yamlConfig.getClientSecret(), "transport.http.oauthIntrospection.clientSecret", environment),
                resolveOptionalText(yamlConfig.getExpectedIssuer(), "transport.http.oauthIntrospection.expectedIssuer", environment),
                null == yamlConfig.getCacheTtlMillis() ? 0L : yamlConfig.getCacheTtlMillis());
    }
    
    private String resolveAccessToken(final String accessToken, final Map<String, String> environment) {
        return Objects.toString(YamlEnvironmentPlaceholderUtils.resolve(accessToken, "transport.http.accessToken", environment), "").trim();
    }
    
    private String resolveOptionalText(final String value, final String propertyName, final Map<String, String> environment) {
        return Objects.toString(YamlEnvironmentPlaceholderUtils.resolve(value, propertyName, environment), "").trim();
    }
    
    private Collection<String> resolveTextList(final Collection<String> values, final String propertyName, final Map<String, String> environment) {
        if (null == values) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(each -> Objects.toString(YamlEnvironmentPlaceholderUtils.resolve(each, propertyName, environment), "").trim())
                .filter(each -> !each.isEmpty()).toList();
    }
}

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
import java.util.List;
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
        MCPYamlConfigurationValidator.validate(yamlConfig, "MCP HTTP transport configuration");
        return new HttpTransportConfiguration(yamlConfig.isEnabled(), yamlConfig.getBindHost(), yamlConfig.isAllowRemoteAccess(), trimOptionalText(yamlConfig.getAccessToken()),
                yamlConfig.getPort(), yamlConfig.getEndpointPath(), trimTextList(yamlConfig.getAllowedOrigins()), trimTextList(yamlConfig.getAuthorizationServers()),
                trimTextList(yamlConfig.getScopesSupported()), trimOptionalText(yamlConfig.getProtectedResource()), swapOAuthIntrospectionToObject(yamlConfig.getOauthIntrospection()));
    }
    
    private OAuthIntrospectionConfiguration swapOAuthIntrospectionToObject(final YamlOAuthIntrospectionConfiguration yamlConfig) {
        if (null == yamlConfig) {
            return new OAuthIntrospectionConfiguration();
        }
        return new OAuthIntrospectionConfiguration(trimOptionalText(yamlConfig.getEndpoint()), trimOptionalText(yamlConfig.getClientId()), trimOptionalText(yamlConfig.getClientSecret()),
                trimOptionalText(yamlConfig.getExpectedIssuer()), null == yamlConfig.getCacheTtlMillis() ? 0L : yamlConfig.getCacheTtlMillis());
    }
    
    private String trimOptionalText(final String value) {
        return Objects.toString(value, "").trim();
    }
    
    private List<String> trimTextList(final Collection<String> values) {
        return values.stream().map(each -> Objects.toString(each, "").trim()).filter(each -> !each.isEmpty()).toList();
    }
}

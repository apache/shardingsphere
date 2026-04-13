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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;

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
        return result;
    }
    
    @Override
    public HttpTransportConfiguration swapToObject(final YamlHttpTransportConfiguration yamlConfig) {
        ShardingSpherePreconditions.checkNotNull(yamlConfig, () -> new IllegalArgumentException("Property `transport.http` is required."));
        String bindHost = resolveBindHost(yamlConfig.getBindHost());
        boolean allowRemoteAccess = yamlConfig.isAllowRemoteAccess();
        String accessToken = resolveAccessToken(yamlConfig.getAccessToken());
        validateRemoteExposurePolicy(bindHost, allowRemoteAccess, accessToken);
        return new HttpTransportConfiguration(yamlConfig.isEnabled(), bindHost, allowRemoteAccess, accessToken, resolvePort(yamlConfig.getPort()),
                resolveEndpointPath(yamlConfig.getEndpointPath()));
    }
    
    private String resolveBindHost(final String bindHost) {
        return resolveRequiredText(bindHost, "transport.http.bindHost");
    }
    
    private String resolveRequiredText(final String value, final String propertyName) {
        ShardingSpherePreconditions.checkNotNull(value, () -> new IllegalArgumentException(String.format("Property `%s` is required.", propertyName)));
        ShardingSpherePreconditions.checkState(!value.isBlank(), () -> new IllegalArgumentException(String.format("Property `%s` cannot be blank.", propertyName)));
        return value;
    }
    
    private String resolveAccessToken(final String accessToken) {
        return Objects.toString(accessToken, "").trim();
    }
    
    private void validateRemoteExposurePolicy(final String bindHost, final boolean allowRemoteAccess, final String accessToken) {
        ShardingSpherePreconditions.checkState(allowRemoteAccess || HttpTransportHostUtils.isLoopbackHost(bindHost),
                () -> new IllegalArgumentException("Property `transport.http.allowRemoteAccess` must be true when `transport.http.bindHost` is not loopback."));
        ShardingSpherePreconditions.checkState(HttpTransportHostUtils.isLoopbackHost(bindHost) || !accessToken.isEmpty(),
                () -> new IllegalArgumentException("Property `transport.http.accessToken` must not be blank when remote HTTP access is enabled."));
    }
    
    private int resolvePort(final Integer port) {
        ShardingSpherePreconditions.checkNotNull(port, () -> new IllegalArgumentException("Property `transport.http.port` is required."));
        ShardingSpherePreconditions.checkState(port >= 0, () -> new IllegalArgumentException("Property `transport.http.port` cannot be negative."));
        return port;
    }
    
    private String resolveEndpointPath(final String endpointPath) {
        String result = resolveRequiredText(endpointPath, "transport.http.endpointPath");
        ShardingSpherePreconditions.checkState(result.startsWith("/"), () -> new IllegalArgumentException("Property `transport.http.endpointPath` must start with '/'."));
        return result;
    }
}

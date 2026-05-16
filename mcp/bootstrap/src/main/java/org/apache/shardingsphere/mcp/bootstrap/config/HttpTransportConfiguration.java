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

import lombok.Getter;
import org.apache.shardingsphere.mcp.bootstrap.config.validator.ValidHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.support.configuration.MCPConfigurationValidator;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * HTTP transport configuration.
 */
@Getter
@ValidHttpTransportConfiguration
public final class HttpTransportConfiguration {
    
    private final boolean enabled;
    
    @NotBlank(message = "is required")
    private final String bindHost;
    
    private final boolean allowRemoteAccess;
    
    private final String accessToken;
    
    @PositiveOrZero(message = "must be zero or positive")
    private final int port;
    
    @NotBlank(message = "is required")
    @Pattern(regexp = "/.*", message = "must start with '/'")
    private final String endpointPath;
    
    private final List<String> allowedOrigins;
    
    private final List<String> authorizationServers;
    
    private final List<String> scopesSupported;
    
    private final String protectedResource;
    
    @NotNull(message = "is required")
    @Valid
    private final OAuthIntrospectionConfiguration oauthIntrospection;
    
    public HttpTransportConfiguration(final boolean enabled, final String bindHost, final boolean allowRemoteAccess, final String accessToken, final int port,
                                      final String endpointPath) {
        this(enabled, bindHost, allowRemoteAccess, accessToken, port, endpointPath, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "",
                new OAuthIntrospectionConfiguration());
    }
    
    public HttpTransportConfiguration(final boolean enabled, final String bindHost, final boolean allowRemoteAccess, final String accessToken, final int port,
                                      final String endpointPath, final Collection<String> authorizationServers, final Collection<String> scopesSupported,
                                      final String protectedResource) {
        this(enabled, bindHost, allowRemoteAccess, accessToken, port, endpointPath, Collections.emptyList(), authorizationServers, scopesSupported, protectedResource,
                new OAuthIntrospectionConfiguration());
    }
    
    public HttpTransportConfiguration(final boolean enabled, final String bindHost, final boolean allowRemoteAccess, final String accessToken, final int port,
                                      final String endpointPath, final Collection<String> authorizationServers, final Collection<String> scopesSupported,
                                      final String protectedResource, final OAuthIntrospectionConfiguration oauthIntrospection) {
        this(enabled, bindHost, allowRemoteAccess, accessToken, port, endpointPath, Collections.emptyList(), authorizationServers, scopesSupported, protectedResource,
                oauthIntrospection);
    }
    
    public HttpTransportConfiguration(final boolean enabled, final String bindHost, final boolean allowRemoteAccess, final String accessToken, final int port,
                                      final String endpointPath, final Collection<String> allowedOrigins, final Collection<String> authorizationServers,
                                      final Collection<String> scopesSupported, final String protectedResource) {
        this(enabled, bindHost, allowRemoteAccess, accessToken, port, endpointPath, allowedOrigins, authorizationServers, scopesSupported, protectedResource,
                new OAuthIntrospectionConfiguration());
    }
    
    public HttpTransportConfiguration(final boolean enabled, final String bindHost, final boolean allowRemoteAccess, final String accessToken, final int port,
                                      final String endpointPath, final Collection<String> allowedOrigins, final Collection<String> authorizationServers,
                                      final Collection<String> scopesSupported, final String protectedResource, final OAuthIntrospectionConfiguration oauthIntrospection) {
        this.enabled = enabled;
        this.bindHost = bindHost;
        this.allowRemoteAccess = allowRemoteAccess;
        this.accessToken = accessToken;
        this.port = port;
        this.endpointPath = endpointPath;
        this.allowedOrigins = createTextList(allowedOrigins);
        this.authorizationServers = createTextList(authorizationServers);
        this.scopesSupported = createTextList(scopesSupported);
        this.protectedResource = Objects.toString(protectedResource, "").trim();
        this.oauthIntrospection = oauthIntrospection;
    }
    
    /**
     * Validate HTTP transport configuration.
     */
    public void validate() {
        MCPConfigurationValidator.validate(this, "MCP HTTP transport configuration");
    }
    
    /**
     * Judge whether OAuth protected resource metadata is enabled.
     *
     * @return OAuth protected resource metadata is enabled or not
     */
    public boolean isProtectedResourceMetadataEnabled() {
        return !authorizationServers.isEmpty();
    }
    
    private List<String> createTextList(final Collection<String> values) {
        return null == values ? Collections.emptyList() : values.stream().map(each -> Objects.toString(each, "").trim()).filter(each -> !each.isEmpty()).toList();
    }
}

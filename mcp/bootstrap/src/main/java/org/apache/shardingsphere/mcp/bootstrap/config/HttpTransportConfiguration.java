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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportOriginUtils;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * HTTP transport configuration.
 */
@Getter
public final class HttpTransportConfiguration {

    private final boolean enabled;

    private final String bindHost;

    private final boolean allowRemoteAccess;

    private final String accessToken;

    private final int port;

    private final String endpointPath;

    private final List<String> allowedOrigins;

    private final List<String> authorizationServers;

    private final List<String> scopesSupported;

    private final String protectedResource;

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
        this.oauthIntrospection = null == oauthIntrospection ? new OAuthIntrospectionConfiguration() : oauthIntrospection;
    }

    /**
     * Validate HTTP transport configuration.
     */
    public void validate() {
        if (!enabled) {
            return;
        }
        boolean loopbackBinding = isLoopbackBinding();
        ShardingSpherePreconditions.checkState(allowRemoteAccess || loopbackBinding,
                () -> new IllegalArgumentException("Property `transport.http.allowRemoteAccess` must be true when `transport.http.bindHost` is not loopback."));
        ShardingSpherePreconditions.checkState(allowedOrigins.isEmpty() || hasValidAllowedOrigins(),
                () -> new IllegalArgumentException("Property `transport.http.allowedOrigins` must use valid HTTP or HTTPS origins."));
        ShardingSpherePreconditions.checkState(loopbackBinding || hasAllowedOrigins(),
                () -> new IllegalArgumentException("Property `transport.http.allowedOrigins` must not be empty when remote HTTP access is enabled."));
        ShardingSpherePreconditions.checkState(loopbackBinding || hasAuthorization(),
                () -> new IllegalArgumentException("HTTP authorization must be configured when remote HTTP access is enabled."));
        ShardingSpherePreconditions.checkState(!hasAccessToken() || !oauthIntrospection.isEnabled(),
                () -> new IllegalArgumentException("Properties `transport.http.accessToken` and `transport.http.oauthIntrospection.endpoint` cannot both be configured."));
        ShardingSpherePreconditions.checkState(!hasAuthorization() || hasAuthorizationServers(),
                () -> new IllegalArgumentException("Property `transport.http.authorizationServers` must not be empty when HTTP authorization is enabled."));
        ShardingSpherePreconditions.checkState(!hasAuthorization() || hasHttpsAuthorizationServers(),
                () -> new IllegalArgumentException("Property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled."));
        ShardingSpherePreconditions.checkState(!oauthIntrospection.isEnabled() || hasValidOAuthIntrospection(),
                () -> new IllegalArgumentException("Property `transport.http.oauthIntrospection` must include a valid endpoint, clientId, clientSecret, and non-negative cacheTtlMillis."));
    }

    /**
     * Judge whether OAuth protected resource metadata is enabled.
     *
     * @return OAuth protected resource metadata is enabled or not
     */
    public boolean isProtectedResourceMetadataEnabled() {
        return hasAuthorizationServers();
    }

    private boolean isLoopbackBinding() {
        return HttpTransportHostUtils.isLoopbackHost(bindHost);
    }

    private boolean hasAccessToken() {
        return !Objects.toString(accessToken, "").trim().isEmpty();
    }

    private boolean hasAllowedOrigins() {
        return !allowedOrigins.isEmpty();
    }

    private boolean hasValidAllowedOrigins() {
        return allowedOrigins.stream().allMatch(HttpTransportOriginUtils::isValidOrigin);
    }

    private boolean hasAuthorization() {
        return hasAccessToken() || oauthIntrospection.isEnabled();
    }

    private boolean hasAuthorizationServers() {
        return !authorizationServers.isEmpty();
    }

    private boolean hasHttpsAuthorizationServers() {
        return authorizationServers.stream().allMatch(this::isHttpsAuthorizationServer);
    }

    private boolean isHttpsAuthorizationServer(final String value) {
        try {
            URI uri = URI.create(value);
            return "https".equals(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT)) && !Objects.toString(uri.getHost(), "").isEmpty()
                    && null == uri.getRawFragment();
        } catch (final IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean hasValidOAuthIntrospection() {
        return isValidIntrospectionEndpoint(oauthIntrospection.getEndpoint()) && !oauthIntrospection.getClientId().isEmpty() && !oauthIntrospection.getClientSecret().isEmpty()
                && oauthIntrospection.getCacheTtlMillis() >= 0L && isValidExpectedIssuer();
    }

    private boolean isValidIntrospectionEndpoint(final String value) {
        try {
            URI uri = URI.create(value);
            boolean https = "https".equals(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT));
            boolean loopbackHttp = "http".equals(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT)) && HttpTransportHostUtils.isLoopbackHost(uri.getHost());
            return (https || loopbackHttp) && !Objects.toString(uri.getHost(), "").isEmpty() && null == uri.getRawFragment();
        } catch (final IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean isValidExpectedIssuer() {
        return oauthIntrospection.getExpectedIssuer().isEmpty() || isHttpsAuthorizationServer(oauthIntrospection.getExpectedIssuer());
    }

    private List<String> createTextList(final Collection<String> values) {
        return null == values ? Collections.emptyList() : values.stream().map(each -> Objects.toString(each, "").trim()).filter(each -> !each.isEmpty()).toList();
    }
}

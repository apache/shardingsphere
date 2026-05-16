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

package org.apache.shardingsphere.mcp.bootstrap.config.validator;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportOriginUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * HTTP transport configuration validator.
 */
public final class HttpTransportConfigurationValidator implements ConstraintValidator<ValidHttpTransportConfiguration, HttpTransportConfiguration> {
    
    @Override
    public boolean isValid(final HttpTransportConfiguration value, final ConstraintValidatorContext context) {
        if (null == value || !value.isEnabled() || !hasValidBasicProperties(value)) {
            return true;
        }
        return validateRemoteAccess(value, context)
                && validateAuthorization(value, context)
                && validateOAuthIntrospection(value, context);
    }
    
    private boolean hasValidBasicProperties(final HttpTransportConfiguration config) {
        return !Objects.toString(config.getBindHost(), "").isBlank()
                && !Objects.toString(config.getEndpointPath(), "").isBlank()
                && config.getEndpointPath().startsWith("/")
                && null != config.getOauthIntrospection()
                && config.getOauthIntrospection().getCacheTtlMillis() >= 0L;
    }
    
    private boolean validateRemoteAccess(final HttpTransportConfiguration config, final ConstraintValidatorContext context) {
        if (!config.isAllowRemoteAccess() && !isLoopbackBinding(config)) {
            addViolation(context, "Property `transport.http.allowRemoteAccess` must be true when `transport.http.bindHost` is not loopback.");
            return false;
        }
        if (!config.getAllowedOrigins().isEmpty() && !hasValidAllowedOrigins(config)) {
            addViolation(context, "Property `transport.http.allowedOrigins` must use valid HTTP or HTTPS origins.");
            return false;
        }
        if (!isLoopbackBinding(config) && config.getAllowedOrigins().isEmpty()) {
            addViolation(context, "Property `transport.http.allowedOrigins` must not be empty when remote HTTP access is enabled.");
            return false;
        }
        return true;
    }
    
    private boolean validateAuthorization(final HttpTransportConfiguration config, final ConstraintValidatorContext context) {
        if (!isLoopbackBinding(config) && !hasAuthorization(config)) {
            addViolation(context, "HTTP authorization must be configured when remote HTTP access is enabled.");
            return false;
        }
        if (hasAccessToken(config) && config.getOauthIntrospection().isEnabled()) {
            addViolation(context, "Properties `transport.http.accessToken` and `transport.http.oauthIntrospection.endpoint` cannot both be configured.");
            return false;
        }
        if (hasAuthorization(config) && config.getAuthorizationServers().isEmpty()) {
            addViolation(context, "Property `transport.http.authorizationServers` must not be empty when HTTP authorization is enabled.");
            return false;
        }
        if (hasAuthorization(config) && !hasHttpsAuthorizationServers(config)) {
            addViolation(context, "Property `transport.http.authorizationServers` must use valid HTTPS URLs when HTTP authorization is enabled.");
            return false;
        }
        return true;
    }
    
    private boolean validateOAuthIntrospection(final HttpTransportConfiguration config, final ConstraintValidatorContext context) {
        OAuthIntrospectionConfiguration oauthIntrospection = config.getOauthIntrospection();
        if (!oauthIntrospection.isEnabled() || hasValidOAuthIntrospection(oauthIntrospection)) {
            return true;
        }
        addViolation(context, "Property `transport.http.oauthIntrospection` must include a valid endpoint, clientId, clientSecret, and non-negative cacheTtlMillis.");
        return false;
    }
    
    private boolean isLoopbackBinding(final HttpTransportConfiguration config) {
        return HttpTransportHostUtils.isLoopbackHost(config.getBindHost());
    }
    
    private boolean hasAccessToken(final HttpTransportConfiguration config) {
        return !Objects.toString(config.getAccessToken(), "").trim().isEmpty();
    }
    
    private boolean hasValidAllowedOrigins(final HttpTransportConfiguration config) {
        return config.getAllowedOrigins().stream().allMatch(HttpTransportOriginUtils::isValidOrigin);
    }
    
    private boolean hasAuthorization(final HttpTransportConfiguration config) {
        return hasAccessToken(config) || config.getOauthIntrospection().isEnabled();
    }
    
    private boolean hasHttpsAuthorizationServers(final HttpTransportConfiguration config) {
        return config.getAuthorizationServers().stream().allMatch(this::isHttpsAuthorizationServer);
    }
    
    private boolean hasValidOAuthIntrospection(final OAuthIntrospectionConfiguration config) {
        return isValidIntrospectionEndpoint(config.getEndpoint()) && !config.getClientId().isEmpty() && !config.getClientSecret().isEmpty()
                && config.getCacheTtlMillis() >= 0L && (config.getExpectedIssuer().isEmpty() || isHttpsAuthorizationServer(config.getExpectedIssuer()));
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
    
    private boolean isHttpsAuthorizationServer(final String value) {
        try {
            URI uri = URI.create(value);
            return "https".equals(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT)) && !Objects.toString(uri.getHost(), "").isEmpty()
                    && null == uri.getRawFragment();
        } catch (final IllegalArgumentException ignored) {
            return false;
        }
    }
    
    private void addViolation(final ConstraintValidatorContext context, final String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

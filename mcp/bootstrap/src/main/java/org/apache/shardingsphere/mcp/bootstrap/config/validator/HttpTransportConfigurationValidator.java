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

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlOAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportHostUtils;
import org.apache.shardingsphere.mcp.bootstrap.transport.HttpTransportOriginUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * HTTP transport configuration validator.
 */
public final class HttpTransportConfigurationValidator implements ConstraintValidator<ValidHttpTransportConfiguration, YamlHttpTransportConfiguration> {

    @Override
    public boolean isValid(final YamlHttpTransportConfiguration value, final ConstraintValidatorContext context) {
        if (null == value || !value.isEnabled() || !hasValidBasicProperties(value)) {
            return true;
        }
        return validateRemoteAccess(value, context)
                && validateAuthorization(value, context)
                && validateOAuthIntrospection(value, context);
    }

    private boolean hasValidBasicProperties(final YamlHttpTransportConfiguration config) {
        return !Objects.toString(config.getBindHost(), "").isBlank()
                && !Objects.toString(config.getEndpointPath(), "").isBlank()
                && config.getEndpointPath().startsWith("/");
    }

    private boolean validateRemoteAccess(final YamlHttpTransportConfiguration config, final ConstraintValidatorContext context) {
        Collection<String> configuredAllowedOrigins = getConfiguredValues(config.getAllowedOrigins());
        if (!config.isAllowRemoteAccess() && !isLoopbackBinding(config)) {
            addViolation(context, "allowRemoteAccess", "must be true when `transport.http.bindHost` is not loopback");
            return false;
        }
        if (!configuredAllowedOrigins.isEmpty() && !hasValidAllowedOrigins(config)) {
            addViolation(context, "allowedOrigins", "must use valid HTTP or HTTPS origins");
            return false;
        }
        if (!isLoopbackBinding(config) && configuredAllowedOrigins.isEmpty()) {
            addViolation(context, "allowedOrigins", "must not be empty when remote HTTP access is enabled");
            return false;
        }
        return true;
    }

    private boolean validateAuthorization(final YamlHttpTransportConfiguration config, final ConstraintValidatorContext context) {
        boolean authorizationConfigured = hasAuthorization(config);
        if (!isLoopbackBinding(config) && !authorizationConfigured) {
            addViolation(context, "accessToken", "or `transport.http.oauthIntrospection.endpoint` must be configured when remote HTTP access is enabled");
            return false;
        }
        if (hasAccessToken(config) && isOAuthIntrospectionEnabled(config.getOauthIntrospection())) {
            addViolation(context, "accessToken", "cannot be configured with `transport.http.oauthIntrospection.endpoint`");
            return false;
        }
        if (authorizationConfigured && getConfiguredValues(config.getAuthorizationServers()).isEmpty()) {
            addViolation(context, "authorizationServers", "must not be empty when HTTP authorization is enabled");
            return false;
        }
        if (authorizationConfigured && !hasHttpsAuthorizationServers(config)) {
            addViolation(context, "authorizationServers", "must use valid HTTPS URLs when HTTP authorization is enabled");
            return false;
        }
        return true;
    }

    private boolean validateOAuthIntrospection(final YamlHttpTransportConfiguration config, final ConstraintValidatorContext context) {
        YamlOAuthIntrospectionConfiguration oauthIntrospection = config.getOauthIntrospection();
        if (null == oauthIntrospection || !isOAuthIntrospectionEnabled(oauthIntrospection) || hasValidOAuthIntrospection(oauthIntrospection)) {
            return true;
        }
        addViolation(context, "oauthIntrospection", "must include a valid endpoint, clientId, clientSecret, and non-negative cacheTtlMillis");
        return false;
    }

    private boolean isLoopbackBinding(final YamlHttpTransportConfiguration config) {
        return HttpTransportHostUtils.isLoopbackHost(config.getBindHost());
    }

    private boolean hasAccessToken(final YamlHttpTransportConfiguration config) {
        return !Objects.toString(config.getAccessToken(), "").trim().isEmpty();
    }

    private boolean hasValidAllowedOrigins(final YamlHttpTransportConfiguration config) {
        return getConfiguredValues(config.getAllowedOrigins()).stream().allMatch(HttpTransportOriginUtils::isValidOrigin);
    }

    private boolean hasAuthorization(final YamlHttpTransportConfiguration config) {
        return hasAccessToken(config) || isOAuthIntrospectionEnabled(config.getOauthIntrospection());
    }

    private boolean hasHttpsAuthorizationServers(final YamlHttpTransportConfiguration config) {
        return getConfiguredValues(config.getAuthorizationServers()).stream().allMatch(this::isHttpsAuthorizationServer);
    }

    private boolean isOAuthIntrospectionEnabled(final YamlOAuthIntrospectionConfiguration config) {
        return null != config && !trimOptionalText(config.getEndpoint()).isEmpty();
    }

    private boolean hasValidOAuthIntrospection(final YamlOAuthIntrospectionConfiguration config) {
        return isValidIntrospectionEndpoint(trimOptionalText(config.getEndpoint())) && !trimOptionalText(config.getClientId()).isEmpty() && !trimOptionalText(config.getClientSecret()).isEmpty()
                && (null == config.getCacheTtlMillis() || config.getCacheTtlMillis() >= 0L)
                && (trimOptionalText(config.getExpectedIssuer()).isEmpty() || isHttpsAuthorizationServer(trimOptionalText(config.getExpectedIssuer())));
    }

    private List<String> getConfiguredValues(final Collection<String> values) {
        if (null == values) {
            return List.of();
        }
        return values.stream().map(this::trimOptionalText).filter(each -> !each.isEmpty()).toList();
    }

    private String trimOptionalText(final String value) {
        return Objects.toString(value, "").trim();
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

    private void addViolation(final ConstraintValidatorContext context, final String propertyName, final String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(propertyName).addConstraintViolation();
    }
}

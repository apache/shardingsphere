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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.validator;

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlSessionAttributionSourceConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.util.Objects;

/**
 * HTTP transport configuration validator.
 */
public final class HttpTransportConfigurationValidator implements ConstraintValidator<ValidHttpTransportConfiguration, YamlHttpTransportConfiguration> {
    
    @Override
    public boolean isValid(final YamlHttpTransportConfiguration value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        if (!isValidBindHost(value.getBindHost())) {
            addViolation(context, "bindHost", "must be a local bind host or IP address");
            return false;
        }
        if (!isValidPort(value.getPort())) {
            addViolation(context, "port", "must be between 0 and 65535");
            return false;
        }
        if (!isValidEndpointPath(value.getEndpointPath())) {
            addViolation(context, "endpointPath", "must be a single absolute path without query or fragment");
            return false;
        }
        if (!isValidSessionAttributionSource(value.getSessionAttributionSource(), context)) {
            return false;
        }
        return true;
    }
    
    private boolean isValidBindHost(final String value) {
        if (null == value) {
            return true;
        }
        String actualValue = Objects.toString(value, "").trim();
        if (actualValue.isEmpty() || actualValue.contains("/") || actualValue.contains("?") || actualValue.contains("#")) {
            return false;
        }
        return !actualValue.contains("://");
    }
    
    private boolean isValidPort(final Integer value) {
        return null == value || value >= 0 && value <= 65535;
    }
    
    private boolean isValidEndpointPath(final String value) {
        if (null == value) {
            return true;
        }
        String actualValue = Objects.toString(value, "").trim();
        if (!actualValue.startsWith("/") || actualValue.startsWith("//")) {
            return false;
        }
        try {
            URI uri = URI.create(actualValue);
            return null == uri.getScheme() && null == uri.getRawAuthority() && null == uri.getRawQuery() && null == uri.getRawFragment();
        } catch (final IllegalArgumentException ignored) {
            return false;
        }
    }
    
    private boolean isValidSessionAttributionSource(final YamlSessionAttributionSourceConfiguration value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        if (!isValidHeaderName(value.getSubjectHeader())) {
            addViolation(context, "sessionAttributionSource.subjectHeader", "must be a valid HTTP header name");
            return false;
        }
        if (!isValidHeaderName(value.getSourceHeader())) {
            addViolation(context, "sessionAttributionSource.sourceHeader", "must be a valid HTTP header name");
            return false;
        }
        if (!isValidHeaderName(value.getAttributeHeaderPrefix())) {
            addViolation(context, "sessionAttributionSource.attributeHeaderPrefix", "must be a valid HTTP header name prefix");
            return false;
        }
        return true;
    }
    
    private boolean isValidHeaderName(final String value) {
        if (null == value) {
            return true;
        }
        String actualValue = Objects.toString(value, "").trim();
        if (actualValue.isEmpty()) {
            return false;
        }
        for (char each : actualValue.toCharArray()) {
            if (Character.isWhitespace(each) || ':' == each) {
                return false;
            }
        }
        return true;
    }
    
    private void addViolation(final ConstraintValidatorContext context, final String propertyName, final String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addPropertyNode(propertyName).addConstraintViolation();
    }
}

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

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * MCP launch configuration validator.
 */
public final class MCPLaunchConfigurationValidator implements ConstraintValidator<ValidMCPLaunchConfiguration, YamlMCPLaunchConfiguration> {
    
    @Override
    public boolean isValid(final YamlMCPLaunchConfiguration value, final ConstraintValidatorContext context) {
        if (null == value || null == value.getTransport()) {
            return true;
        }
        return isValid(value.getTransport(), context);
    }
    
    private boolean isValid(final YamlMCPTransportConfiguration value, final ConstraintValidatorContext context) {
        if (null == value.getHttp() || null == value.getStdio()) {
            return true;
        }
        if (value.getHttp().isEnabled() && value.getStdio().isEnabled()) {
            addViolation(context, "HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport.");
            return false;
        }
        if (!value.getHttp().isEnabled() && !value.getStdio().isEnabled()) {
            addViolation(context, "Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true.");
            return false;
        }
        return true;
    }
    
    private void addViolation(final ConstraintValidatorContext context, final String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

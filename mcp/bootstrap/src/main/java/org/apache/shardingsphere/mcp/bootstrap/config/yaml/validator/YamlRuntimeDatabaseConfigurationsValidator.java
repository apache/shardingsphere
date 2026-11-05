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

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML runtime database configurations validator.
 */
public final class YamlRuntimeDatabaseConfigurationsValidator implements ConstraintValidator<ValidYamlRuntimeDatabaseConfigurations, Map<?, ?>> {
    
    @Override
    public boolean isValid(final Map<?, ?> value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        boolean result = true;
        for (Entry<?, ?> entry : value.entrySet()) {
            result = validateRuntimeDatabase((String) entry.getKey(), (YamlRuntimeDatabaseConfiguration) entry.getValue(), context) && result;
        }
        return result;
    }
    
    private boolean validateRuntimeDatabase(final String databaseName, final YamlRuntimeDatabaseConfiguration config, final ConstraintValidatorContext context) {
        if (isPlaceholder(databaseName)) {
            addViolation(context, "contains placeholder database name");
            return false;
        }
        if (null == config) {
            addViolation(context, "contains null database configuration");
            return false;
        }
        return validateNoPlaceholders(config, context);
    }
    
    private boolean isPlaceholder(final String value) {
        String actualValue = value.trim();
        int startIndex = actualValue.indexOf('<');
        return startIndex >= 0 && actualValue.indexOf('>', startIndex + 1) > startIndex + 1;
    }
    
    private void addViolation(final ConstraintValidatorContext context, final String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
    
    private boolean validateNoPlaceholders(final YamlRuntimeDatabaseConfiguration config, final ConstraintValidatorContext context) {
        boolean result = validateNoPlaceholder("jdbcUrl", config.getJdbcUrl(), context);
        result = validateNoPlaceholder("username", config.getUsername(), context) && result;
        result = validateNoPlaceholder("driverClassName", config.getDriverClassName(), context) && result;
        return validateNoPasswordPlaceholder(config.getPassword(), context) && result;
    }
    
    private boolean validateNoPlaceholder(final String propertyName, final String value, final ConstraintValidatorContext context) {
        if (null == value || !isPlaceholder(value)) {
            return true;
        }
        addViolation(context, String.format("contains placeholder database property `%s`", propertyName));
        return false;
    }
    
    private boolean validateNoPasswordPlaceholder(final String value, final ConstraintValidatorContext context) {
        if (null == value || !isStandalonePlaceholder(value)) {
            return true;
        }
        addViolation(context, "contains placeholder database property `password`");
        return false;
    }
    
    private boolean isStandalonePlaceholder(final String value) {
        String actualValue = value.trim();
        return actualValue.startsWith("<") && actualValue.endsWith(">") && actualValue.length() > 2;
    }
}

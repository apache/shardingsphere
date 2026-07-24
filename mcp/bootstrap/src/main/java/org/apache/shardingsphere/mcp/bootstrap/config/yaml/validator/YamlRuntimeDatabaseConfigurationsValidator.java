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
public final class YamlRuntimeDatabaseConfigurationsValidator implements ConstraintValidator<ValidYamlRuntimeDatabaseConfigurations, Map<String, YamlRuntimeDatabaseConfiguration>> {
    
    @Override
    public boolean isValid(final Map<String, YamlRuntimeDatabaseConfiguration> value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        boolean result = true;
        for (Entry<String, YamlRuntimeDatabaseConfiguration> entry : value.entrySet()) {
            result = validateRuntimeDatabase(entry, context) && result;
        }
        return result;
    }
    
    private boolean validateRuntimeDatabase(final Entry<String, YamlRuntimeDatabaseConfiguration> databaseEntry, final ConstraintValidatorContext context) {
        if (isPlaceholder(databaseEntry.getKey())) {
            addViolation(context, String.format("contains placeholder database name `%s`", databaseEntry.getKey()));
            return false;
        }
        if (null == databaseEntry.getValue()) {
            addViolation(context, String.format("contains null configuration for database `%s`", databaseEntry.getKey()));
            return false;
        }
        return validateNoPlaceholders(databaseEntry, context);
    }
    
    private boolean validateNoPlaceholders(final Entry<String, YamlRuntimeDatabaseConfiguration> databaseEntry, final ConstraintValidatorContext context) {
        YamlRuntimeDatabaseConfiguration config = databaseEntry.getValue();
        boolean result = validateNoPlaceholder(databaseEntry.getKey(), "jdbcUrl", config.getJdbcUrl(), context);
        result = validateNoPlaceholder(databaseEntry.getKey(), "username", config.getUsername(), context) && result;
        result = validateNoPlaceholder(databaseEntry.getKey(), "driverClassName", config.getDriverClassName(), context) && result;
        return validateNoPasswordPlaceholder(databaseEntry.getKey(), config.getPassword(), context) && result;
    }
    
    private boolean validateNoPlaceholder(final String databaseName, final String propertyName, final String value, final ConstraintValidatorContext context) {
        if (null == value || !isPlaceholder(value)) {
            return true;
        }
        addViolation(context, String.format("contains placeholder database `%s` property `%s`", databaseName, propertyName));
        return false;
    }
    
    private boolean validateNoPasswordPlaceholder(final String databaseName, final String value, final ConstraintValidatorContext context) {
        if (null == value || !isStandalonePlaceholder(value)) {
            return true;
        }
        addViolation(context, String.format("contains placeholder database `%s` property `password`", databaseName));
        return false;
    }
    
    private boolean isPlaceholder(final String value) {
        String actualValue = value.trim();
        int startIndex = actualValue.indexOf('<');
        return startIndex >= 0 && actualValue.indexOf('>', startIndex + 1) > startIndex + 1;
    }
    
    private boolean isStandalonePlaceholder(final String value) {
        String actualValue = value.trim();
        return actualValue.startsWith("<") && actualValue.endsWith(">") && actualValue.length() > 2;
    }
    
    private void addViolation(final ConstraintValidatorContext context, final String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

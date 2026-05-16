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

import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlRuntimeDatabaseConfigurationProperties;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Map.Entry;

/**
 * YAML runtime database configurations validator.
 */
public final class YamlRuntimeDatabaseConfigurationsValidator implements ConstraintValidator<ValidYamlRuntimeDatabaseConfigurations, Map<String, Map<String, Object>>> {
    
    @Override
    public boolean isValid(final Map<String, Map<String, Object>> value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        boolean result = true;
        for (Entry<String, Map<String, Object>> entry : value.entrySet()) {
            result = validateRuntimeDatabase(entry, context) && result;
        }
        return result;
    }
    
    private boolean validateRuntimeDatabase(final Entry<String, Map<String, Object>> entry, final ConstraintValidatorContext context) {
        if (null == entry.getValue()) {
            addViolation(context, String.format("contains null configuration for database `%s`", entry.getKey()));
            return false;
        }
        return validateSupportedProperties(entry, context) && validateRequiredProperties(entry, context);
    }
    
    private boolean validateSupportedProperties(final Entry<String, Map<String, Object>> entry, final ConstraintValidatorContext context) {
        boolean result = true;
        for (String each : entry.getValue().keySet()) {
            if (!YamlRuntimeDatabaseConfigurationProperties.SUPPORTED_PROPERTIES.contains(each)) {
                addViolation(context, String.format("contains unsupported property `%s` for database `%s`", each, entry.getKey()));
                result = false;
            }
        }
        return result;
    }
    
    private boolean validateRequiredProperties(final Entry<String, Map<String, Object>> entry, final ConstraintValidatorContext context) {
        boolean result = validateRequiredText(entry, YamlRuntimeDatabaseConfigurationProperties.DATABASE_TYPE, context);
        result = validateRequiredText(entry, YamlRuntimeDatabaseConfigurationProperties.JDBC_URL, context) && result;
        result = validateExplicitText(entry, YamlRuntimeDatabaseConfigurationProperties.USERNAME, context) && result;
        result = validateExplicitText(entry, YamlRuntimeDatabaseConfigurationProperties.PASSWORD, context) && result;
        return validateExplicitText(entry, YamlRuntimeDatabaseConfigurationProperties.DRIVER_CLASS_NAME, context) && result;
    }
    
    private boolean validateRequiredText(final Entry<String, Map<String, Object>> entry, final String key, final ConstraintValidatorContext context) {
        Object value = entry.getValue().get(key);
        if (null != value && !value.toString().isBlank()) {
            return true;
        }
        addViolation(context, String.format("contains database `%s` property `%s` is required", entry.getKey(), key));
        return false;
    }
    
    private boolean validateExplicitText(final Entry<String, Map<String, Object>> entry, final String key, final ConstraintValidatorContext context) {
        if (null != entry.getValue().get(key)) {
            return true;
        }
        addViolation(context, String.format("contains database `%s` property `%s` is required. Use an empty string when no value is needed", entry.getKey(), key));
        return false;
    }
    
    private void addViolation(final ConstraintValidatorContext context, final String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}

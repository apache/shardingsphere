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

package org.apache.shardingsphere.infra.config.rule.validator.constraint.reference;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration reference exists validator.
 */
public final class ConfigurationReferenceExistsValidator implements ConstraintValidator<ConfigurationReferenceExists, Object> {
    
    private ReferencePathNode referencePaths;
    
    private String poolProperty;
    
    private boolean allowEmpty;
    
    private String message;
    
    @Override
    public void initialize(final ConfigurationReferenceExists constraintAnnotation) {
        ShardingSpherePreconditions.checkState(0 != constraintAnnotation.referencePaths().length, () -> new ValidationException("At least one configuration reference property is required."));
        referencePaths = new ReferencePathNode();
        for (String each : constraintAnnotation.referencePaths()) {
            referencePaths.addPath(parseReferencePaths(each));
        }
        poolProperty = constraintAnnotation.pool();
        ShardingSpherePreconditions.checkState(!poolProperty.isEmpty() && !poolProperty.contains("."),
                () -> new ValidationException("Configuration pool must be a property on the annotated object."));
        allowEmpty = constraintAnnotation.allowEmpty();
        message = constraintAnnotation.message();
    }
    
    private String[] parseReferencePaths(final String referencePath) {
        String[] result = referencePath.split("\\.", -1);
        for (String each : result) {
            ShardingSpherePreconditions.checkNotEmpty(each, () -> new ValidationException(String.format("Invalid configuration reference property path `%s`.", referencePath)));
        }
        return result;
    }
    
    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        Object pool = getProperty(value, poolProperty);
        ShardingSpherePreconditions.checkState(null == pool || pool instanceof Map, () -> new ValidationException(String.format("Configuration pool property `%s` must be a map.", poolProperty)));
        Map<?, ?> configurations = null == pool ? Collections.emptyMap() : (Map<?, ?>) pool;
        return areReferencesValid(value, referencePaths, configurations, context, null, "");
    }
    
    private Object getProperty(final Object value, final String property) {
        try {
            Method getter = value.getClass().getMethod("get" + Character.toUpperCase(property.charAt(0)) + property.substring(1));
            return getter.invoke(value);
        } catch (final ReflectiveOperationException ex) {
            throw new ValidationException(String.format("Can not read property `%s` from `%s`.", property, value.getClass().getName()), ex);
        }
    }
    
    private boolean areReferencesValid(final Object value, final ReferencePathNode node, final Map<?, ?> pool,
                                       final ConstraintValidatorContext context, final String rootProperty, final String path) {
        if (null == value) {
            return true;
        }
        if (value instanceof Optional) {
            return areReferencesValid(((Optional<?>) value).orElse(null), node, pool, context, rootProperty, path);
        }
        if (value instanceof Collection) {
            return areCollectionReferencesValid((Collection<?>) value, node, pool, context, rootProperty, path);
        }
        return (!node.pathEnd || isReferenceValid(value, pool, context, rootProperty, path)) && areChildReferencesValid(value, node, pool, context, rootProperty, path);
    }
    
    private boolean areCollectionReferencesValid(final Collection<?> values, final ReferencePathNode node, final Map<?, ?> pool,
                                                 final ConstraintValidatorContext context, final String rootProperty, final String path) {
        return values.stream().allMatch(each -> areReferencesValid(each, node, pool, context, rootProperty, path));
    }
    
    private boolean isReferenceValid(final Object value, final Map<?, ?> pool, final ConstraintValidatorContext context, final String rootProperty, final String path) {
        ShardingSpherePreconditions.checkState(value instanceof String, () -> new ValidationException(String.format("Configuration reference property `%s` must be a string.", path)));
        if (allowEmpty && ((String) value).isEmpty() || pool.containsKey(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message.replace("{pool}", poolProperty).replace("{reference}", (String) value)).addPropertyNode(rootProperty).addConstraintViolation();
        return false;
    }
    
    private boolean areChildReferencesValid(final Object value, final ReferencePathNode node, final Map<?, ?> pool,
                                            final ConstraintValidatorContext context, final String rootProperty, final String path) {
        return node.children.entrySet().stream().allMatch(each -> areReferencesValid(
                getProperty(value, each.getKey()), each.getValue(), pool, context, null == rootProperty ? each.getKey() : rootProperty, path.isEmpty() ? each.getKey() : path + "." + each.getKey()));
    }
    
    private static final class ReferencePathNode {
        
        private final Map<String, ReferencePathNode> children = new LinkedHashMap<>();
        
        private boolean pathEnd;
        
        private void addPath(final String[] properties) {
            ReferencePathNode current = this;
            for (String each : properties) {
                current = current.children.computeIfAbsent(each, ignored -> new ReferencePathNode());
            }
            current.pathEnd = true;
        }
    }
}

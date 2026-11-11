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

package org.apache.shardingsphere.infra.config.rule.validator.constraint.spi;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SPI type exists validator.
 */
public final class SPITypeExistsValidator implements ConstraintValidator<SPITypeExists, Object> {
    
    private String spiClassName;
    
    @Override
    public void initialize(final SPITypeExists constraintAnnotation) {
        spiClassName = constraintAnnotation.spiClassName();
    }
    
    @Override
    public boolean isValid(final Object value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        if (value instanceof Map) {
            Map<?, ?> values = (Map<?, ?>) value;
            int size = values.size();
            if (0 == size) {
                return true;
            }
            return size > 1 ? isMapSPITypeExists(values) : values.values().stream().allMatch(this::isSPITypeExists);
        }
        return isSPITypeExists(value);
    }
    
    private boolean isMapSPITypeExists(final Map<?, ?> values) {
        Map<Class<?>, Method> typeGetters = new HashMap<>();
        Set<String> validatedTypes = new HashSet<>();
        Class<? extends TypedSPI> spiClass = null;
        for (Object each : values.values()) {
            if (null == each) {
                continue;
            }
            Object type = getMapValueType(each, typeGetters);
            try {
                if (null == spiClass) {
                    spiClass = loadSPIClass(spiClassName);
                }
                if ((null == type || type instanceof String) && !validatedTypes.add((String) type)) {
                    continue;
                }
                if (!TypedSPILoader.containsService(spiClass, type)) {
                    return false;
                }
            } catch (final ClassNotFoundException ex) {
                throw new ValidationException(String.format("Can not load SPI class `%s`.", spiClassName), ex);
            }
        }
        return true;
    }
    
    private Object getMapValueType(final Object value, final Map<Class<?>, Method> typeGetters) {
        if (value instanceof String) {
            return value;
        }
        try {
            Method getter = typeGetters.get(value.getClass());
            if (null == getter) {
                getter = value.getClass().getMethod("getType");
                typeGetters.put(value.getClass(), getter);
            }
            return getter.invoke(value);
        } catch (final ReflectiveOperationException ex) {
            throw new ValidationException(String.format("Can not read property `type` from `%s`.", value.getClass().getName()), ex);
        }
    }
    
    private Class<? extends TypedSPI> loadSPIClass(final String spiClassName) throws ClassNotFoundException {
        Class<?> result = Class.forName(spiClassName, false, Thread.currentThread().getContextClassLoader());
        ShardingSpherePreconditions.checkState(TypedSPI.class.isAssignableFrom(result), () -> new ValidationException(String.format("Class `%s` does not implement TypedSPI.", spiClassName)));
        return result.asSubclass(TypedSPI.class);
    }
    
    private boolean isSPITypeExists(final Object value) {
        return null == value || containsService(spiClassName, getTypeValue(value));
    }
    
    private boolean containsService(final String spiClassName, final Object type) {
        try {
            Class<?> spiClass = Class.forName(spiClassName, false, Thread.currentThread().getContextClassLoader());
            ShardingSpherePreconditions.checkState(TypedSPI.class.isAssignableFrom(spiClass), () -> new ValidationException(String.format("Class `%s` does not implement TypedSPI.", spiClassName)));
            return TypedSPILoader.containsService(spiClass.asSubclass(TypedSPI.class), type);
        } catch (final ClassNotFoundException ex) {
            throw new ValidationException(String.format("Can not load SPI class `%s`.", spiClassName), ex);
        }
    }
    
    private Object getTypeValue(final Object value) {
        if (value instanceof String) {
            return value;
        }
        try {
            Method getter = value.getClass().getMethod("getType");
            return getter.invoke(value);
        } catch (final ReflectiveOperationException ex) {
            throw new ValidationException(String.format("Can not read property `type` from `%s`.", value.getClass().getName()), ex);
        }
    }
}

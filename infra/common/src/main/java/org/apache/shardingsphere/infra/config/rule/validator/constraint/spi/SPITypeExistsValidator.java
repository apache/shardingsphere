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
        return null == value || containsService(spiClassName, getTypeValue(value));
    }
    
    private boolean containsService(final String spiClassName, final Object type) {
        try {
            Class<?> spiClass = Class.forName(spiClassName, false, Thread.currentThread().getContextClassLoader());
            ShardingSpherePreconditions.checkState(TypedSPI.class.isAssignableFrom(spiClass), () -> new ValidationException(String.format("Class `%s` does not implement TypedSPI.", spiClassName)));
            return TypedSPILoader.containsService(spiClass.asSubclass(TypedSPI.class), type);
        } catch (final ClassNotFoundException ignored) {
            return false;
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

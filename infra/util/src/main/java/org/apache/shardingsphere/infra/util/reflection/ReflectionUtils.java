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

package org.apache.shardingsphere.infra.util.reflection;

import com.google.common.base.CaseFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {
    
    private static final String GETTER_PREFIX = "get";
    
    /**
     * Get field value.
     * 
     * @param target target
     * @param fieldName field name
     * @param <T> type of field value
     * @return field value
     */
    public static <T> Optional<T> getFieldValue(final Object target, final String fieldName) {
        return findField(fieldName, target.getClass()).map(optional -> getFieldValue(target, optional));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(IllegalAccessException.class)
    private static <T> T getFieldValue(final Object target, final Field field) {
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        T result = (T) field.get(target);
        if (!accessible) {
            field.setAccessible(false);
        }
        return result;
    }
    
    private static Optional<Field> findField(final String fieldName, final Class<?> targetClass) {
        Class<?> currentTargetClass = targetClass;
        while (Object.class != currentTargetClass) {
            try {
                return Optional.of(currentTargetClass.getDeclaredField(fieldName));
            } catch (final NoSuchFieldException ignored) {
                currentTargetClass = currentTargetClass.getSuperclass();
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get static field value.
     *
     * @param target target
     * @param fieldName field name
     * @param <T> type of field value
     * @return field value
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T> T getStaticFieldValue(final Class<?> target, final String fieldName) {
        Field field = target.getDeclaredField(fieldName);
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        T result = (T) field.get(target);
        if (!accessible) {
            field.setAccessible(false);
        }
        return result;
    }
    
    /**
     * Set static field value.
     * 
     * @param target target
     * @param fieldName field name
     * @param value value
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static void setStaticFieldValue(final Class<?> target, final String fieldName, final Object value) {
        Field field = target.getDeclaredField(fieldName);
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        field.set(target, value);
        if (!accessible) {
            field.setAccessible(false);
        }
    }
    
    /**
     * Invoke method.
     *
     * @param method method
     * @param target target
     * @param args arguments
     * @param <T> type of invoke result
     * @return invoke result
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T> T invokeMethod(final Method method, final Object target, final Object... args) {
        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        T result = (T) method.invoke(target, args);
        if (!accessible) {
            method.setAccessible(false);
        }
        return result;
    }
    
    /**
     * Get field value by get method.
     *
     * @param target target
     * @param fieldName field name
     * @param <T> type of field value
     * @return field value
     */
    public static <T> Optional<T> getFieldValueByGetMethod(final Object target, final String fieldName) {
        String getterName = GETTER_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
        final Optional<Method> method = findMethod(target.getClass(), getterName);
        if (method.isPresent()) {
            T value = invokeMethod(method.get(), target);
            return Optional.ofNullable(value);
        } else {
            return Optional.empty();
        }
    }
    
    private static Optional<Method> findMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        try {
            return Optional.of(clazz.getMethod(methodName, parameterTypes));
        } catch (final NoSuchMethodException ex) {
            Class<?> superclass = clazz.getSuperclass();
            if (null != superclass && Object.class != superclass) {
                return findMethod(superclass, methodName, parameterTypes);
            }
        }
        return Optional.empty();
    }
}

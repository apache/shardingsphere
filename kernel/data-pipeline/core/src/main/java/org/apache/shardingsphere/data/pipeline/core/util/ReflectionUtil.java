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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {
    
    /**
     * Set value into target object field.
     *
     * @param target target object
     * @param fieldName field name
     * @param value new value
     * @throws NoSuchFieldException no such field exception
     * @throws IllegalAccessException illegal access exception
     */
    public static void setFieldValue(final Object target, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(target.getClass(), fieldName, true);
        field.set(target, value);
    }
    
    /**
     * Get field value from instance target object.
     *
     * @param target target object
     * @param fieldName field name
     * @param valueClass expected value class
     * @param <T> expected value class
     * @return target filed value
     * @throws NoSuchFieldException no such field exception
     * @throws IllegalAccessException illegal access exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(final Object target, final String fieldName, final Class<T> valueClass) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(target.getClass(), fieldName, true);
        Object value = field.get(target);
        if (null == value) {
            return null;
        }
        if (valueClass.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        throw new ClassCastException("field " + fieldName + " is " + value.getClass().getName() + " can cast to " + valueClass.getName());
    }
    
    private static Field getField(final Class<?> targetClass, final String fieldName, final boolean isDeclared) throws NoSuchFieldException {
        Field result = isDeclared ? targetClass.getDeclaredField(fieldName) : targetClass.getField(fieldName);
        result.setAccessible(true);
        return result;
    }
    
    /**
     * Invoke method.
     *
     * @param target target object
     * @param methodName method name
     * @param parameterTypes parameter types
     * @param parameterValues parameter values
     * @return invoke method result.
     * @throws NoSuchMethodException no such field exception
     * @throws InvocationTargetException invocation target exception
     * @throws IllegalAccessException illegal access exception
     */
    public static Object invokeMethod(final Object target, final String methodName, final Class<?>[] parameterTypes,
                                      final Object[] parameterValues) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, parameterValues);
    }
    
    /**
     * invoke method in parent class.
     *
     * @param target target object
     * @param methodName method name
     * @param parameterTypes parameter types
     * @param parameterValues parameter values
     * @return invoke method result.
     * @throws NoSuchMethodException no such field exception
     * @throws InvocationTargetException invocation target exception
     * @throws IllegalAccessException illegal access exception
     */
    public static Object invokeMethodInParentClass(final Object target, final String methodName, final Class<?>[] parameterTypes,
                                                   final Object[] parameterValues) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = null;
        for (Class<?> clazz = target.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                break;
            } catch (final NoSuchMethodException ignored) {
            }
        }
        if (null == method) {
            throw new NoSuchMethodException("not find method ");
        }
        return method.invoke(target, parameterValues);
    }
}

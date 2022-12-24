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

package org.apache.shardingsphere.agent.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Agent reflection utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentReflectionUtil {
    
    /**
     * Get field value.
     *
     * @param target target
     * @param fieldName field name
     * @param <T> type of field value
     * @return field value
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T> T getFieldValue(final Object target, final String fieldName) {
        Class<?> clazz = target.getClass();
        while (null != clazz) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                boolean accessible = field.isAccessible();
                if (!accessible) {
                    field.setAccessible(true);
                }
                T result = (T) field.get(target);
                if (!accessible) {
                    field.setAccessible(false);
                }
                return result;
            } catch (final NoSuchFieldException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException(String.format("Can not find field name `%s` in class %s.", fieldName, target.getClass()));
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
}

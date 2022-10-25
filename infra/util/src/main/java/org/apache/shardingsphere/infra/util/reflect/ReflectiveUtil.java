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

package org.apache.shardingsphere.infra.util.reflect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Reflective utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectiveUtil {
    
    /**
     * Get field value.
     *
     * @param target target
     * @param fieldName field name
     * @return field value
     */
    @SneakyThrows(IllegalAccessException.class)
    public static Object getFieldValue(final Object target, final String fieldName) {
        Field field = getField(target.getClass(), fieldName);
        Objects.requireNonNull(field).setAccessible(true);
        return field.get(target);
    }
    
    private static Field getField(final Class<?> target, final String fieldName) {
        Class<?> clazz = target;
        while (null != clazz) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (final NoSuchFieldException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
    
    /**
     * Set value to specified field.
     * 
     * @param target target
     * @param fieldName field name
     * @param value value
     */
    @SneakyThrows(IllegalAccessException.class)
    public static void setField(final Object target, final String fieldName, final Object value) {
        Field field = getField(target.getClass(), fieldName);
        Objects.requireNonNull(field).setAccessible(true);
        field.set(target, value);
    }
    
    /**
     * Set value to specified static field.
     *
     * @param target target
     * @param fieldName field name
     * @param value value
     */
    @SneakyThrows(IllegalAccessException.class)
    public static void setStaticField(final Class<?> target, final String fieldName, final Object value) {
        Field field = getField(target, fieldName);
        if (Modifier.isStatic(Objects.requireNonNull(field).getModifiers())) {
            field.setAccessible(true);
            field.set(null, value);
        }
    }
}

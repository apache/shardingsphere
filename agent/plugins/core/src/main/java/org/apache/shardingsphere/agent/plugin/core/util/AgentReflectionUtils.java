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

package org.apache.shardingsphere.agent.plugin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Agent reflection utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentReflectionUtils {
    
    /**
     * Get field value.
     *
     * @param target target
     * @param fieldName field name
     * @param <T> type of field value
     * @return field value
     * @throws IllegalStateException illegal state exception
     */
    public static <T> T getFieldValue(final Object target, final String fieldName) {
        Optional<Field> field = findField(fieldName, target.getClass());
        if (field.isPresent()) {
            return getFieldValue(target, field.get());
        }
        throw new IllegalStateException(String.format("Can not find field name `%s` in class %s.", fieldName, target.getClass()));
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
}

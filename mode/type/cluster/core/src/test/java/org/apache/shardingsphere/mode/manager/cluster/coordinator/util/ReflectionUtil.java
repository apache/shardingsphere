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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.util;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

/**
 * Reflection utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {
    
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
        Preconditions.checkNotNull(value);
        if (valueClass.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        throw new ClassCastException("field " + fieldName + " is " + value.getClass().getName() + " can cast to " + valueClass.getName());
    }
    
    /**
     * Get field from class.
     *
     * @param targetClass target class
     * @param fieldName field name
     * @param isDeclared is declared
     * @return {@link Field}
     * @throws NoSuchFieldException no such field exception
     */
    public static Field getField(final Class<?> targetClass, final String fieldName, final boolean isDeclared) throws NoSuchFieldException {
        Field result = isDeclared ? targetClass.getDeclaredField(fieldName) : targetClass.getField(fieldName);
        result.setAccessible(true);
        return result;
    }
}

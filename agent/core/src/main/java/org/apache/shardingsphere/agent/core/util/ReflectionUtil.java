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

/**
 * Reflection utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {
    
    /**
     * Get field value.
     *
     * @param target target
     * @param fieldName field name
     * @return field value
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static Object getFieldValue(final Object target, final String fieldName) {
        Class<?> clazz = target.getClass();
        while (null != clazz) {
            try {
                Field result = clazz.getDeclaredField(fieldName);
                result.setAccessible(true);
                return result.get(target);
            } catch (final NoSuchFieldException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException(String.format("Can not find field name `%s` in class %s.", fieldName, target.getClass()));
    }
}

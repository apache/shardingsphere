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

package org.apache.shardingsphere.transaction.xa.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

/**
 * Reflective utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectiveUtil {
    
    /**
     * Get field.
     * 
     * @param target target
     * @param fieldName field name
     * @return field
     */
    private static Field getField(final Object target, final String fieldName) {
        Class clazz = target.getClass();
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (final NoSuchFieldException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}

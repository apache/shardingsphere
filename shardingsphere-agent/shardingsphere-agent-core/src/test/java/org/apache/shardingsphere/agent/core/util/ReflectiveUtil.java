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
import java.lang.reflect.Modifier;

/**
 * Reflective utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectiveUtil {
    
    /**
     * Set value to static field.
     *
     * @param target target
     * @param fieldName field name
     * @param value value
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static void setStaticField(final Class<?> target, final String fieldName, final Object value) {
        Field[] fields = target.getDeclaredFields();
        for (Field each : fields) {
            if (Modifier.isStatic(each.getModifiers()) && each.getName().equalsIgnoreCase(fieldName)) {
                each.setAccessible(true);
                Field modifiersField = getModifiersField();
                modifiersField.setAccessible(true);
                modifiersField.setInt(each, each.getModifiers() & ~Modifier.FINAL);
                each.set(null, value);
            }
        }
    }
    
    private static Field getModifiersField() throws NoSuchFieldException {
        try {
            return Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException e) {
            try {
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                for (Field field : fields) {
                    if ("modifiers".equals(field.getName())) {
                        return field;
                    }
                }
            } catch (ReflectiveOperationException ex) {
                e.addSuppressed(ex);
            }
            throw e;
        }
    }
}

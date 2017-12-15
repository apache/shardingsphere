/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.spring.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldValueUtil {
    
    private static Object getFieldValue(final Class<?> clazz, final Object obj, final String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field.get(obj);
        } catch (final NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object getFieldValue(final Object obj, final String fieldName, final boolean fromSuperclass) {
        if (null == obj || Strings.isNullOrEmpty(fieldName)) {
            return null;
        }
        Class<?> clazz = fromSuperclass ? obj.getClass().getSuperclass() : obj.getClass();
        return getFieldValue(clazz, obj, fieldName);
    }
    
    public static Object getFieldValue(final Object obj, final String fieldName) {
        return getFieldValue(obj, fieldName, false);
    }
}

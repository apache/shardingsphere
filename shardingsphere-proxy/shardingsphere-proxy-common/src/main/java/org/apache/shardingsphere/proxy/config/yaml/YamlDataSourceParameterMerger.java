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

package org.apache.shardingsphere.proxy.config.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * YAML datasource parameter merged.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlDataSourceParameterMerger {
    
    /**
     * Merged datasource parameter.
     *
     * @param source source of YAML datasource parameter
     * @param commonProps common properties
     */
    public static void merged(final Object source, final Map<String, Object> commonProps) {
        if (commonProps.isEmpty()) {
            return;
        }
        Class<?> clazz = source.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if ("serialVersionUID".equals(field.getName())) {
                continue;
            }
            //filter JacocoData field
            if (field.isSynthetic()) {
                continue;
            }
            Object value = getValue(field, clazz, source);
            if (isDefaultValue(field, value)) {
                setValue(field, clazz, source, commonProps);
            }
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Object getValue(final Field field, final Class<?> clazz, final Object source) {
        String fieldName = field.getName();
        String firstLetter = fieldName.substring(0, 1).toUpperCase();
        String getMethodName;
        if (boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType())) {
            getMethodName = "is" + firstLetter + fieldName.substring(1);
        } else {
            getMethodName = "get" + firstLetter + fieldName.substring(1);
        }
        Method getMethod = clazz.getMethod(getMethodName);
        return getMethod.invoke(source);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static void setValue(final Field field, final Class<?> clazz, final Object source, final Map<String, Object> commonProps) {
        String fieldName = field.getName();
        String firstLetter = fieldName.substring(0, 1).toUpperCase();
        String setMethodName = "set" + firstLetter + fieldName.substring(1);
        Method setMethod = clazz.getMethod(setMethodName, field.getType());
        Object commonValue = commonProps.get(fieldName);
        if (null != commonValue) {
            Class<?> parameterType = setMethod.getParameterTypes()[0];
            if (String.class.equals(parameterType)) {
                setMethod.invoke(source, commonValue.toString());
            } else {
                setMethod.invoke(source, commonValue);
            }
            
        }
    }
    
    private static boolean isDefaultValue(final Field field, final Object value) {
        Class<?> clazz = field.getType();
        if (int.class.equals(clazz) || Integer.class.equals(clazz)) {
            return (int) value == 0;
        } else if (byte.class.equals(clazz) || Byte.class.equals(clazz)) {
            return (byte) value == 0;
        } else if (long.class.equals(clazz) || Long.class.equals(clazz)) {
            return (long) value == 0L;
        } else if (double.class.equals(clazz) || Double.class.equals(clazz)) {
            return (double) value == 0.0d;
        } else if (float.class.equals(clazz) || Float.class.equals(clazz)) {
            return (float) value == 0.0f;
        } else if (short.class.equals(clazz) || Short.class.equals(clazz)) {
            return (short) value == 0;
        } else if (boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
            return !((boolean) value);
        } else if (String.class.equals(clazz)) {
            return null == value || "".equals(value);
        }
        return false;
    }
}


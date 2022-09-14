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

package org.apache.shardingsphere.mode.repository.cluster.nacos.utils;

import com.google.common.base.CaseFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Reflection utils.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {
    
    private static final String SETTER_PREFIX = "set";
    
    /**
     * Set object field.
     *
     * @param targetObject       targetObject
     * @param fieldAndFiledValue field name and field value
     */
    public static void setFields(final Object targetObject, final Map<String, Object> fieldAndFiledValue) {
        Method[] methods = targetObject.getClass().getMethods();
        fieldAndFiledValue.forEach((field, fieldValue) -> {
            String setterMethodName = SETTER_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field);
            if (null != fieldValue) {
                Arrays.stream(methods)
                        .filter(each -> each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length)
                        .findFirst()
                        .ifPresent(method -> setField(targetObject, method, fieldValue));
            }
        });
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static void setField(final Object targetObject, final Method method, final Object fieldValue) {
        method.invoke(targetObject, fieldValue);
    }
}

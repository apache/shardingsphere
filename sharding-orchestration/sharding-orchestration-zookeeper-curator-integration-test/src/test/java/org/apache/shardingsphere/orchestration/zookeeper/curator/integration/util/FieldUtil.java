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

package org.apache.shardingsphere.orchestration.zookeeper.curator.integration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldUtil {
    
    /**
     * Set static final field.
     * 
     * @param target target
     * @param fieldName field name
     * @param fieldValue field value
     */
    @SneakyThrows
    public static void setStaticFinalField(final Object target, final String fieldName, final Object fieldValue) {
        Field timeServiceField = target.getClass().getDeclaredField(fieldName);
        timeServiceField.setAccessible(true);
        Field modifiers = timeServiceField.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(timeServiceField, timeServiceField.getModifiers() & ~Modifier.FINAL);
        timeServiceField.set(target, fieldValue);
        modifiers.setInt(timeServiceField, timeServiceField.getModifiers() & ~Modifier.FINAL);
    }
}

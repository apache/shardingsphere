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

package io.shardingsphere.core.util;

import java.lang.reflect.Method;

/**
 * Reflective util.
 *
 * @author zhaojun
 */
public class ReflectiveUtil {
    
    /**
     * Find method using reflect.
     *
     * @param target target object
     * @param methodName method name
     * @param parameterTypes parameter types
     * @return method
     * @throws NoSuchMethodException No such method exception
     */
    @SuppressWarnings("unchecked")
    public static Method findMethod(final Object target, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        Method result;
        Class clazz = target.getClass();
        while (null != clazz) {
            try {
                result = clazz.getDeclaredMethod(methodName, parameterTypes);
                result.setAccessible(true);
                return result;
            } catch (NoSuchMethodException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchMethodException(String.format("Cannot find method '%s' in %s", methodName, target.getClass().getName()));
    }
}

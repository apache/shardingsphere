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

package io.shardingjdbc.orchestration.spring.boot.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import io.shardingjdbc.core.exception.ShardingJdbcException;

public class PropertyUtil {
    
    private static int springBootVersion = 1;
    
    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (ClassNotFoundException e) {
            springBootVersion = 2;
        }
    }
    
    /**
     * Spring Boot 1.x is compatible with Spring Boot 2.x by Using Java Reflect.
     * @param environment : the environment context
     * @param prefix : the prefix part of property key
     * @param targetClass : the target class type of result
     * @param <T> : refer to @param targetClass
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T handle(final Environment environment, final String prefix, final Class<T> targetClass) {
        switch (springBootVersion) {
            case 1:
                return (T) v1(environment, prefix);
            default:
                return (T) v2(environment, prefix, targetClass);
        }
    }
    
    private static Object v1(final Environment environment, final String prefix) {
        try {
            Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
            Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
            Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
            Object resolverObject = resolverConstructor.newInstance(environment);
            String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
            return getSubPropertiesMethod.invoke(resolverObject, prefixParam);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new ShardingJdbcException(ex.getMessage(), ex);
        }
    }
    
    private static Object v2(final Environment environment, final String prefix, final Class<?> targetClass) {
        try {
            Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
            Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
            Object binderObject = getMethod.invoke(null, environment);
            String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
            Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
            Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
            return resultGetMethod.invoke(bindResultObject);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            throw new ShardingJdbcException(ex.getMessage(), ex);
        }
    }
}

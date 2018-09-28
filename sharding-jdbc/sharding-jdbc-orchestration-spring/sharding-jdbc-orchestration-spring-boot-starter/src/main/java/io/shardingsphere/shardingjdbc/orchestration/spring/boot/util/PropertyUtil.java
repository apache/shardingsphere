/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.shardingjdbc.orchestration.spring.boot.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyUtil {
    
    private static int springBootVersion = 1;
    
    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (ClassNotFoundException ignored) {
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

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static Object v1(final Environment environment, final String prefix) {
        Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
        Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
        Object resolverObject = resolverConstructor.newInstance(environment);
        String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
        Method getPropertyMethod = resolverClass.getDeclaredMethod("getProperty", String.class);
        Map<String, Object> dataSourceProps = (Map<String, Object>) getSubPropertiesMethod.invoke(resolverObject, prefixParam);
        Map<String, Object> propertiesWithPlaceholderResolved = new HashMap<>();
        for (Entry<String, Object> entry : dataSourceProps.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && ((String) value).contains(
                    PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX)) {
                String resolvedValue = (String) getPropertyMethod.invoke(resolverObject, prefixParam + key);
                propertiesWithPlaceholderResolved.put(key, resolvedValue);
            } else {
                propertiesWithPlaceholderResolved.put(key, value);
            }
        }
        return Collections.unmodifiableMap(propertiesWithPlaceholderResolved);
    }
    
    @SneakyThrows
    private static Object v2(final Environment environment, final String prefix, final Class<?> targetClass) {
        Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
        Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
        Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
        Object binderObject = getMethod.invoke(null, environment);
        String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
        Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
        Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
        return resultGetMethod.invoke(bindResultObject);
    }
}

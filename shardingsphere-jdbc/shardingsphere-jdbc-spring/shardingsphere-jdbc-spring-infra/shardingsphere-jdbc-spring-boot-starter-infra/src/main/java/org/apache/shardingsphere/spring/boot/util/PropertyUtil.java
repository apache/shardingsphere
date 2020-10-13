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

package org.apache.shardingsphere.spring.boot.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyUtil {
    
    private static int springBootVersion = 1;
    
    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (final ClassNotFoundException ignored) {
            springBootVersion = 2;
        }
    }
    
    /**
     * Whether environment contain properties with specified prefix.
     *
     * @param environment the environment context
     * @param prefix the prefix part of property key
     * @return true if contain, otherwise false
     */
    @SuppressWarnings("unchecked")
    public static boolean containPropertyPrefix(final Environment environment, final String prefix) {
        try {
            Map<String, Object> props = (Map<String, Object>) (1 == springBootVersion ? v1(environment, prefix, false) : v2(environment, prefix, Map.class));
            return !props.isEmpty();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return false;
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
        return 1 == springBootVersion ? (T) v1(environment, prefix, true) : (T) v2(environment, prefix, targetClass);
    }
    /**
     * convert flat properties to datasource properties. e.g.
     * { ds1.username=u1, ds1.passwd=p1, ds2.username=u2 } to
     * { ds1={username=u1, passwd=p1}, ds2={username=u2} }
     * @param flatPropsMap datasource properties in flat format.
     * @return converted properties
     */
    public static Map<String, Map<String, Object>> translateDataSourceProps(Map<String, Object> flatPropsMap) {
        if (flatPropsMap == null || flatPropsMap.isEmpty()) {
            return null;
        }
        Map<String, Map<String, Object>> dsProps = new HashMap<>();
        for (Map.Entry<String, Object> prop : flatPropsMap.entrySet()) {
            String key = prop.getKey();
            if (key.contains(".")) {
                String[] names = key.split("\\.");
                Map<String, Object> dsMap =  dsProps.get(names[0]);
                if (dsMap == null) {
                    dsMap = new HashMap<>(20);
                    dsProps.put(names[0], dsMap);
                }
                dsMap.put(names[1], prop.getValue());
            }
        }
        return dsProps;
    }
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private static Object v1(final Environment environment, final String prefix, final boolean handlePlaceholder) {
        Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
        Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
        Object resolverObject = resolverConstructor.newInstance(environment);
        String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
        Method getPropertyMethod = resolverClass.getDeclaredMethod("getProperty", String.class);
        Map<String, Object> dataSourceProps = (Map<String, Object>) getSubPropertiesMethod.invoke(resolverObject, prefixParam);
        Map<String, Object> result = new HashMap<>(dataSourceProps.size(), 1);
        for (Entry<String, Object> entry : dataSourceProps.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (handlePlaceholder && value instanceof String && ((String) value).contains(PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX)) {
                String resolvedValue = (String) getPropertyMethod.invoke(resolverObject, prefixParam + key);
                result.put(key, resolvedValue);
            } else {
                result.put(key, value);
            }
        }
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
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

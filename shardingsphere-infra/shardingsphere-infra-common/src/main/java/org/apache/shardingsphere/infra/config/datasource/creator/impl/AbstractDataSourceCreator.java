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

package org.apache.shardingsphere.infra.config.datasource.creator.impl;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.creator.DataSourceCreator;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract data source creator.
 */
public abstract class AbstractDataSourceCreator implements DataSourceCreator {
    
    static {
        GENERAL_CLASS_TYPE = Sets.newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class, Collection.class, List.class);
        SKIPPED_PROPERTY_NAMES = Sets.newHashSet("loginTimeout");
    }
    
    protected static final Collection<Class<?>> GENERAL_CLASS_TYPE;
    
    protected static final Collection<String> SKIPPED_PROPERTY_NAMES;
    
    private static final String GETTER_PREFIX = "get";
    
    private static final String SETTER_PREFIX = "set";
    
    @SneakyThrows(ReflectiveOperationException.class)
    protected DataSource buildDataSource(final String dataSourceClassName) {
        return (DataSource) Class.forName(dataSourceClassName).getConstructor().newInstance();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    protected void setField(final DataSource dataSource, final Method[] methods, final String property, final Object value) {
        try {
            if (isSkip(property)) {
                return;
            }
            Optional<Method> setterMethod = findSetterMethod(methods, property);
            if (setterMethod.isPresent() && null != value) {
                setDataSourceField(setterMethod.get(), dataSource, value);
            }
        } catch (final IllegalArgumentException ex) {
            throw new ShardingSphereConfigurationException("Incorrect configuration item: the property %s of the dataSource, because %s", property, ex.getMessage());
        }
    }
    
    private void setDataSourceField(final Method method, final DataSource target, final Object value) throws InvocationTargetException, IllegalAccessException {
        Class<?> paramType = method.getParameterTypes()[0];
        if (paramType == int.class) {
            method.invoke(target, Integer.parseInt(value.toString()));
        } else if (paramType == long.class) {
            method.invoke(target, Long.parseLong(value.toString()));
        } else if (paramType == boolean.class || paramType == Boolean.class) {
            method.invoke(target, Boolean.parseBoolean(value.toString()));
        } else if (paramType == String.class) {
            method.invoke(target, value.toString());
        } else {
            method.invoke(target, value);
        }
    }
    
    private Optional<Method> findSetterMethod(final Method[] methods, final String property) {
        String setterMethodName = SETTER_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property);
        return Arrays.stream(methods)
                .filter(each -> each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length)
                .findFirst();
    }
    
    protected DataSourceConfiguration buildDataSourceConfig(final DataSource dataSource) {
        DataSourceConfiguration result = new DataSourceConfiguration(dataSource.getClass().getName());
        result.getProps().putAll(findAllGetterProperties(dataSource));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<String, Object> findAllGetterProperties(final Object target) {
        Collection<Method> allGetterMethods = findAllGetterMethods(target.getClass());
        Map<String, Object> result = new LinkedHashMap<>(allGetterMethods.size(), 1);
        for (Method each : allGetterMethods) {
            String propertyName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, each.getName().substring(GETTER_PREFIX.length()));
            if (GENERAL_CLASS_TYPE.contains(each.getReturnType()) && !SKIPPED_PROPERTY_NAMES.contains(propertyName)) {
                result.put(propertyName, each.invoke(target));
            }
        }
        return result;
    }
    
    private static Collection<Method> findAllGetterMethods(final Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Collection<Method> result = new HashSet<>(methods.length);
        for (Method each : methods) {
            if (each.getName().startsWith(GETTER_PREFIX) && 0 == each.getParameterTypes().length) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isSkip(final String property) {
        return SKIPPED_PROPERTY_NAMES.contains(property);
    }
}

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

package io.shardingsphere.core.config;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source configuration.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class DataSourceConfiguration {
    
    private static final String GETTER_PREFIX = "get";
    
    private static final String SETTER_PREFIX = "set";
    
    private static final Collection<Class<?>> GENERAL_CLASS_TYPE;
    
    private static final Collection<String> SKIPPED_PROPERTY_NAMES;
    
    static {
        GENERAL_CLASS_TYPE = Sets.<Class<?>>newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class);
        SKIPPED_PROPERTY_NAMES = Sets.newHashSet("loginTimeout");
    }
    
    private final String dataSourceClassName;
    
    private final Map<String, Object> properties = new LinkedHashMap<>();
    
    /**
     * Get data source configuration.
     * 
     * @param dataSource data source
     * @return data source configuration
     */
    public static DataSourceConfiguration getDataSourceConfiguration(final DataSource dataSource) {
        DataSourceConfiguration result = new DataSourceConfiguration(dataSource.getClass().getName());
        result.getProperties().putAll(findAllGetterProperties(dataSource));
        return result;
    }
    
    /**
     * Get data source configuration.
     *
     * @param dataSourceParameter data source parameter
     * @return data source configuration
     */
    public static DataSourceConfiguration getDataSourceConfiguration(final DataSourceParameter dataSourceParameter) {
        DataSourceConfiguration result = new DataSourceConfiguration(PoolType.HIKARI.getClassName());
        result.getProperties().putAll(findAllGetterProperties(dataSourceParameter));
        return result;
    }
    
    private static Map<String, Object> findAllGetterProperties(final Object target) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            for (Method each : findAllGetterMethods(target.getClass())) {
                String propertyName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, each.getName().substring(GETTER_PREFIX.length()));
                if (GENERAL_CLASS_TYPE.contains(each.getReturnType()) && !SKIPPED_PROPERTY_NAMES.contains(propertyName)) {
                    result.put(propertyName, each.invoke(target));
                }
            }
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingConfigurationException(ex);
        }
        return result;
    }
    
    private static Collection<Method> findAllGetterMethods(final Class<?> clazz) {
        Collection<Method> result = new HashSet<>();
        for (Method each : clazz.getMethods()) {
            if (each.getName().startsWith(GETTER_PREFIX) && 0 == each.getParameterTypes().length) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Create data source.
     * 
     * @return data source
     */
    public DataSource createDataSource() {
        try {
            DataSource result = (DataSource) Class.forName(dataSourceClassName).newInstance();
            Method[] methods = result.getClass().getMethods();
            for (Entry<String, Object> entry : properties.entrySet()) {
                if (SKIPPED_PROPERTY_NAMES.contains(entry.getKey())) {
                    continue;
                }
                Optional<Method> setterMethod = findSetterMethod(methods, entry.getKey());
                if (setterMethod.isPresent()) {
                    setterMethod.get().invoke(result, entry.getValue());
                }
            }
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingConfigurationException(ex);
        }
    }
    
    private Optional<Method> findSetterMethod(final Method[] methods, final String property) {
        String setterMethodName = Joiner.on("").join(SETTER_PREFIX, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property));
        for (Method each : methods) {
            if (each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Create data source parameter.
     *
     * @return data source parameter
     */
    public DataSourceParameter createDataSourceParameter() {
        DataSourceParameter result = new DataSourceParameter();
        for (Field each : result.getClass().getDeclaredFields()) {
            try {
                each.setAccessible(true);
                if (properties.containsKey(each.getName())) {
                    each.set(result, properties.get(each.getName()));
                }
            } catch (final ReflectiveOperationException ignored) {
            }
        }
        return result;
    }
}

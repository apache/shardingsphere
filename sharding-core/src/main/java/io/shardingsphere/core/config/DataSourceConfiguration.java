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
import com.google.common.collect.Sets;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source configuration.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class DataSourceConfiguration {
    
    private static final Collection<Class<?>> GENERAL_CLASS_TYPE;
    
    static {
        GENERAL_CLASS_TYPE = Sets.<Class<?>>newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class);
    }
    
    private final String dataSourceClassName;
    
    private final Map<String, Object> properties;
    
    /**
     * Get data source configuration.
     * 
     * @param dataSource data source
     * @return data source configuration
     */
    public static DataSourceConfiguration getDataSourceConfiguration(final DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        try {
            for (Method each : findAllGetterMethods(dataSource)) {
                String propertyName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, each.getName().substring(3));
                properties.put(propertyName, each.invoke(dataSource));
            }
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingConfigurationException(ex);
        }
        return new DataSourceConfiguration(dataSource.getClass().getName(), properties);
    }
    
    private static Collection<Method> findAllGetterMethods(final DataSource dataSource) {
        Collection<Method> result = new HashSet<>();
        for (Method each : dataSource.getClass().getMethods()) {
            if (each.getName().startsWith("get") && 0 == each.getParameterTypes().length && GENERAL_CLASS_TYPE.contains(each.getReturnType())) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * create data source.
     * 
     * @return data source
     */
    public DataSource createDataSource() {
        try {
            DataSource result = (DataSource) Class.forName(dataSourceClassName).newInstance();
            Method[] methods = result.getClass().getMethods();
            for (Entry<String, Object> entry : properties.entrySet()) {
                Method setterMethod = findSetterMethodByName(methods, entry.getKey());
                if (null != setterMethod) {
                    setterMethod.invoke(result, entry.getValue());
                }
            }
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingConfigurationException(ex);
        }
    }
    
    private Method findSetterMethodByName(final Method[] methods, final String property) {
        String setterMethodName = Joiner.on("").join("set", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, property));
        for (Method each : methods) {
            if (each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length) {
                return each;
            }
        }
        return null;
    }
}

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

package org.apache.shardingsphere.infra.datasource.pool.creator;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataFactory;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaDataReflection;
import org.apache.shardingsphere.infra.datasource.pool.metadata.type.DefaultDataSourcePoolFieldMetaData;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Data source reflection.
 */
public final class DataSourceReflection {
    
    static {
        GENERAL_CLASS_TYPES = Sets.newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class, Collection.class, List.class, Properties.class);
        SKIPPED_PROPERTY_KEYS = Sets.newHashSet("loginTimeout", "driverClassName");
    }
    
    private static final Collection<Class<?>> GENERAL_CLASS_TYPES;
    
    private static final Collection<String> SKIPPED_PROPERTY_KEYS;
    
    private static final String IS_PREFIX = "is";
    
    private static final String GETTER_PREFIX = "get";
    
    private static final String SETTER_PREFIX = "set";
    
    private final DataSource dataSource;
    
    private final Method[] dataSourceMethods;
    
    public DataSourceReflection(final DataSource dataSource) {
        this.dataSource = dataSource;
        dataSourceMethods = dataSource.getClass().getMethods();
    }
    
    /**
     * Convert to properties.
     * 
     * @return properties converted from data source
     */
    public Map<String, Object> convertToProperties() {
        Map<String, Object> getterProps = convertToProperties(GETTER_PREFIX);
        Map<String, Object> isProps = convertToProperties(IS_PREFIX);
        Map<String, Object> result = new LinkedHashMap<>(getterProps.size() + isProps.size(), 1);
        result.putAll(getterProps);
        result.putAll(isProps);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<String, Object> convertToProperties(final String prefix) {
        Collection<Method> getterMethods = findAllGetterMethods(prefix);
        Map<String, Object> result = new LinkedHashMap<>(getterMethods.size(), 1);
        for (Method each : getterMethods) {
            String fieldName = getGetterFieldName(each, prefix);
            if (GENERAL_CLASS_TYPES.contains(each.getReturnType()) && !SKIPPED_PROPERTY_KEYS.contains(fieldName)) {
                Object fieldValue = each.invoke(dataSource);
                if (null != fieldValue) {
                    result.put(fieldName, fieldValue);
                }
            }
        }
        return result;
    }
    
    private Collection<Method> findAllGetterMethods(final String methodPrefix) {
        Collection<Method> result = new HashSet<>(dataSourceMethods.length);
        for (Method each : dataSourceMethods) {
            if (each.getName().startsWith(methodPrefix) && 0 == each.getParameterTypes().length) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static String getGetterFieldName(final Method method, final String methodPrefix) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, method.getName().substring(methodPrefix.length()));
    }
    
    /**
     * Set data source field.
     * 
     * @param fieldName field name
     * @param fieldValue field value
     */
    public void setField(final String fieldName, final Object fieldValue) {
        if (null != fieldValue && !isSkippedProperty(fieldName)) {
            findSetterMethod(fieldName).ifPresent(optional -> setField(optional, fieldValue));
        }
    }
    
    @SuppressWarnings("rawtypes")
    @SneakyThrows(ReflectiveOperationException.class)
    private void setField(final Method method, final Object fieldValue) {
        Class<?> paramType = method.getParameterTypes()[0];
        if (int.class == paramType || Integer.class == paramType) {
            method.invoke(dataSource, Integer.parseInt(fieldValue.toString()));
        } else if (long.class == paramType || Long.class == paramType) {
            method.invoke(dataSource, Long.parseLong(fieldValue.toString()));
        } else if (boolean.class == paramType || Boolean.class == paramType) {
            method.invoke(dataSource, Boolean.parseBoolean(fieldValue.toString()));
        } else if (String.class == paramType) {
            method.invoke(dataSource, fieldValue.toString());
        } else if (Properties.class == paramType) {
            Properties props = new Properties();
            props.putAll((Map) fieldValue);
            method.invoke(dataSource, props);
        } else {
            method.invoke(dataSource, fieldValue);
        }
    }
    
    private boolean isSkippedProperty(final String key) {
        return SKIPPED_PROPERTY_KEYS.contains(key);
    }
    
    private Optional<Method> findSetterMethod(final String fieldName) {
        String setterMethodName = SETTER_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
        return Arrays.stream(dataSourceMethods).filter(each -> each.getName().equals(setterMethodName) && 1 == each.getParameterTypes().length).findFirst();
    }
    
    /**
     * Add default data source properties.
     */
    public void addDefaultDataSourceProperties() {
        DataSourcePoolMetaDataReflection dataSourcePoolMetaDataReflection = new DataSourcePoolMetaDataReflection(dataSource,
                DataSourcePoolMetaDataFactory.newInstance(dataSource.getClass().getName()).map(DataSourcePoolMetaData::getFieldMetaData).orElseGet(DefaultDataSourcePoolFieldMetaData::new));
        String jdbcUrl = dataSourcePoolMetaDataReflection.getJdbcUrl();
        Properties jdbcConnectionProps = dataSourcePoolMetaDataReflection.getJdbcConnectionProperties();
        if (null == jdbcUrl || null == jdbcConnectionProps) {
            return;
        }
        DataSourceMetaData dataSourceMetaData = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcUrl).getDataSourceMetaData(jdbcUrl, null);
        Properties queryProps = dataSourceMetaData.getQueryProperties();
        for (Entry<Object, Object> entry : dataSourceMetaData.getDefaultQueryProperties().entrySet()) {
            String defaultPropertyKey = entry.getKey().toString();
            String defaultPropertyValue = entry.getValue().toString();
            if (!containsDefaultProperty(defaultPropertyKey, jdbcConnectionProps, queryProps)) {
                jdbcConnectionProps.setProperty(defaultPropertyKey, defaultPropertyValue);
            }
        }
    }
    
    private boolean containsDefaultProperty(final String defaultPropertyKey, final Properties targetDataSourceProps, final Properties queryProps) {
        return targetDataSourceProps.containsKey(defaultPropertyKey) || queryProps.containsKey(defaultPropertyKey);
    }
}

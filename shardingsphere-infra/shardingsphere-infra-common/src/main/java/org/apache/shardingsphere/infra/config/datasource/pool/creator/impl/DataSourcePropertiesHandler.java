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

package org.apache.shardingsphere.infra.config.datasource.pool.creator.impl;

import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Data source properties handler.
 */
@RequiredArgsConstructor
public final class DataSourcePropertiesHandler {
    
    private static final String GETTER_PREFIX = "get";
    
    private final DataSource dataSource;
            
    /**
     * Add default data source properties to target data source.
     * 
     * @param dataSourcePropertiesFieldName data source properties field name
     * @param jdbcUrlFieldName JDBC URL field name
     * @param defaultDataSourceProps default data source properties
     */
    public void addDefaultProperties(final String dataSourcePropertiesFieldName, final String jdbcUrlFieldName, final Properties defaultDataSourceProps) {
        if (null == dataSourcePropertiesFieldName || null == jdbcUrlFieldName) {
            return;
        }
        Properties targetDataSourceProps = getDataSourceProperties(dataSourcePropertiesFieldName);
        Map<String, String> jdbcUrlProps = new ConnectionURLParser(getJdbcUrl(jdbcUrlFieldName)).getProperties();
        for (Entry<Object, Object> entry : defaultDataSourceProps.entrySet()) {
            String defaultPropertyKey = entry.getKey().toString();
            String defaultPropertyValue = entry.getValue().toString();
            if (!containsDefaultProperty(defaultPropertyKey, targetDataSourceProps, jdbcUrlProps)) {
                targetDataSourceProps.setProperty(defaultPropertyKey, defaultPropertyValue);
            }
        }
    }
    
    private boolean containsDefaultProperty(final String defaultPropertyKey, final Properties targetDataSourceProps, final Map<String, String> jdbcUrlProps) {
        return targetDataSourceProps.containsKey(defaultPropertyKey) || jdbcUrlProps.containsKey(defaultPropertyKey);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Properties getDataSourceProperties(final String dataSourcePropertiesFieldName) {
        return (Properties) dataSource.getClass().getMethod(getGetterMethodName(dataSourcePropertiesFieldName)).invoke(dataSource);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private String getJdbcUrl(final String jdbcUrlFieldName) {
        return (String) dataSource.getClass().getMethod(getGetterMethodName(jdbcUrlFieldName)).invoke(dataSource);
    }
    
    private String getGetterMethodName(final String fieldName) {
        String methodName =  GETTER_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
        return methodName;
    }
}

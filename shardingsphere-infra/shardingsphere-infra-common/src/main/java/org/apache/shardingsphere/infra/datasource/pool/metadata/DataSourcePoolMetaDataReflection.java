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

package org.apache.shardingsphere.infra.datasource.pool.metadata;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Properties;

/**
 * Data source pool meta data reflection.
 */
@RequiredArgsConstructor
public final class DataSourcePoolMetaDataReflection {
    
    private final DataSource targetDataSource;
    
    private final DataSourcePoolFieldMetaData dataSourcePoolFieldMetaData;
    
    public DataSourcePoolMetaDataReflection(final DataSource targetDataSource) {
        this(targetDataSource, null);
    }
    
    /**
     * Get JDBC URL.
     *
     * @return JDBC URL
     */
    public String getJdbcUrl() {
        return null == dataSourcePoolFieldMetaData ? getFieldValue("jdbcUrl", "url") : getFieldValue(dataSourcePoolFieldMetaData.getJdbcUrlFieldName());
    }
    
    /**
     * Get username.
     * 
     * @return username
     */
    public String getUsername() {
        return null == dataSourcePoolFieldMetaData ? getFieldValue("username", "user") : getFieldValue(dataSourcePoolFieldMetaData.getUsername());
    }
    
    /**
     * Get password.
     *
     * @return password
     */
    public String getPassword() {
        return null == dataSourcePoolFieldMetaData ? getFieldValue("password") : getFieldValue(dataSourcePoolFieldMetaData.getPassword());
    }
    
    /**
     * Get JDBC connection properties.
     * 
     * @return JDBC connection properties
     */
    public Properties getJdbcConnectionProperties() {
        return null == dataSourcePoolFieldMetaData ? getFieldValue("dataSourceProperties", "connectionProperties") : getFieldValue(dataSourcePoolFieldMetaData.getJdbcUrlPropertiesFieldName());
    }
    
    private <T> T getFieldValue(final String... fieldNames) {
        for (String each : fieldNames) {
            Optional<T> result = findFieldValue(each);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> Optional<T> findFieldValue(final String fieldName) {
        Class<?> dataSourceClass = targetDataSource.getClass();
        Field field = null;
        boolean isFound = false;
        while (!isFound) {
            try {
                field = dataSourceClass.getDeclaredField(fieldName);
                isFound = true;
            } catch (final ReflectiveOperationException ignored) {
                dataSourceClass = dataSourceClass.getSuperclass();
                if (Object.class == dataSourceClass) {
                    return Optional.empty();
                }
            }
        }
        field.setAccessible(true);
        return Optional.ofNullable((T) field.get(targetDataSource));
    }
}

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
import java.util.Properties;

/**
 * Data source pool meta data reflection.
 */
@RequiredArgsConstructor
public final class DataSourcePoolMetaDataReflection {
    
    private final DataSource targetDataSource;
    
    private final DataSourcePoolFieldMetaData dataSourcePoolFieldMetaData;
    
    /**
     * Get JDBC URL.
     *
     * @return JDBC URL
     */
    public String getJdbcUrl() {
        return getFieldValue(dataSourcePoolFieldMetaData.getJdbcUrlFieldName());
    }
    
    /**
     * Get username.
     * 
     * @return username
     */
    public String getUsername() {
        return getFieldValue(dataSourcePoolFieldMetaData.getUsernameFieldName());
    }
    
    /**
     * Get password.
     *
     * @return password
     */
    public String getPassword() {
        return getFieldValue(dataSourcePoolFieldMetaData.getPasswordFieldName());
    }
    
    /**
     * Get JDBC connection properties.
     * 
     * @return JDBC connection properties
     */
    public Properties getJdbcConnectionProperties() {
        return getFieldValue(dataSourcePoolFieldMetaData.getJdbcUrlPropertiesFieldName());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> T getFieldValue(final String fieldName) {
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
                    return null;
                }
            }
        }
        field.setAccessible(true);
        return (T) field.get(targetDataSource);
    }
}

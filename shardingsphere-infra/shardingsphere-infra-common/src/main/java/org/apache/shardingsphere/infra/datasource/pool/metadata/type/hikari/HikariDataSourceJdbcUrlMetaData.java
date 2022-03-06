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

package org.apache.shardingsphere.infra.datasource.pool.metadata.type.hikari;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourceJdbcUrlMetaData;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Hikari data source JDBC URL meta data.
 */
public final class HikariDataSourceJdbcUrlMetaData implements DataSourceJdbcUrlMetaData {
    
    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return "dataSourceProperties";
    }
    
    @Override
    public Properties getJdbcUrlProperties(final DataSource targetDataSource) {
        return (Properties) getFieldValue(targetDataSource, "dataSourceProperties");
    }
    
    @Override
    public void appendJdbcUrlProperties(final String key, final String value, final DataSource targetDataSource) {
        ((Properties) getFieldValue(targetDataSource, "dataSourceProperties")).put(key, value);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object getFieldValue(final DataSource targetDataSource, final String fieldName) {
        Class<?> dataSourceClass = targetDataSource.getClass();
        Field field = null;
        boolean found = false;
        while (!found) {
            try {
                field = dataSourceClass.getDeclaredField(fieldName);
                found = true;
            } catch (final ReflectiveOperationException ex) {
                dataSourceClass = dataSourceClass.getSuperclass();
                if (Object.class == dataSourceClass) {
                    throw ex;
                }
            }
        }
        field.setAccessible(true);
        return field.get(targetDataSource);
    }
}

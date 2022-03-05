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

package org.apache.shardingsphere.infra.datasource.pool.metadata.fixture;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourceJdbcUrlMetaData;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Properties;

public final class MockedDataSourceJdbcUrlMetaData implements DataSourceJdbcUrlMetaData {
    
    @Override
    public String getJdbcUrl(final DataSource targetDataSource) {
        return (String) getFieldValue(targetDataSource, "url");
    }
    
    @Override
    public String getUsername(final DataSource targetDataSource) {
        return (String) getFieldValue(targetDataSource, "username");
    }
    
    @Override
    public String getPassword(final DataSource targetDataSource) {
        return (String) getFieldValue(targetDataSource, "password");
    }
    
    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return null;
    }
    
    @Override
    public Properties getJdbcUrlProperties(final DataSource targetDataSource) {
        return new Properties();
    }
    
    @Override
    public void appendJdbcUrlProperties(final String key, final String value, final DataSource targetDataSource) {
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object getFieldValue(final DataSource targetDataSource, final String fieldName) {
        Field field = targetDataSource.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(targetDataSource);
    }
}

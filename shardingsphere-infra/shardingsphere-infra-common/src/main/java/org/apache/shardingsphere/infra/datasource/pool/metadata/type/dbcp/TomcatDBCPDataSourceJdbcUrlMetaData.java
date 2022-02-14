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

package org.apache.shardingsphere.infra.datasource.pool.metadata.type.dbcp;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourceJdbcUrlMetaData;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Tomcat DBCP data source JDBC URL meta data.
 */
public final class TomcatDBCPDataSourceJdbcUrlMetaData implements DataSourceJdbcUrlMetaData<BasicDataSource> {
    
    @Override
    public String getJdbcUrl(final BasicDataSource targetDataSource) {
        return targetDataSource.getUrl();
    }
    
    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return "connectionProperties";
    }
    
    @Override
    @SneakyThrows(ReflectiveOperationException.class)
    public Properties getJdbcUrlProperties(final BasicDataSource targetDataSource) {
        Field field = BasicDataSource.class.getDeclaredField("connectionProperties");
        field.setAccessible(true);
        return (Properties) field.get(targetDataSource);
    }
    
    @Override
    public void appendJdbcUrlProperties(final String key, final String value, final BasicDataSource targetDataSource) {
        targetDataSource.addConnectionProperty(key, value);
    }
}

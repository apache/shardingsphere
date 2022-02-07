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

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourceJdbcUrlMetaData;

import java.util.Properties;

/**
 * Hikari data source JDBC URL meta data.
 */
public final class HikariDataSourceJdbcUrlMetaData implements DataSourceJdbcUrlMetaData<HikariDataSource> {
    
    @Override
    public String getJdbcUrl(final HikariDataSource targetDataSource) {
        return targetDataSource.getJdbcUrl();
    }
    
    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return "dataSourceProperties";
    }
    
    @Override
    public Properties getJdbcUrlProperties(final HikariDataSource targetDataSource) {
        return targetDataSource.getDataSourceProperties();
    }
    
    @Override
    public void appendJdbcUrlProperties(final String key, final String value, final HikariDataSource targetDataSource) {
        targetDataSource.getDataSourceProperties().put(key, value);
    }
}

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

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreationMetaData;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Hikari data source pool creator.
 */
@Getter
public final class HikariDataSourcePoolCreationMetaData implements DataSourcePoolCreationMetaData {
    
    private final Map<String, Object> defaultProperties = new HashMap<>(6, 1);
    
    private final Map<String, Object> invalidProperties = new HashMap<>(2, 1);
    
    private final Map<String, String> propertySynonyms = new HashMap<>(2, 1);
    
    private final Properties defaultJdbcUrlProperties = new Properties();
    
    public HikariDataSourcePoolCreationMetaData() {
        buildDefaultProperties();
        buildInvalidProperties();
        buildPropertySynonyms();
        buildDefaultJdbcUrlProperties();
    }
    
    private void buildDefaultProperties() {
        defaultProperties.put("connectionTimeout", 30 * 1000L);
        defaultProperties.put("idleTimeout", 60 * 1000L);
        defaultProperties.put("maxLifetime", 30 * 70 * 1000L);
        defaultProperties.put("maximumPoolSize", 50);
        defaultProperties.put("minimumIdle", 1);
        defaultProperties.put("readOnly", false);
    }
    
    private void buildInvalidProperties() {
        invalidProperties.put("minimumIdle", -1);
        invalidProperties.put("maximumPoolSize", -1);
    }
    
    private void buildPropertySynonyms() {
        propertySynonyms.put("maxPoolSize", "maximumPoolSize");
        propertySynonyms.put("minPoolSize", "minimumIdle");
    }
    
    private void buildDefaultJdbcUrlProperties() {
        defaultJdbcUrlProperties.setProperty("useServerPrepStmts", Boolean.TRUE.toString());
        defaultJdbcUrlProperties.setProperty("cachePrepStmts", Boolean.TRUE.toString());
        defaultJdbcUrlProperties.setProperty("prepStmtCacheSize", "200000");
        defaultJdbcUrlProperties.setProperty("prepStmtCacheSqlLimit", "2048");
        defaultJdbcUrlProperties.setProperty("useLocalSessionState", Boolean.TRUE.toString());
        defaultJdbcUrlProperties.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        defaultJdbcUrlProperties.setProperty("cacheResultSetMetadata", Boolean.FALSE.toString());
        defaultJdbcUrlProperties.setProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        defaultJdbcUrlProperties.setProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        defaultJdbcUrlProperties.setProperty("maintainTimeStats", Boolean.FALSE.toString());
        defaultJdbcUrlProperties.setProperty("netTimeoutForStreamingResults", "0");
        defaultJdbcUrlProperties.setProperty("tinyInt1isBit", Boolean.FALSE.toString());
        defaultJdbcUrlProperties.setProperty("useSSL", Boolean.FALSE.toString());
        defaultJdbcUrlProperties.setProperty("serverTimezone", "UTC");
    }
    
    @Override
    public String getJdbcUrlFieldName() {
        return "jdbcUrl";
    }
    
    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return "dataSourceProperties";
    }
    
    @Override
    public String getType() {
        return HikariDataSource.class.getCanonicalName();
    }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Hikari data source pool creator.
 */
@Getter
public final class HikariDataSourcePoolCreator extends AbstractDataSourcePoolCreator {
    
    private final Map<String, String> propertySynonyms = new HashMap<>(2, 1);
    
    private final Properties defaultDataSourceProperties = new Properties();
    
    private final Map<String, Object> invalidProperties = new HashMap<>(2, 1);
    
    public HikariDataSourcePoolCreator() {
        buildPropertySynonyms();
        buildDefaultDataSourceProperties();
        buildInvalidProperties();
    }
    
    private void buildPropertySynonyms() {
        propertySynonyms.put("maxPoolSize", "maximumPoolSize");
        propertySynonyms.put("minPoolSize", "minimumIdle");
    }
    
    private void buildDefaultDataSourceProperties() {
        defaultDataSourceProperties.setProperty("useServerPrepStmts", Boolean.TRUE.toString());
        defaultDataSourceProperties.setProperty("cachePrepStmts", Boolean.TRUE.toString());
        defaultDataSourceProperties.setProperty("prepStmtCacheSize", "200000");
        defaultDataSourceProperties.setProperty("prepStmtCacheSqlLimit", "2048");
        defaultDataSourceProperties.setProperty("useLocalSessionState", Boolean.TRUE.toString());
        defaultDataSourceProperties.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        defaultDataSourceProperties.setProperty("cachedefaultDataSourcePropsSetMetadata", Boolean.FALSE.toString());
        defaultDataSourceProperties.setProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        defaultDataSourceProperties.setProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        defaultDataSourceProperties.setProperty("maintainTimeStats", Boolean.FALSE.toString());
        defaultDataSourceProperties.setProperty("netTimeoutForStreamingdefaultDataSourcePropss", "0");
        defaultDataSourceProperties.setProperty("tinyInt1isBit", Boolean.FALSE.toString());
        defaultDataSourceProperties.setProperty("useSSL", Boolean.FALSE.toString());
        defaultDataSourceProperties.setProperty("serverTimezone", "UTC");
    }
    
    private void buildInvalidProperties() {
        invalidProperties.put("minimumIdle", -1);
        invalidProperties.put("maximumPoolSize", -1);
    }
    
    @Override
    protected String getJdbcUrlPropertyName() {
        return "jdbcUrl";
    }
    
    @Override
    protected String getDataSourcePropertiesPropertyName() {
        return "dataSourceProperties";
    }
    
    @Override
    public String getType() {
        return HikariDataSource.class.getCanonicalName();
    }
}

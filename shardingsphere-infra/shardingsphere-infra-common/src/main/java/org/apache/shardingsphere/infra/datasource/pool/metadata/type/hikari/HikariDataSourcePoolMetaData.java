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
import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Hikari data source pool meta data.
 */
@Getter
public final class HikariDataSourcePoolMetaData implements DataSourcePoolMetaData<HikariDataSource> {
    
    private final Map<String, Object> defaultProperties = new HashMap<>(6, 1);
    
    private final Map<String, Object> invalidProperties = new HashMap<>(2, 1);
    
    private final Map<String, String> propertySynonyms = new HashMap<>(6, 1);
    
    private final Collection<String> transientFieldNames = new LinkedList<>();
    
    public HikariDataSourcePoolMetaData() {
        buildDefaultProperties();
        buildInvalidProperties();
        buildPropertySynonyms();
        buildTransientFieldNames();
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
        propertySynonyms.put("url", "jdbcUrl");
        propertySynonyms.put("connectionTimeoutMilliseconds", "connectionTimeout");
        propertySynonyms.put("idleTimeoutMilliseconds", "idleTimeout");
        propertySynonyms.put("maxLifetimeMilliseconds", "maxLifetime");
        propertySynonyms.put("maxPoolSize", "maximumPoolSize");
        propertySynonyms.put("minPoolSize", "minimumIdle");
    }
    
    private void buildTransientFieldNames() {
        transientFieldNames.add("running");
        transientFieldNames.add("poolName");
        transientFieldNames.add("registerMbeans");
        transientFieldNames.add("closed");
    }
    
    @Override
    public HikariDataSourceJdbcUrlMetaData getJdbcUrlMetaData() {
        return new HikariDataSourceJdbcUrlMetaData();
    }
    
    @Override
    public String getType() {
        return HikariDataSource.class.getName();
    }
}

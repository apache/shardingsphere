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

package org.apache.shardingsphere.infra.datasource.hikari.metadata;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolPropertiesValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Hikari data source pool meta data.
 */
public final class HikariDataSourcePoolMetaData implements DataSourcePoolMetaData {
    
    private static final Map<String, Object> DEFAULT_PROPS = new HashMap<>(6, 1F);
    
    private static final Map<String, Object> INVALID_PROPS = new HashMap<>(2, 1F);
    
    private static final Map<String, String> PROP_SYNONYMS = new HashMap<>(6, 1F);
    
    private static final Collection<String> TRANSIENT_FIELD_NAMES = new LinkedList<>();
    
    static {
        buildDefaultProperties();
        buildInvalidProperties();
        buildPropertySynonyms();
        buildTransientFieldNames();
    }
    
    private static void buildDefaultProperties() {
        DEFAULT_PROPS.put("connectionTimeout", 30 * 1000L);
        DEFAULT_PROPS.put("idleTimeout", 60 * 1000L);
        DEFAULT_PROPS.put("maxLifetime", 30 * 70 * 1000L);
        DEFAULT_PROPS.put("maximumPoolSize", 50);
        DEFAULT_PROPS.put("minimumIdle", 1);
        DEFAULT_PROPS.put("readOnly", false);
        DEFAULT_PROPS.put("keepaliveTime", 0);
    }
    
    private static void buildInvalidProperties() {
        INVALID_PROPS.put("minimumIdle", -1);
        INVALID_PROPS.put("maximumPoolSize", -1);
    }
    
    private static void buildPropertySynonyms() {
        PROP_SYNONYMS.put("url", "jdbcUrl");
        PROP_SYNONYMS.put("connectionTimeoutMilliseconds", "connectionTimeout");
        PROP_SYNONYMS.put("idleTimeoutMilliseconds", "idleTimeout");
        PROP_SYNONYMS.put("maxLifetimeMilliseconds", "maxLifetime");
        PROP_SYNONYMS.put("maxPoolSize", "maximumPoolSize");
        PROP_SYNONYMS.put("minPoolSize", "minimumIdle");
    }
    
    private static void buildTransientFieldNames() {
        TRANSIENT_FIELD_NAMES.add("running");
        TRANSIENT_FIELD_NAMES.add("poolName");
        TRANSIENT_FIELD_NAMES.add("closed");
    }
    
    @Override
    public Map<String, Object> getDefaultProperties() {
        return DEFAULT_PROPS;
    }
    
    @Override
    public Map<String, Object> getInvalidProperties() {
        return INVALID_PROPS;
    }
    
    @Override
    public Map<String, String> getPropertySynonyms() {
        return PROP_SYNONYMS;
    }
    
    @Override
    public Collection<String> getTransientFieldNames() {
        return TRANSIENT_FIELD_NAMES;
    }
    
    @Override
    public HikariDataSourcePoolFieldMetaData getFieldMetaData() {
        return new HikariDataSourcePoolFieldMetaData();
    }
    
    @Override
    public DataSourcePoolPropertiesValidator getDataSourcePoolPropertiesValidator() {
        return new HikariDataSourcePoolPropertiesValidator();
    }
    
    @Override
    public String getType() {
        return "com.zaxxer.hikari.HikariDataSource";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}

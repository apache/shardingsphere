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

package org.apache.shardingsphere.mode.repository.cluster.nacos.props.metadata.type;

import org.apache.shardingsphere.mode.repository.cluster.nacos.props.metadata.DataSourceMetaData;

import java.util.HashMap;
import java.util.Map;

/**
 * Hikari data source meta data.
 */
public class HikariDataSourceMetaData implements DataSourceMetaData {
    
    private static final Map<String, String> PROPERTY_SYNONYMS = new HashMap<>(6, 1);
    
    static {
        PROPERTY_SYNONYMS.put("url", "jdbcUrl");
        PROPERTY_SYNONYMS.put("connectionTimeoutMilliseconds", "connectionTimeout");
        PROPERTY_SYNONYMS.put("idleTimeoutMilliseconds", "idleTimeout");
        PROPERTY_SYNONYMS.put("maxLifetimeMilliseconds", "maxLifetime");
        PROPERTY_SYNONYMS.put("maxPoolSize", "maximumPoolSize");
        PROPERTY_SYNONYMS.put("minPoolSize", "minimumIdle");
    }
    
    @Override
    public Map<String, String> getPropertySynonyms() {
        return PROPERTY_SYNONYMS;
    }
    
    @Override
    public String getType() {
        return "com.zaxxer.hikari.HikariDataSource";
    }
}

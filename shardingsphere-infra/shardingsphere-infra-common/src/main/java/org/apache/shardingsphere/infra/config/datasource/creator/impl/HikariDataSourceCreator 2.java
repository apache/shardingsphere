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

package org.apache.shardingsphere.infra.config.datasource.creator.impl;

import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Hikari data source creator.
 */
public final class HikariDataSourceCreator extends AbstractDataSourceCreator {
    
    private final Map<String, Object> skippedProperties = new HashMap<>(2, 1);
    
    public HikariDataSourceCreator() {
        skippedProperties.put("minimumIdle", -1);
        skippedProperties.put("maximumPoolSize", -1);
    }
    
    @Override
    public DataSource createDataSource(final DataSourceConfiguration dataSourceConfig) {
        addPropertySynonyms(dataSourceConfig);
        DataSource result = buildDataSource(dataSourceConfig.getDataSourceClassName());
        Method[] methods = result.getClass().getMethods();
        for (Entry<String, Object> entry : dataSourceConfig.getAllProps().entrySet()) {
            if (isInvalidProperty(entry.getKey(), entry.getValue())) {
                continue;
            }
            setField(result, methods, entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private void addPropertySynonyms(final DataSourceConfiguration dataSourceConfig) {
        dataSourceConfig.addPropertySynonym("maxPoolSize", "maximumPoolSize");
        dataSourceConfig.addPropertySynonym("minPoolSize", "minimumIdle");
    }
    
    private boolean isInvalidProperty(final String property, final Object value) {
        return skippedProperties.containsKey(property) && null != value && value.equals(skippedProperties.get(property));
    }
    
    @Override
    public DataSourceConfiguration createDataSourceConfiguration(final DataSource dataSource) {
        return buildDataSourceConfig(dataSource);
    }
    
    @Override
    public String getType() {
        return "com.zaxxer.hikari.HikariDataSource";
    }
}

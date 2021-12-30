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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Default data source properties handler.
 */
public final class DefaultDataSourcePropertiesHandler {
    
    private final Properties targetDataSourceProps;
    
    private final Map<String, String> jdbcUrlProps;
    
    private final Properties defaultDataSourceProps;
    
    public DefaultDataSourcePropertiesHandler(final Properties targetDataSourceProps, final String jdbcUrl, final Properties defaultDataSourceProps) {
        this.targetDataSourceProps = targetDataSourceProps;
        jdbcUrlProps = new ConnectionURLParser(jdbcUrl).getProperties();
        this.defaultDataSourceProps = defaultDataSourceProps;
    }
    
    /**
     * Add default data source properties to target data source properties.
     */
    public void addDefaultProperties() {
        for (Entry<Object, Object> entry : defaultDataSourceProps.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            if (!targetDataSourceProps.containsKey(key) && !jdbcUrlProps.containsKey(key)) {
                targetDataSourceProps.setProperty(key, value);
            }
        }
    }
}

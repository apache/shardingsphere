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

package org.apache.shardingsphere.infra.datasource.pool.metadata.type;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Default data source pool meta data.
 */
public final class DefaultDataSourcePoolMetaData implements DataSourcePoolMetaData<DataSource> {
    
    @Override
    public Map<String, Object> getDefaultProperties() {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, Object> getInvalidProperties() {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, String> getPropertySynonyms() {
        return Collections.emptyMap();
    }
    
    @Override
    public String getJdbcUrl(final DataSource targetDataSource) {
        return null;
    }
    
    @Override
    public String getJdbcUrlPropertiesFieldName() {
        return null;
    }
    
    @Override
    public Properties getJdbcUrlProperties(final DataSource targetDataSource) {
        return null;
    }
    
    @Override
    public void appendJdbcUrlProperties(final String key, final String value, final DataSource targetDataSource) {
    }
    
    @Override
    public Collection<String> getTransientFieldNames() {
        return Collections.emptyList();
    }
    
    @Override
    public String getType() {
        return "Default";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}

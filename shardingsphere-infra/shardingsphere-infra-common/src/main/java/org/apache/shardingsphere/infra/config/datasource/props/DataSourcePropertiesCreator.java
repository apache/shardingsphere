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

package org.apache.shardingsphere.infra.config.datasource.props;

import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourceReflection;
import org.apache.shardingsphere.infra.config.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.config.datasource.pool.metadata.DataSourcePoolMetaDataFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data source properties creator.
 */
public final class DataSourcePropertiesCreator {
    
    static {
        ShardingSphereServiceLoader.register(DataSourcePoolMetaData.class);
    }
    
    private final DataSourcePoolMetaData poolMetaData;
    
    public DataSourcePropertiesCreator(final String dataSourceClassName) {
        poolMetaData = DataSourcePoolMetaDataFactory.newInstance(dataSourceClassName);
    }
    
    /**
     * Create data source properties.
     * 
     * @param dataSource data source
     * @return created data source properties
     */
    public DataSourceProperties createDataSourceProperties(final DataSource dataSource) {
        DataSourceProperties result = new DataSourceProperties(dataSource.getClass().getName());
        result.getProps().putAll(createProperties(dataSource));
        return result;
    }
    
    private Map<String, Object> createProperties(final DataSource dataSource) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Entry<String, Object> entry : new DataSourceReflection(dataSource).convertToProperties().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            if (isValidProperty(propertyName, propertyValue)) {
                result.put(propertyName, propertyValue);
            }
        }
        return result;
    }
    
    private boolean isValidProperty(final String key, final Object value) {
        return !poolMetaData.getInvalidProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getInvalidProperties().get(key));
    }
}

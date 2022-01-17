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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourceReflection;
import org.apache.shardingsphere.infra.config.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.config.datasource.pool.metadata.DataSourcePoolMetaDataFactory;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data source properties creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePropertiesCreator {
    
    /**
     * Create data source properties.
     *
     * @param dataSourceMap data source map
     * @return created data source properties
     */
    public static Map<String, DataSourceProperties> create(final Map<String, DataSource> dataSourceMap) {
        return dataSourceMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Create data source properties.
     * 
     * @param dataSource data source
     * @return created data source properties
     */
    public static DataSourceProperties create(final DataSource dataSource) {
        return new DataSourceProperties(dataSource.getClass().getName(), createProperties(dataSource));
    }
    
    private static Map<String, Object> createProperties(final DataSource dataSource) {
        Map<String, Object> result = new LinkedHashMap<>();
        DataSourcePoolMetaData poolMetaData = DataSourcePoolMetaDataFactory.newInstance(dataSource.getClass().getName());
        for (Entry<String, Object> entry : new DataSourceReflection(dataSource).convertToProperties().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            if (isValidProperty(propertyName, propertyValue, poolMetaData)) {
                result.put(propertyName, propertyValue);
            }
        }
        return result;
    }
    
    private static boolean isValidProperty(final String key, final Object value, final DataSourcePoolMetaData poolMetaData) {
        return !poolMetaData.getInvalidProperties().containsKey(key) || null == value || !value.equals(poolMetaData.getInvalidProperties().get(key));
    }
}

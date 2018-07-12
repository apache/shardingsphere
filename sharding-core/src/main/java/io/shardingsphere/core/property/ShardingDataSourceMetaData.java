/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.property;

import io.shardingsphere.core.constant.DatabaseType;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding data source meta data.
 *
 * @author panjuan
 */
public class ShardingDataSourceMetaData {
    
    private final Map<String, DataSourceMetaData> dataSourceMetaDataMap;
    
    public ShardingDataSourceMetaData(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) {
        dataSourceMetaDataMap = getDataSourceMetaDataMap(dataSourceMap, databaseType);
    }
    
    private Map<String, DataSourceMetaData> getDataSourceMetaDataMap(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) {
        Map<String, DataSourceMetaData> result = new LinkedHashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), DataSourceMetaDataFactory.getDataSourceMetaData(databaseType, entry.getValue()));
        }
        return result;
    }
    
    /**
     * Get all instance data source names.
     *
     * @return instance data source names
     */
    public Collection<String> getAllInstanceDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        for (Entry<String, DataSourceMetaData> entry : dataSourceMetaDataMap.entrySet()) {
            if (!isExisted(entry.getKey(), result)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
    
    private boolean isExisted(final String dataSourceName, final Collection<String> existedDataSourceNames) {
        for (String each : existedDataSourceNames) {
            if (dataSourceMetaDataMap.get(each).isInSameDatabaseInstance(dataSourceMetaDataMap.get(dataSourceName))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get actual schema name.
     *
     * @param actualDataSourceName actual data source name
     * @return actual schema name
     */
    public String getActualSchemaName(final String actualDataSourceName) {
        return dataSourceMetaDataMap.get(actualDataSourceName).getSchemeName();
    }
}

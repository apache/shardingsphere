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

package org.apache.shardingsphere.infra.metadata.resource;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data sources meta data.
 */
public final class DataSourcesMetaData {
    
    private final Map<String, DataSourceMetaData> dataSourceMetaDataMap;
    
    public DataSourcesMetaData(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        dataSourceMetaDataMap = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Map<String, Object> standardProps = DataSourcePropertiesCreator.create(entry.getValue()).getConnectionPropertySynonyms().getStandardProperties();
            dataSourceMetaDataMap.put(entry.getKey(), databaseType.getDataSourceMetaData(standardProps.get("url").toString(), standardProps.get("username").toString()));
        }
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
        DataSourceMetaData dataSourceMetaData = dataSourceMetaDataMap.get(dataSourceName);
        return existedDataSourceNames.stream().anyMatch(each -> dataSourceMetaData.isInSameDatabaseInstance(dataSourceMetaDataMap.get(each)));
    }
    
    /**
     * Get data source meta data.
     * 
     * @param dataSourceName data source name
     * @return data source meta data
     */
    public DataSourceMetaData getDataSourceMetaData(final String dataSourceName) {
        return dataSourceMetaDataMap.get(dataSourceName);
    }
}

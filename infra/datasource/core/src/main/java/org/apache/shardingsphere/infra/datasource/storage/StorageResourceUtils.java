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

package org.apache.shardingsphere.infra.datasource.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageResourceUtils {
    
    /**
     * Get storage node data sources.
     *
     * @param dataSources data sources
     * @return storage node data sources
     */
    public static Map<StorageNode, DataSource> getStorageNodeDataSources(final Map<String, DataSource> dataSources) {
        Map<StorageNode, DataSource> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            result.put(new StorageNode(entry.getKey()), entry.getValue());
        }
        return result;
    }
    
    /**
     * Get storage unit node mappers from provided data sources.
     *
     * @param dataSources data sources
     * @return storage unit node mappers
     */
    public static Map<String, StorageUnitNodeMapper> getStorageUnitNodeMappers(final Map<String, DataSource> dataSources) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            DataSourcePoolProperties dataSourcePoolProperties = DataSourcePoolPropertiesCreator.create(entry.getValue());
            String url = dataSourcePoolProperties.getConnectionPropertySynonyms().getStandardProperties().get("url").toString();
            result.put(entry.getKey(), new StorageUnitNodeMapper(entry.getKey(), new StorageNode(entry.getKey()), url));
        }
        return result;
    }
}

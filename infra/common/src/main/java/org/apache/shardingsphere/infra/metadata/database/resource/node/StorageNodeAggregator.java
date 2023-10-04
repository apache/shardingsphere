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

package org.apache.shardingsphere.infra.metadata.database.resource.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.connector.url.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Storage node aggregator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageNodeAggregator {
    
    /**
     * Aggregate data source map to storage node name grouped.
     *
     * @param dataSourceMap storage unit name and data source map
     * @return storage node name and data source map
     */
    public static Map<StorageNodeName, DataSource> aggregateDataSources(final Map<String, DataSource> dataSourceMap) {
        return dataSourceMap.entrySet().stream().collect(
                Collectors.toMap(entry -> new StorageNodeName(entry.getKey()), Entry::getValue, (oldValue, currentValue) -> currentValue, () -> new LinkedHashMap<>(dataSourceMap.size(), 1F)));
    }
    
    /**
     * Aggregate data source pool properties map to storage node name grouped.
     *
     * @param storageUnitDataSourcePoolPropsMap storage unit name and data source pool properties map
     * @return storage node name and data source pool properties map
     */
    public static Map<StorageNodeName, DataSourcePoolProperties> aggregateDataSourcePoolProperties(final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolPropsMap) {
        Map<StorageNodeName, DataSourcePoolProperties> result = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : storageUnitDataSourcePoolPropsMap.entrySet()) {
            Map<String, Object> standardProps = entry.getValue().getConnectionPropertySynonyms().getStandardProperties();
            String url = standardProps.get("url").toString();
            boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
            StorageNodeName storageNodeName = getStorageNodeName(entry.getKey(), url, standardProps.get("username").toString(), isInstanceConnectionAvailable);
            result.putIfAbsent(storageNodeName, entry.getValue());
        }
        return result;
    }
    
    private static StorageNodeName getStorageNodeName(final String dataSourceName, final String url, final String username, final boolean isInstanceConnectionAvailable) {
        try {
            JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
            return isInstanceConnectionAvailable ? new StorageNodeName(jdbcUrl.getHostname(), jdbcUrl.getPort(), username) : new StorageNodeName(dataSourceName);
        } catch (final UnrecognizedDatabaseURLException ex) {
            return new StorageNodeName(dataSourceName);
        }
    }
}

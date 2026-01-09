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
import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
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
     * Aggregate data source map to storage node grouped.
     *
     * @param dataSourceMap storage unit name and data source map
     * @return storage node and data source map
     */
    public static Map<StorageNode, DataSource> aggregateDataSources(final Map<String, DataSource> dataSourceMap) {
        return dataSourceMap.entrySet().stream().collect(
                Collectors.toMap(entry -> new StorageNode(entry.getKey()), Entry::getValue, (oldValue, currentValue) -> currentValue, () -> new LinkedHashMap<>(dataSourceMap.size(), 1F)));
    }
    
    /**
     * Aggregate data source pool properties map to storage node grouped.
     *
     * @param storageUnitDataSourcePoolPropsMap storage unit name and data source pool properties map
     * @param isInstanceConnectionEnabled is instance connection enabled
     * @return storage node and data source pool properties map
     */
    public static Map<StorageNode, DataSourcePoolProperties> aggregateDataSourcePoolProperties(final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolPropsMap,
                                                                                               final boolean isInstanceConnectionEnabled) {
        Map<StorageNode, DataSourcePoolProperties> result = new LinkedHashMap<>(storageUnitDataSourcePoolPropsMap.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : storageUnitDataSourcePoolPropsMap.entrySet()) {
            Map<String, Object> standardProps = entry.getValue().getConnectionPropertySynonyms().getStandardProperties();
            String url = standardProps.get("url").toString();
            String username = standardProps.get("username").toString();
            DatabaseType databaseType = DatabaseTypeFactory.get(url);
            StorageNode storageNode = getStorageNode(entry.getKey(), url, username, databaseType, isInstanceConnectionEnabled);
            result.putIfAbsent(storageNode, entry.getValue());
        }
        return result;
    }
    
    private static StorageNode getStorageNode(final String dataSourceName, final String url, final String username,
                                              final DatabaseType databaseType, final boolean isInstanceConnectionEnabled) {
        try {
            ConnectionProperties connectionProps = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType).parse(url, username, null);
            boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getConnectionOption().isInstanceConnectionAvailable();
            return isInstanceConnectionEnabled && isInstanceConnectionAvailable
                    ? new StorageNode(connectionProps.getHostname(), connectionProps.getPort(), username)
                    : new StorageNode(dataSourceName);
        } catch (final UnrecognizedDatabaseURLException ex) {
            return new StorageNode(dataSourceName);
        }
    }
}

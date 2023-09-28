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

package org.apache.shardingsphere.infra.metadata.database.resource.unit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.connector.url.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Storage unit node mapper utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageUnitNodeMapperUtils {
    
    /**
     * Get storage unit node mappers from data sources.
     *
     * @param dataSources data sources
     * @return storage unit node mappers
     */
    public static Map<String, StorageUnitNodeMapper> fromDataSources(final Map<String, DataSource> dataSources) {
        return dataSources.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> fromDataSource(entry.getKey(), entry.getValue()), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    private static StorageUnitNodeMapper fromDataSource(final String storageUnitName, final DataSource dataSource) {
        DataSourcePoolProperties props = DataSourcePoolPropertiesCreator.create(dataSource);
        String url = props.getConnectionPropertySynonyms().getStandardProperties().get("url").toString();
        return new StorageUnitNodeMapper(storageUnitName, new StorageNode(new StorageNodeName(storageUnitName), url));
    }
    
    /**
     * Get storage unit node mappers from data source pool properties.
     *
     * @param propsMap data source pool properties map
     * @return storage unit node mappers
     */
    public static Map<String, StorageUnitNodeMapper> fromDataSourcePoolProperties(final Map<String, DataSourcePoolProperties> propsMap) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            String storageUnitName = entry.getKey();
            result.put(storageUnitName, fromDataSourcePoolProperties(storageUnitName, entry.getValue()));
        }
        return result;
    }
    
    private static StorageUnitNodeMapper fromDataSourcePoolProperties(final String storageUnitName, final DataSourcePoolProperties props) {
        Map<String, Object> standardProps = props.getConnectionPropertySynonyms().getStandardProperties();
        String url = standardProps.get("url").toString();
        boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
        StorageNodeName storageNodeName = getStorageNodeName(storageUnitName, url, standardProps.get("username").toString(), isInstanceConnectionAvailable);
        return createStorageUnitNodeMapper(storageNodeName, storageUnitName, url, isInstanceConnectionAvailable);
    }
    
    private static StorageNodeName getStorageNodeName(final String dataSourceName, final String url, final String username, final boolean isInstanceConnectionAvailable) {
        try {
            JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
            return isInstanceConnectionAvailable ? new StorageNodeName(jdbcUrl.getHostname(), jdbcUrl.getPort(), username) : new StorageNodeName(dataSourceName);
        } catch (final UnrecognizedDatabaseURLException ex) {
            return new StorageNodeName(dataSourceName);
        }
    }
    
    private static StorageUnitNodeMapper createStorageUnitNodeMapper(final StorageNodeName storageNodeName,
                                                                     final String storageUnitName, final String url, final boolean isInstanceConnectionAvailable) {
        return isInstanceConnectionAvailable
                ? new StorageUnitNodeMapper(storageUnitName, new StorageNode(storageNodeName, url, new StandardJdbcUrlParser().parse(url).getDatabase()))
                : new StorageUnitNodeMapper(storageUnitName, new StorageNode(storageNodeName, url));
    }
    
    /**
     * Get storage node grouped data source pool properties map.
     *
     * @param storageUnitDataSourcePoolProps storage unit grouped data source pool properties map
     * @return storage node grouped data source pool properties map
     */
    public static Map<StorageNodeName, DataSourcePoolProperties> getStorageNodeDataSourcePoolProperties(final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Map<StorageNodeName, DataSourcePoolProperties> result = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : storageUnitDataSourcePoolProps.entrySet()) {
            Map<String, Object> standardProps = entry.getValue().getConnectionPropertySynonyms().getStandardProperties();
            String url = standardProps.get("url").toString();
            boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
            StorageNodeName storageNodeName = getStorageNodeName(entry.getKey(), url, standardProps.get("username").toString(), isInstanceConnectionAvailable);
            result.putIfAbsent(storageNodeName, entry.getValue());
        }
        return result;
    }
}

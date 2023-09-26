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

package org.apache.shardingsphere.infra.metadata.database.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.connector.url.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapper;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storage resource creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageResourceCreator {
    
    /**
     * Create storage resource.
     *
     * @param propsMap data source pool properties map
     * @return created storage resource
     */
    public static StorageResource createStorageResource(final Map<String, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>();
        Map<String, StorageUnitNodeMapper> mappers = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            String storageUnitName = entry.getKey();
            StorageUnitNodeMapper mapper = getStorageUnitNodeMapper(storageUnitName, entry.getValue());
            mappers.put(storageUnitName, mapper);
            storageNodes.computeIfAbsent(mapper.getStorageNode(), key -> DataSourcePoolCreator.create(storageUnitName, entry.getValue(), true, storageNodes.values()));
        }
        return new StorageResource(storageNodes, mappers);
    }
    
    /**
     * Get storage unit node mappers.
     *
     * @param propsMap data source pool properties map
     * @return storage unit node mappers
     */
    public static Map<String, StorageUnitNodeMapper> getStorageUnitNodeMappers(final Map<String, DataSourcePoolProperties> propsMap) {
        Map<String, StorageUnitNodeMapper> result = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            String storageUnitName = entry.getKey();
            result.put(storageUnitName, getStorageUnitNodeMapper(storageUnitName, entry.getValue()));
        }
        return result;
    }
    
    private static StorageUnitNodeMapper getStorageUnitNodeMapper(final String storageUnitName, final DataSourcePoolProperties props) {
        Map<String, Object> standardProps = props.getConnectionPropertySynonyms().getStandardProperties();
        String url = standardProps.get("url").toString();
        boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
        StorageNode storageNode = new StorageNode(getStorageNodeName(storageUnitName, url, standardProps.get("username").toString(), isInstanceConnectionAvailable));
        return createStorageUnitNodeMapper(storageNode, storageUnitName, url, isInstanceConnectionAvailable);
    }
    
    private static String getStorageNodeName(final String dataSourceName, final String url, final String username, final boolean isInstanceConnectionAvailable) {
        try {
            JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
            return isInstanceConnectionAvailable ? generateStorageNodeName(jdbcUrl.getHostname(), jdbcUrl.getPort(), username) : dataSourceName;
        } catch (final UnrecognizedDatabaseURLException ex) {
            return dataSourceName;
        }
    }
    
    private static String generateStorageNodeName(final String hostname, final int port, final String username) {
        return String.format("%s_%s_%s", hostname, port, username);
    }
    
    private static StorageUnitNodeMapper createStorageUnitNodeMapper(final StorageNode storageNode, final String storageUnitName, final String url, final boolean isInstanceConnectionAvailable) {
        return isInstanceConnectionAvailable
                ? new StorageUnitNodeMapper(storageUnitName, storageNode, new StandardJdbcUrlParser().parse(url).getDatabase(), url)
                : new StorageUnitNodeMapper(storageUnitName, storageNode, url);
    }
    
    /**
     * Get storage node grouped data source pool properties map.
     *
     * @param storageUnitDataSourcePoolProps storage unit grouped data source pool properties map
     * @return storage node grouped data source pool properties map
     */
    public static Map<StorageNode, DataSourcePoolProperties> getStorageNodeDataSourcePoolProperties(final Map<String, DataSourcePoolProperties> storageUnitDataSourcePoolProps) {
        Map<StorageNode, DataSourcePoolProperties> result = new LinkedHashMap<>();
        Collection<StorageNode> storageNodes = new HashSet<>();
        for (Entry<String, DataSourcePoolProperties> entry : storageUnitDataSourcePoolProps.entrySet()) {
            Map<String, Object> standardProps = entry.getValue().getConnectionPropertySynonyms().getStandardProperties();
            String url = standardProps.get("url").toString();
            boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
            StorageNode storageNode = new StorageNode(getStorageNodeName(entry.getKey(), url, standardProps.get("username").toString(), isInstanceConnectionAvailable));
            if (storageNodes.add(storageNode)) {
                result.put(storageNode, entry.getValue());
            }
        }
        return result;
    }
}

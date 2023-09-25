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
            Map<String, Object> standardProps = entry.getValue().getConnectionPropertySynonyms().getStandardProperties();
            String url = standardProps.get("url").toString();
            boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
            StorageNode storageNode = new StorageNode(getStorageNodeName(storageUnitName, url, standardProps.get("username").toString(), isInstanceConnectionAvailable));
            if (!storageNodes.containsKey(storageNode)) {
                storageNodes.put(storageNode, DataSourcePoolCreator.create(storageUnitName, entry.getValue(), true, storageNodes.values()));
            }
            mappers.put(storageUnitName, getStorageUnitNodeMapper(storageNode, storageUnitName, url, isInstanceConnectionAvailable));
        }
        return new StorageResource(storageNodes, mappers);
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
    
    private static StorageUnitNodeMapper getStorageUnitNodeMapper(final StorageNode storageNode, final String storageUnitName, final String url, final boolean isInstanceConnectionAvailable) {
        return isInstanceConnectionAvailable
                ? new StorageUnitNodeMapper(storageUnitName, storageNode, new StandardJdbcUrlParser().parse(url).getDatabase(), url)
                : new StorageUnitNodeMapper(storageUnitName, storageNode, url);
    }
    
    /**
     * Create storage resource without data source.
     *
     * @param propsMap data source pool properties map
     * @return created storage resource
     */
    public static StorageResource createStorageResourceWithoutDataSource(final Map<String, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>();
        Map<String, StorageUnitNodeMapper> mappers = new LinkedHashMap<>();
        Map<String, DataSourcePoolProperties> newPropsMap = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            String storageUnitName = entry.getKey();
            Map<String, Object> standardProps = entry.getValue().getConnectionPropertySynonyms().getStandardProperties();
            String url = standardProps.get("url").toString();
            boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
            StorageNode storageNode = new StorageNode(getStorageNodeName(storageUnitName, url, standardProps.get("username").toString(), isInstanceConnectionAvailable));
            if (!storageNodes.containsKey(storageNode)) {
                storageNodes.put(storageNode, null);
                newPropsMap.put(storageNode.getName(), entry.getValue());
            }
            mappers.put(storageUnitName, getStorageUnitNodeMapper(storageNode, storageUnitName, url, isInstanceConnectionAvailable));
        }
        return new StorageResource(storageNodes, mappers, newPropsMap);
    }
}

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

package org.apache.shardingsphere.infra.metadata.database.resource.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.connector.url.JdbcUrl;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.connector.url.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;

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
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageNodeProperties storageNodeProps = getStorageNodeProperties(entry.getKey(), entry.getValue());
            StorageNode storageNode = new StorageNode(storageNodeProps.getName());
            if (!storageNodes.containsKey(storageNode)) {
                storageNodes.put(storageNode, DataSourcePoolCreator.create(entry.getKey(), entry.getValue(), true, storageNodes.values()));
            }
            appendStorageUnitNodeMapper(storageUnitNodeMappers, storageNodeProps, entry.getKey(), entry.getValue());
        }
        return new StorageResource(storageNodes, storageUnitNodeMappers);
    }
    
    /**
     * Create storage resource without data source.
     *
     * @param propsMap data source pool properties map
     * @return created storage resource
     */
    public static StorageResourceWithProperties createStorageResourceWithoutDataSource(final Map<String, DataSourcePoolProperties> propsMap) {
        Map<StorageNode, DataSource> storageNodes = new LinkedHashMap<>();
        Map<String, StorageUnitNodeMapper> storageUnitNodeMappers = new LinkedHashMap<>();
        Map<String, DataSourcePoolProperties> newPropsMap = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageNodeProperties storageNodeProps = getStorageNodeProperties(entry.getKey(), entry.getValue());
            StorageNode storageNode = new StorageNode(storageNodeProps.getName());
            if (storageNodes.containsKey(storageNode)) {
                appendStorageUnitNodeMapper(storageUnitNodeMappers, storageNodeProps, entry.getKey(), entry.getValue());
                continue;
            }
            storageNodes.put(storageNode, null);
            appendStorageUnitNodeMapper(storageUnitNodeMappers, storageNodeProps, entry.getKey(), entry.getValue());
            newPropsMap.put(storageNodeProps.getName(), entry.getValue());
        }
        return new StorageResourceWithProperties(storageNodes, storageUnitNodeMappers, newPropsMap);
    }
    
    private static void appendStorageUnitNodeMapper(final Map<String, StorageUnitNodeMapper> storageUnitNodeMappers, final StorageNodeProperties storageNodeProps,
                                                    final String unitName, final DataSourcePoolProperties props) {
        String url = props.getConnectionPropertySynonyms().getStandardProperties().get("url").toString();
        storageUnitNodeMappers.put(unitName, getStorageUnitNodeMapper(storageNodeProps, unitName, url));
    }
    
    private static StorageUnitNodeMapper getStorageUnitNodeMapper(final StorageNodeProperties storageNodeProps, final String unitName, final String url) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(storageNodeProps.getDatabaseType()).getDialectDatabaseMetaData();
        return dialectDatabaseMetaData.isInstanceConnectionAvailable()
                ? new StorageUnitNodeMapper(unitName, new StorageNode(storageNodeProps.getName()), storageNodeProps.getCatalog(), url)
                : new StorageUnitNodeMapper(unitName, new StorageNode(storageNodeProps.getName()), url);
    }
    
    private static StorageNodeProperties getStorageNodeProperties(final String dataSourceName, final DataSourcePoolProperties storageNodeProps) {
        Map<String, Object> standardProps = storageNodeProps.getConnectionPropertySynonyms().getStandardProperties();
        String url = standardProps.get("url").toString();
        String username = standardProps.get("username").toString();
        DatabaseType databaseType = DatabaseTypeFactory.get(url);
        return getStorageNodeProperties(dataSourceName, url, username, databaseType);
    }
    
    private static StorageNodeProperties getStorageNodeProperties(final String dataSourceName, final String url, final String username, final DatabaseType databaseType) {
        try {
            JdbcUrl jdbcUrl = new StandardJdbcUrlParser().parse(url);
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
            String nodeName = dialectDatabaseMetaData.isInstanceConnectionAvailable() ? generateStorageNodeName(jdbcUrl.getHostname(), jdbcUrl.getPort(), username) : dataSourceName;
            return new StorageNodeProperties(nodeName, databaseType, jdbcUrl.getDatabase());
        } catch (final UnrecognizedDatabaseURLException ex) {
            return new StorageNodeProperties(dataSourceName, databaseType, null);
        }
    }
    
    private static String generateStorageNodeName(final String hostname, final int port, final String username) {
        return String.format("%s_%s_%s", hostname, port, username);
    }
}

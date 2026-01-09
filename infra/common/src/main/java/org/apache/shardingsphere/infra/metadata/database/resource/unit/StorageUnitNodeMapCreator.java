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
import org.apache.shardingsphere.database.connector.core.exception.UnrecognizedDatabaseURLException;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Storage unit node map creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageUnitNodeMapCreator {
    
    /**
     * Create storage unit node map.
     *
     * @param propsMap data source pool properties map
     * @param isInstanceConnectionEnabled is instance connection enabled
     * @return storage unit node map
     */
    public static Map<String, StorageNode> create(final Map<String, DataSourcePoolProperties> propsMap, final boolean isInstanceConnectionEnabled) {
        return propsMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> create(entry.getKey(), entry.getValue(), isInstanceConnectionEnabled),
                (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static StorageNode create(final String storageUnitName, final DataSourcePoolProperties props, final boolean isInstanceConnectionEnabled) {
        Map<String, Object> standardProps = props.getConnectionPropertySynonyms().getStandardProperties();
        String url = standardProps.get("url").toString();
        String username = standardProps.get("username").toString();
        boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().getConnectionOption().isInstanceConnectionAvailable();
        try {
            ConnectionProperties connectionProps = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, DatabaseTypeFactory.get(url)).parse(url, username, null);
            return isInstanceConnectionEnabled && isInstanceConnectionAvailable
                    ? new StorageNode(connectionProps.getHostname(), connectionProps.getPort(), username)
                    : new StorageNode(storageUnitName);
        } catch (final UnrecognizedDatabaseURLException ex) {
            return new StorageNode(storageUnitName);
        }
    }
}

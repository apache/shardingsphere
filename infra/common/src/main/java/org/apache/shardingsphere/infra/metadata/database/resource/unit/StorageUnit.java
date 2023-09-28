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

import lombok.Getter;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
import org.apache.shardingsphere.infra.state.datasource.DataSourceStateManager;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * Storage unit.
 */
@Getter
public final class StorageUnit {
    
    private final DataSourcePoolProperties dataSourcePoolProperties;
    
    private final StorageNode storageNode;
    
    private final DataSource dataSource;
    
    private final DatabaseType storageType;
    
    private final ConnectionProperties connectionProperties;
    
    public StorageUnit(final String databaseName, final Map<StorageNodeName, DataSource> storageNodeDataSources, final DataSourcePoolProperties props, final StorageNode storageNode) {
        this.dataSourcePoolProperties = props;
        this.storageNode = storageNode;
        dataSource = getStorageUnitDataSource(storageNodeDataSources);
        boolean isDataSourceEnabled = !DataSourceStateManager.getInstance().getEnabledDataSources(databaseName, Collections.singletonMap(storageNode.getName().getName(), dataSource)).isEmpty();
        storageType = createStorageType(isDataSourceEnabled);
        connectionProperties = createConnectionProperties(isDataSourceEnabled);
    }
    
    private DataSource getStorageUnitDataSource(final Map<StorageNodeName, DataSource> storageNodeDataSources) {
        DataSource dataSource = storageNodeDataSources.get(storageNode.getName());
        return new CatalogSwitchableDataSource(dataSource, storageNode.getCatalog(), storageNode.getUrl());
    }
    
    private DatabaseType createStorageType(final boolean isDataSourceEnabled) {
        return DatabaseTypeEngine.getStorageType(isDataSourceEnabled ? Collections.singleton(dataSource) : Collections.emptyList());
    }
    
    private ConnectionProperties createConnectionProperties(final boolean isDataSourceEnabled) {
        if (!isDataSourceEnabled) {
            return null;
        }
        Map<String, Object> standardProps = DataSourcePoolPropertiesCreator.create(dataSource).getConnectionPropertySynonyms().getStandardProperties();
        ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, storageType);
        return parser.parse(standardProps.get("url").toString(), standardProps.get("username").toString(), storageNode.getCatalog());
    }
}

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
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.database.core.connector.url.StandardJdbcUrlParser;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Storage unit.
 */
@Getter
public final class StorageUnit {
    
    private final StorageNode storageNode;
    
    private final DatabaseType storageType;
    
    private final String catalog;
    
    private final DataSource dataSource;
    
    private final DataSourcePoolProperties dataSourcePoolProperties;
    
    private final ConnectionProperties connectionProperties;
    
    public StorageUnit(final StorageNode storageNode, final DataSourcePoolProperties dataSourcePoolProperties, final DataSource dataSource) {
        this.storageNode = storageNode;
        Map<String, Object> standardProps = dataSourcePoolProperties.getConnectionPropertySynonyms().getStandardProperties();
        String url = standardProps.get("url").toString();
        storageType = DatabaseTypeFactory.get(url);
        boolean isInstanceConnectionAvailable = new DatabaseTypeRegistry(DatabaseTypeFactory.get(url)).getDialectDatabaseMetaData().isInstanceConnectionAvailable();
        catalog = isInstanceConnectionAvailable ? new StandardJdbcUrlParser().parse(url).getDatabase() : null;
        this.dataSource = new CatalogSwitchableDataSource(dataSource, catalog, url);
        this.dataSourcePoolProperties = dataSourcePoolProperties;
        connectionProperties = createConnectionProperties(standardProps);
    }
    
    private ConnectionProperties createConnectionProperties(final Map<String, Object> standardProps) {
        ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, storageType);
        return parser.parse(standardProps.get("url").toString(), standardProps.getOrDefault("username", "").toString(), catalog);
    }
}

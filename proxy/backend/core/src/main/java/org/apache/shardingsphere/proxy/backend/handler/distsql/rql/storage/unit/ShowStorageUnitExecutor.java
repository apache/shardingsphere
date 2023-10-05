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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.storage.unit;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.proxy.backend.util.StorageUnitUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Show storage unit executor.
 */
public final class ShowStorageUnitExecutor implements RQLExecutor<ShowStorageUnitsStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "host", "port", "db", "connection_timeout_milliseconds", "idle_timeout_milliseconds",
                "max_lifetime_milliseconds", "max_pool_size", "min_pool_size", "read_only", "other_attributes");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowStorageUnitsStatement sqlStatement) {
        ResourceMetaData resourceMetaData = database.getResourceMetaData();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (Entry<String, DataSourcePoolProperties> entry : getDataSourcePoolPropertiesMap(database, sqlStatement).entrySet()) {
            String key = entry.getKey();
            ConnectionProperties connectionProps = resourceMetaData.getStorageUnits().get(key).getConnectionProperties();
            Map<String, Object> poolProps = entry.getValue().getPoolPropertySynonyms().getStandardProperties();
            Map<String, Object> customProps = getCustomProps(entry.getValue().getCustomProperties().getProperties(), connectionProps.getQueryProperties());
            result.add(new LocalDataQueryResultRow(key,
                    resourceMetaData.getStorageUnits().get(key).getStorageType().getType(),
                    connectionProps.getHostname(),
                    connectionProps.getPort(),
                    connectionProps.getCatalog(),
                    getStandardProperty(poolProps, "connectionTimeoutMilliseconds"),
                    getStandardProperty(poolProps, "idleTimeoutMilliseconds"),
                    getStandardProperty(poolProps, "maxLifetimeMilliseconds"),
                    getStandardProperty(poolProps, "maxPoolSize"),
                    getStandardProperty(poolProps, "minPoolSize"),
                    getStandardProperty(poolProps, "readOnly"),
                    customProps.isEmpty() ? "" : JsonUtils.toJsonString(customProps)));
        }
        return result;
    }
    
    private Map<String, Object> getCustomProps(final Map<String, Object> customProps, final Properties queryProps) {
        Map<String, Object> result = new LinkedHashMap<>(customProps.size() + 1, 1F);
        result.putAll(customProps);
        if (!queryProps.isEmpty()) {
            result.put("queryProperties", queryProps);
        }
        return result;
    }
    
    private Map<String, DataSourcePoolProperties> getDataSourcePoolPropertiesMap(final ShardingSphereDatabase database, final ShowStorageUnitsStatement sqlStatement) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(database.getResourceMetaData().getStorageUnits().size(), 1F);
        Map<String, DataSourcePoolProperties> propsMap = database.getResourceMetaData().getStorageUnits().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getDataSourcePoolProperties(), (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        Optional<Integer> usageCount = sqlStatement.getUsageCount();
        if (usageCount.isPresent()) {
            Map<String, Collection<String>> inUsedStorageUnits = StorageUnitUtils.getInUsedStorageUnits(
                    database.getRuleMetaData(), database.getResourceMetaData().getStorageUnits().size());
            for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnits().entrySet()) {
                Integer currentUsageCount = inUsedStorageUnits.containsKey(entry.getKey()) ? inUsedStorageUnits.get(entry.getKey()).size() : 0;
                if (usageCount.get().equals(currentUsageCount)) {
                    result.put(entry.getKey(), getDataSourcePoolProperties(
                            propsMap, entry.getKey(), storageUnits.get(entry.getKey()).getStorageType(), entry.getValue().getDataSource()));
                }
            }
        } else {
            for (Entry<String, StorageUnit> entry : storageUnits.entrySet()) {
                result.put(entry.getKey(),
                        getDataSourcePoolProperties(propsMap, entry.getKey(), storageUnits.get(entry.getKey()).getStorageType(), entry.getValue().getDataSource()));
            }
        }
        return result;
    }
    
    private DataSourcePoolProperties getDataSourcePoolProperties(final Map<String, DataSourcePoolProperties> propsMap, final String storageUnitName,
                                                                 final DatabaseType databaseType, final DataSource dataSource) {
        DataSourcePoolProperties result = getDataSourcePoolProperties(dataSource);
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        if (dialectDatabaseMetaData.isInstanceConnectionAvailable() && propsMap.containsKey(storageUnitName)) {
            DataSourcePoolProperties unitDataSourcePoolProps = propsMap.get(storageUnitName);
            for (Entry<String, Object> entry : unitDataSourcePoolProps.getPoolPropertySynonyms().getStandardProperties().entrySet()) {
                if (null != entry.getValue()) {
                    result.getPoolPropertySynonyms().getStandardProperties().put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }
    
    private DataSourcePoolProperties getDataSourcePoolProperties(final DataSource dataSource) {
        return dataSource instanceof CatalogSwitchableDataSource
                ? DataSourcePoolPropertiesCreator.create(((CatalogSwitchableDataSource) dataSource).getDataSource())
                : DataSourcePoolPropertiesCreator.create(dataSource);
    }
    
    private String getStandardProperty(final Map<String, Object> standardProps, final String key) {
        if (standardProps.containsKey(key) && null != standardProps.get(key)) {
            return standardProps.get(key).toString();
        }
        return "";
    }
    
    @Override
    public Class<ShowStorageUnitsStatement> getType() {
        return ShowStorageUnitsStatement.class;
    }
}

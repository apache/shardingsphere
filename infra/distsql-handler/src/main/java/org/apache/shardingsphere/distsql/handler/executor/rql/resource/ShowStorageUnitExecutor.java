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

package org.apache.shardingsphere.distsql.handler.executor.rql.resource;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Show storage unit executor.
 */
@Setter
public final class ShowStorageUnitExecutor implements DistSQLQueryExecutor<ShowStorageUnitsStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public Collection<String> getColumnNames(final ShowStorageUnitsStatement sqlStatement) {
        return Arrays.asList("name", "type", "host", "port", "db", "connection_timeout_milliseconds", "idle_timeout_milliseconds",
                "max_lifetime_milliseconds", "max_pool_size", "min_pool_size", "read_only", "other_attributes");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStorageUnitsStatement sqlStatement, final ContextManager contextManager) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (Entry<String, StorageUnit> entry : getToBeShownStorageUnits(sqlStatement).entrySet()) {
            ConnectionProperties connectionProps = entry.getValue().getConnectionProperties();
            DataSourcePoolProperties dataSourcePoolProps = getDataSourcePoolProperties(entry.getValue());
            Map<String, Object> poolProps = dataSourcePoolProps.getPoolPropertySynonyms().getStandardProperties();
            Map<String, Object> customProps = getCustomProperties(dataSourcePoolProps.getCustomProperties().getProperties(), connectionProps.getQueryProperties());
            result.add(new LocalDataQueryResultRow(entry.getKey(),
                    entry.getValue().getStorageType().getType(),
                    connectionProps.getHostname(),
                    connectionProps.getPort(),
                    connectionProps.getCatalog(),
                    getStandardProperty(poolProps, "connectionTimeoutMilliseconds"),
                    getStandardProperty(poolProps, "idleTimeoutMilliseconds"),
                    getStandardProperty(poolProps, "maxLifetimeMilliseconds"),
                    getStandardProperty(poolProps, "maxPoolSize"),
                    getStandardProperty(poolProps, "minPoolSize"),
                    getStandardProperty(poolProps, "readOnly"),
                    customProps));
        }
        return result;
    }
    
    private Map<String, StorageUnit> getToBeShownStorageUnits(final ShowStorageUnitsStatement sqlStatement) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(database.getResourceMetaData().getStorageUnits().size(), 1F);
        Optional<Integer> usageCount = sqlStatement.getUsageCount();
        if (usageCount.isPresent()) {
            Map<String, Collection<Class<? extends ShardingSphereRule>>> inUsedStorageUnits = database.getRuleMetaData().getInUsedStorageUnitNameAndRulesMap();
            for (Entry<String, StorageUnit> entry : database.getResourceMetaData().getStorageUnits().entrySet()) {
                int currentUsageCount = inUsedStorageUnits.containsKey(entry.getKey()) ? inUsedStorageUnits.get(entry.getKey()).size() : 0;
                if (usageCount.get().equals(currentUsageCount)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            result.putAll(database.getResourceMetaData().getStorageUnits());
        }
        return result;
    }
    
    private DataSourcePoolProperties getDataSourcePoolProperties(final StorageUnit storageUnit) {
        DataSource dataSource = storageUnit.getDataSource();
        DataSourcePoolProperties result = DataSourcePoolPropertiesCreator.create(
                dataSource instanceof CatalogSwitchableDataSource ? ((CatalogSwitchableDataSource) dataSource).getDataSource() : dataSource);
        if (new DatabaseTypeRegistry(storageUnit.getStorageType()).getDialectDatabaseMetaData().isInstanceConnectionAvailable()) {
            for (Entry<String, Object> entry : storageUnit.getDataSourcePoolProperties().getPoolPropertySynonyms().getStandardProperties().entrySet()) {
                if (null != entry.getValue()) {
                    result.getPoolPropertySynonyms().getStandardProperties().put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }
    
    private Map<String, Object> getCustomProperties(final Map<String, Object> customProps, final Properties queryProps) {
        Map<String, Object> result = new LinkedHashMap<>(customProps);
        result.remove("dataSourceProperties");
        if (!queryProps.isEmpty()) {
            result.put("queryProperties", queryProps);
        }
        return result;
    }
    
    private String getStandardProperty(final Map<String, Object> standardProps, final String key) {
        return standardProps.containsKey(key) && null != standardProps.get(key) ? standardProps.get(key).toString() : "";
    }
    
    @Override
    public Class<ShowStorageUnitsStatement> getType() {
        return ShowStorageUnitsStatement.class;
    }
}

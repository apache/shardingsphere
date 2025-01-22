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

package org.apache.shardingsphere.mode.metadata.refresher;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.StatisticsCollectEngine;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.lock.global.GlobalLockDefinition;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.data.AlteredShardingSphereDatabaseData;
import org.apache.shardingsphere.mode.metadata.refresher.lock.StatisticsLock;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ShardingSphere statistics refresh engine.
 */
@Slf4j
public final class ShardingSphereStatisticsRefreshEngine {
    
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("statistics-collect-%d"));
    
    private final ContextManager contextManager;
    
    private final LockContext lockContext;
    
    public ShardingSphereStatisticsRefreshEngine(final ContextManager contextManager) {
        this.contextManager = contextManager;
        lockContext = contextManager.getComputeNodeInstanceContext().getLockContext();
    }
    
    /**
     * Async refresh.
     */
    public void asyncRefresh() {
        if (InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            EXECUTOR_SERVICE.execute(this::refresh);
        }
    }
    
    /**
     * Refresh.
     */
    public void refresh() {
        try {
            if (contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED)) {
                collectAndRefresh();
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Collect data error", ex);
        }
    }
    
    private void collectAndRefresh() {
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition(new StatisticsLock());
        if (lockContext.tryLock(lockDefinition, 5000L)) {
            try {
                ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
                ShardingSphereStatistics collectedStatistics = new ShardingSphereStatistics();
                for (ShardingSphereDatabase each : metaData.getAllDatabases()) {
                    collectForDatabase(each, metaData, collectedStatistics);
                }
                compareAndUpdate(collectedStatistics);
            } finally {
                lockContext.unlock(lockDefinition);
            }
        }
    }
    
    private void collectForDatabase(final ShardingSphereDatabase database, final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        for (ShardingSphereSchema each : database.getAllSchemas()) {
            collectForSchema(database.getProtocolType(), each.getName(), each, metaData, statistics);
        }
    }
    
    private void collectForSchema(final DatabaseType protocolType, final String databaseName, final ShardingSphereSchema schema,
                                  final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        for (ShardingSphereTable each : schema.getAllTables()) {
            collectForTable(protocolType, databaseName, schema.getName(), each, metaData, statistics);
        }
    }
    
    private void collectForTable(final DatabaseType protocolType, final String databaseName, final String schemaName, final ShardingSphereTable table,
                                 final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        Optional<ShardingSphereTableData> tableData = Optional.empty();
        try {
            tableData = new StatisticsCollectEngine(protocolType).collect(databaseName, schemaName, table, metaData);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error(String.format("Collect %s.%s.%s data failed", databaseName, schemaName, table.getName()), ex);
        }
        ShardingSphereDatabaseData databaseData = statistics.containsDatabase(databaseName) ? statistics.getDatabase(databaseName) : new ShardingSphereDatabaseData();
        ShardingSphereSchemaData schemaData = databaseData.containsSchema(schemaName) ? databaseData.getSchema(schemaName) : new ShardingSphereSchemaData();
        tableData.ifPresent(optional -> schemaData.putTable(table.getName(), optional));
        databaseData.putSchema(schemaName, schemaData);
        statistics.putDatabase(databaseName, databaseData);
    }
    
    private void compareAndUpdate(final ShardingSphereStatistics collectedStatistics) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        ShardingSphereStatistics existedStatistics = contextManager.getMetaDataContexts().getStatistics();
        for (Entry<String, ShardingSphereDatabaseData> entry : collectedStatistics.getDatabaseData().entrySet()) {
            if (existedStatistics.containsDatabase(entry.getKey())) {
                compareAndUpdateDatabase(metaData.getDatabase(entry.getKey()), existedStatistics.getDatabase(entry.getKey()), entry.getValue());
            } else {
                updateDatabase(entry.getKey(), entry.getValue());
                existedStatistics.putDatabase(entry.getKey(), entry.getValue());
            }
        }
        for (String each : new ArrayList<>(existedStatistics.getDatabaseData().keySet())) {
            if (!collectedStatistics.containsDatabase(each)) {
                existedStatistics.dropDatabase(each);
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getShardingSphereDataPersistService().delete(each);
            }
        }
    }
    
    private void compareAndUpdateDatabase(final ShardingSphereDatabase database, final ShardingSphereDatabaseData currentDatabaseData,
                                          final ShardingSphereDatabaseData collectedDatabaseData) {
        for (Entry<String, ShardingSphereSchemaData> entry : collectedDatabaseData.getSchemaData().entrySet()) {
            if (currentDatabaseData.containsSchema(entry.getKey())) {
                compareAndUpdateSchema(database.getName(), entry.getKey(), currentDatabaseData.getSchema(entry.getKey()), entry.getValue());
            } else {
                updateSchema(database.getName(), entry.getKey(), entry.getValue());
                currentDatabaseData.putSchema(entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void compareAndUpdateSchema(final String databaseName, final String schemaName,
                                        final ShardingSphereSchemaData currentSchemaData, final ShardingSphereSchemaData collectedSchemaData) {
        for (Entry<String, ShardingSphereTableData> entry : collectedSchemaData.getTableData().entrySet()) {
            if (currentSchemaData.containsTable(entry.getKey())) {
                compareAndUpdateTable(databaseName, schemaName, currentSchemaData.getTable(entry.getKey()), entry.getValue());
            } else {
                updateTable(databaseName, schemaName, entry.getKey(), entry.getValue());
                currentSchemaData.putTable(entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void compareAndUpdateTable(final String databaseName, final String schemaName,
                                       final ShardingSphereTableData currentTableData, final ShardingSphereTableData collectedTableData) {
        if (currentTableData.equals(collectedTableData)) {
            return;
        }
        ShardingSphereTable shardingSphereTable = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(currentTableData.getName());
        AlteredShardingSphereDatabaseData alteredShardingSphereDatabaseData = new AlteredShardingSphereDatabaseData(databaseName, schemaName, currentTableData.getName());
        Map<String, ShardingSphereRowData> currentTableRowData = currentTableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
        Map<String, ShardingSphereRowData> collectedTableRowData = collectedTableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
        YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(shardingSphereTable.getAllColumns()));
        for (Entry<String, ShardingSphereRowData> entry : collectedTableRowData.entrySet()) {
            if (!currentTableRowData.containsKey(entry.getKey())) {
                alteredShardingSphereDatabaseData.getAddedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            } else if (!currentTableRowData.get(entry.getKey()).equals(entry.getValue())) {
                alteredShardingSphereDatabaseData.getUpdatedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        for (Entry<String, ShardingSphereRowData> entry : currentTableRowData.entrySet()) {
            if (!collectedTableRowData.containsKey(entry.getKey())) {
                alteredShardingSphereDatabaseData.getDeletedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getShardingSphereDataPersistService().update(alteredShardingSphereDatabaseData);
    }
    
    private void updateDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData) {
        for (Entry<String, ShardingSphereSchemaData> entry : databaseData.getSchemaData().entrySet()) {
            updateSchema(databaseName, entry.getKey(), entry.getValue());
        }
    }
    
    private void updateSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData) {
        for (Entry<String, ShardingSphereTableData> entry : schemaData.getTableData().entrySet()) {
            updateTable(databaseName, schemaName, entry.getKey(), entry.getValue());
        }
    }
    
    private void updateTable(final String databaseName, final String schemaName, final String tableName, final ShardingSphereTableData tableData) {
        ShardingSphereTable shardingSphereTable = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
        AlteredShardingSphereDatabaseData alteredShardingSphereDatabaseData = new AlteredShardingSphereDatabaseData(databaseName, schemaName, tableName);
        YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(shardingSphereTable.getAllColumns()));
        for (ShardingSphereRowData each : tableData.getRows()) {
            alteredShardingSphereDatabaseData.getAddedRows().add(swapper.swapToYamlConfiguration(each));
        }
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getShardingSphereDataPersistService().update(alteredShardingSphereDatabaseData);
    }
}

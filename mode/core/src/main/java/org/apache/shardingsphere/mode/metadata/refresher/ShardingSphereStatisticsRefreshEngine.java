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
                ShardingSphereStatistics changedStatistics = new ShardingSphereStatistics();
                for (ShardingSphereDatabase each : metaData.getAllDatabases()) {
                    collectForDatabase(each, metaData, changedStatistics);
                }
                compareAndUpdate(changedStatistics);
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
    
    private void compareAndUpdate(final ShardingSphereStatistics changedStatistics) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        ShardingSphereStatistics statistics = contextManager.getMetaDataContexts().getStatistics();
        for (Entry<String, ShardingSphereDatabaseData> entry : changedStatistics.getDatabaseData().entrySet()) {
            compareAndUpdateForDatabase(entry.getKey(), statistics.getDatabase(entry.getKey()), entry.getValue(), statistics, metaData.getDatabase(entry.getKey()));
        }
        for (String each : new ArrayList<>(statistics.getDatabaseData().keySet())) {
            if (!changedStatistics.containsDatabase(each)) {
                statistics.dropDatabase(each);
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getShardingSphereDataPersistService().delete(each);
            }
        }
    }
    
    private void compareAndUpdateForDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData, final ShardingSphereDatabaseData changedDatabaseData,
                                             final ShardingSphereStatistics statistics, final ShardingSphereDatabase database) {
        for (Entry<String, ShardingSphereSchemaData> entry : changedDatabaseData.getSchemaData().entrySet()) {
            compareAndUpdateForSchema(databaseName, entry.getKey(), databaseData.getSchema(entry.getKey()), entry.getValue(), statistics, database.getSchema(entry.getKey()));
        }
    }
    
    private void compareAndUpdateForSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData,
                                           final ShardingSphereSchemaData changedSchemaData, final ShardingSphereStatistics statistics, final ShardingSphereSchema schema) {
        for (Entry<String, ShardingSphereTableData> entry : changedSchemaData.getTableData().entrySet()) {
            compareAndUpdateForTable(databaseName, schemaName, schemaData.getTable(entry.getKey()), entry.getValue(), statistics, schema.getTable(entry.getKey()));
        }
    }
    
    private void compareAndUpdateForTable(final String databaseName, final String schemaName, final ShardingSphereTableData tableData,
                                          final ShardingSphereTableData changedTableData, final ShardingSphereStatistics statistics, final ShardingSphereTable table) {
        if (!tableData.equals(changedTableData)) {
            statistics.getDatabase(databaseName).getSchema(schemaName).putTable(changedTableData.getName(), changedTableData);
            AlteredShardingSphereDatabaseData alteredShardingSphereDatabaseData = createAlteredShardingSphereDatabaseData(databaseName, schemaName, tableData, changedTableData, table);
            contextManager.getPersistServiceFacade().getMetaDataPersistService().getShardingSphereDataPersistService().update(alteredShardingSphereDatabaseData);
        }
    }
    
    private AlteredShardingSphereDatabaseData createAlteredShardingSphereDatabaseData(final String databaseName, final String schemaName, final ShardingSphereTableData tableData,
                                                                                      final ShardingSphereTableData changedTableData, final ShardingSphereTable table) {
        AlteredShardingSphereDatabaseData result = new AlteredShardingSphereDatabaseData(databaseName, schemaName, tableData.getName());
        Map<String, ShardingSphereRowData> tableDataMap = tableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
        Map<String, ShardingSphereRowData> changedTableDataMap = changedTableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
        YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(table.getAllColumns()));
        for (Entry<String, ShardingSphereRowData> entry : changedTableDataMap.entrySet()) {
            if (!tableDataMap.containsKey(entry.getKey())) {
                result.getAddedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            } else if (!tableDataMap.get(entry.getKey()).equals(entry.getValue())) {
                result.getUpdatedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        for (Entry<String, ShardingSphereRowData> entry : tableDataMap.entrySet()) {
            if (!changedTableDataMap.containsKey(entry.getKey())) {
                result.getDeletedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        return result;
    }
}

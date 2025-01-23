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

package org.apache.shardingsphere.mode.metadata.refresher.statistics;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.collector.table.TableStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlRowStatisticsSwapper;
import org.apache.shardingsphere.mode.metadata.persist.statistics.AlteredDatabaseStatistics;
import org.apache.shardingsphere.mode.lock.global.GlobalLockDefinition;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Statistics refresh engine.
 */
@Slf4j
public final class StatisticsRefreshEngine {
    
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("statistics-collect-%d"));
    
    private final ContextManager contextManager;
    
    private final LockContext lockContext;
    
    public StatisticsRefreshEngine(final ContextManager contextManager) {
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
            log.error("Collect statistics error.", ex);
        }
    }
    
    private void collectAndRefresh() {
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition(new StatisticsLock());
        if (lockContext.tryLock(lockDefinition, 5000L)) {
            try {
                ShardingSphereStatistics currentStatistics = contextManager.getMetaDataContexts().getStatistics();
                ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
                ShardingSphereStatistics changedStatistics = new ShardingSphereStatistics();
                for (Entry<String, DatabaseStatistics> entry : currentStatistics.getDatabaseStatisticsMap().entrySet()) {
                    if (metaData.containsDatabase(entry.getKey())) {
                        collectForDatabase(entry.getKey(), entry.getValue(), metaData, changedStatistics);
                    }
                }
                compareAndUpdate(currentStatistics, changedStatistics, metaData);
            } finally {
                lockContext.unlock(lockDefinition);
            }
        }
    }
    
    private void collectForDatabase(final String databaseName, final DatabaseStatistics databaseStatistics, final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        for (Entry<String, SchemaStatistics> entry : databaseStatistics.getSchemaStatisticsMap().entrySet()) {
            if (metaData.getDatabase(databaseName).containsSchema(entry.getKey())) {
                collectForSchema(databaseName, entry.getKey(), entry.getValue(), metaData, statistics);
            }
        }
    }
    
    private void collectForSchema(final String databaseName, final String schemaName, final SchemaStatistics schemaStatistics,
                                  final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        for (Entry<String, TableStatistics> entry : schemaStatistics.getTableStatisticsMap().entrySet()) {
            if (metaData.getDatabase(databaseName).getSchema(schemaName).containsTable(entry.getKey())) {
                collectForTable(databaseName, schemaName, metaData.getDatabase(databaseName).getSchema(schemaName).getTable(entry.getKey()), metaData, statistics);
            }
        }
    }
    
    private void collectForTable(final String databaseName, final String schemaName, final ShardingSphereTable table,
                                 final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        Optional<TableStatisticsCollector> tableStatisticsCollector = TypedSPILoader.findService(TableStatisticsCollector.class, table.getName());
        Optional<TableStatistics> tableStatistics;
        if (tableStatisticsCollector.isPresent()) {
            try {
                tableStatistics = tableStatisticsCollector.get().collect(databaseName, table, metaData);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("Collect {}.{}.{} statistics failed.", databaseName, schemaName, table.getName(), ex);
                tableStatistics = Optional.empty();
            }
        } else {
            tableStatistics = Optional.empty();
        }
        DatabaseStatistics databaseStatistics = statistics.containsDatabaseStatistics(databaseName) ? statistics.getDatabaseStatistics(databaseName) : new DatabaseStatistics();
        SchemaStatistics schemaStatistics = databaseStatistics.containsSchemaStatistics(schemaName) ? databaseStatistics.getSchemaStatistics(schemaName) : new SchemaStatistics();
        tableStatistics.ifPresent(optional -> schemaStatistics.putTableStatistics(table.getName(), optional));
        databaseStatistics.putSchemaStatistics(schemaName, schemaStatistics);
        statistics.putDatabaseStatistics(databaseName, databaseStatistics);
    }
    
    private void compareAndUpdate(final ShardingSphereStatistics currentStatistics, final ShardingSphereStatistics changedStatistics, final ShardingSphereMetaData metaData) {
        for (Entry<String, DatabaseStatistics> entry : changedStatistics.getDatabaseStatisticsMap().entrySet()) {
            compareAndUpdateForDatabase(metaData.getDatabase(entry.getKey()), currentStatistics, currentStatistics.getDatabaseStatistics(entry.getKey()), entry.getValue());
        }
        for (Entry<String, DatabaseStatistics> entry : currentStatistics.getDatabaseStatisticsMap().entrySet()) {
            if (!changedStatistics.containsDatabaseStatistics(entry.getKey())) {
                currentStatistics.dropDatabaseStatistics(entry.getKey());
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getStatisticsPersistService().delete(entry.getKey());
            }
        }
    }
    
    private void compareAndUpdateForDatabase(final ShardingSphereDatabase database, final ShardingSphereStatistics currentStatistics,
                                             final DatabaseStatistics currentDatabaseStatistics, final DatabaseStatistics changedDatabaseStatistics) {
        for (Entry<String, SchemaStatistics> entry : changedDatabaseStatistics.getSchemaStatisticsMap().entrySet()) {
            compareAndUpdateForSchema(database.getName(), database.getSchema(entry.getKey()), currentStatistics, currentDatabaseStatistics.getSchemaStatistics(entry.getKey()), entry.getValue());
        }
    }
    
    private void compareAndUpdateForSchema(final String databaseName, final ShardingSphereSchema schema, final ShardingSphereStatistics currentStatistics,
                                           final SchemaStatistics currentSchemaStatistics, final SchemaStatistics changedSchemaStatistics) {
        for (Entry<String, TableStatistics> entry : changedSchemaStatistics.getTableStatisticsMap().entrySet()) {
            compareAndUpdateForTable(databaseName, schema.getName(), schema.getTable(entry.getKey()), currentStatistics, currentSchemaStatistics.getTableStatistics(entry.getKey()), entry.getValue());
        }
    }
    
    private void compareAndUpdateForTable(final String databaseName, final String schemaName, final ShardingSphereTable table,
                                          final ShardingSphereStatistics currentStatistics, final TableStatistics currentTableStatistics, final TableStatistics changedTableStatistics) {
        if (!currentTableStatistics.equals(changedTableStatistics)) {
            currentStatistics.getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).putTableStatistics(changedTableStatistics.getName(), changedTableStatistics);
            AlteredDatabaseStatistics alteredDatabaseStatistics = createAlteredDatabaseStatistics(databaseName, schemaName, table, currentTableStatistics, changedTableStatistics);
            contextManager.getPersistServiceFacade().getMetaDataPersistService().getStatisticsPersistService().update(alteredDatabaseStatistics);
        }
    }
    
    private AlteredDatabaseStatistics createAlteredDatabaseStatistics(final String databaseName, final String schemaName, final ShardingSphereTable table,
                                                                      final TableStatistics currentTableStatistics, final TableStatistics changedTableStatistics) {
        AlteredDatabaseStatistics result = new AlteredDatabaseStatistics(databaseName, schemaName, currentTableStatistics.getName());
        Map<String, RowStatistics> tableStatisticsMap = currentTableStatistics.getRows().stream().collect(Collectors.toMap(RowStatistics::getUniqueKey, Function.identity()));
        Map<String, RowStatistics> changedTableStatisticsMap = changedTableStatistics.getRows().stream().collect(Collectors.toMap(RowStatistics::getUniqueKey, Function.identity()));
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(new ArrayList<>(table.getAllColumns()));
        for (Entry<String, RowStatistics> entry : changedTableStatisticsMap.entrySet()) {
            if (!tableStatisticsMap.containsKey(entry.getKey())) {
                result.getAddedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            } else if (!tableStatisticsMap.get(entry.getKey()).equals(entry.getValue())) {
                result.getUpdatedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        for (Entry<String, RowStatistics> entry : tableStatisticsMap.entrySet()) {
            if (!changedTableStatisticsMap.containsKey(entry.getKey())) {
                result.getDeletedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        return result;
    }
}

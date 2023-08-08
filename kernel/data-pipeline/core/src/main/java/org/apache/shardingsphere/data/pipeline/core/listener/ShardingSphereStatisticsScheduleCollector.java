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

package org.apache.shardingsphere.data.pipeline.core.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.ShardingSphereSchemaDataAlteredEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ShardingSphere statistics schedule collector.
 */
@RequiredArgsConstructor
@Slf4j
public final class ShardingSphereStatisticsScheduleCollector {
    
    private final ScheduledExecutorService dataCollectorExecutor = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("data-collect-%d"));
    
    private final ContextManager contextManager;
    
    /**
     * Start.
     */
    public void start() {
        dataCollectorExecutor.scheduleWithFixedDelay(new ShardingSphereDataCollectorRunnable(contextManager), 0, 30, TimeUnit.SECONDS);
    }
    
    @RequiredArgsConstructor
    protected static final class ShardingSphereDataCollectorRunnable implements Runnable {
        
        private final ContextManager contextManager;
        
        @Override
        public void run() {
            ShardingSphereStatistics statistics = contextManager.getMetaDataContexts().getStatistics();
            ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
            ShardingSphereStatistics changedStatistics = new ShardingSphereStatistics();
            statistics.getDatabaseData().forEach((key, value) -> {
                if (metaData.containsDatabase(key)) {
                    collectForDatabase(key, value, metaData.getDatabases(), changedStatistics);
                }
            });
            compareUpdateAndSendEvent(statistics, changedStatistics, metaData.getDatabases());
        }
        
        private void collectForDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData,
                                        final Map<String, ShardingSphereDatabase> databases, final ShardingSphereStatistics statistics) {
            databaseData.getSchemaData().forEach((key, value) -> {
                if (databases.get(databaseName.toLowerCase()).containsSchema(key)) {
                    collectForSchema(databaseName, key, value, databases, statistics);
                }
            });
        }
        
        private void collectForSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData,
                                      final Map<String, ShardingSphereDatabase> databases, final ShardingSphereStatistics statistics) {
            schemaData.getTableData().forEach((key, value) -> {
                if (databases.get(databaseName.toLowerCase()).getSchema(schemaName).containsTable(key)) {
                    collectForTable(databaseName, schemaName, databases.get(databaseName).getSchema(schemaName).getTable(key), databases, statistics);
                }
            });
        }
        
        private void collectForTable(final String databaseName, final String schemaName, final ShardingSphereTable table,
                                     final Map<String, ShardingSphereDatabase> databases, final ShardingSphereStatistics statistics) {
            Optional<ShardingSphereStatisticsCollector> dataCollector = TypedSPILoader.findService(ShardingSphereStatisticsCollector.class, table.getName());
            if (!dataCollector.isPresent()) {
                return;
            }
            Optional<ShardingSphereTableData> tableData = Optional.empty();
            try {
                tableData = dataCollector.get().collect(databaseName, table, databases);
            } catch (final SQLException ex) {
                log.error("Collect data failed!", ex);
            }
            tableData.ifPresent(optional -> statistics.getDatabaseData().computeIfAbsent(databaseName.toLowerCase(), key -> new ShardingSphereDatabaseData())
                    .getSchemaData().computeIfAbsent(schemaName, key -> new ShardingSphereSchemaData()).getTableData().put(table.getName().toLowerCase(), optional));
        }
        
        private void compareUpdateAndSendEvent(final ShardingSphereStatistics statistics, final ShardingSphereStatistics changedStatistics,
                                               final Map<String, ShardingSphereDatabase> databases) {
            changedStatistics.getDatabaseData().forEach((key, value) -> compareUpdateAndSendEventForDatabase(key, statistics.getDatabaseData().get(key), value, statistics,
                    databases.get(key.toLowerCase())));
        }
        
        private void compareUpdateAndSendEventForDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData, final ShardingSphereDatabaseData changedDatabaseData,
                                                          final ShardingSphereStatistics statistics, final ShardingSphereDatabase database) {
            changedDatabaseData.getSchemaData().forEach((key, value) -> compareUpdateAndSendEventForSchema(databaseName, key, databaseData.getSchemaData().get(key), value, statistics,
                    database.getSchema(key)));
        }
        
        private void compareUpdateAndSendEventForSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData,
                                                        final ShardingSphereSchemaData changedSchemaData, final ShardingSphereStatistics statistics, final ShardingSphereSchema schema) {
            changedSchemaData.getTableData().forEach((key, value) -> compareUpdateAndSendEventForTable(databaseName, schemaName, schemaData.getTableData().get(key), value, statistics,
                    schema.getTable(key)));
        }
        
        private void compareUpdateAndSendEventForTable(final String databaseName, final String schemaName, final ShardingSphereTableData tableData,
                                                       final ShardingSphereTableData changedTableData, final ShardingSphereStatistics statistics, final ShardingSphereTable table) {
            if (tableData.equals(changedTableData)) {
                return;
            }
            statistics.getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().put(changedTableData.getName().toLowerCase(), changedTableData);
            ShardingSphereSchemaDataAlteredEvent event = getShardingSphereSchemaDataAlteredEvent(databaseName, schemaName, tableData, changedTableData, table);
            contextManager.getInstanceContext().getEventBusContext().post(event);
        }
        
        private ShardingSphereSchemaDataAlteredEvent getShardingSphereSchemaDataAlteredEvent(final String databaseName, final String schemaName, final ShardingSphereTableData tableData,
                                                                                             final ShardingSphereTableData changedTableData, final ShardingSphereTable table) {
            ShardingSphereSchemaDataAlteredEvent result = new ShardingSphereSchemaDataAlteredEvent(databaseName, schemaName, tableData.getName());
            Map<String, ShardingSphereRowData> tableDataMap = tableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
            Map<String, ShardingSphereRowData> changedTableDataMap = changedTableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
            YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(table.getColumnValues()));
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
}

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

package org.apache.shardingsphere.data.pipeline.core.execute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.collector.ShardingSphereDataCollector;
import org.apache.shardingsphere.infra.metadata.data.event.ShardingSphereSchemaDataAlteredEvent;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;

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
 * ShardingSphere data schedule collector.
 */
@RequiredArgsConstructor
@Slf4j
public final class ShardingSphereDataScheduleCollector {
    
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
            ShardingSphereData shardingSphereData = contextManager.getMetaDataContexts().getShardingSphereData();
            ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
            ShardingSphereData changedShardingSphereData = new ShardingSphereData();
            shardingSphereData.getDatabaseData().forEach((key, value) -> {
                if (metaData.containsDatabase(key)) {
                    collectForDatabase(key, value, metaData.getDatabases(), changedShardingSphereData);
                }
            });
            compareUpdateAndSendEvent(shardingSphereData, changedShardingSphereData, metaData.getDatabases());
        }
        
        private void collectForDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData,
                                        final Map<String, ShardingSphereDatabase> databases, final ShardingSphereData changedShardingSphereData) {
            databaseData.getSchemaData().forEach((key, value) -> {
                if (databases.get(databaseName.toLowerCase()).containsSchema(key)) {
                    collectForSchema(databaseName, key, value, databases, changedShardingSphereData);
                }
            });
        }
        
        private void collectForSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData,
                                      final Map<String, ShardingSphereDatabase> databases, final ShardingSphereData changedShardingSphereData) {
            schemaData.getTableData().forEach((key, value) -> {
                if (databases.get(databaseName.toLowerCase()).getSchema(schemaName).containsTable(key)) {
                    collectForTable(databaseName, schemaName, databases.get(databaseName).getSchema(schemaName).getTable(key), databases, changedShardingSphereData);
                }
            });
        }
        
        private void collectForTable(final String databaseName, final String schemaName, final ShardingSphereTable table,
                                     final Map<String, ShardingSphereDatabase> databases, final ShardingSphereData changedShardingSphereData) {
            Optional<ShardingSphereDataCollector> dataCollector = TypedSPILoader.findService(ShardingSphereDataCollector.class, table.getName());
            if (!dataCollector.isPresent()) {
                return;
            }
            Optional<ShardingSphereTableData> tableData = Optional.empty();
            try {
                tableData = dataCollector.get().collect(databaseName, table, databases);
            } catch (final SQLException ex) {
                log.error("Collect data failed!", ex);
            }
            tableData.ifPresent(optional -> changedShardingSphereData.getDatabaseData().computeIfAbsent(databaseName.toLowerCase(), key -> new ShardingSphereDatabaseData())
                    .getSchemaData().computeIfAbsent(schemaName, key -> new ShardingSphereSchemaData()).getTableData().put(table.getName().toLowerCase(), optional));
        }
        
        private void compareUpdateAndSendEvent(final ShardingSphereData shardingSphereData, final ShardingSphereData changedShardingSphereData, final Map<String, ShardingSphereDatabase> databases) {
            changedShardingSphereData.getDatabaseData().forEach((key, value) -> compareUpdateAndSendEventForDatabase(key, shardingSphereData.getDatabaseData().get(key), value, shardingSphereData,
                    databases.get(key.toLowerCase())));
        }
        
        private void compareUpdateAndSendEventForDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData, final ShardingSphereDatabaseData changedDatabaseData,
                                                          final ShardingSphereData shardingSphereData, final ShardingSphereDatabase database) {
            changedDatabaseData.getSchemaData().forEach((key, value) -> compareUpdateAndSendEventForSchema(databaseName, key, databaseData.getSchemaData().get(key), value, shardingSphereData,
                    database.getSchema(key)));
        }
        
        private void compareUpdateAndSendEventForSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData,
                                                        final ShardingSphereSchemaData changedSchemaData, final ShardingSphereData shardingSphereData, final ShardingSphereSchema schema) {
            changedSchemaData.getTableData().forEach((key, value) -> compareUpdateAndSendEventForTable(databaseName, schemaName, schemaData.getTableData().get(key), value, shardingSphereData,
                    schema.getTable(key)));
        }
        
        private void compareUpdateAndSendEventForTable(final String databaseName, final String schemaName, final ShardingSphereTableData tableData,
                                                       final ShardingSphereTableData changedTableData, final ShardingSphereData shardingSphereData, final ShardingSphereTable table) {
            if (tableData.equals(changedTableData)) {
                return;
            }
            shardingSphereData.getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().put(changedTableData.getName().toLowerCase(), changedTableData);
            ShardingSphereSchemaDataAlteredEvent event = getShardingSphereSchemaDataAlteredEvent(databaseName, schemaName, tableData, changedTableData, table);
            contextManager.getInstanceContext().getEventBusContext().post(event);
        }
        
        private ShardingSphereSchemaDataAlteredEvent getShardingSphereSchemaDataAlteredEvent(final String databaseName, final String schemaName, final ShardingSphereTableData tableData,
                                                                                             final ShardingSphereTableData changedTableData, final ShardingSphereTable table) {
            ShardingSphereSchemaDataAlteredEvent result = new ShardingSphereSchemaDataAlteredEvent(databaseName, schemaName, tableData.getName());
            Map<String, ShardingSphereRowData> tableDataMap = tableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
            Map<String, ShardingSphereRowData> changedTableDataMap = changedTableData.getRows().stream().collect(Collectors.toMap(ShardingSphereRowData::getUniqueKey, Function.identity()));
            YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(table.getColumns().values()));
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

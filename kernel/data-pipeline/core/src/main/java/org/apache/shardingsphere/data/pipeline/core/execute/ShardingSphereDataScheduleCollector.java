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
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.collector.ShardingSphereDataCollector;
import org.apache.shardingsphere.infra.metadata.data.collector.ShardingSphereDataCollectorFactory;
import org.apache.shardingsphere.infra.metadata.data.event.ShardingSphereSchemaDataAlteredEvent;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereTableDataSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private static final class ShardingSphereDataCollectorRunnable implements Runnable {
        
        private final ContextManager contextManager;
        
        @Override
        public void run() {
            ShardingSphereData shardingSphereData = contextManager.getMetaDataContexts().getShardingSphereData();
            ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
            ShardingSphereData changedShardingSphereData = new ShardingSphereData();
            shardingSphereData.getDatabaseData().forEach((key, value) -> collectForDatabase(key, value, metaData.getDatabases(), changedShardingSphereData));
            compareUpdateAndSendEvent(shardingSphereData, changedShardingSphereData);
        }
        
        private void collectForDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData,
                                        final Map<String, ShardingSphereDatabase> databases, final ShardingSphereData changedShardingSphereData) {
            databaseData.getSchemaData().forEach((key, value) -> collectForSchema(databaseName, key, value, databases, changedShardingSphereData));
        }
        
        private void collectForSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData,
                                      final Map<String, ShardingSphereDatabase> databases, final ShardingSphereData changedShardingSphereData) {
            schemaData.getTableData().forEach((key, value) -> collectForTable(databaseName, schemaName, databases.get(databaseName).getSchema(schemaName).getTable(key),
                    databases, changedShardingSphereData));
        }
        
        private void collectForTable(final String databaseName, final String schemaName, final ShardingSphereTable table,
                                     final Map<String, ShardingSphereDatabase> databases, final ShardingSphereData changedShardingSphereData) {
            Optional<ShardingSphereDataCollector> shardingSphereDataCollector = ShardingSphereDataCollectorFactory.findInstance(table.getName());
            if (!shardingSphereDataCollector.isPresent()) {
                return;
            }
            Optional<ShardingSphereTableData> tableData = Optional.empty();
            try {
                tableData = shardingSphereDataCollector.get().collect(databaseName, table, databases);
            } catch (SQLException ex) {
                log.error("Collect data failed!", ex);
            }
            tableData.ifPresent(shardingSphereTableData -> changedShardingSphereData.getDatabaseData().computeIfAbsent(databaseName.toLowerCase(), key -> new ShardingSphereDatabaseData())
                    .getSchemaData().computeIfAbsent(schemaName, key -> new ShardingSphereSchemaData()).getTableData().put(table.getName().toLowerCase(), shardingSphereTableData));
        }
        
        private void compareUpdateAndSendEvent(final ShardingSphereData shardingSphereData, final ShardingSphereData changedShardingSphereData) {
            changedShardingSphereData.getDatabaseData().forEach((key, value) -> compareUpdateAndSendEventForDatabase(key, shardingSphereData.getDatabaseData().get(key), value, shardingSphereData));
        }
        
        private void compareUpdateAndSendEventForDatabase(final String databaseName, final ShardingSphereDatabaseData databaseData,
                                                          final ShardingSphereDatabaseData changedDatabaseData, final ShardingSphereData shardingSphereData) {
            changedDatabaseData.getSchemaData().forEach((key, value) -> compareUpdateAndSendEventForSchema(databaseName, key, databaseData.getSchemaData().get(key), value, shardingSphereData));
        }
        
        private void compareUpdateAndSendEventForSchema(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData,
                                                        final ShardingSphereSchemaData changedSchemaData, final ShardingSphereData shardingSphereData) {
            changedSchemaData.getTableData().forEach((key, value) -> compareUpdateAndSendEventForTable(databaseName, schemaName, schemaData.getTableData().get(key), value, shardingSphereData));
        }
        
        private void compareUpdateAndSendEventForTable(final String databaseName, final String schemaName, final ShardingSphereTableData tableData,
                                                       final ShardingSphereTableData changedTableData, final ShardingSphereData shardingSphereData) {
            if (tableData.equals(changedTableData)) {
                return;
            }
            shardingSphereData.getDatabaseData().get(databaseName).getSchemaData().get(schemaName).getTableData().put(changedTableData.getName().toLowerCase(), changedTableData);
            ShardingSphereSchemaDataAlteredEvent event = new ShardingSphereSchemaDataAlteredEvent(databaseName, schemaName);
            event.getAlteredYamlTables().add(new YamlShardingSphereTableDataSwapper().swapToYamlConfiguration(changedTableData));
            contextManager.getInstanceContext().getEventBusContext().post(event);
        }
    }
}

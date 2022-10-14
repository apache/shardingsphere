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
import org.apache.shardingsphere.data.pipeline.spi.data.collector.ShardingSphereDataCollector;
import org.apache.shardingsphere.data.pipeline.spi.data.collector.ShardingSphereDataCollectorFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.event.ShardingSphereSchemaDataAlteredEvent;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ShardingSphere data schedule collector.
 */
@Slf4j
@RequiredArgsConstructor
public final class ShardingSphereDataScheduleCollector {
    
    private static final String SHARDING_SPHERE = "shardingsphere";
    
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
            DatabaseType databaseType = metaData.getDatabases().values().iterator().next().getProtocolType();
            // TODO refactor by dialect database
            if (databaseType instanceof MySQLDatabaseType) {
                collectForMySQL(shardingSphereData, metaData, databaseType);
            } else if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
                collectForPostgreSQL(shardingSphereData, metaData, databaseType);
            }
        }
        
        private void collectForMySQL(final ShardingSphereData shardingSphereData, final ShardingSphereMetaData metaData, final DatabaseType databaseType) {
            Optional<Collection<ShardingSphereTable>> shardingSphereTables = Optional.ofNullable(metaData.getDatabase(SHARDING_SPHERE))
                    .map(database -> database.getSchema(SHARDING_SPHERE)).map(schema -> schema.getTables().values());
            shardingSphereTables.ifPresent(tables -> tables.forEach(table -> metaData.getDatabases().forEach((key, value) -> {
                if (!databaseType.getSystemDatabaseSchemaMap().containsKey(key)) {
                    collectAndSendEvent(shardingSphereData, table, value, databaseType);
                }
            })));
        }
        
        private void collectForPostgreSQL(final ShardingSphereData shardingSphereData, final ShardingSphereMetaData metaData, final DatabaseType databaseType) {
            metaData.getDatabases().forEach((key, value) -> {
                if (!databaseType.getSystemDatabaseSchemaMap().containsKey(key)) {
                    Optional<Collection<ShardingSphereTable>> shardingSphereTables = Optional.ofNullable(value.getSchema(SHARDING_SPHERE)).map(schema -> schema.getTables().values());
                    shardingSphereTables.ifPresent(tables -> tables.forEach(table -> collectAndSendEvent(shardingSphereData, table, value, databaseType)));
                }
            });
        }
        
        private void collectAndSendEvent(final ShardingSphereData shardingSphereData, final ShardingSphereTable table, final ShardingSphereDatabase database, final DatabaseType databaseType) {
            String databaseName = database.getName();
            Optional<ShardingSphereDataCollector> shardingSphereDataCollector = ShardingSphereDataCollectorFactory.findInstance(table.getName());
            if (!shardingSphereDataCollector.isPresent()) {
                return;
            }
            Optional<ShardingSphereTableData> tableData = Optional.empty();
            try {
                tableData = shardingSphereDataCollector.get().collect(database, table);
            } catch (SQLException ex) {
                log.error("Collect data for sharding_table_statistics error!", ex);
            }
            tableData.ifPresent(optional -> updateAndSendEvent(shardingSphereData, table.getName(), optional, databaseType, databaseName));
        }
        
        private void updateAndSendEvent(final ShardingSphereData shardingSphereData, final String tableName, final ShardingSphereTableData changedTableData,
                                        final DatabaseType databaseType, final String databaseName) {
            Optional<ShardingSphereTableData> originTableData = getOriginTableData(shardingSphereData, tableName, databaseName, databaseType);
            if (originTableData.isPresent() && originTableData.get().equals(changedTableData)) {
                return;
            }
            Optional<String> shardingSphereDataDatabaseName = findShardingSphereDatabaseName(databaseName, databaseType);
            if (!shardingSphereDataDatabaseName.isPresent()) {
                return;
            }
            Optional.ofNullable(shardingSphereData.getDatabaseData().get(shardingSphereDataDatabaseName.get())).map(database -> database.getSchemaData().get(SHARDING_SPHERE))
                    .ifPresent(shardingSphereSchemaData -> shardingSphereSchemaData.getTableData().put(tableName, changedTableData));
            ShardingSphereSchemaDataAlteredEvent event = new ShardingSphereSchemaDataAlteredEvent(shardingSphereDataDatabaseName.get(), SHARDING_SPHERE);
            event.getAlteredTables().add(changedTableData);
            contextManager.getInstanceContext().getEventBusContext().post(event);
        }
        
        private Optional<ShardingSphereTableData> getOriginTableData(final ShardingSphereData shardingSphereData, final String tableName, final String databaseName, final DatabaseType databaseType) {
            Optional<String> shardingSphereDataDatabaseName = findShardingSphereDatabaseName(databaseName, databaseType);
            return shardingSphereDataDatabaseName.flatMap(optional -> Optional.ofNullable(shardingSphereData.getDatabaseData().get(optional))
                    .map(database -> database.getSchemaData().get(SHARDING_SPHERE)).map(shardingSphereSchemaData -> shardingSphereSchemaData.getTableData().get(tableName)));
        }
        
        private Optional<String> findShardingSphereDatabaseName(final String databaseName, final DatabaseType databaseType) {
            if (databaseType instanceof MySQLDatabaseType) {
                return Optional.of(SHARDING_SPHERE);
            } else if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
                return Optional.of(databaseName);
            }
            // TODO support other database type
            return Optional.empty();
        }
    }
}

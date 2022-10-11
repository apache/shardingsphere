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
import org.apache.shardingsphere.data.pipeline.spi.data.collector.ShardingSphereDataCollectorFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
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
                    collectForEachDatabase(shardingSphereData, table, value, databaseType);
                }
            })));
        }
        
        private void collectForPostgreSQL(final ShardingSphereData shardingSphereData, final ShardingSphereMetaData metaData, final DatabaseType databaseType) {
            metaData.getDatabases().forEach((key, value) -> {
                if (!databaseType.getSystemDatabaseSchemaMap().containsKey(key)) {
                    Optional<Collection<ShardingSphereTable>> shardingSphereTables = Optional.ofNullable(value.getSchema(SHARDING_SPHERE)).map(schema -> schema.getTables().values());
                    shardingSphereTables.ifPresent(tables -> tables.forEach(table -> collectForEachDatabase(shardingSphereData, table, value, databaseType)));
                }
            });
        }
        
        private void collectForEachDatabase(final ShardingSphereData shardingSphereData, final ShardingSphereTable table, final ShardingSphereDatabase database, final DatabaseType databaseType) {
            String databaseName = database.getName();
            Map<String, DataSource> dataSources = database.getResourceMetaData().getDataSources();
            ShardingSphereDataCollectorFactory.findInstance(table.getName()).ifPresent(shardingSphereDataCollector -> {
                try {
                    shardingSphereDataCollector.collect(shardingSphereData, databaseName, database.getRuleMetaData(), dataSources, databaseType);
                } catch (SQLException ex) {
                    log.error("Collect data for sharding_table_statistics error!", ex);
                }
            });
        }
    }
}

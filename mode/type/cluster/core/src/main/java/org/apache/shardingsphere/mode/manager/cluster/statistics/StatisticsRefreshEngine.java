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

package org.apache.shardingsphere.mode.manager.cluster.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.shardingsphere.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Statistics refresh engine.
 */
@RequiredArgsConstructor
@Slf4j
public final class StatisticsRefreshEngine {
    
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("statistics-collect-%d"));
    
    private final ContextManager contextManager;
    
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
                contextManager.getExclusiveOperatorEngine().operate(new RefreshStatisticsOperation(), 5000L, this::refreshStatistics);
            }
            cleanStatisticsData();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("Refresh statistics error.", ex);
        }
    }
    
    private void refreshStatistics() {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        for (ShardingSphereDatabase each : metaData.getAllDatabases()) {
            refreshForDatabase(metaData, each);
        }
    }
    
    private void refreshForDatabase(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database) {
        for (ShardingSphereSchema each : database.getAllSchemas()) {
            refreshForSchema(metaData, database.getName(), each);
        }
    }
    
    private void refreshForSchema(final ShardingSphereMetaData metaData, final String databaseName, final ShardingSphereSchema schema) {
        for (ShardingSphereTable each : schema.getAllTables()) {
            refreshForTable(metaData, databaseName, schema.getName(), each);
        }
    }
    
    private void refreshForTable(final ShardingSphereMetaData metaData, final String databaseName, final String schemaName, final ShardingSphereTable table) {
        try {
            Optional<DialectDatabaseStatisticsCollector> dialectStatisticsCollector = "shardingsphere".equalsIgnoreCase(schemaName)
                    ? Optional.of(new ShardingSphereStatisticsCollector())
                    : DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, metaData.getDatabase(databaseName).getProtocolType());
            if (dialectStatisticsCollector.isPresent()) {
                Optional<Collection<Map<String, Object>>> rowColumnValues = dialectStatisticsCollector.get().collectRowColumnValues(databaseName, schemaName, table.getName(), metaData);
                rowColumnValues.ifPresent(optional -> new StatisticsStorageEngine(contextManager, databaseName, schemaName, table.getName(), optional).storage());
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("Refresh {}.{}.{} statistics failed.", databaseName, schemaName, table.getName(), ex);
        }
    }
    
    private void cleanStatisticsData() {
        try {
            ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
            ShardingSphereStatistics statistics = contextManager.getMetaDataContexts().getStatistics();
            for (Entry<String, DatabaseStatistics> entry : statistics.getDatabaseStatisticsMap().entrySet()) {
                if (!metaData.containsDatabase(entry.getKey())) {
                    contextManager.getPersistServiceFacade().getMetaDataFacade().getStatisticsService().delete(entry.getKey());
                }
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("Clean up useless statistics data failed.", ex);
        }
    }
}

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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Meta data contexts.
 */
@Getter
public final class MetaDataContexts implements AutoCloseable {
    
    private final MetaDataBasedPersistService persistService;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereStatistics statistics;
    
    public MetaDataContexts(final MetaDataBasedPersistService persistService, final ShardingSphereMetaData metaData) {
        this.persistService = persistService;
        this.metaData = metaData;
        this.statistics = initStatistics(metaData);
    }
    
    private ShardingSphereStatistics initStatistics(final ShardingSphereMetaData metaData) {
        if (metaData.getDatabases().isEmpty()) {
            return new ShardingSphereStatistics();
        }
        DatabaseType protocolType = metaData.getDatabases().values().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        // TODO can `protocolType instanceof SchemaSupportedDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
        DatabaseType databaseType = dialectDatabaseMetaData.getDefaultSchema().isPresent() ? TypedSPILoader.getService(DatabaseType.class, "PostgreSQL") : protocolType;
        Optional<ShardingSphereStatisticsBuilder> statisticsBuilder = DatabaseTypedSPILoader.findService(ShardingSphereStatisticsBuilder.class, databaseType);
        if (!statisticsBuilder.isPresent()) {
            return new ShardingSphereStatistics();
        }
        ShardingSphereStatistics result = statisticsBuilder.get().build(metaData);
        Optional<ShardingSphereStatistics> loadedStatistics = persistService.getShardingSphereDataPersistService().load(metaData);
        loadedStatistics.ifPresent(optional -> useLoadedToReplaceInit(result, optional));
        return result;
    }
    
    private void useLoadedToReplaceInit(final ShardingSphereStatistics initStatistics, final ShardingSphereStatistics loadedStatistics) {
        for (Entry<String, ShardingSphereDatabaseData> entry : initStatistics.getDatabaseData().entrySet()) {
            if (loadedStatistics.getDatabaseData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitByDatabaseData(entry.getValue(), loadedStatistics.getDatabaseData().get(entry.getKey()));
            }
        }
    }
    
    private void useLoadedToReplaceInitByDatabaseData(final ShardingSphereDatabaseData initDatabaseData, final ShardingSphereDatabaseData loadedDatabaseData) {
        for (Entry<String, ShardingSphereSchemaData> entry : initDatabaseData.getSchemaData().entrySet()) {
            if (loadedDatabaseData.getSchemaData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitBySchemaData(entry.getValue(), loadedDatabaseData.getSchemaData().get(entry.getKey()));
            }
        }
    }
    
    private void useLoadedToReplaceInitBySchemaData(final ShardingSphereSchemaData initSchemaData, final ShardingSphereSchemaData loadedSchemaData) {
        for (Entry<String, ShardingSphereTableData> entry : initSchemaData.getTableData().entrySet()) {
            if (loadedSchemaData.getTableData().containsKey(entry.getKey())) {
                entry.setValue(loadedSchemaData.getTableData().get(entry.getKey()));
            }
        }
    }
    
    @SneakyThrows(Exception.class)
    @Override
    public void close() {
        persistService.getRepository().close();
        for (ShardingSphereRule each : getAllRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
    
    private Collection<ShardingSphereRule> getAllRules() {
        Collection<ShardingSphereRule> result = new LinkedList<>(metaData.getGlobalRuleMetaData().getRules());
        metaData.getDatabases().values().stream().map(each -> each.getRuleMetaData().getRules()).forEach(result::addAll);
        return result;
    }
}

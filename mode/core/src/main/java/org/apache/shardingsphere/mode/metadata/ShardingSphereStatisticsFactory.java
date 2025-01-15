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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.util.Optional;

/**
 * ShardingSphere statistics factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereStatisticsFactory {
    
    /**
     * Create statistics.
     *
     * @param persistService meta data persist service
     * @param metaData meta data
     * @return created statistics
     */
    public static ShardingSphereStatistics create(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        if (metaData.getAllDatabases().isEmpty()) {
            return new ShardingSphereStatistics();
        }
        Optional<ShardingSphereStatisticsBuilder> statisticsBuilder = DatabaseTypedSPILoader.findService(ShardingSphereStatisticsBuilder.class, getDatabaseType(metaData));
        if (!statisticsBuilder.isPresent()) {
            return new ShardingSphereStatistics();
        }
        ShardingSphereStatistics builtStatistics = build(metaData, statisticsBuilder.get());
        Optional<ShardingSphereStatistics> loadedStatistics = persistService.getShardingSphereDataPersistService().load(metaData);
        if (!loadedStatistics.isPresent()) {
            return builtStatistics;
        }
        putStatisticsIfAbsent(loadedStatistics.get(), builtStatistics);
        return loadedStatistics.get();
    }
    
    private static DatabaseType getDatabaseType(final ShardingSphereMetaData metaData) {
        DatabaseType protocolType = metaData.getAllDatabases().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        // TODO can `protocolType instanceof SchemaSupportedDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
        return dialectDatabaseMetaData.getDefaultSchema().isPresent() ? TypedSPILoader.getService(DatabaseType.class, "PostgreSQL") : protocolType;
    }
    
    private static ShardingSphereStatistics build(final ShardingSphereMetaData metaData, final ShardingSphereStatisticsBuilder statisticsBuilder) {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        for (ShardingSphereDatabase each : metaData.getAllDatabases()) {
            ShardingSphereDatabaseData databaseData = statisticsBuilder.build(each);
            if (!databaseData.getSchemaData().isEmpty()) {
                result.putDatabase(each.getName(), databaseData);
            }
        }
        return result;
    }
    
    private static void putStatisticsIfAbsent(final ShardingSphereStatistics loadedStatistics, final ShardingSphereStatistics builtStatistics) {
        loadedStatistics.getDatabaseData().keySet().stream().filter(builtStatistics::containsDatabase).forEach(builtStatistics::dropDatabase);
        builtStatistics.getDatabaseData().forEach(loadedStatistics::putDatabase);
    }
}

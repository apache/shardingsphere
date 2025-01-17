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
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereDefaultStatisticsBuilder;
import org.apache.shardingsphere.infra.metadata.statistics.builder.DialectStatisticsAppender;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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
        
        ShardingSphereStatistics loadedStatistics = persistService.getShardingSphereDataPersistService().load(metaData).orElse(new ShardingSphereStatistics());
        Collection<ShardingSphereDatabase> unloadedDatabases = metaData.getAllDatabases().stream().filter(each -> !loadedStatistics.containsDatabase(each.getName())).collect(Collectors.toList());
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        Optional<DialectStatisticsAppender> dialectStatisticsAppender = DatabaseTypedSPILoader.findService(DialectStatisticsAppender.class, getDatabaseType(metaData));
        for (ShardingSphereDatabase each : unloadedDatabases) {
            ShardingSphereDatabaseData databaseData = new ShardingSphereDefaultStatisticsBuilder().build(each);
            dialectStatisticsAppender.ifPresent(optional -> optional.append(databaseData, each));
            if (!databaseData.getSchemaData().isEmpty()) {
                result.putDatabase(each.getName(), databaseData);
            }
        }
        loadedStatistics.getDatabaseData().forEach(result::putDatabase);
        return result;
    }
    
    private static DatabaseType getDatabaseType(final ShardingSphereMetaData metaData) {
        DatabaseType protocolType = metaData.getAllDatabases().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        // TODO can `protocolType instanceof SchemaSupportedDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
        return dialectDatabaseMetaData.getDefaultSchema().isPresent() ? TypedSPILoader.getService(DatabaseType.class, "PostgreSQL") : protocolType;
    }
}

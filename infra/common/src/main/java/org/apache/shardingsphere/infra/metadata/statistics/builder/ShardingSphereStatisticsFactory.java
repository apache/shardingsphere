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

package org.apache.shardingsphere.infra.metadata.statistics.builder;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Map.Entry;
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
     * @param metaData meta data
     * @param loadedStatistics loaded statistics
     * @return created statistics
     */
    public static ShardingSphereStatistics create(final ShardingSphereMetaData metaData, final ShardingSphereStatistics loadedStatistics) {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        if (metaData.getAllDatabases().isEmpty()) {
            return result;
        }
        Optional<DialectStatisticsAppender> dialectStatisticsAppender = DatabaseTypedSPILoader.findService(DialectStatisticsAppender.class, getDatabaseType(metaData));
        Collection<ShardingSphereDatabase> unloadedDatabases = metaData.getAllDatabases().stream().filter(each -> !loadedStatistics.containsDatabase(each.getName())).collect(Collectors.toList());
        for (ShardingSphereDatabase each : unloadedDatabases) {
            ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
            dialectStatisticsAppender.ifPresent(optional -> optional.append(databaseData, each));
            if (!databaseData.getSchemaData().isEmpty()) {
                result.putDatabase(each.getName(), databaseData);
            }
        }
        loadedStatistics.getDatabaseData().forEach(result::putDatabase);
        fillDefaultShardingSphereStatistics(metaData, result);
        return result;
    }
    
    private static DatabaseType getDatabaseType(final ShardingSphereMetaData metaData) {
        DatabaseType protocolType = metaData.getAllDatabases().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        // TODO can `protocolType instanceof SchemaSupportedDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
        return dialectDatabaseMetaData.getDefaultSchema().isPresent() ? TypedSPILoader.getService(DatabaseType.class, "PostgreSQL") : protocolType;
    }
    
    private static void fillDefaultShardingSphereStatistics(final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        for (ShardingSphereDatabase database : metaData.getAllDatabases()) {
            ShardingSphereDatabaseData defaultDatabaseData = new ShardingSphereDefaultStatisticsBuilder().build(database);
            Collection<String> defaultSchemaNames = new CaseInsensitiveSet<>(defaultDatabaseData.getSchemaData().keySet());
            if (database.getAllSchemas().stream().noneMatch(optional -> defaultSchemaNames.contains(optional.getName()))) {
                continue;
            }
            if (!statistics.containsDatabase(database.getName())) {
                statistics.putDatabase(database.getName(), defaultDatabaseData);
                continue;
            }
            fillDefaultShardingSphereStatistics(defaultDatabaseData, statistics.getDatabase(database.getName()));
        }
    }
    
    private static void fillDefaultShardingSphereStatistics(final ShardingSphereDatabaseData defaultDatabaseData, final ShardingSphereDatabaseData existedDatabaseData) {
        for (Entry<String, ShardingSphereSchemaData> entry : defaultDatabaseData.getSchemaData().entrySet()) {
            if (!existedDatabaseData.containsSchema(entry.getKey())) {
                existedDatabaseData.putSchema(entry.getKey(), entry.getValue());
                continue;
            }
            fillDefaultShardingSphereStatistics(entry.getValue(), existedDatabaseData.getSchema(entry.getKey()));
        }
    }
    
    private static void fillDefaultShardingSphereStatistics(final ShardingSphereSchemaData defaultSchemaData, final ShardingSphereSchemaData existedSchemaData) {
        for (Entry<String, ShardingSphereTableData> entry : defaultSchemaData.getTableData().entrySet()) {
            if (!existedSchemaData.containsTable(entry.getKey())) {
                existedSchemaData.putTable(entry.getKey(), entry.getValue());
            }
        }
    }
}

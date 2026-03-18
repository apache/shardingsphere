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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
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
        Collection<ShardingSphereDatabase> unloadedDatabases = metaData.getAllDatabases().stream()
                .filter(each -> !loadedStatistics.containsDatabaseStatistics(each.getName())).collect(Collectors.toList());
        for (ShardingSphereDatabase each : unloadedDatabases) {
            DatabaseStatistics databaseStatistics = new DatabaseStatistics();
            dialectStatisticsAppender.ifPresent(optional -> optional.append(databaseStatistics, each));
            if (!databaseStatistics.getSchemaStatisticsMap().isEmpty()) {
                result.putDatabaseStatistics(each.getName(), databaseStatistics);
            }
        }
        loadedStatistics.getDatabaseStatisticsMap().forEach(result::putDatabaseStatistics);
        fillDefaultStatistics(metaData, result);
        return result;
    }
    
    private static DatabaseType getDatabaseType(final ShardingSphereMetaData metaData) {
        DatabaseType protocolType = metaData.getAllDatabases().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        // TODO can `protocolType instanceof SchemaSupportedDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
        return dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() ? TypedSPILoader.getService(DatabaseType.class, "PostgreSQL") : protocolType;
    }
    
    private static void fillDefaultStatistics(final ShardingSphereMetaData metaData, final ShardingSphereStatistics statistics) {
        for (ShardingSphereDatabase database : metaData.getAllDatabases()) {
            DatabaseStatistics defaultDatabaseStatistics = new ShardingSphereDefaultStatisticsBuilder().build(database);
            Collection<String> defaultSchemaNames = new CaseInsensitiveSet<>(defaultDatabaseStatistics.getSchemaStatisticsMap().keySet());
            if (database.getAllSchemas().stream().noneMatch(optional -> defaultSchemaNames.contains(optional.getName()))) {
                continue;
            }
            if (!statistics.containsDatabaseStatistics(database.getName())) {
                statistics.putDatabaseStatistics(database.getName(), defaultDatabaseStatistics);
                continue;
            }
            fillDefaultStatistics(defaultDatabaseStatistics, statistics.getDatabaseStatistics(database.getName()));
        }
    }
    
    private static void fillDefaultStatistics(final DatabaseStatistics defaultDatabaseStatistics, final DatabaseStatistics existedDatabaseStatistics) {
        for (Entry<String, SchemaStatistics> entry : defaultDatabaseStatistics.getSchemaStatisticsMap().entrySet()) {
            if (!existedDatabaseStatistics.containsSchemaStatistics(entry.getKey())) {
                existedDatabaseStatistics.putSchemaStatistics(entry.getKey(), entry.getValue());
                continue;
            }
            fillDefaultStatistics(entry.getValue(), existedDatabaseStatistics.getSchemaStatistics(entry.getKey()));
        }
    }
    
    private static void fillDefaultStatistics(final SchemaStatistics defaultSchemaStatistics, final SchemaStatistics existedSchemaStatistics) {
        for (Entry<String, TableStatistics> entry : defaultSchemaStatistics.getTableStatisticsMap().entrySet()) {
            if (!existedSchemaStatistics.containsTableStatistics(entry.getKey())) {
                existedSchemaStatistics.putTableStatistics(entry.getKey(), entry.getValue());
            }
        }
    }
}

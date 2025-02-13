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

package org.apache.shardingsphere.infra.metadata.statistics.collector.shardingsphere;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Statistics collector for ShardingSphere.
 */
public final class ShardingSphereStatisticsCollector implements DialectDatabaseStatisticsCollector {
    
    private static final Map<String, Collection<String>> STATISTICS_SCHEMA_TABLES = new CaseInsensitiveMap<>();
    
    static {
        for (ShardingSphereTableStatisticsCollector each : ShardingSphereServiceLoader.getServiceInstances(ShardingSphereTableStatisticsCollector.class)) {
            if (!STATISTICS_SCHEMA_TABLES.containsKey(each.getSchemaName())) {
                STATISTICS_SCHEMA_TABLES.put(each.getSchemaName(), new CaseInsensitiveSet<>());
            }
            STATISTICS_SCHEMA_TABLES.get(each.getSchemaName()).add(each.getTableName());
        }
    }
    
    @Override
    public Optional<Collection<Map<String, Object>>> collectRowColumnValues(final String databaseName, final String schemaName, final String tableName,
                                                                            final ShardingSphereMetaData metaData) throws SQLException {
        Optional<ShardingSphereTableStatisticsCollector> tableStatisticsCollector = TypedSPILoader.findService(ShardingSphereTableStatisticsCollector.class,
                String.format("%s.%s", schemaName, tableName));
        return tableStatisticsCollector.isPresent() ? Optional.of(tableStatisticsCollector.get().collect(databaseName, schemaName, tableName, metaData)) : Optional.empty();
    }
    
    @Override
    public boolean isStatisticsTables(final Map<String, Collection<String>> schemaTables) {
        if (schemaTables.isEmpty()) {
            return false;
        }
        for (Entry<String, Collection<String>> entry : schemaTables.entrySet()) {
            if (!STATISTICS_SCHEMA_TABLES.containsKey(entry.getKey())) {
                return false;
            }
            if (!STATISTICS_SCHEMA_TABLES.get(entry.getKey()).containsAll(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getDatabaseType() {
        return "ShardingSphere";
    }
}

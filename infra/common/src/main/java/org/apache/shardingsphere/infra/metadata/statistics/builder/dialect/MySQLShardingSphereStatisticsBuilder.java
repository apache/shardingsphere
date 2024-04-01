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

package org.apache.shardingsphere.infra.metadata.statistics.builder.dialect;

import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * ShardingSphere statistics builder for MySQL.
 */

public final class MySQLShardingSphereStatisticsBuilder implements ShardingSphereStatisticsBuilder {
    
    private static final String SHARDING_SPHERE = "shardingsphere";
    
    private static final String CLUSTER_INFORMATION = "cluster_information";
    
    private static final String SHARDING_TABLE_STATISTICS = "sharding_table_statistics";
    
    @Override
    public ShardingSphereStatistics build(final ShardingSphereMetaData metaData) {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        for (Entry<String, ShardingSphereDatabase> entry : metaData.getDatabases().entrySet()) {
            ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
            collectDatabaseData(metaData, entry.getValue(), databaseData);
            result.getDatabaseData().put(entry.getKey(), databaseData);
        }
        return result;
    }
    
    private void collectDatabaseData(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final ShardingSphereDatabaseData databaseData) {
        for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
            if (SHARDING_SPHERE.equals(entry.getKey())) {
                ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
                initForClusterInformationTable(schemaData);
                collectTableData(metaData, database, entry.getValue().getTable(SHARDING_TABLE_STATISTICS), schemaData);
                databaseData.getSchemaData().put(SHARDING_SPHERE, schemaData);
            }
        }
    }
    
    private void initForClusterInformationTable(final ShardingSphereSchemaData schemaData) {
        ShardingSphereTableData tableData = new ShardingSphereTableData(CLUSTER_INFORMATION);
        tableData.getRows().add(new ShardingSphereRowData(Collections.singletonList(ShardingSphereVersion.VERSION)));
        schemaData.getTableData().put(CLUSTER_INFORMATION, tableData);
    }
    
    private void collectTableData(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final ShardingSphereTable table, final ShardingSphereSchemaData schemaData) {
        Optional<ShardingSphereStatisticsCollector> dataCollector = TypedSPILoader.findService(ShardingSphereStatisticsCollector.class, table.getName());
        ShardingSphereTableData tableData = new ShardingSphereTableData(table.getName());
        if (dataCollector.isPresent()) {
            try {
                dataCollector.get().collect(database.getName(), table, metaData.getDatabases(), metaData.getGlobalRuleMetaData()).ifPresent(optional -> tableData.getRows().addAll(optional.getRows()));
                // CHECKSTYLE:OFF
            } catch (final Exception ignored) {
                // CHECKSTYLE:ON
            }
        }
        schemaData.getTableData().put(table.getName(), tableData);
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}

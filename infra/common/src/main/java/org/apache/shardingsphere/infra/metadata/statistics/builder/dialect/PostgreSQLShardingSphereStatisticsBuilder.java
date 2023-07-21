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
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ShardingSphere statistics builder for PostgreSQL.
 */

public final class PostgreSQLShardingSphereStatisticsBuilder implements ShardingSphereStatisticsBuilder {
    
    private static final Map<String, Collection<String>> COLLECTED_SCHEMA_TABLES = new LinkedHashMap<>();
    
    private static final Map<String, Collection<String>> INIT_DATA_SCHEMA_TABLES = new LinkedHashMap<>();
    
    static {
        COLLECTED_SCHEMA_TABLES.put("shardingsphere", Collections.singletonList("sharding_table_statistics"));
        COLLECTED_SCHEMA_TABLES.put("pg_catalog", Arrays.asList("pg_class", "pg_namespace"));
        INIT_DATA_SCHEMA_TABLES.put("shardingsphere", Collections.singletonList("cluster_information"));
    }
    
    @Override
    public ShardingSphereStatistics build(final ShardingSphereMetaData metaData) {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        for (Entry<String, ShardingSphereDatabase> entry : metaData.getDatabases().entrySet()) {
            if (new PostgreSQLDatabaseType().getSystemDatabaseSchemaMap().containsKey(entry.getKey())) {
                continue;
            }
            ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
            appendSchemaData(entry.getValue(), databaseData);
            result.getDatabaseData().put(entry.getKey(), databaseData);
        }
        return result;
    }
    
    private void appendSchemaData(final ShardingSphereDatabase shardingSphereDatabase, final ShardingSphereDatabaseData databaseData) {
        for (Entry<String, ShardingSphereSchema> entry : shardingSphereDatabase.getSchemas().entrySet()) {
            if (COLLECTED_SCHEMA_TABLES.containsKey(entry.getKey()) || INIT_DATA_SCHEMA_TABLES.containsKey(entry.getKey())) {
                ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
                appendTableData(entry, schemaData);
                databaseData.getSchemaData().put(entry.getKey(), schemaData);
            }
        }
    }
    
    private void appendTableData(final Entry<String, ShardingSphereSchema> schemaEntry, final ShardingSphereSchemaData schemaData) {
        for (Entry<String, ShardingSphereTable> entry : schemaEntry.getValue().getTables().entrySet()) {
            ShardingSphereTableData tableData = new ShardingSphereTableData(entry.getValue().getName());
            if (null != COLLECTED_SCHEMA_TABLES.get(schemaEntry.getKey()) && COLLECTED_SCHEMA_TABLES.get(schemaEntry.getKey()).contains(entry.getKey())) {
                schemaData.getTableData().put(entry.getKey(), tableData);
            }
            if (null != INIT_DATA_SCHEMA_TABLES.get(schemaEntry.getKey()) && INIT_DATA_SCHEMA_TABLES.get(schemaEntry.getKey()).contains(entry.getKey())) {
                tableData.getRows().add(new ShardingSphereRowData(Collections.singletonList(ShardingSphereVersion.VERSION)));
                schemaData.getTableData().put(entry.getKey(), tableData);
            }
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}

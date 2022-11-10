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

package org.apache.shardingsphere.infra.metadata.data.builder.dialect;

import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.builder.ShardingSphereDataBuilder;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Postgre SQL ShardingSphere data Builder.
 */

public final class PostgreSQLShardingSphereDataBuilder implements ShardingSphereDataBuilder {
    
    private static final Map<String, Collection<String>> COLLECTED_SCHEMA_TABLES = new LinkedHashMap<>();
    
    static {
        COLLECTED_SCHEMA_TABLES.put("shardingsphere", Collections.singletonList("sharding_table_statistics"));
        COLLECTED_SCHEMA_TABLES.put("pg_catalog", Arrays.asList("pg_class", "pg_namespace"));
    }
    
    @Override
    public ShardingSphereData build(final ShardingSphereMetaData metaData) {
        ShardingSphereData result = new ShardingSphereData();
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
            if (COLLECTED_SCHEMA_TABLES.containsKey(entry.getKey())) {
                ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
                appendTableData(entry, schemaData);
                databaseData.getSchemaData().put(entry.getKey(), schemaData);
            }
        }
    }
    
    private void appendTableData(final Entry<String, ShardingSphereSchema> schemaEntry, final ShardingSphereSchemaData schemaData) {
        for (Entry<String, ShardingSphereTable> entry : schemaEntry.getValue().getTables().entrySet()) {
            if (COLLECTED_SCHEMA_TABLES.get(schemaEntry.getKey()).contains(entry.getKey())) {
                schemaData.getTableData().put(entry.getKey(), new ShardingSphereTableData(entry.getValue().getName(),
                        new ArrayList<>(entry.getValue().getColumns().values())));
            }
        }
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}

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

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.builder.DialectStatisticsAppender;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Statistics appender for PostgreSQL.
 */
public final class PostgreSQLStatisticsAppender implements DialectStatisticsAppender {
    
    private static final Map<String, Collection<String>> INIT_DATA_SCHEMA_TABLES = new LinkedHashMap<>();
    
    static {
        INIT_DATA_SCHEMA_TABLES.put("pg_catalog", Arrays.asList("pg_class", "pg_namespace"));
    }
    
    @Override
    public void append(final ShardingSphereDatabaseData databaseData, final ShardingSphereDatabase database) {
        for (Entry<String, Collection<String>> entry : INIT_DATA_SCHEMA_TABLES.entrySet()) {
            ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
            if (null != database.getSchema(entry.getKey())) {
                initTables(database.getSchema(entry.getKey()), entry.getValue(), schemaData);
                databaseData.putSchema(entry.getKey(), schemaData);
            }
        }
    }
    
    private void initTables(final ShardingSphereSchema schema, final Collection<String> tables, final ShardingSphereSchemaData schemaData) {
        for (ShardingSphereTable each : schema.getAllTables()) {
            if (tables.contains(each.getName().toLowerCase())) {
                schemaData.putTable(each.getName().toLowerCase(), new ShardingSphereTableData(each.getName()));
            }
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}

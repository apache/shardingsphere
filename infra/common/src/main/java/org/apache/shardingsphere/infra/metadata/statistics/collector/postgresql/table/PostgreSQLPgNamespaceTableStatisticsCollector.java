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

package org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.table;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.PostgreSQLTableStatisticsCollector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Table statistics collector for pg_catalog.pg_namespace of PostgreSQL.
 */
public final class PostgreSQLPgNamespaceTableStatisticsCollector implements PostgreSQLTableStatisticsCollector {
    
    private static final String PUBLIC_SCHEMA = "public";
    
    private static final Long PUBLIC_SCHEMA_OID = 0L;
    
    @Override
    public Collection<Map<String, Object>> collect(final String databaseName, final String schemaName, final String tableName, final ShardingSphereMetaData metaData) {
        Collection<Map<String, Object>> result = new LinkedList<>();
        long oid = 1L;
        for (ShardingSphereSchema each : metaData.getDatabase(databaseName).getAllSchemas()) {
            Map<String, Object> columnValues = new CaseInsensitiveMap<>(2, 1F);
            columnValues.put("oid", PUBLIC_SCHEMA.equalsIgnoreCase(each.getName()) ? PUBLIC_SCHEMA_OID : oid++);
            columnValues.put("nspname", each.getName());
            result.add(columnValues);
        }
        return result;
    }
    
    @Override
    public String getSchemaName() {
        return "pg_catalog";
    }
    
    @Override
    public String getTableName() {
        return "pg_namespace";
    }
    
    @Override
    public String getType() {
        return "pg_catalog.pg_namespace";
    }
}

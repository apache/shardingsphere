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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * CDC schema table utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CDCSchemaTableUtils {
    
    /**
     * Parse table expression with schema.
     *
     * @param database database
     * @param schemaTables schema tables
     * @return map key is schema, value is table names
     */
    public static Map<String, Set<String>> parseTableExpressionWithSchema(final ShardingSphereDatabase database, final Collection<SchemaTable> schemaTables) {
        Collection<String> systemSchemas = DatabaseTypedSPILoader.getService(DialectSystemDatabase.class, database.getProtocolType()).getSystemSchemas();
        if (schemaTables.stream().anyMatch(each -> "*".equals(each.getTable()) && ("*".equals(each.getSchema()) || each.getSchema().isEmpty()))) {
            return parseTableExpressionWithAllTables(database, systemSchemas);
        }
        Map<String, Set<String>> result = new HashMap<>();
        for (SchemaTable each : schemaTables) {
            if ("*".equals(each.getSchema())) {
                result.putAll(parseTableExpressionWithAllSchema(database, systemSchemas, each));
            } else if ("*".equals(each.getTable())) {
                result.putAll(parseTableExpressionWithAllTable(database, each));
            } else {
                result.computeIfAbsent(each.getSchema(), ignored -> new HashSet<>()).add(each.getTable());
            }
        }
        return result;
    }
    
    private static Map<String, Set<String>> parseTableExpressionWithAllTables(final ShardingSphereDatabase database, final Collection<String> systemSchemas) {
        Map<String, Set<String>> result = new HashMap<>(database.getSchemas().size(), 1);
        for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
            if (!systemSchemas.contains(entry.getKey())) {
                entry.getValue().getAllTableNames().forEach(tableName -> result.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).add(tableName));
            }
            
        }
        return result;
    }
    
    private static Map<String, Set<String>> parseTableExpressionWithAllSchema(final ShardingSphereDatabase database, final Collection<String> systemSchemas, final SchemaTable table) {
        Map<String, Set<String>> result = new HashMap<>();
        for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
            if (!systemSchemas.contains(entry.getKey())) {
                entry.getValue().getAllTableNames().stream().filter(tableName -> tableName.equals(table.getTable())).findFirst()
                        .ifPresent(optional -> result.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).add(optional));
            }
        }
        return result;
    }
    
    private static Map<String, Set<String>> parseTableExpressionWithAllTable(final ShardingSphereDatabase database, final SchemaTable each) {
        Map<String, Set<String>> result = new HashMap<>();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData();
        String schemaName = each.getSchema().isEmpty() ? dialectDatabaseMetaData.getDefaultSchema().orElseThrow(() -> new IllegalStateException("Default schema should exist.")) : each.getSchema();
        ShardingSphereSchema schema = database.getSchema(schemaName);
        ShardingSpherePreconditions.checkNotNull(schema, () -> new SchemaNotFoundException(each.getSchema()));
        schema.getAllTableNames().forEach(tableName -> result.computeIfAbsent(schemaName, ignored -> new HashSet<>()).add(tableName));
        return result;
    }
    
    /**
     * Parse table expression without schema.
     *
     * @param database database
     * @param tableNames table names
     * @return parsed table names
     */
    public static Collection<String> parseTableExpressionWithoutSchema(final ShardingSphereDatabase database, final List<String> tableNames) {
        ShardingSphereSchema schema = database.getSchema(database.getName());
        Set<String> allTableNames = null == schema ? Collections.emptySet() : new HashSet<>(schema.getAllTableNames());
        return tableNames.stream().anyMatch("*"::equals) ? allTableNames : new HashSet<>(tableNames);
    }
}

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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.SchemaSupportedDatabaseType;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
        Map<String, Set<String>> result = new HashMap<>();
        Collection<String> systemSchemas = database.getProtocolType().getSystemSchemas();
        Optional<SchemaTable> allSchemaTablesOptional = schemaTables.stream().filter(each -> "*".equals(each.getTable()) && ("*".equals(each.getSchema()) || each.getSchema().isEmpty())).findFirst();
        if (allSchemaTablesOptional.isPresent()) {
            for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
                if (systemSchemas.contains(entry.getKey())) {
                    continue;
                }
                entry.getValue().getAllTableNames().forEach(tableName -> result.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).add(tableName));
            }
            return result;
        }
        for (SchemaTable each : schemaTables) {
            if ("*".equals(each.getSchema())) {
                for (Entry<String, ShardingSphereSchema> entry : database.getSchemas().entrySet()) {
                    if (systemSchemas.contains(entry.getKey())) {
                        continue;
                    }
                    entry.getValue().getAllTableNames().stream().filter(tableName -> tableName.equals(each.getTable())).findFirst()
                            .ifPresent(tableName -> result.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).add(tableName));
                }
            } else if ("*".equals(each.getTable())) {
                String schemaName = each.getSchema().isEmpty() ? getDefaultSchema(database.getProtocolType()) : each.getSchema();
                ShardingSphereSchema schema = database.getSchema(schemaName);
                ShardingSpherePreconditions.checkNotNull(schema, () -> new SchemaNotFoundException(each.getSchema()));
                schema.getAllTableNames().forEach(tableName -> result.computeIfAbsent(schemaName, ignored -> new HashSet<>()).add(tableName));
            } else {
                result.computeIfAbsent(each.getSchema(), ignored -> new HashSet<>()).add(each.getTable());
            }
        }
        return result;
    }
    
    private static String getDefaultSchema(final DatabaseType databaseType) {
        if (!(databaseType instanceof SchemaSupportedDatabaseType)) {
            return null;
        }
        return ((SchemaSupportedDatabaseType) databaseType).getDefaultSchema();
    }
    
    /**
     * Parse table expression without schema.
     *
     * @param database database
     * @param tableNames table names
     * @return parsed table names
     */
    public static Collection<String> parseTableExpressionWithoutSchema(final ShardingSphereDatabase database, final List<String> tableNames) {
        Optional<String> allTablesOptional = tableNames.stream().filter("*"::equals).findFirst();
        Set<String> allTableNames = new HashSet<>(database.getSchema(database.getName()).getAllTableNames());
        return allTablesOptional.isPresent() ? allTableNames : new HashSet<>(tableNames);
    }
}

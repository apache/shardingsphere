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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData();
        for (SchemaTable each : schemaTables) {
            if ("*".equals(each.getSchema())) {
                result.putAll(parseTableExpressionWithAllSchema(database, systemSchemas, each));
            } else if ("*".equals(each.getTable())) {
                result.putAll(parseTableExpressionWithAllTable(database, each));
            } else {
                String schemaName = each.getSchema();
                if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent() && schemaName.isEmpty()) {
                    schemaName = dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().get();
                }
                ShardingSpherePreconditions.checkNotNull(database.getSchema(schemaName).getTable(each.getTable()), () -> new TableNotFoundException(each.getTable()));
                result.computeIfAbsent(schemaName, ignored -> new HashSet<>()).add(each.getTable());
            }
        }
        return result;
    }
    
    private static Map<String, Set<String>> parseTableExpressionWithAllTables(final ShardingSphereDatabase database, final Collection<String> systemSchemas) {
        Map<String, Set<String>> result = new HashMap<>(database.getAllSchemas().size(), 1F);
        for (ShardingSphereSchema schema : database.getAllSchemas()) {
            if (!systemSchemas.contains(schema.getName())) {
                schema.getAllTables().forEach(each -> result.computeIfAbsent(schema.getName(), ignored -> new HashSet<>()).add(each.getName()));
            }
        }
        return result;
    }
    
    private static Map<String, Set<String>> parseTableExpressionWithAllSchema(final ShardingSphereDatabase database, final Collection<String> systemSchemas, final SchemaTable table) {
        Map<String, Set<String>> result = new HashMap<>(database.getAllSchemas().size(), 1F);
        for (ShardingSphereSchema schema : database.getAllSchemas()) {
            if (!systemSchemas.contains(schema.getName())) {
                schema.getAllTables().stream().filter(each -> each.getName().equals(table.getTable())).findFirst()
                        .ifPresent(optional -> result.computeIfAbsent(schema.getName(), ignored -> new HashSet<>()).add(optional.getName()));
            }
        }
        return result;
    }
    
    private static Map<String, Set<String>> parseTableExpressionWithAllTable(final ShardingSphereDatabase database, final SchemaTable schemaTable) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(database.getProtocolType()).getDialectDatabaseMetaData();
        String schemaName = schemaTable.getSchema().isEmpty()
                ? dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().orElseThrow(() -> new IllegalStateException("Default schema should exist."))
                : schemaTable.getSchema();
        ShardingSphereSchema schema = database.getSchema(schemaName);
        ShardingSpherePreconditions.checkNotNull(schema, () -> new SchemaNotFoundException(schemaTable.getSchema()));
        Collection<ShardingSphereTable> tables = schema.getAllTables();
        Map<String, Set<String>> result = new HashMap<>(tables.size(), 1F);
        tables.forEach(each -> result.computeIfAbsent(schemaName, ignored -> new HashSet<>()).add(each.getName()));
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
        Set<String> allTableNames = null == schema ? Collections.emptySet() : new HashSet<>(schema.getAllTables().stream().map(ShardingSphereTable::getName).collect(Collectors.toSet()));
        return tableNames.stream().anyMatch("*"::equals) ? allTableNames : new HashSet<>(tableNames);
    }
}

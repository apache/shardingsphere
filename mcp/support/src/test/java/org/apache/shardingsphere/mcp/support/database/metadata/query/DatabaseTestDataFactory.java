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

package org.apache.shardingsphere.mcp.support.database.metadata.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.fixture.SupportDatabaseTypeFactoryMocker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DatabaseTestDataFactory {
    
    private static List<DatabaseFixture> createDatabaseMetadata() {
        return List.of(
                new DatabaseFixture("logic_db", "MySQL", "", List.of(
                        new SchemaFixture("public", List.of(
                                new TableFixture("orders", List.of("order_id", "amount"), List.of("order_idx")),
                                new TableFixture("order_items", List.of("item_id"), List.of())),
                                List.of(new TableFixture("orders_view", List.of("order_id"), List.of())), List.of()))),
                new DatabaseFixture("runtime_db", "PostgreSQL", "", List.of(
                        new SchemaFixture("public", List.of(), List.of(), List.of("order_seq")))),
                new DatabaseFixture("warehouse", "Hive", "", List.of(
                        new SchemaFixture("warehouse", List.of(new TableFixture("facts", List.of(), List.of())), List.of(), List.of()))));
    }
    
    static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(3, 1F);
        for (DatabaseFixture each : createDatabaseMetadata()) {
            result.put(each.database, createRuntimeDatabaseConfiguration(each));
        }
        return result;
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final DatabaseFixture databaseMetadata) {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        try {
            when(result.openConnection(databaseMetadata.database)).thenAnswer(invocation -> createConnection(databaseMetadata));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }
    
    private static Connection createConnection(final DatabaseFixture databaseMetadata) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseMetadata.databaseVersion);
        when(databaseMetaData.getURL()).thenReturn(SupportDatabaseTypeFactoryMocker.createJdbcUrl(databaseMetadata.databaseType));
        when(databaseMetaData.getSearchStringEscape()).thenReturn("\\");
        when(databaseMetaData.getTables(nullable(String.class), nullable(String.class), eq("%"), any(String[].class))).thenAnswer(invocation -> {
            String[] tableTypes = invocation.getArgument(3, String[].class);
            return createResultSet("TABLE".equals(tableTypes[0]) ? createTableRows(databaseMetadata) : createViewRows(databaseMetadata));
        });
        when(databaseMetaData.getColumns(nullable(String.class), nullable(String.class), anyString(), eq("%")))
                .thenAnswer(invocation -> createResultSet(createColumnRows(databaseMetadata, invocation.getArgument(2, String.class))));
        when(databaseMetaData.getIndexInfo(nullable(String.class), nullable(String.class), anyString(), eq(false), eq(false)))
                .thenAnswer(invocation -> createResultSet(createIndexRows(databaseMetadata, invocation.getArgument(2, String.class))));
        ResultSet sequenceResultSet = createResultSet(createSequenceRows(databaseMetadata));
        when(statement.executeQuery(anyString())).thenReturn(sequenceResultSet);
        return result;
    }
    
    private static List<Map<String, Object>> createTableRows(final DatabaseFixture databaseMetadata) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas) {
            for (TableFixture table : each.tables) {
                result.add(Map.of("TABLE_SCHEM", each.schema, "TABLE_CAT", "", "TABLE_NAME", table.name));
            }
        }
        return result;
    }
    
    private static List<Map<String, Object>> createViewRows(final DatabaseFixture databaseMetadata) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas) {
            for (TableFixture view : each.views) {
                result.add(Map.of("TABLE_SCHEM", each.schema, "TABLE_CAT", "", "TABLE_NAME", view.name));
            }
        }
        return result;
    }
    
    private static List<Map<String, Object>> createColumnRows(final DatabaseFixture databaseMetadata, final String objectName) {
        List<Map<String, Object>> result = new LinkedList<>();
        String relationName = "%".equals(objectName) ? "" : unescapePattern(objectName);
        for (SchemaFixture each : databaseMetadata.schemas) {
            for (TableFixture table : each.tables) {
                if (relationName.isEmpty() || table.name.equals(relationName)) {
                    for (int i = 0; i < table.columns.size(); i++) {
                        result.add(createColumnRow(table.name, table.columns.get(i), i + 1));
                    }
                }
            }
            for (TableFixture view : each.views) {
                if (relationName.isEmpty() || view.name.equals(relationName)) {
                    for (int i = 0; i < view.columns.size(); i++) {
                        result.add(createColumnRow(view.name, view.columns.get(i), i + 1));
                    }
                }
            }
        }
        return result;
    }
    
    private static String unescapePattern(final String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean escaped = false;
        for (char each : value.toCharArray()) {
            if (escaped) {
                result.append(each);
                escaped = false;
            } else if ('\\' == each) {
                escaped = true;
            } else {
                result.append(each);
            }
        }
        return escaped ? result.append('\\').toString() : result.toString();
    }
    
    private static Map<String, Object> createColumnRow(final String relationName, final String columnName, final int ordinalPosition) {
        return Map.of("TABLE_NAME", relationName, "COLUMN_NAME", columnName, "ORDINAL_POSITION", ordinalPosition,
                "DATA_TYPE", Types.INTEGER, "TYPE_NAME", "INT", "NULLABLE", DatabaseMetaData.columnNullable);
    }
    
    private static List<Map<String, Object>> createIndexRows(final DatabaseFixture databaseMetadata, final String tableName) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas) {
            for (TableFixture table : each.tables) {
                if (table.name.equals(tableName)) {
                    for (String index : table.indexes) {
                        result.add(Map.of("INDEX_NAME", index, "TYPE", DatabaseMetaData.tableIndexOther, "NON_UNIQUE", false,
                                "ORDINAL_POSITION", 1, "COLUMN_NAME", table.columns.isEmpty() ? "" : table.columns.getFirst()));
                    }
                }
            }
        }
        return result;
    }
    
    private static List<Map<String, Object>> createSequenceRows(final DatabaseFixture databaseMetadata) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (SchemaFixture each : databaseMetadata.schemas) {
            for (String sequence : each.sequences) {
                result.add(Map.of("SEQUENCE_SCHEMA", each.schema, "SEQUENCE_NAME", sequence));
            }
        }
        return result;
    }
    
    private static ResultSet createResultSet(final List<Map<String, Object>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getString(anyString())).thenAnswer(invocation -> {
            Object value = rows.get(rowIndex.get()).get(invocation.getArgument(0, String.class));
            return null == value ? null : value.toString();
        });
        when(result.getInt(anyString())).thenAnswer(invocation -> getNumber(rows, rowIndex.get(), invocation.getArgument(0, String.class)).intValue());
        when(result.getShort(anyString())).thenAnswer(invocation -> getNumber(rows, rowIndex.get(), invocation.getArgument(0, String.class)).shortValue());
        when(result.getBoolean(anyString())).thenAnswer(invocation -> Boolean.TRUE.equals(rows.get(rowIndex.get()).get(invocation.getArgument(0, String.class))));
        return result;
    }
    
    private static Number getNumber(final List<Map<String, Object>> rows, final int rowIndex, final String columnLabel) {
        Object value = rows.get(rowIndex).get(columnLabel);
        return value instanceof Number ? (Number) value : 0;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class DatabaseFixture {
        
        private final String database;
        
        private final String databaseType;
        
        private final String databaseVersion;
        
        private final List<SchemaFixture> schemas;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class SchemaFixture {
        
        private final String schema;
        
        private final List<TableFixture> tables;
        
        private final List<TableFixture> views;
        
        private final List<String> sequences;
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TableFixture {
        
        private final String name;
        
        private final List<String> columns;
        
        private final List<String> indexes;
    }
}

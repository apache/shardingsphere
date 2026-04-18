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

package org.apache.shardingsphere.mcp.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceTestDataFactory {

    /**
     * Create default database metadata.
     *
     * @return default database metadata
     */
    public static List<MCPDatabaseMetadata> createDatabaseMetadata() {
        return List.of(
                new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                        new MCPSchemaMetadata("logic_db", "public", List.of(
                                new MCPTableMetadata("logic_db", "public", "orders",
                                        List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "order_id")),
                                        List.of(new MCPIndexMetadata("logic_db", "public", "orders", "order_idx"))),
                                new MCPTableMetadata("logic_db", "public", "order_items",
                                        List.of(new MCPColumnMetadata("logic_db", "public", "order_items", "", "item_id")), List.of())),
                                List.of(new MCPViewMetadata("logic_db", "public", "orders_view",
                                        List.of(new MCPColumnMetadata("logic_db", "public", "", "orders_view", "order_id"))))))),
                new MCPDatabaseMetadata("runtime_db", "H2", "", List.of(
                        new MCPSchemaMetadata("runtime_db", "public", List.of(), List.of(), List.of(new MCPSequenceMetadata("runtime_db", "public", "order_seq"))))),
                new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                        new MCPSchemaMetadata("warehouse", "warehouse", List.of(new MCPTableMetadata("warehouse", "warehouse", "facts", List.of(), List.of())), List.of()))));
    }

    /**
     * Create runtime databases from metadata.
     *
     * @param databaseMetadataList database metadata list
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final List<MCPDatabaseMetadata> databaseMetadataList) {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(databaseMetadataList.size(), 1F);
        for (MCPDatabaseMetadata each : databaseMetadataList) {
            result.put(each.getDatabase(), createRuntimeDatabaseConfiguration(each));
        }
        return result;
    }

    /**
     * Create default runtime databases.
     *
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        return createRuntimeDatabases(createDatabaseMetadata());
    }

    /**
     * Create runtime context from metadata.
     *
     * @param databaseMetadataList database metadata list
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext(final List<MCPDatabaseMetadata> databaseMetadataList) {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = createRuntimeDatabases(databaseMetadataList);
        return new MCPRuntimeContext(new MCPSessionManager(runtimeDatabases), new MCPDatabaseCapabilityProvider(runtimeDatabases));
    }

    /**
     * Create default runtime context.
     *
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext() {
        return createRuntimeContext(createDatabaseMetadata());
    }

    /**
     * Create request context from metadata.
     *
     * @param databaseMetadataList database metadata list
     * @return request context
     */
    public static MCPRequestContext createRequestContext(final List<MCPDatabaseMetadata> databaseMetadataList) {
        return new MCPRequestContext(createRuntimeContext(databaseMetadataList));
    }

    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final MCPDatabaseMetadata databaseMetadata) {
        RuntimeDatabaseConfiguration result = mock(RuntimeDatabaseConfiguration.class);
        try {
            when(result.getDatabaseType()).thenReturn(databaseMetadata.getDatabaseType());
            when(result.openConnection(databaseMetadata.getDatabase())).thenAnswer(invocation -> createConnection(databaseMetadata));
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }

    private static Connection createConnection(final MCPDatabaseMetadata databaseMetadata) throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        when(result.getMetaData()).thenReturn(databaseMetaData);
        when(result.createStatement()).thenReturn(statement);
        when(databaseMetaData.getDatabaseProductName()).thenReturn(databaseMetadata.getDatabaseType());
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn(databaseMetadata.getDatabaseVersion());
        when(databaseMetaData.getURL()).thenReturn(String.format("jdbc:mock:%s", databaseMetadata.getDatabase().toLowerCase(Locale.ENGLISH)));
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

    private static List<Map<String, String>> createTableRows(final MCPDatabaseMetadata databaseMetadata) {
        List<Map<String, String>> result = new LinkedList<>();
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            for (MCPTableMetadata table : each.getTables()) {
                result.add(Map.of("TABLE_SCHEM", each.getSchema(), "TABLE_CAT", "", "TABLE_NAME", table.getTable()));
            }
        }
        return result;
    }

    private static List<Map<String, String>> createViewRows(final MCPDatabaseMetadata databaseMetadata) {
        List<Map<String, String>> result = new LinkedList<>();
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            for (MCPViewMetadata view : each.getViews()) {
                result.add(Map.of("TABLE_SCHEM", each.getSchema(), "TABLE_CAT", "", "TABLE_NAME", view.getView()));
            }
        }
        return result;
    }

    private static List<Map<String, String>> createColumnRows(final MCPDatabaseMetadata databaseMetadata, final String objectName) {
        List<Map<String, String>> result = new LinkedList<>();
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            for (MCPTableMetadata table : each.getTables()) {
                if (table.getTable().equals(objectName)) {
                    for (MCPColumnMetadata column : table.getColumns()) {
                        result.add(Map.of("COLUMN_NAME", column.getColumn()));
                    }
                }
            }
            for (MCPViewMetadata view : each.getViews()) {
                if (view.getView().equals(objectName)) {
                    for (MCPColumnMetadata column : view.getColumns()) {
                        result.add(Map.of("COLUMN_NAME", column.getColumn()));
                    }
                }
            }
        }
        return result;
    }

    private static List<Map<String, String>> createIndexRows(final MCPDatabaseMetadata databaseMetadata, final String tableName) {
        List<Map<String, String>> result = new LinkedList<>();
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            for (MCPTableMetadata table : each.getTables()) {
                if (table.getTable().equals(tableName)) {
                    for (MCPIndexMetadata index : table.getIndexes()) {
                        result.add(Map.of("INDEX_NAME", index.getIndex()));
                    }
                }
            }
        }
        return result;
    }

    private static List<Map<String, String>> createSequenceRows(final MCPDatabaseMetadata databaseMetadata) {
        List<Map<String, String>> result = new LinkedList<>();
        for (MCPSchemaMetadata each : databaseMetadata.getSchemas()) {
            for (MCPSequenceMetadata sequence : each.getSequences()) {
                result.add(Map.of("SEQUENCE_SCHEMA", each.getSchema(), "SEQUENCE_NAME", sequence.getSequence()));
            }
        }
        return result;
    }

    private static ResultSet createResultSet(final List<Map<String, String>> rows) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        AtomicInteger rowIndex = new AtomicInteger(-1);
        when(result.next()).thenAnswer(invocation -> rowIndex.incrementAndGet() < rows.size());
        when(result.getString(anyString())).thenAnswer(invocation -> rows.get(rowIndex.get()).get(invocation.getArgument(0, String.class)));
        return result;
    }
}

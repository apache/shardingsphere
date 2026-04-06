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

package org.apache.shardingsphere.mcp.tool;

import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.model.MetadataSearchHit;
import org.apache.shardingsphere.mcp.protocol.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataToolDispatcherTest {
    
    @Test
    void assertDispatchListDatabases() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        ToolDispatchResult actual = dispatcher.dispatch(new ToolRequest("list_databases", "", "", "", "", "", Set.of(), 10, ""));
        assertThat(actual.getMetadataItems().size(), is(3));
        assertThat(((MCPDatabaseMetadata) actual.getMetadataItems().get(0)).getDatabase(), is("analytics_db"));
        assertThat(((MCPDatabaseMetadata) actual.getMetadataItems().get(2)).getDatabase(), is("warehouse"));
    }
    
    @Test
    void assertDispatchListTablesWithPagination() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        ToolDispatchResult actual = dispatcher.dispatch(new ToolRequest("list_tables", "logic_db", "public", "", "", "order", Set.of(), 1, ""));
        assertThat(actual.getMetadataItems().size(), is(1));
        assertThat(((MCPTableMetadata) actual.getMetadataItems().get(0)).getTable(), is("order_items"));
        assertThat(actual.getNextPageToken(), is("1"));
    }
    
    @Test
    void assertDispatchSearchMetadataAcrossDatabases() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        Set<MetadataObjectType> objectTypes = new LinkedHashSet<>();
        objectTypes.add(MetadataObjectType.TABLE);
        objectTypes.add(MetadataObjectType.VIEW);
        objectTypes.add(MetadataObjectType.INDEX);
        objectTypes.add(MetadataObjectType.MATERIALIZED_VIEW);
        objectTypes.add(MetadataObjectType.SEQUENCE);
        ToolDispatchResult actual = dispatcher.dispatch(new ToolRequest("search_metadata", "", "", "", "", "order", objectTypes, 20, ""));
        Set<String> actualNames = new LinkedHashSet<>();
        for (Object each : actual.getMetadataItems()) {
            actualNames.add(((MetadataSearchHit) each).getName());
        }
        assertTrue(actualNames.contains("orders"));
        assertTrue(actualNames.contains("order_items"));
        assertTrue(actualNames.contains("active_orders"));
        assertTrue(actualNames.contains("idx_orders_status"));
        assertFalse(actualNames.contains("mv_orders"));
        assertFalse(actualNames.contains("order_seq"));
    }
    
    @Test
    void assertDispatchSearchDatabaseObjectsAcrossDatabases() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        ToolDispatchResult actual = dispatcher.dispatch(new ToolRequest("search_metadata", "", "", "", "", "", Set.of(MetadataObjectType.DATABASE), 10, ""));
        assertThat(actual.getMetadataItems().size(), is(3));
        assertThat(((MetadataSearchHit) actual.getMetadataItems().get(0)).getName(), is("analytics_db"));
        assertThat(((MetadataSearchHit) actual.getMetadataItems().get(2)).getName(), is("warehouse"));
    }
    
    @Test
    void assertDispatchSearchMetadataWithSchemaWithoutDatabase() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> dispatcher.dispatch(new ToolRequest("search_metadata", "", "public", "", "", "order", Set.of(), 10, "")));
        assertThat(actual.getMessage(), is("Schema cannot be provided without database."));
    }
    
    @Test
    void assertDispatchDescribeView() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        ToolDispatchResult actual = dispatcher.dispatch(new ToolRequest("describe_view", "logic_db", "public", "active_orders", "", "", Set.of(), 10, ""));
        assertThat(actual.getMetadataItems().size(), is(1));
        assertThat(((MCPViewMetadata) actual.getMetadataItems().get(0)).getView(), is("active_orders"));
        assertThat(((MCPViewMetadata) actual.getMetadataItems().get(0)).getColumns().size(), is(0));
    }
    
    @Test
    void assertDispatchListIndexesWithUnsupportedDatabaseType() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class,
                () -> dispatcher.dispatch(new ToolRequest("list_indexes", "warehouse", "warehouse", "facts", "", "", Set.of(), 10, "")));
        assertThat(actual.getMessage(), is("Index resources are not supported for the current database."));
    }
    
    @Test
    void assertDispatchWithInvalidPageToken() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        InvalidPageTokenException actual = assertThrows(InvalidPageTokenException.class,
                () -> dispatcher.dispatch(new ToolRequest("list_tables", "logic_db", "public", "", "", "", Set.of(), 10, "invalid")));
        assertThat(actual.getMessage(), is("Invalid page token."));
    }
    
    @Test
    void assertDispatchWithPageOffsetBeyondResultSize() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        ToolDispatchResult actual = dispatcher.dispatch(new ToolRequest("list_tables", "logic_db", "public", "", "", "", Set.of(), 10, "99"));
        assertThat(actual.getMetadataItems().size(), is(0));
        assertThat(actual.getNextPageToken(), is(""));
    }
    
    @Test
    void assertDispatchWithUnsupportedTool() {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> dispatcher.dispatch(new ToolRequest("unsupported_tool", "", "", "", "", "", Set.of(), 10, "")));
        assertThat(actual.getMessage(), is("Unsupported metadata tool."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertDispatchWithMissingRequiredFieldCases")
    void assertDispatchWithMissingRequiredField(final String name, final ToolRequest toolRequest, final String expectedMessage) {
        MetadataToolDispatcher dispatcher = new MetadataToolDispatcher(createDatabaseMetadataSnapshots());
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> dispatcher.dispatch(toolRequest));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private DatabaseMetadataSnapshots createDatabaseMetadataSnapshots() {
        Map<String, MCPDatabaseMetadata> databaseSnapshots = new LinkedHashMap<>();
        databaseSnapshots.put("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                new MCPSchemaMetadata("logic_db", "public", List.of(
                        new MCPTableMetadata("logic_db", "public", "orders",
                                List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "order_id"),
                                        new MCPColumnMetadata("logic_db", "public", "orders", "", "status")),
                                List.of(new MCPIndexMetadata("logic_db", "public", "orders", "idx_orders_status"))),
                        new MCPTableMetadata("logic_db", "public", "order_items", List.of(), List.of())),
                        List.of(new MCPViewMetadata("logic_db", "public", "active_orders", List.of()))))));
        databaseSnapshots.put("analytics_db", new MCPDatabaseMetadata("analytics_db", "PostgreSQL", "", List.of(
                new MCPSchemaMetadata("analytics_db", "public", List.of(
                        new MCPTableMetadata("analytics_db", "public", "metrics",
                                List.of(new MCPColumnMetadata("analytics_db", "public", "metrics", "", "metric_id")), List.of())),
                        List.of()))));
        databaseSnapshots.put("warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(
                        new MCPTableMetadata("warehouse", "warehouse", "facts",
                                List.of(new MCPColumnMetadata("warehouse", "warehouse", "facts", "", "fact_id")), List.of())),
                        List.of()))));
        return new DatabaseMetadataSnapshots(databaseSnapshots);
    }
    
    static Stream<Arguments> assertDispatchWithMissingRequiredFieldCases() {
        return Stream.of(
                Arguments.of("list schemas without database", new ToolRequest("list_schemas", "", "", "", "", "", Set.of(), 10, ""), "Database is required."),
                Arguments.of("list columns without object name", new ToolRequest("list_columns", "logic_db", "public", "", "TABLE", "", Set.of(), 10, ""),
                        "Parent object type and object name are required."),
                Arguments.of("list indexes without table", new ToolRequest("list_indexes", "logic_db", "public", "", "", "", Set.of(), 10, ""), "Table name is required."),
                Arguments.of("describe table without object name", new ToolRequest("describe_table", "logic_db", "public", "", "", "", Set.of(), 10, ""),
                        "Database, schema, and object name are required."));
    }
}

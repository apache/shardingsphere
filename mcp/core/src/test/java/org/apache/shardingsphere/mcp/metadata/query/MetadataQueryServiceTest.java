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

package org.apache.shardingsphere.mcp.metadata.query;

import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataQueryServiceTest {
    
    private final MCPRequestContext requestContext = ResourceTestDataFactory.createRequestContext(ResourceTestDataFactory.createDatabaseMetadata());
    
    private final MetadataQueryService metadataQueryService = new MetadataQueryService(requestContext.getDatabaseCapabilityProvider(), requestContext.getMetadataContext());
    
    @AfterEach
    void closeRequestContext() {
        requestContext.close();
    }
    
    @Test
    void assertQueryDatabases() {
        List<MCPDatabaseMetadata> actual = metadataQueryService.queryDatabases();
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0).getDatabase(), is("logic_db"));
        assertTrue(actual.get(0).getSchemas().isEmpty());
        assertThat(actual.get(1).getDatabase(), is("runtime_db"));
        assertThat(actual.get(2).getDatabase(), is("warehouse"));
    }
    
    @Test
    void assertQueryDatabase() {
        Optional<MCPDatabaseMetadata> actual = metadataQueryService.queryDatabase("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabase(), is("logic_db"));
        assertThat(actual.get().getSchemas().get(0).getSchema(), is("public"));
    }
    
    @Test
    void assertQuerySchemas() {
        List<MCPSchemaMetadata> actual = metadataQueryService.querySchemas("logic_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSchema(), is("public"));
        assertTrue(actual.get(0).getTables().isEmpty());
    }
    
    @Test
    void assertQuerySchemasWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.querySchemas("unknown_db").isEmpty());
    }
    
    @Test
    void assertQuerySchema() {
        Optional<MCPSchemaMetadata> actual = metadataQueryService.querySchema("logic_db", "public");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchema(), is("public"));
        assertThat(actual.get().getTables().size(), is(2));
        assertThat(actual.get().getViews().size(), is(1));
    }
    
    @Test
    void assertQuerySchemaWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.querySchema("unknown_db", "public").isPresent());
    }
    
    @Test
    void assertQueryTablesBySchema() {
        List<MCPTableMetadata> actual = metadataQueryService.queryTables("logic_db", "public");
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getTable(), is("order_items"));
        assertThat(actual.get(1).getTable(), is("orders"));
        assertTrue(actual.get(1).getColumns().isEmpty());
    }
    
    @Test
    void assertQueryTablesWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryTables("unknown_db", "public").isEmpty());
    }
    
    @Test
    void assertQueryTable() {
        Optional<MCPTableMetadata> actual = metadataQueryService.queryTable("logic_db", "public", "orders");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTable(), is("orders"));
        assertThat(actual.get().getColumns().size(), is(1));
        assertThat(actual.get().getIndexes().size(), is(1));
    }
    
    @Test
    void assertQueryTableWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryTable("unknown_db", "public", "orders").isPresent());
    }
    
    @Test
    void assertQueryTableColumns() {
        List<MCPColumnMetadata> actual = metadataQueryService.queryTableColumns("logic_db", "public", "orders");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getColumn(), is("order_id"));
    }
    
    @Test
    void assertQueryTableColumnsWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryTableColumns("unknown_db", "public", "orders").isEmpty());
    }
    
    @Test
    void assertQueryTableColumn() {
        Optional<MCPColumnMetadata> actual = metadataQueryService.queryTableColumn("logic_db", "public", "orders", "order_id");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getColumn(), is("order_id"));
        assertThat(actual.get().getTable(), is("orders"));
    }
    
    @Test
    void assertQueryTableColumnWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryTableColumn("unknown_db", "public", "orders", "order_id").isPresent());
    }
    
    @Test
    void assertQueryViews() {
        List<MCPViewMetadata> actual = metadataQueryService.queryViews("logic_db", "public");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getView(), is("orders_view"));
        assertTrue(actual.get(0).getColumns().isEmpty());
    }
    
    @Test
    void assertQueryViewsWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryViews("unknown_db", "public").isEmpty());
    }
    
    @Test
    void assertQueryView() {
        Optional<MCPViewMetadata> actual = metadataQueryService.queryView("logic_db", "public", "orders_view");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getView(), is("orders_view"));
        assertThat(actual.get().getColumns().size(), is(1));
    }
    
    @Test
    void assertQueryViewWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryView("unknown_db", "public", "orders_view").isPresent());
    }
    
    @Test
    void assertQueryViewColumns() {
        List<MCPColumnMetadata> actual = metadataQueryService.queryViewColumns("logic_db", "public", "orders_view");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getColumn(), is("order_id"));
        assertThat(actual.get(0).getView(), is("orders_view"));
    }
    
    @Test
    void assertQueryViewColumnsWithUnsupportedDatabase() {
        assertTrue(metadataQueryService.queryViewColumns("unknown_db", "public", "orders_view").isEmpty());
    }
    
    @Test
    void assertQueryViewColumn() {
        Optional<MCPColumnMetadata> actual = metadataQueryService.queryViewColumn("logic_db", "public", "orders_view", "order_id");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getColumn(), is("order_id"));
        assertThat(actual.get().getView(), is("orders_view"));
    }
    
    @Test
    void assertQueryViewColumnWithUnsupportedDatabase() {
        assertFalse(metadataQueryService.queryViewColumn("unknown_db", "public", "orders_view", "order_id").isPresent());
    }
    
    @Test
    void assertQueryIndexes() {
        List<MCPIndexMetadata> actual = metadataQueryService.queryIndexes("logic_db", "public", "orders");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getIndex(), is("order_idx"));
    }
    
    @Test
    void assertQueryIndexesWithUnsupportedIndexType() {
        assertThat(assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.queryIndexes("warehouse", "warehouse", "facts")).getMessage(),
                is("Index resources are not supported for the current database."));
    }
    
    @Test
    void assertQueryIndex() {
        Optional<MCPIndexMetadata> actual = metadataQueryService.queryIndex("logic_db", "public", "orders", "order_idx");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getIndex(), is("order_idx"));
    }
    
    @Test
    void assertQueryIndexWithUnsupportedIndexType() {
        assertThat(assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.queryIndex("warehouse", "warehouse", "facts", "facts_idx")).getMessage(),
                is("Index resources are not supported for the current database."));
    }
    
    @Test
    void assertQuerySequences() {
        List<MCPSequenceMetadata> actual = metadataQueryService.querySequences("runtime_db", "public");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSequence(), is("order_seq"));
    }
    
    @Test
    void assertQuerySequencesWithUnsupportedSequenceType() {
        assertThat(assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.querySequences("logic_db", "public")).getMessage(),
                is("Sequence resources are not supported for the current database."));
    }
    
    @Test
    void assertQuerySequence() {
        Optional<MCPSequenceMetadata> actual = metadataQueryService.querySequence("runtime_db", "public", "order_seq");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSequence(), is("order_seq"));
    }
    
    @Test
    void assertQuerySequenceWithUnsupportedSequenceType() {
        assertThat(assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.querySequence("logic_db", "public", "order_seq")).getMessage(),
                is("Sequence resources are not supported for the current database."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedMetadataObjectTypeArguments")
    void assertIsSupportedMetadataObjectType(final String name, final String databaseName, final SupportedMCPMetadataObjectType objectType, final boolean expected) {
        assertThat(metadataQueryService.isSupportedMetadataObjectType(databaseName, objectType), is(expected));
    }
    
    private static Stream<Arguments> supportedMetadataObjectTypeArguments() {
        return Stream.of(
                Arguments.of("supported table", "logic_db", SupportedMCPMetadataObjectType.TABLE, true),
                Arguments.of("unsupported sequence", "logic_db", SupportedMCPMetadataObjectType.SEQUENCE, false),
                Arguments.of("missing database", "unknown_db", SupportedMCPMetadataObjectType.TABLE, false));
    }
}

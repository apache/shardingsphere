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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.DatabaseMetadataFixture;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.RequestScopeFixture;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SearchMetadataToolServiceTest {
    
    @Test
    void assertExecuteSearchAcrossDatabases() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("", "", "order",
                Set.of(SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.INDEX)));
        Set<String> actualNames = actual.getItems().stream().map(MetadataSearchHit::getName).collect(Collectors.toSet());
        assertTrue(actualNames.contains("orders"));
        assertTrue(actualNames.contains("order_items"));
        assertTrue(actualNames.contains("active_orders"));
        assertTrue(actualNames.contains("idx_orders_status"));
        assertThat(extractResourceHintUri(findHit(actual, "idx_orders_status")), is("shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/idx_orders_status"));
        assertFalse(actualNames.contains("mv_orders"));
        assertFalse(actualNames.contains("order_seq"));
    }
    
    @Test
    void assertExecuteSearchAcrossDatabasesWithDatabaseObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("", "", "", Set.of(SupportedMCPMetadataObjectType.DATABASE)));
        assertThat(actual.getItems().size(), is(4));
        assertThat(actual.getItems().getFirst().getName(), is("analytics_db"));
        assertThat(actual.getItems().get(1).getName(), is("logic_db"));
        assertThat(extractResourceHintUri(actual.getItems().get(1)), is("shardingsphere://databases/logic_db"));
        assertThat(extractParentResourceHintUri(actual.getItems().get(1)), is("shardingsphere://databases"));
        assertThat(extractNextResourceHintUris(actual.getItems().get(1)), is(List.of("shardingsphere://databases/logic_db/capabilities", "shardingsphere://databases/logic_db/schemas")));
        assertThat(actual.getItems().get(1).getDerivationStatus(), is("derived"));
        assertThat(actual.getItems().get(2).getName(), is("runtime_db"));
        assertThat(actual.getItems().get(3).getName(), is("warehouse"));
    }
    
    @Test
    void assertExecuteBlankAllDatabaseSearchGuardsObjectExpansion() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("", "", "", Set.of()));
        assertThat(actual.getItems().size(), is(4));
        assertThat(actual.getTotalMatchCount(), is(4));
        assertThat(actual.getReturnedCount(), is(4));
        assertFalse(actual.isTruncated());
        assertThat(actual.getSearchContext().get("object_types"), is(List.of("database")));
        assertTrue((Boolean) actual.getSearchContext().get("broad_search_guarded"));
        assertThat(actual.getSearchContext().get("recommended_narrowing_arguments"), is(List.of("database", "query", "object_types")));
        assertFalse(actual.getItems().stream().map(MetadataSearchHit::getName).collect(Collectors.toSet()).contains("orders"));
    }
    
    @Test
    void assertExecuteSearchCapsLargeResult() {
        MetadataSearchResult actual = execute(createLargeDatabaseMetadata(),
                new MetadataSearchRequest("large_db", "", "", Set.of(SupportedMCPMetadataObjectType.TABLE)));
        assertThat(actual.getItems().size(), is(100));
        assertThat(actual.getTotalMatchCount(), is(101));
        assertThat(actual.getReturnedCount(), is(100));
        assertTrue(actual.isTruncated());
        assertThat(actual.getLargeResultThreshold(), is(100));
    }
    
    @Test
    void assertExecuteSearchWithCompleteResult() {
        Set<SupportedMCPMetadataObjectType> objectTypes = Set.of(SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW);
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "", "order", objectTypes));
        assertThat(actual.getItems().size(), is(4));
        assertThat(actual.getItems().getFirst().getName(), is("order_items"));
        assertThat(actual.getItems().getFirst().getMatchKind(), is("prefix"));
        assertThat(actual.getItems().getFirst().getMatchedFields(), is(List.of("name", "table")));
        assertThat(actual.getItems().getFirst().getMatchedValue(), is("order_items"));
        assertThat(actual.getItems().get(1).getName(), is("orders"));
        assertThat(actual.getItems().get(2).getName(), is("active_orders"));
        assertThat(actual.getItems().get(3).getName(), is("archived_orders"));
        Map<?, ?> actualSearchContext = actual.getSearchContext();
        assertThat(actualSearchContext.get("query"), is("order"));
        assertThat(actualSearchContext.get("database"), is("logic_db"));
        assertThat(actualSearchContext.get("database_scope"), is("single_database"));
        assertThat(actualSearchContext.get("schema"), is(""));
        assertThat(actualSearchContext.get("object_types"), is(List.of("table", "view")));
        assertThat(actualSearchContext.keySet(), is(Set.of("query", "database", "database_scope", "schema", "object_types")));
        assertThat(actual.getTotalMatchCount(), is(4));
    }
    
    @Test
    void assertExecuteSearchWithDefaultObjectTypes() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("runtime_db", "", "", Set.of()));
        assertThat(actual.getItems().size(), is(3));
        assertThat(actual.getItems().getFirst().getObjectType(), is("database"));
        assertThat(actual.getItems().get(1).getObjectType(), is("schema"));
        assertThat(actual.getItems().get(2).getObjectType(), is("sequence"));
        assertThat(actual.getItems().get(2).getName(), is("order_seq"));
        assertThat(extractResourceHintUri(actual.getItems().get(2)), is("shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq"));
        assertThat(extractParentResourceHintUri(actual.getItems().get(2)), is("shardingsphere://databases/runtime_db/schemas/public/sequences"));
        assertThat(actual.getItems().get(2).getMatchKind(), is("all"));
        assertThat(actual.getSearchContext().keySet(), is(Set.of("query", "database", "database_scope", "schema", "object_types")));
    }
    
    @Test
    void assertExecuteSearchWithDefaultObjectTypesDoesNotQueryStorageUnits() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MetadataSearchResult actual = execute(createDatabaseMetadata(), queryFacade, new MetadataSearchRequest("logic_db", "", "", Set.of()));
        assertFalse(((List<?>) actual.getSearchContext().get("object_types")).contains("storage_unit"));
        verifyNoInteractions(queryFacade);
    }
    
    @Test
    void assertExecuteSearchWithSchemaWithoutDatabase() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> execute(createDatabaseMetadata(), new MetadataSearchRequest("", "public", "order", Set.of())));
        assertThat(actual.getMessage(), is("Schema cannot be provided without database."));
    }
    
    @Test
    void assertExecuteSearchWithSchemaObjectTypeAndSchemaFilter() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "public", "public", Set.of(SupportedMCPMetadataObjectType.SCHEMA)));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().getFirst().getObjectType(), is("schema"));
        assertThat(actual.getItems().getFirst().getName(), is("public"));
        assertThat(extractResourceHintUri(actual.getItems().getFirst()), is("shardingsphere://databases/logic_db/schemas/public"));
    }
    
    @Test
    void assertExecuteSearchWithTableObjectTypeAndSchemaFilter() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "public", "order", Set.of(SupportedMCPMetadataObjectType.TABLE)));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().getFirst().getName(), is("order_items"));
        assertThat(actual.getItems().get(1).getName(), is("orders"));
        assertThat(extractResourceHintUri(actual.getItems().get(1)), is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
        assertThat(extractNextResourceHintUris(actual.getItems().get(1)), is(List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns",
                "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes")));
    }
    
    @Test
    void assertExecuteSearchWithViewObjectTypeAndSchemaFilter() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "public", "orders", Set.of(SupportedMCPMetadataObjectType.VIEW)));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().getFirst().getName(), is("active_orders"));
        assertThat(extractResourceHintUri(actual.getItems().getFirst()), is("shardingsphere://databases/logic_db/schemas/public/views/active_orders"));
        assertThat(extractNextResourceHintUris(actual.getItems().getFirst()), is(List.of("shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns")));
        assertThat(actual.getItems().get(1).getName(), is("archived_orders"));
    }
    
    @Test
    void assertExecuteSearchWithColumnObjectTypeMatchedByViewName() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "", "active", Set.of(SupportedMCPMetadataObjectType.COLUMN)));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().getFirst().getName(), is("order_id"));
        assertThat(actual.getItems().getFirst().getView(), is("active_orders"));
        assertThat(extractResourceHintUri(actual.getItems().getFirst()), is("shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns/order_id"));
        assertThat(actual.getItems().get(1).getName(), is("order_status"));
        assertThat(actual.getItems().get(1).getView(), is("active_orders"));
    }
    
    @Test
    void assertExecuteSearchWithDatabaseAndTableInEmptySchema() {
        MetadataSearchResult actual = execute(createDatabaseMetadataWithEmptySchema(),
                new MetadataSearchRequest("schema_less_db", "", "", Set.of()));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().getFirst().getObjectType(), is("database"));
        assertThat(actual.getItems().get(1).getObjectType(), is("table"));
        assertThat(actual.getItems().get(1).getName(), is("schema_less_orders"));
        assertThat(extractResourceHintUri(actual.getItems().get(1)), is(""));
        assertThat(actual.getItems().get(1).getDerivationStatus(), is("not_safe_to_derive"));
        assertThat(actual.getItems().get(1).getDerivationReason(), is("Metadata hit does not include database, schema, and table names safe for resource URI derivation."));
    }
    
    @Test
    void assertExecuteSearchWithEncodedUriCharacter() {
        MetadataSearchResult actual = execute(createDatabaseMetadataWithUnsafeUriName(),
                new MetadataSearchRequest("逻辑 库", "public/main", "orders", Set.of(SupportedMCPMetadataObjectType.TABLE)));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().getFirst().getName(), is("orders?archive%2026"));
        assertThat(extractResourceHintUri(actual.getItems().getFirst()),
                is("shardingsphere://databases/%E9%80%BB%E8%BE%91%20%E5%BA%93/schemas/public%2Fmain/tables/orders%3Farchive%252026"));
        assertThat(actual.getItems().getFirst().getDerivationStatus(), is("derived"));
        assertThat(actual.getItems().getFirst().getMatchKind(), is("prefix"));
        assertThat(actual.getItems().getFirst().getMatchedFields(), is(List.of("name", "table")));
    }
    
    @Test
    void assertExecuteSearchWithEmptyQuery() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("logic_db", "", "", Set.of()));
        assertThat(actual.getItems().size(), is(11));
        assertThat(actual.getTotalMatchCount(), is(11));
        Set<String> actualNames = actual.getItems().stream().map(MetadataSearchHit::getName).collect(Collectors.toSet());
        assertTrue(actualNames.contains("logic_db"));
        assertTrue(actualNames.contains("active_orders"));
        assertTrue(actualNames.contains("idx_orders_status"));
    }
    
    @Test
    void assertExecuteSearchWithSequenceObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("runtime_db", "", "order",
                Set.of(SupportedMCPMetadataObjectType.SEQUENCE)));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().getFirst().getName(), is("order_seq"));
        assertThat(extractResourceHintUri(actual.getItems().getFirst()), is("shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq"));
    }
    
    @Test
    void assertExecuteSearchWithStorageUnitObjectType() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "", "SHOW STORAGE UNITS FROM logic_db"))
                .thenReturn(List.of(Map.of("name", "write_ds"), Map.of("name", "read_ds")));
        MetadataSearchResult actual = execute(createDatabaseMetadata(), queryFacade,
                new MetadataSearchRequest("logic_db", "", "write", Set.of(SupportedMCPMetadataObjectType.STORAGE_UNIT)));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().getFirst().getObjectType(), is("storage_unit"));
        assertThat(actual.getItems().getFirst().getName(), is("write_ds"));
        assertThat(extractResourceHintUri(actual.getItems().getFirst()), is("shardingsphere://databases/logic_db/storage-units/write_ds"));
        assertThat(extractParentResourceHintUri(actual.getItems().getFirst()), is("shardingsphere://databases/logic_db/storage-units"));
        assertThat(extractNextResourceHintUris(actual.getItems().getFirst()), is(List.of("shardingsphere://databases/logic_db/storage-units/write_ds/used-by-rules")));
    }
    
    @Test
    void assertExecuteSearchWithSchemaScopedSequenceObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("runtime_db", "public", "order", Set.of(SupportedMCPMetadataObjectType.SEQUENCE)));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().getFirst().getName(), is("order_seq"));
    }
    
    @Test
    void assertExecuteSearchWithUnsupportedIndexObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("warehouse", "", "", Set.of(SupportedMCPMetadataObjectType.INDEX)));
        assertThat(actual.getItems().size(), is(0));
    }
    
    @Test
    void assertExecuteSearchWithUnsupportedSequenceObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "", "", Set.of(SupportedMCPMetadataObjectType.SEQUENCE)));
        assertThat(actual.getItems().size(), is(0));
    }
    
    @Test
    void assertExecuteSearchWithUnsearchableObjectType() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MetadataSearchResult actual = new SearchMetadataToolService(metadataQueryFacade, mock(MCPFeatureQueryFacade.class)).execute(
                new MetadataSearchRequest("logic_db", "", "", Set.of(SupportedMCPMetadataObjectType.MATERIALIZED_VIEW)));
        assertThat(actual.getItems(), is(List.of()));
        verifyNoInteractions(metadataQueryFacade);
    }
    
    private MetadataSearchResult execute(final List<DatabaseMetadataFixture> databaseMetadata, final MetadataSearchRequest request) {
        try (RequestScopeFixture requestScopeFixture = ResourceTestDataFactory.createRequestScopeFixture(databaseMetadata)) {
            MCPRequestScope requestScope = requestScopeFixture.getRequestScope();
            return new SearchMetadataToolService(requestScope.getMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class)).execute(request);
        }
    }
    
    private MetadataSearchResult execute(final List<DatabaseMetadataFixture> databaseMetadata, final MCPFeatureQueryFacade queryFacade, final MetadataSearchRequest request) {
        try (RequestScopeFixture requestScopeFixture = ResourceTestDataFactory.createRequestScopeFixture(databaseMetadata)) {
            MCPRequestScope requestScope = requestScopeFixture.getRequestScope();
            return new SearchMetadataToolService(requestScope.getMetadataQueryFacade(), queryFacade).execute(request);
        }
    }
    
    private MetadataSearchHit findHit(final MetadataSearchResult searchResult, final String name) {
        return searchResult.getItems().stream().filter(each -> name.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private String extractResourceHintUri(final MetadataSearchHit hit) {
        return String.valueOf(hit.getResource().getOrDefault("uri", ""));
    }
    
    private String extractParentResourceHintUri(final MetadataSearchHit hit) {
        return String.valueOf(hit.getParentResource().getOrDefault("uri", ""));
    }
    
    private List<String> extractNextResourceHintUris(final MetadataSearchHit hit) {
        return hit.getNextResources().stream().map(each -> String.valueOf(each.get("uri"))).toList();
    }
    
    private List<DatabaseMetadataFixture> createDatabaseMetadata() {
        List<DatabaseMetadataFixture> result = new LinkedList<>();
        result.add(ResourceTestDataFactory.createDatabase("logic_db", "MySQL", "", List.of(
                ResourceTestDataFactory.createSchema("public", List.of(
                        ResourceTestDataFactory.createTable("orders", List.of("order_id", "status"), List.of("idx_orders_status")),
                        ResourceTestDataFactory.createTable("order_items", List.of(), List.of())),
                        List.of(
                                ResourceTestDataFactory.createTable("active_orders", List.of("order_id", "order_status"), List.of()),
                                ResourceTestDataFactory.createTable("archived_orders", List.of(), List.of())),
                        List.of()))));
        result.add(ResourceTestDataFactory.createDatabase("analytics_db", "PostgreSQL", "", List.of(
                ResourceTestDataFactory.createSchema("public", List.of(ResourceTestDataFactory.createTable("metrics", List.of("metric_id"), List.of())), List.of(), List.of()))));
        result.add(ResourceTestDataFactory.createDatabase("warehouse", "Hive", "", List.of(
                ResourceTestDataFactory.createSchema("warehouse", List.of(ResourceTestDataFactory.createTable("facts", List.of("fact_id"), List.of())), List.of(), List.of()))));
        result.add(ResourceTestDataFactory.createDatabase("runtime_db", "PostgreSQL", "", List.of(
                ResourceTestDataFactory.createSchema("public", List.of(), List.of(), List.of("order_seq")))));
        return result;
    }
    
    private List<DatabaseMetadataFixture> createDatabaseMetadataWithEmptySchema() {
        return List.of(ResourceTestDataFactory.createDatabase("schema_less_db", "PostgreSQL", "",
                List.of(ResourceTestDataFactory.createSchema("", List.of(ResourceTestDataFactory.createTable("schema_less_orders", List.of(), List.of())), List.of(), List.of()))));
    }
    
    private List<DatabaseMetadataFixture> createLargeDatabaseMetadata() {
        List<ResourceTestDataFactory.TableMetadataFixture> tables = new LinkedList<>();
        for (int index = 0; index < 101; index++) {
            tables.add(ResourceTestDataFactory.createTable("table_" + index, List.of(), List.of()));
        }
        return List.of(ResourceTestDataFactory.createDatabase("large_db", "MySQL", "", List.of(ResourceTestDataFactory.createSchema("public", tables, List.of(), List.of()))));
    }
    
    private List<DatabaseMetadataFixture> createDatabaseMetadataWithUnsafeUriName() {
        return List.of(ResourceTestDataFactory.createDatabase("逻辑 库", "MySQL", "",
                List.of(ResourceTestDataFactory.createSchema("public/main", List.of(ResourceTestDataFactory.createTable("orders?archive%2026", List.of(), List.of())), List.of(), List.of()))));
    }
}

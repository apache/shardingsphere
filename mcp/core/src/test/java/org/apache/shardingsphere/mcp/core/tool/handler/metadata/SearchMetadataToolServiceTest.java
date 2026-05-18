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
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPViewMetadata;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
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

class SearchMetadataToolServiceTest {

    @Test
    void assertExecuteSearchAcrossDatabases() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("", "", "order",
                Set.of(SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.INDEX), 20, ""));
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
                new MetadataSearchRequest("", "", "", Set.of(SupportedMCPMetadataObjectType.DATABASE), 10, ""));
        assertThat(actual.getItems().size(), is(4));
        assertThat(actual.getItems().get(0).getName(), is("analytics_db"));
        assertThat(actual.getItems().get(1).getName(), is("logic_db"));
        assertThat(extractResourceHintUri(actual.getItems().get(1)), is("shardingsphere://databases/logic_db"));
        assertThat(extractParentResourceHintUri(actual.getItems().get(1)), is("shardingsphere://databases"));
        assertThat(extractNextResourceHintUris(actual.getItems().get(1)), is(List.of("shardingsphere://databases/logic_db/capabilities", "shardingsphere://databases/logic_db/schemas")));
        assertThat(actual.getItems().get(1).getDerivationStatus(), is("derived"));
        assertThat(actual.getItems().get(2).getName(), is("runtime_db"));
        assertThat(actual.getItems().get(3).getName(), is("warehouse"));
        assertThat(actual.getNextPageToken(), is(""));
    }

    @Test
    void assertExecuteBlankAllDatabaseSearchGuardsObjectExpansion() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("", "", "", Set.of(), 10, ""));
        assertThat(actual.getItems().size(), is(4));
        assertThat(actual.getTotalMatchCount(), is(4));
        assertThat(actual.getSearchContext().get("object_types"), is(List.of("database")));
        assertTrue((Boolean) actual.getSearchContext().get("broad_search_guarded"));
        assertThat(actual.getSearchContext().get("recommended_narrowing_arguments"), is(List.of("database", "query", "object_types")));
        assertFalse(actual.getItems().stream().map(MetadataSearchHit::getName).collect(Collectors.toSet()).contains("orders"));
    }

    @Test
    void assertExecuteSearchWithPagination() {
        Set<SupportedMCPMetadataObjectType> objectTypes = new LinkedHashSet<>(Arrays.asList(SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW));
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "", "order", objectTypes, 1, ""));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().get(0).getName(), is("order_items"));
        assertThat(actual.getItems().get(0).getMatchKind(), is("prefix"));
        assertThat(actual.getItems().get(0).getMatchedFields(), is(List.of("name", "table")));
        assertThat(actual.getItems().get(0).getMatchedValue(), is("order_items"));
        Map<?, ?> actualSearchContext = actual.getSearchContext();
        assertThat(actualSearchContext.get("query"), is("order"));
        assertThat(actualSearchContext.get("database"), is("logic_db"));
        assertThat(actualSearchContext.get("database_scope"), is("single_database"));
        assertThat(actualSearchContext.get("schema"), is(""));
        assertThat(actualSearchContext.get("object_types"), is(List.of("table", "view")));
        assertThat(actualSearchContext.get("page_size"), is(1));
        assertFalse(actualSearchContext.containsKey("page_offset"));
        assertThat(actual.getTotalMatchCount(), is(4));
        assertFalse(actual.getNextPageToken().isEmpty());
        assertFalse(actual.getNextPageToken().matches("\\d+"));
        MetadataSearchResult actualNextPage = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "", "order", objectTypes, 1, actual.getNextPageToken()));
        assertThat(actualNextPage.getItems().get(0).getName(), is("orders"));
    }

    @Test
    void assertExecuteSearchWithDefaultPageSize() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("runtime_db", "", "", Set.of(), 0, ""));
        assertThat(actual.getItems().size(), is(3));
        assertThat(actual.getItems().get(0).getObjectType(), is("database"));
        assertThat(actual.getItems().get(1).getObjectType(), is("schema"));
        assertThat(actual.getItems().get(2).getObjectType(), is("sequence"));
        assertThat(actual.getItems().get(2).getName(), is("order_seq"));
        assertThat(extractResourceHintUri(actual.getItems().get(2)), is("shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq"));
        assertThat(extractParentResourceHintUri(actual.getItems().get(2)), is("shardingsphere://databases/runtime_db/schemas/public/sequences"));
        assertThat(actual.getItems().get(2).getMatchKind(), is("all"));
        assertThat(actual.getSearchContext().get("page_size"), is(50));
        assertThat(actual.getNextPageToken(), is(""));
    }

    @Test
    void assertExecuteSearchWithSchemaWithoutDatabase() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> execute(createDatabaseMetadata(), new MetadataSearchRequest("", "public", "order", Set.of(), 10, "")));
        assertThat(actual.getMessage(), is("Schema cannot be provided without database."));
    }

    @Test
    void assertExecuteSearchWithSchemaObjectTypeAndSchemaFilter() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "public", "public", Set.of(SupportedMCPMetadataObjectType.SCHEMA), 10, ""));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().get(0).getObjectType(), is("schema"));
        assertThat(actual.getItems().get(0).getName(), is("public"));
        assertThat(extractResourceHintUri(actual.getItems().get(0)), is("shardingsphere://databases/logic_db/schemas/public"));
    }

    @Test
    void assertExecuteSearchWithTableObjectTypeAndSchemaFilter() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "public", "order", Set.of(SupportedMCPMetadataObjectType.TABLE), 10, ""));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().get(0).getName(), is("order_items"));
        assertThat(actual.getItems().get(1).getName(), is("orders"));
        assertThat(extractResourceHintUri(actual.getItems().get(1)), is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
        assertThat(extractNextResourceHintUris(actual.getItems().get(1)), is(List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns",
                "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes")));
    }

    @Test
    void assertExecuteSearchWithViewObjectTypeAndSchemaFilter() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "public", "orders", Set.of(SupportedMCPMetadataObjectType.VIEW), 10, ""));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().get(0).getName(), is("active_orders"));
        assertThat(extractResourceHintUri(actual.getItems().get(0)), is("shardingsphere://databases/logic_db/schemas/public/views/active_orders"));
        assertThat(extractNextResourceHintUris(actual.getItems().get(0)), is(List.of("shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns")));
        assertThat(actual.getItems().get(1).getName(), is("archived_orders"));
    }

    @Test
    void assertExecuteSearchWithColumnObjectTypeMatchedByViewName() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "", "active", Set.of(SupportedMCPMetadataObjectType.COLUMN), 10, ""));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().get(0).getName(), is("order_id"));
        assertThat(actual.getItems().get(0).getView(), is("active_orders"));
        assertThat(extractResourceHintUri(actual.getItems().get(0)), is("shardingsphere://databases/logic_db/schemas/public/views/active_orders/columns/order_id"));
        assertThat(actual.getItems().get(1).getName(), is("order_status"));
        assertThat(actual.getItems().get(1).getView(), is("active_orders"));
    }

    @Test
    void assertExecuteSearchWithDatabaseAndTableInEmptySchema() {
        MetadataSearchResult actual = execute(createDatabaseMetadataWithEmptySchema(),
                new MetadataSearchRequest("schema_less_db", "", "", Set.of(), 10, ""));
        assertThat(actual.getItems().size(), is(2));
        assertThat(actual.getItems().get(0).getObjectType(), is("database"));
        assertThat(actual.getItems().get(1).getObjectType(), is("table"));
        assertThat(actual.getItems().get(1).getName(), is("schema_less_orders"));
        assertThat(extractResourceHintUri(actual.getItems().get(1)), is(""));
        assertThat(actual.getItems().get(1).getDerivationStatus(), is("not_safe_to_derive"));
        assertThat(actual.getItems().get(1).getDerivationReason(), is("Metadata hit does not include database, schema, and table names safe for resource URI derivation."));
    }

    @Test
    void assertExecuteSearchWithEncodedUriCharacter() {
        MetadataSearchResult actual = execute(createDatabaseMetadataWithUnsafeUriName(),
                new MetadataSearchRequest("逻辑 库", "public/main", "orders", Set.of(SupportedMCPMetadataObjectType.TABLE), 10, ""));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().get(0).getName(), is("orders?archive%2026"));
        assertThat(extractResourceHintUri(actual.getItems().get(0)),
                is("shardingsphere://databases/%E9%80%BB%E8%BE%91%20%E5%BA%93/schemas/public%2Fmain/tables/orders%3Farchive%252026"));
        assertThat(actual.getItems().get(0).getDerivationStatus(), is("derived"));
        assertThat(actual.getItems().get(0).getMatchKind(), is("prefix"));
        assertThat(actual.getItems().get(0).getMatchedFields(), is(List.of("name", "table")));
    }

    @Test
    void assertExecuteSearchWithNullViewValueInColumnMetadata() {
        MetadataSearchResult actual = execute(createDatabaseMetadataWithNullViewColumn(),
                new MetadataSearchRequest("null_view_db", "", "missing", Set.of(SupportedMCPMetadataObjectType.COLUMN), 10, ""));
        assertThat(actual.getItems().size(), is(0));
        assertThat(actual.getNextPageToken(), is(""));
    }

    @Test
    void assertExecuteSearchWithEmptyQuery() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("logic_db", "", "", Set.of(), 10, ""));
        assertThat(actual.getItems().size(), is(10));
        assertFalse(actual.getNextPageToken().isEmpty());
        assertFalse(actual.getNextPageToken().matches("\\d+"));
        Set<String> actualNames = actual.getItems().stream().map(MetadataSearchHit::getName).collect(Collectors.toSet());
        assertTrue(actualNames.contains("logic_db"));
        assertTrue(actualNames.contains("active_orders"));
        assertFalse(actualNames.contains("idx_orders_status"));
    }

    @Test
    void assertExecuteSearchWithSequenceObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("runtime_db", "", "order",
                Set.of(SupportedMCPMetadataObjectType.SEQUENCE), 10, ""));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().get(0).getName(), is("order_seq"));
        assertThat(extractResourceHintUri(actual.getItems().get(0)), is("shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq"));
    }

    @Test
    void assertExecuteSearchWithSchemaScopedSequenceObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("runtime_db", "public", "order", Set.of(SupportedMCPMetadataObjectType.SEQUENCE), 10, ""));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().get(0).getName(), is("order_seq"));
    }

    @Test
    void assertExecuteSearchWithUnsupportedIndexObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("warehouse", "", "", Set.of(SupportedMCPMetadataObjectType.INDEX), 10, ""));
        assertThat(actual.getItems().size(), is(0));
        assertThat(actual.getNextPageToken(), is(""));
    }

    @Test
    void assertExecuteSearchWithUnsupportedSequenceObjectType() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(),
                new MetadataSearchRequest("logic_db", "", "", Set.of(SupportedMCPMetadataObjectType.SEQUENCE), 10, ""));
        assertThat(actual.getItems().size(), is(0));
        assertThat(actual.getNextPageToken(), is(""));
    }

    @Test
    void assertExecuteSearchWithInvalidPageToken() {
        InvalidPageTokenException actual = assertThrows(InvalidPageTokenException.class,
                () -> execute(createDatabaseMetadata(), new MetadataSearchRequest("logic_db", "", "order", Set.of(), 10, "invalid")));
        assertThat(actual.getMessage(), is("Invalid page token."));
    }

    @Test
    void assertExecuteSearchWithNegativePageToken() {
        InvalidPageTokenException actual = assertThrows(InvalidPageTokenException.class,
                () -> execute(createDatabaseMetadata(), new MetadataSearchRequest("logic_db", "", "order", Set.of(), 10, "-1")));
        assertThat(actual.getMessage(), is("Invalid page token."));
    }

    @Test
    void assertExecuteSearchWithInvalidPageSize() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> execute(createDatabaseMetadata(), new MetadataSearchRequest("logic_db", "", "order", Set.of(), 501, "")));
        assertThat(actual.getMessage(), is("page_size must be an integer between 1 and 500."));
    }

    @Test
    void assertExecuteSearchWithPageOffsetBeyondResultSize() {
        MetadataSearchResult actual = execute(createDatabaseMetadata(), new MetadataSearchRequest("logic_db", "", "order", Set.of(), 10, "99"));
        assertThat(actual.getItems().size(), is(0));
        assertThat(actual.getNextPageToken(), is(""));
    }

    private MetadataSearchResult execute(final List<MCPDatabaseMetadata> databaseMetadata, final MetadataSearchRequest request) {
        try (MCPRequestScope requestScope = ResourceTestDataFactory.createRequestScope(databaseMetadata)) {
            return new SearchMetadataToolService(requestScope.getMetadataQueryFacade()).execute(request);
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

    private List<MCPDatabaseMetadata> createDatabaseMetadata() {
        List<MCPDatabaseMetadata> result = new LinkedList<>();
        result.add(new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                new MCPSchemaMetadata("logic_db", "public", List.of(
                        new MCPTableMetadata("logic_db", "public", "orders",
                                List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "order_id"),
                                        new MCPColumnMetadata("logic_db", "public", "orders", "", "status")),
                                List.of(new MCPIndexMetadata("logic_db", "public", "orders", "idx_orders_status"))),
                        new MCPTableMetadata("logic_db", "public", "order_items", List.of(), List.of())),
                        List.of(
                                new MCPViewMetadata("logic_db", "public", "active_orders",
                                        List.of(new MCPColumnMetadata("logic_db", "public", "", "active_orders", "order_id"),
                                                new MCPColumnMetadata("logic_db", "public", "", "active_orders", "order_status"))),
                                new MCPViewMetadata("logic_db", "public", "archived_orders", List.of())),
                        List.of()))));
        result.add(new MCPDatabaseMetadata("analytics_db", "PostgreSQL", "", List.of(
                new MCPSchemaMetadata("analytics_db", "public", List.of(
                        new MCPTableMetadata("analytics_db", "public", "metrics",
                                List.of(new MCPColumnMetadata("analytics_db", "public", "metrics", "", "metric_id")), List.of())),
                        List.of(), List.of()))));
        result.add(new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(
                        new MCPTableMetadata("warehouse", "warehouse", "facts",
                                List.of(new MCPColumnMetadata("warehouse", "warehouse", "facts", "", "fact_id")), List.of())),
                        List.of(), List.of()))));
        result.add(new MCPDatabaseMetadata("runtime_db", "H2", "", List.of(
                new MCPSchemaMetadata("runtime_db", "public", List.of(), List.of(), List.of(new MCPSequenceMetadata("runtime_db", "public", "order_seq"))))));
        return result;
    }

    private List<MCPDatabaseMetadata> createDatabaseMetadataWithEmptySchema() {
        return List.of(new MCPDatabaseMetadata("schema_less_db", "H2", "",
                List.of(new MCPSchemaMetadata("schema_less_db", "", List.of(new MCPTableMetadata("schema_less_db", "", "schema_less_orders", List.of(), List.of())), List.of(), List.of()))));
    }

    private List<MCPDatabaseMetadata> createDatabaseMetadataWithUnsafeUriName() {
        return List.of(new MCPDatabaseMetadata("逻辑 库", "MySQL", "",
                List.of(new MCPSchemaMetadata("逻辑 库", "public/main",
                        List.of(new MCPTableMetadata("逻辑 库", "public/main", "orders?archive%2026", List.of(), List.of())), List.of(), List.of()))));
    }

    private List<MCPDatabaseMetadata> createDatabaseMetadataWithNullViewColumn() {
        return List.of(new MCPDatabaseMetadata("null_view_db", "H2", "",
                List.of(new MCPSchemaMetadata("null_view_db", "public", List.of(),
                        List.of(new MCPViewMetadata("null_view_db", "public", "active_view", List.of(new MCPColumnMetadata("null_view_db", "public", "", null, "status")))), List.of()))));
    }
}

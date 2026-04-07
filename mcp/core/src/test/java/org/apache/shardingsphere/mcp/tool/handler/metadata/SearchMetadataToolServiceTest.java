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

package org.apache.shardingsphere.mcp.tool.handler.metadata;

import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.tool.response.MetadataSearchHit;
import org.apache.shardingsphere.mcp.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.protocol.exception.InvalidPageTokenException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchMetadataToolServiceTest {
    
    @Test
    void assertExecuteSearchAcrossDatabases() {
        MetadataSearchResult actual = new SearchMetadataToolService(createDatabaseMetadataCatalog()).execute(new MetadataSearchRequest("", "", "order",
                Set.of(MetadataObjectType.TABLE, MetadataObjectType.VIEW, MetadataObjectType.INDEX), 20, ""));
        Set<String> actualNames = new LinkedHashSet<>();
        for (MetadataSearchHit each : actual.getItems()) {
            actualNames.add(each.getName());
        }
        assertTrue(actualNames.contains("orders"));
        assertTrue(actualNames.contains("order_items"));
        assertTrue(actualNames.contains("active_orders"));
        assertTrue(actualNames.contains("idx_orders_status"));
        assertFalse(actualNames.contains("mv_orders"));
        assertFalse(actualNames.contains("order_seq"));
    }
    
    @Test
    void assertExecuteSearchWithPagination() {
        Set<MetadataObjectType> objectTypes = new LinkedHashSet<>();
        objectTypes.add(MetadataObjectType.TABLE);
        objectTypes.add(MetadataObjectType.VIEW);
        MetadataSearchResult actual = new SearchMetadataToolService(createDatabaseMetadataCatalog()).execute(new MetadataSearchRequest("logic_db", "", "order",
                objectTypes, 1, ""));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().get(0).getName(), is("order_items"));
        assertThat(actual.getNextPageToken(), is("1"));
    }
    
    @Test
    void assertExecuteSearchWithSchemaWithoutDatabase() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> new SearchMetadataToolService(createDatabaseMetadataCatalog()).execute(new MetadataSearchRequest("", "public", "order", Set.of(), 10, "")));
        assertThat(actual.getMessage(), is("Schema cannot be provided without database."));
    }
    
    @Test
    void assertExecuteSearchWithEmptyQuery() {
        MetadataSearchResult actual = new SearchMetadataToolService(createDatabaseMetadataCatalog()).execute(new MetadataSearchRequest("logic_db", "", "", Set.of(), 10, ""));
        Set<String> actualNames = new LinkedHashSet<>();
        for (MetadataSearchHit each : actual.getItems()) {
            actualNames.add(each.getName());
        }
        assertThat(actual.getItems().size(), is(8));
        assertThat(actual.getNextPageToken(), is(""));
        assertTrue(actualNames.contains("logic_db"));
        assertTrue(actualNames.contains("idx_orders_status"));
    }
    
    @Test
    void assertExecuteSearchWithSequenceObjectType() {
        MetadataSearchResult actual = new SearchMetadataToolService(createDatabaseMetadataCatalog()).execute(new MetadataSearchRequest("runtime_db", "", "order",
                Set.of(MetadataObjectType.SEQUENCE), 10, ""));
        assertThat(actual.getItems().size(), is(1));
        assertThat(actual.getItems().get(0).getName(), is("order_seq"));
    }
    
    @Test
    void assertExecuteSearchWithInvalidPageToken() {
        InvalidPageTokenException actual = assertThrows(InvalidPageTokenException.class,
                () -> new SearchMetadataToolService(createDatabaseMetadataCatalog()).execute(new MetadataSearchRequest("logic_db", "", "order", Set.of(), 10, "invalid")));
        assertThat(actual.getMessage(), is("Invalid page token."));
    }
    
    @Test
    void assertExecuteSearchWithPageOffsetBeyondResultSize() {
        MetadataSearchResult actual = new SearchMetadataToolService(createDatabaseMetadataCatalog()).execute(new MetadataSearchRequest("logic_db", "", "order", Set.of(), 10, "99"));
        assertThat(actual.getItems().size(), is(0));
        assertThat(actual.getNextPageToken(), is(""));
    }
    
    private MCPDatabaseMetadataCatalog createDatabaseMetadataCatalog() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>();
        databaseMetadataMap.put("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                new MCPSchemaMetadata("logic_db", "public", List.of(
                        new MCPTableMetadata("logic_db", "public", "orders",
                                List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "order_id"),
                                        new MCPColumnMetadata("logic_db", "public", "orders", "", "status")),
                                List.of(new MCPIndexMetadata("logic_db", "public", "orders", "idx_orders_status"))),
                        new MCPTableMetadata("logic_db", "public", "order_items", List.of(), List.of())),
                        List.of(new MCPViewMetadata("logic_db", "public", "active_orders", List.of()))))));
        databaseMetadataMap.put("analytics_db", new MCPDatabaseMetadata("analytics_db", "PostgreSQL", "", List.of(
                new MCPSchemaMetadata("analytics_db", "public", List.of(
                        new MCPTableMetadata("analytics_db", "public", "metrics",
                                List.of(new MCPColumnMetadata("analytics_db", "public", "metrics", "", "metric_id")), List.of())),
                        List.of()))));
        databaseMetadataMap.put("warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(
                        new MCPTableMetadata("warehouse", "warehouse", "facts",
                                List.of(new MCPColumnMetadata("warehouse", "warehouse", "facts", "", "fact_id")), List.of())),
                        List.of()))));
        databaseMetadataMap.put("runtime_db", new MCPDatabaseMetadata("runtime_db", "H2", "", List.of(
                new MCPSchemaMetadata("runtime_db", "public", List.of(), List.of(), List.of(new MCPSequenceMetadata("runtime_db", "public", "order_seq"))))));
        return new MCPDatabaseMetadataCatalog(databaseMetadataMap);
    }
}

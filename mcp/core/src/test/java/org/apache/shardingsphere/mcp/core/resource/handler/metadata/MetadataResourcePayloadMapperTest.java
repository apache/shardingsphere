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

package org.apache.shardingsphere.mcp.core.resource.handler.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetadataResourcePayloadMapperTest {
    
    @Test
    void assertMapDatabaseDetail() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.querySchemas("logic_db")).thenReturn(List.of(createSchemaMetadata()));
        List<?> actual = new MetadataResourcePayloadMapper(metadataQueryFacade, new MCPUriVariables(Map.of()), true)
                .map(createMetadata("logical-database"), List.of(new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", true, true)));
        Map<?, ?> actualDatabase = (Map<?, ?>) actual.getFirst();
        assertThat(actualDatabase.get("database"), is("logic_db"));
        assertThat(actualDatabase.get("databaseType"), is("FixtureDB"));
        Map<?, ?> actualSchema = getFirstMap(actualDatabase, "schemas");
        assertThat(actualSchema.get("schema"), is("public"));
        assertThat(getNames(actualSchema, "tables", "table"), is(List.of("order_items", "orders")));
        assertThat(getNames(actualSchema, "views", "view"), is(List.of("active_orders")));
        assertThat(getNames(actualSchema, "sequences", "sequence"), is(List.of("order_seq")));
    }
    
    @Test
    void assertMapTableDetail() {
        MCPUriVariables uriVariables = new MCPUriVariables(Map.of("database", "logic_db", "schema", "public"));
        List<?> actual = new MetadataResourcePayloadMapper(mock(MCPMetadataQueryFacade.class), uriVariables, true)
                .map(createMetadata("table"), List.of(createTable("orders", TableType.TABLE)));
        Map<?, ?> actualTable = (Map<?, ?>) actual.getFirst();
        assertThat(actualTable.get("database"), is("logic_db"));
        assertThat(actualTable.get("schema"), is("public"));
        assertThat(actualTable.get("table"), is("orders"));
        assertThat(getNames(actualTable, "columns", "column"), is(List.of("amount", "order_id")));
        assertThat(getNames(actualTable, "indexes", "index"), is(List.of("idx_amount", "idx_order_id")));
    }
    
    @Test
    void assertMapViewDetail() {
        MCPUriVariables uriVariables = new MCPUriVariables(Map.of("database", "logic_db", "schema", "public"));
        List<?> actual = new MetadataResourcePayloadMapper(mock(MCPMetadataQueryFacade.class), uriVariables, true)
                .map(createMetadata("view"), List.of(createTable("active_orders", TableType.VIEW)));
        Map<?, ?> actualView = (Map<?, ?>) actual.getFirst();
        assertThat(actualView.get("database"), is("logic_db"));
        assertThat(actualView.get("schema"), is("public"));
        assertThat(actualView.get("view"), is("active_orders"));
        assertThat(getNames(actualView, "columns", "column"), is(List.of("amount", "order_id")));
    }
    
    private ShardingSphereMCPResourceMetadata createMetadata(final String objectScope) {
        return new ShardingSphereMCPResourceMetadata("", List.of(), "detail", objectScope, "", List.of(), List.of(), List.of());
    }
    
    private ShardingSphereSchema createSchemaMetadata() {
        return new ShardingSphereSchema("public", mock(DatabaseType.class), List.of(
                createTable("orders", TableType.TABLE),
                createTable("order_items", TableType.TABLE),
                createTable("active_orders", TableType.VIEW)), List.of(), List.of(new ShardingSphereSequence("order_seq")));
    }
    
    private ShardingSphereTable createTable(final String name, final TableType tableType) {
        return new ShardingSphereTable(name,
                List.of(new ShardingSphereColumn("order_id", Types.OTHER, false, false, false, true, false, true),
                        new ShardingSphereColumn("amount", Types.OTHER, false, false, false, true, false, true)),
                List.of(new ShardingSphereIndex("idx_order_id", List.of(), false), new ShardingSphereIndex("idx_amount", List.of(), false)),
                List.of(), tableType);
    }
    
    private Map<?, ?> getFirstMap(final Map<?, ?> payload, final String key) {
        return (Map<?, ?>) ((List<?>) payload.get(key)).getFirst();
    }
    
    private List<String> getNames(final Map<?, ?> payload, final String collectionKey, final String nameKey) {
        return ((List<?>) payload.get(collectionKey)).stream().map(each -> String.valueOf(((Map<?, ?>) each).get(nameKey))).toList();
    }
}

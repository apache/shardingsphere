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

package org.apache.shardingsphere.mcp.support.database.metadata.context;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcMetadataLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata.Nullability;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RequestScopedMetadataContextTest {
    
    @Test
    void assertLoadColumnsOncePerRelation() {
        RuntimeDatabaseConfiguration configuration = mock(RuntimeDatabaseConfiguration.class);
        RuntimeDatabaseProfile profile = mock(RuntimeDatabaseProfile.class);
        MCPDatabaseCapabilityProvider capabilityProvider = createCapabilityProvider(profile);
        List<MCPColumnMetadata> expected = List.of(new MCPColumnMetadata("orders", "order_id", 1, Types.BIGINT, "BIGINT", Nullability.NOT_NULLABLE));
        try (
                MockedConstruction<MCPJdbcMetadataLoader> mockedLoader = mockConstruction(MCPJdbcMetadataLoader.class,
                        (mock, context) -> when(mock.loadColumns("logic_db", configuration, profile, "public", "orders")).thenReturn(expected))) {
            RequestScopedMetadataContext context = new RequestScopedMetadataContext(Map.of("logic_db", configuration), capabilityProvider);
            assertThat(context.loadColumns("logic_db", "public", "orders").orElseThrow().stream().map(MCPColumnMetadata::getName).toList(), is(List.of("order_id")));
            assertThat(context.loadColumns("logic_db", "public", "orders").orElseThrow().stream().map(MCPColumnMetadata::getName).toList(), is(List.of("order_id")));
            verify(mockedLoader.constructed().getFirst()).loadColumns("logic_db", configuration, profile, "public", "orders");
        }
    }
    
    @Test
    void assertLoadColumnsWithoutRuntimeBinding() {
        try (MockedConstruction<MCPJdbcMetadataLoader> mockedLoader = mockConstruction(MCPJdbcMetadataLoader.class)) {
            RequestScopedMetadataContext context = new RequestScopedMetadataContext(Map.of(), createCapabilityProvider());
            assertTrue(context.loadColumns("logic_db", "public", "orders").isEmpty());
            verifyNoInteractions(mockedLoader.constructed().getFirst());
        }
    }
    
    @Test
    void assertLoadSchemaColumnsOncePerSchema() {
        RuntimeDatabaseConfiguration configuration = mock(RuntimeDatabaseConfiguration.class);
        RuntimeDatabaseProfile profile = mock(RuntimeDatabaseProfile.class);
        MCPDatabaseCapabilityProvider capabilityProvider = createCapabilityProvider(profile);
        List<MCPColumnMetadata> expected = List.of(new MCPColumnMetadata("orders", "order_id", 1, Types.BIGINT, "BIGINT", Nullability.NOT_NULLABLE));
        try (
                MockedConstruction<MCPJdbcMetadataLoader> mockedLoader = mockConstruction(MCPJdbcMetadataLoader.class,
                        (mock, context) -> when(mock.loadSchemaColumns("logic_db", configuration, profile, "public")).thenReturn(expected))) {
            RequestScopedMetadataContext context = new RequestScopedMetadataContext(Map.of("logic_db", configuration), capabilityProvider);
            assertThat(context.loadSchemaColumns("logic_db", "public").orElseThrow().stream().map(MCPColumnMetadata::getName).toList(), is(List.of("order_id")));
            assertThat(context.loadSchemaColumns("logic_db", "public").orElseThrow().stream().map(MCPColumnMetadata::getName).toList(), is(List.of("order_id")));
            verify(mockedLoader.constructed().getFirst()).loadSchemaColumns("logic_db", configuration, profile, "public");
        }
    }
    
    @Test
    void assertLoadSchemaColumnsWithoutRuntimeBinding() {
        try (MockedConstruction<MCPJdbcMetadataLoader> mockedLoader = mockConstruction(MCPJdbcMetadataLoader.class)) {
            RequestScopedMetadataContext context = new RequestScopedMetadataContext(Map.of(), createCapabilityProvider());
            assertTrue(context.loadSchemaColumns("logic_db", "public").isEmpty());
            verifyNoInteractions(mockedLoader.constructed().getFirst());
        }
    }
    
    @Test
    void assertLoadIndexesOncePerTable() {
        RuntimeDatabaseConfiguration configuration = mock(RuntimeDatabaseConfiguration.class);
        RuntimeDatabaseProfile profile = mock(RuntimeDatabaseProfile.class);
        MCPDatabaseCapabilityProvider capabilityProvider = createCapabilityProvider(profile);
        List<ShardingSphereIndex> expected = List.of(new ShardingSphereIndex("orders_idx", List.of("order_id"), true));
        try (
                MockedConstruction<MCPJdbcMetadataLoader> mockedLoader = mockConstruction(MCPJdbcMetadataLoader.class,
                        (mock, context) -> when(mock.loadIndexes("logic_db", configuration, profile, "public", "orders")).thenReturn(expected))) {
            RequestScopedMetadataContext context = new RequestScopedMetadataContext(Map.of("logic_db", configuration), capabilityProvider);
            assertThat(context.loadIndexes("logic_db", "public", "orders").orElseThrow().stream().map(ShardingSphereIndex::getName).toList(), is(List.of("orders_idx")));
            assertThat(context.loadIndexes("logic_db", "public", "orders").orElseThrow().stream().map(ShardingSphereIndex::getName).toList(), is(List.of("orders_idx")));
            verify(mockedLoader.constructed().getFirst()).loadIndexes("logic_db", configuration, profile, "public", "orders");
        }
    }
    
    @Test
    void assertLoadIndexesWithoutRuntimeBinding() {
        try (MockedConstruction<MCPJdbcMetadataLoader> mockedLoader = mockConstruction(MCPJdbcMetadataLoader.class)) {
            RequestScopedMetadataContext context = new RequestScopedMetadataContext(Map.of(), createCapabilityProvider());
            assertTrue(context.loadIndexes("logic_db", "public", "orders").isEmpty());
            verifyNoInteractions(mockedLoader.constructed().getFirst());
        }
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider() {
        return mock(MCPDatabaseCapabilityProvider.class);
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider(final RuntimeDatabaseProfile profile) {
        MCPDatabaseCapabilityProvider result = mock(MCPDatabaseCapabilityProvider.class);
        when(result.findDatabaseProfile("logic_db")).thenReturn(Optional.of(profile));
        return result;
    }
}

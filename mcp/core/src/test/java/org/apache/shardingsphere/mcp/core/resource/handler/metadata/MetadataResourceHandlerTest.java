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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureCapabilityFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetadataResourceHandlerTest {
    
    @Test
    void assertGetResourceDescriptor() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases", (requestContext, uriVariables) -> List.of());
        MCPResourceDescriptor actual = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(handler.getResourceUriTemplate());
        assertThat(actual.getUriTemplate(), is("shardingsphere://databases"));
        assertThat(actual.getTitle(), is("Logical Databases"));
    }
    
    @Test
    void assertHandleListResource() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases",
                (requestContext, uriVariables) -> List.of(Map.of("database", "logic_db")));
        MCPSuccessPayload actual = handler.handle(mock(MCPFeatureRequestContext.class), new MCPUriVariables(Map.of()));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("response_mode"), is("list"));
        assertThat(actualPayload.get("summary"), is("Returned 1 of 1 logical-database metadata entries."));
        assertThat(actualPayload.get("items"), is(List.of(Map.of("database", "logic_db"))));
        assertThat(actualPayload.get("count"), is(1));
        assertThat(actualPayload.get("continuation_mode"), is("none"));
        assertThat(((Map<?, ?>) actualPayload.get("self_resource")).get("uri"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actualPayload.get("self_resource")).get("resource_kind"), is("logical-database"));
        assertThat(((Map<?, ?>) actualPayload.get("self_resource")).get("source_field"), is("self_resource"));
        assertThat(actualPayload.get("total_count"), is(1));
        assertFalse((Boolean) actualPayload.get("truncated"));
    }
    
    @Test
    void assertHandleRootListResourceWithoutRuntimeDatabase() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases", (requestContext, uriVariables) -> List.of());
        MCPSuccessPayload actual = handler.handle(mock(MCPFeatureRequestContext.class), new MCPUriVariables(Map.of()));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("no_runtime_database"));
        assertThat(actualEmptyState.get("reason"), is("No ShardingSphere-Proxy logical database is available to MCP. Configure runtimeDatabases before reading metadata."));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.toPayload().get("recovery");
        assertThat(actualRecovery.get("recovery_category"), is("unavailable_runtime"));
        assertThat(actualRecovery.get("category"), is("no_runtime_database"));
    }
    
    @Test
    void assertHandleListResourceWithUnknownDatabase() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas", (requestContext, uriVariables) -> List.of());
        MCPSuccessPayload actual = handler.handle(createDatabaseContext(Optional.empty()), new MCPUriVariables(Map.of("database", "missing_db")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("unknown_database"));
        assertThat(actualEmptyState.get("reason"), is("The requested logical database is not visible to MCP. Check runtimeDatabases and ShardingSphere-Proxy connectivity."));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.toPayload().get("recovery");
        assertThat(actualRecovery.get("recovery_category"), is("not_found"));
        assertThat(actualRecovery.get("category"), is("unknown_database"));
    }
    
    @Test
    void assertHandleListResourceWithEmptyScope() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas", (requestContext, uriVariables) -> List.of());
        MCPSuccessPayload actual = handler.handle(createDatabaseContext(Optional.of(
                new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT, IdentifierCasePolicyFactory.newInsensitivePolicySet()))),
                new MCPUriVariables(Map.of("database", "logic_db")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("empty_scope"));
        assertThat(actualEmptyState.get("reason"), is("No metadata items are visible in this scope. Check metadata permissions if objects are expected."));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.toPayload().get("recovery");
        assertThat(actualRecovery.get("recovery_category"), is("empty_scope"));
        assertThat(actualRecovery.get("category"), is("empty_scope"));
    }
    
    @Test
    void assertHandleSchemaDetailResourceNotVisible() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}", (requestContext, uriVariables) -> List.of());
        MCPSuccessPayload actual = handler.handle(createDatabaseContext(Optional.of(
                new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT, IdentifierCasePolicyFactory.newInsensitivePolicySet()))),
                new MCPUriVariables(Map.of("database", "logic_db", "schema", "missing_schema")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("schema_not_visible"));
        assertThat(actualEmptyState.get("reason"), is("The requested schema is not visible in the current metadata scope."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("requested_token"), is("missing_schema"));
        assertThat(((Map<?, ?>) ((List<?>) actual.toPayload().get("next_actions")).getFirst()).get("type"), is("resource_read"));
    }
    
    @Test
    void assertHandleObjectDetailResourceNotVisible() {
        MetadataResourceHandler handler = new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}", (requestContext, uriVariables) -> List.of());
        MCPSuccessPayload actual = handler.handle(createDatabaseContext(Optional.of(
                new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT, IdentifierCasePolicyFactory.newInsensitivePolicySet()))),
                new MCPUriVariables(Map.of("database", "logic_db", "schema", "public", "table", "missing_table")));
        Map<?, ?> actualEmptyState = (Map<?, ?>) actual.toPayload().get("empty_state");
        assertThat(actualEmptyState.get("category"), is("object_not_visible"));
        assertThat(actualEmptyState.get("reason"), is("The requested metadata object is not visible in the current metadata scope."));
        assertThat(((Map<?, ?>) actual.toPayload().get("recovery")).get("requested_token"), is("missing_table"));
    }
    
    private MCPFeatureRequestContext createDatabaseContext(final Optional<RuntimeDatabaseProfile> databaseProfile) {
        MCPFeatureCapabilityFacade capabilityFacade = mock(MCPFeatureCapabilityFacade.class);
        when(capabilityFacade.findDatabaseProfile("logic_db")).thenReturn(databaseProfile);
        when(capabilityFacade.findDatabaseProfile("missing_db")).thenReturn(Optional.empty());
        MCPFeatureRequestContext result = mock(MCPFeatureRequestContext.class);
        when(result.getCapabilityFacade()).thenReturn(capabilityFacade);
        return result;
    }
    
}

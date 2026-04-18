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

import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPResourceControllerTest {
    
    @Test
    void assertHandleWithUnsupportedResourceUri() {
        Map<String, Object> actual = createController().handle("unsupported://resource").toPayload();
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actual.get("message"), is("Unsupported resource URI."));
    }
    
    @Test
    void assertHandleServiceCapabilities() {
        Map<String, Object> actual = createController().handle("shardingsphere://capabilities").toPayload();
        assertTrue(((List<?>) actual.get("supportedResources")).contains("shardingsphere://databases/{database}/capabilities"));
        assertTrue(((List<?>) actual.get("supportedResources")).contains("shardingsphere://databases/{database}/encrypt-rules"));
        assertTrue(((List<?>) actual.get("supportedResources")).contains("shardingsphere://plugins/mask-algorithms"));
        assertTrue(((List<?>) actual.get("supportedTools")).contains("search_metadata"));
        assertTrue(((List<?>) actual.get("supportedTools")).contains("execute_query"));
        assertTrue(((List<?>) actual.get("supportedTools")).contains("plan_encrypt_mask_rule"));
        assertTrue(((List<?>) actual.get("supportedTools")).contains("apply_encrypt_mask_rule"));
        assertTrue(((List<?>) actual.get("supportedTools")).contains("validate_encrypt_mask_rule"));
        assertTrue(((Set<?>) actual.get("supportedStatementClasses")).contains(SupportedMCPStatement.QUERY));
    }
    
    @Test
    void assertHandleDatabaseCapabilities() {
        Map<String, Object> actual = createController().handle("shardingsphere://databases/logic_db/capabilities").toPayload();
        assertThat(actual.get("database"), is("logic_db"));
        assertThat(actual.get("databaseType"), is("MySQL"));
        assertTrue((Boolean) actual.get("supportsTransactionControl"));
    }
    
    @Test
    void assertHandleWithUnknownDatabaseCapabilities() {
        Map<String, Object> actual = createController().handle("shardingsphere://databases/missing_db/capabilities").toPayload();
        assertThat(actual.get("error_code"), is("not_found"));
        assertThat(actual.get("message"), is("Database capability does not exist."));
    }
    
    @Test
    void assertHandleMetadataItems() {
        Map<String, Object> actual = createController().handle("shardingsphere://databases/logic_db/schemas/public/tables").toPayload();
        assertThat(((List<?>) actual.get("items")).size(), is(2));
        assertThat(((MCPTableMetadata) ((List<?>) actual.get("items")).get(0)).getTable(), is("order_items"));
    }
    
    @Test
    void assertHandleSequenceMetadataItems() {
        Map<String, Object> actual = createController().handle("shardingsphere://databases/runtime_db/schemas/public/sequences").toPayload();
        assertThat(((List<?>) actual.get("items")).size(), is(1));
        assertThat(((MCPSequenceMetadata) ((List<?>) actual.get("items")).get(0)).getSequence(), is("order_seq"));
    }
    
    @Test
    void assertHandleWithUnsupportedIndexResource() {
        Map<String, Object> actual = createController().handle("shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes").toPayload();
        assertThat(actual.get("error_code"), is("unsupported"));
        assertThat(actual.get("message"), is("Index resources are not supported for the current database."));
    }
    
    @Test
    void assertHandleWithUnsupportedSequenceResource() {
        Map<String, Object> actual = createController().handle("shardingsphere://databases/warehouse/schemas/warehouse/sequences").toPayload();
        assertThat(actual.get("error_code"), is("unsupported"));
        assertThat(actual.get("message"), is("Sequence resources are not supported for the current database."));
    }
    
    private MCPResourceController createController() {
        return new MCPResourceController(ResourceTestDataFactory.createRuntimeContext());
    }
}

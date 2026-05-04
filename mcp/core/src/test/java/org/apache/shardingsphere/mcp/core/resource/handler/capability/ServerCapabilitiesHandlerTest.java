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

package org.apache.shardingsphere.mcp.core.resource.handler.capability;

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerCapabilitiesHandlerTest {

    @Test
    void assertHandleReturnsCoreModelSurfaceContract() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext())) {
            Map<String, Object> actual = new ServerCapabilitiesHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertTrue(((Collection<?>) actual.get("supportedResources")).contains("shardingsphere://capabilities"));
            assertTrue(((Collection<?>) actual.get("supportedTools")).containsAll(List.of("search_metadata", "execute_query", "execute_update", "apply_workflow", "validate_workflow")));
            assertFalse(((List<?>) actual.get("resources")).isEmpty());
            assertFalse(((List<?>) actual.get("resourceTemplates")).isEmpty());
            assertFalse(((List<?>) actual.get("tools")).isEmpty());
            assertFalse(((List<?>) actual.get("prompts")).isEmpty());
            assertFalse(((List<?>) actual.get("completionTargets")).isEmpty());
            assertFalse(((List<?>) actual.get("resourceNavigation")).isEmpty());
            Map<?, ?> protocolAvailability = (Map<?, ?>) actual.get("protocolAvailability");
            assertTrue((Boolean) protocolAvailability.get("resourceNavigation"));
            assertFalse(protocolAvailability.containsKey("sampling"));
            assertFalse(protocolAvailability.containsKey("roots"));
            assertTrue(((Map<?, ?>) actual.get("fingerprints")).containsKey("descriptorCatalog"));
            assertModelContract(actual);
            assertSecurityHints(actual);
            assertResourcePayloadContracts(actual);
            assertCoreToolSchemas(actual);
        }
    }

    private void assertModelContract(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("model_contract");
        assertThat(actual.get("safe_first_resource"), is("shardingsphere://capabilities"));
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertTrue(((Map<?, ?>) actual.get("sql_tool_selection")).containsKey("side_effecting"));
        assertTrue(actual.containsKey("workflow_session_rule"));
        assertTrue(actual.containsKey("recovery_rule"));
    }

    private void assertSecurityHints(final Map<String, Object> capabilities) {
        Map<?, ?> actual = (Map<?, ?>) capabilities.get("security_hints");
        assertTrue(actual.containsKey("http_access_token"));
        assertTrue(actual.containsKey("remote_access"));
        assertTrue(actual.containsKey("stdio_stdout"));
    }

    private void assertResourcePayloadContracts(final Map<String, Object> capabilities) {
        Map<?, ?> capabilityCatalog = findResource(capabilities, "shardingsphere://capabilities");
        assertThat(((Map<?, ?>) capabilityCatalog.get("payload_contract")).get("response_kind"), is("capability-catalog"));
        Map<?, ?> databaseDetail = findResource(capabilities, "shardingsphere://databases/{database}");
        Map<?, ?> payloadContract = (Map<?, ?>) databaseDetail.get("payload_contract");
        assertThat(payloadContract.get("response_kind"), is("detail"));
        assertTrue(((List<?>) payloadContract.get("stable_fields")).containsAll(List.of("found", "items", "count", "item")));
        Map<?, ?> databaseCapabilities = findResource(capabilities, "shardingsphere://databases/{database}/capabilities");
        assertThat(((Map<?, ?>) databaseCapabilities.get("payload_contract")).get("response_kind"), is("detail-object"));
        Map<?, ?> databases = findResource(capabilities, "shardingsphere://databases");
        Map<?, ?> databaseListPayloadContract = (Map<?, ?>) databases.get("payload_contract");
        assertThat(databaseListPayloadContract.get("stable_fields"), is(List.of("items", "has_more")));
        assertThat(databaseListPayloadContract.get("optional_fields"), is(List.of("next_page_token")));
    }

    private void assertCoreToolSchemas(final Map<String, Object> capabilities) {
        Map<?, ?> searchMetadataTool = findTool(capabilities, "search_metadata");
        Map<?, ?> objectTypesSchema = findInputSchema(searchMetadataTool, "object_types");
        assertTrue(((List<?>) ((Map<?, ?>) objectTypesSchema.get("items")).get("enum")).containsAll(List.of("database", "schema", "table", "view", "column", "index", "sequence")));
        Map<?, ?> executeUpdateTool = findTool(capabilities, "execute_update");
        Map<?, ?> executeUpdateOutputProperties = (Map<?, ?>) ((Map<?, ?>) executeUpdateTool.get("outputSchema")).get("properties");
        assertTrue(((List<?>) ((Map<?, ?>) executeUpdateOutputProperties.get("result_kind")).get("enum")).containsAll(List.of("preview", "result_set", "update_count", "statement_ack")));
    }

    private Map<?, ?> findResource(final Map<String, Object> capabilities, final String uriPattern) {
        List<?> resources = (List<?>) capabilities.get(uriPattern.contains("{") ? "resourceTemplates" : "resources");
        return resources.stream().map(each -> (Map<?, ?>) each).filter(each -> uriPattern.equals(each.get("uriPattern"))).findFirst().orElseThrow();
    }

    private Map<?, ?> findTool(final Map<String, Object> capabilities, final String toolName) {
        return ((List<?>) capabilities.get("tools")).stream().map(each -> (Map<?, ?>) each).filter(each -> toolName.equals(each.get("name"))).findFirst().orElseThrow();
    }

    private Map<?, ?> findInputSchema(final Map<?, ?> tool, final String fieldName) {
        Map<?, ?> field = ((List<?>) tool.get("inputFields")).stream().map(each -> (Map<?, ?>) each).filter(each -> fieldName.equals(each.get("name"))).findFirst().orElseThrow();
        return (Map<?, ?>) field.get("schema");
    }
}

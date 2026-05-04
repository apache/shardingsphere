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
            assertCoreToolSchemas(actual);
        }
    }

    private void assertCoreToolSchemas(final Map<String, Object> capabilities) {
        Map<?, ?> searchMetadataTool = findTool(capabilities, "search_metadata");
        Map<?, ?> objectTypesSchema = findInputSchema(searchMetadataTool, "object_types");
        assertTrue(((List<?>) ((Map<?, ?>) objectTypesSchema.get("items")).get("enum")).containsAll(List.of("database", "schema", "table", "view", "column", "index", "sequence")));
        Map<?, ?> executeUpdateTool = findTool(capabilities, "execute_update");
        Map<?, ?> executeUpdateOutputProperties = (Map<?, ?>) ((Map<?, ?>) executeUpdateTool.get("outputSchema")).get("properties");
        assertTrue(((List<?>) ((Map<?, ?>) executeUpdateOutputProperties.get("result_kind")).get("enum")).containsAll(List.of("preview", "result_set", "update_count", "statement_ack")));
    }

    private Map<?, ?> findTool(final Map<String, Object> capabilities, final String toolName) {
        return ((List<?>) capabilities.get("tools")).stream().map(each -> (Map<?, ?>) each).filter(each -> toolName.equals(each.get("name"))).findFirst().orElseThrow();
    }

    private Map<?, ?> findInputSchema(final Map<?, ?> tool, final String fieldName) {
        Map<?, ?> field = ((List<?>) tool.get("inputFields")).stream().map(each -> (Map<?, ?>) each).filter(each -> fieldName.equals(each.get("name"))).findFirst().orElseThrow();
        return (Map<?, ?>) field.get("schema");
    }
}

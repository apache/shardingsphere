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
            assertTrue((Boolean) ((Map<?, ?>) actual.get("protocolAvailability")).get("resourceNavigation"));
            assertTrue(((Map<?, ?>) actual.get("fingerprints")).containsKey("descriptorCatalog"));
        }
    }
}

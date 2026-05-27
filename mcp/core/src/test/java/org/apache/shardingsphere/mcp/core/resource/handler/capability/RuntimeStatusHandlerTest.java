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

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeStatusHandlerTest {
    
    @Test
    void assertHandle() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext(ResourceTestDataFactory.createDatabaseMetadata(), "http"))) {
            Map<String, Object> actual = new RuntimeStatusHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertThat(actual.get("response_mode"), is("runtime"));
            assertThat(actual.get("server_status"), is("ready"));
            assertThat(actual.get("status"), is("available"));
            assertThat(actual.get("transport"), is("http"));
            assertThat(actual.get("active_transport"), is("http"));
            assertThat(actual.get("configured_database_count"), is(3));
            assertTrue(((List<?>) actual.get("databases")).stream().map(each -> ((Map<?, ?>) each).get("database")).anyMatch("logic_db"::equals));
            assertThat(((Map<?, ?>) actual.get("redaction_summary")).get("marker"), is("******"));
            assertRuntimeDiagnostics(actual, "ready");
            assertTrue(String.valueOf(actual.get("capability_fingerprint")).matches("[0-9a-f]{64}"));
            assertRuntimeCapability((List<?>) actual.get("databases"), "logic_db");
            assertThat(extractResourceUris((List<?>) actual.get("resources_to_read")), is(List.of("shardingsphere://capabilities", "shardingsphere://databases")));
            assertThat(actual.get("next_actions"), is(List.of()));
        }
    }
    
    @Test
    void assertHandleWithStdioTransport() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext(ResourceTestDataFactory.createDatabaseMetadata(), "stdio"))) {
            Map<String, Object> actual = new RuntimeStatusHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertThat(actual.get("transport"), is("stdio"));
            assertThat(actual.get("active_transport"), is("stdio"));
        }
    }

    @Test
    void assertHandleWithEmptyRuntimeDatabase() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext(List.of(), "http"))) {
            Map<String, Object> actual = new RuntimeStatusHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertThat(actual.get("server_status"), is("configuration_required"));
            assertThat(actual.get("status"), is("configuration_required"));
            assertThat(actual.get("configured_database_count"), is(0));
            assertThat(((List<?>) actual.get("databases")).size(), is(0));
            Map<?, ?> readiness = (Map<?, ?>) actual.get("readiness");
            assertFalse((Boolean) readiness.get("ready"));
            assertThat(readiness.get("reason"), is("No runtime databases are configured."));
            assertRuntimeDiagnostics(actual, "invalid_configuration");
            assertThat(extractResourceUris((List<?>) actual.get("resources_to_read")), is(List.of("shardingsphere://capabilities")));
            List<?> nextActions = (List<?>) actual.get("next_actions");
            assertThat(((Map<?, ?>) nextActions.get(0)).get("type"), is("resource_read"));
            assertThat(((Map<?, ?>) nextActions.get(1)).get("type"), is("ask_user"));
            assertThat(((Map<?, ?>) nextActions.get(1)).get("required_inputs"), is(List.of("runtimeDatabases")));
        }
    }
    
    private void assertRuntimeDiagnostics(final Map<String, Object> payload, final String expectedCategory) {
        Map<?, ?> actualDiagnostics = (Map<?, ?>) payload.get("diagnostics");
        assertThat(actualDiagnostics.get("current_category"), is(expectedCategory));
        List<?> actualSafeCategories = (List<?>) actualDiagnostics.get("safe_categories");
        assertTrue(actualSafeCategories.contains("missing_jdbc_driver"));
        assertTrue(actualSafeCategories.contains("authentication_failed"));
        assertTrue(actualSafeCategories.contains("connection_timeout"));
        assertTrue(actualSafeCategories.contains("invalid_configuration"));
        assertTrue(actualSafeCategories.contains("database_unavailable"));
        assertTrue(actualSafeCategories.contains("connection_failed"));
        List<?> actualOperatorNextActions = (List<?>) actualDiagnostics.get("operator_next_actions");
        assertThat(actualOperatorNextActions.size(), is(6));
        assertThat(((Map<?, ?>) actualOperatorNextActions.get(3)).get("category"), is("invalid_configuration"));
        assertTrue((Boolean) ((Map<?, ?>) actualOperatorNextActions.get(3)).get("secret_safe"));
    }
    
    private void assertRuntimeCapability(final List<?> databases, final String databaseName) {
        Map<?, ?> actualDatabase = databases.stream().map(each -> (Map<?, ?>) each).filter(each -> databaseName.equals(each.get("database"))).findFirst().orElseThrow();
        assertThat(actualDatabase.get("metadata_visibility"), is("ready"));
        assertThat(actualDatabase.get("capability_visibility"), is("ready"));
        assertThat(actualDatabase.get("feature_visibility"), is("ready"));
        Map<?, ?> actualCapabilities = (Map<?, ?>) actualDatabase.get("capabilities");
        assertTrue((Boolean) actualCapabilities.get("available"));
        assertTrue(((List<?>) actualCapabilities.get("supported_statement_classes")).contains("QUERY"));
        assertTrue(((List<?>) actualCapabilities.get("supported_metadata_object_types")).contains("TABLE"));
    }
    
    private List<String> extractResourceUris(final List<?> resources) {
        return resources.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
    }
}

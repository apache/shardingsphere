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

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.support.security.MCPRuntimeProtectionPolicy;
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
        MCPFeatureRuntimeRequestContext requestContext =
                new MCPFeatureRuntimeRequestContext(ResourceTestDataFactory.createRuntimeContext(ResourceTestDataFactory.createDatabaseMetadata(), MCPTransportType.STREAMABLE_HTTP),
                        new MCPSessionIdentity("session-1", "", "", Map.of()));
        Map<String, Object> actual = new RuntimeStatusHandler().handle(requestContext, new MCPUriVariables(Map.of())).toPayload();
        assertThat(actual.get("response_mode"), is("runtime"));
        assertThat(actual.get("summary"), is("Runtime is ready with 3 configured logical database(s)."));
        assertThat(actual.get("server_status"), is("ready"));
        assertThat(actual.get("status"), is("available"));
        assertThat(actual.get("transport"), is("http"));
        assertThat(actual.get("active_transport"), is("http"));
        assertThat(((Map<?, ?>) actual.get("transport_security_summary")).get("recommended_exposure"), is("loopback_or_trusted_gateway"));
        assertThat(actual.get("configured_database_count"), is(3));
        assertTrue(((List<?>) actual.get("databases")).stream().map(each -> ((Map<?, ?>) each).get("database")).anyMatch("logic_db"::equals));
        assertThat(((Map<?, ?>) actual.get("redaction_summary")).get("marker"), is("******"));
        assertRuntimeDiagnostics(actual, "ready");
        assertRuntimeProtection(actual);
        assertFalse(actual.containsKey("capability_fingerprint"));
        assertRuntimeCapability((List<?>) actual.get("databases"), "logic_db");
        assertThat(extractResourceUris((List<?>) actual.get("resources_to_read")), is(List.of("shardingsphere://capabilities", "shardingsphere://databases")));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("next_actions")).getFirst()).get("resource_uri"), is("shardingsphere://databases"));
    }
    
    @Test
    void assertHandleWithStdioTransport() {
        MCPFeatureRuntimeRequestContext requestContext =
                new MCPFeatureRuntimeRequestContext(ResourceTestDataFactory.createRuntimeContext(ResourceTestDataFactory.createDatabaseMetadata(), MCPTransportType.STDIO),
                        new MCPSessionIdentity("session-1", "", "", Map.of()));
        Map<String, Object> actual = new RuntimeStatusHandler().handle(requestContext, new MCPUriVariables(Map.of())).toPayload();
        assertThat(actual.get("transport"), is("stdio"));
        assertThat(actual.get("active_transport"), is("stdio"));
        assertThat(((Map<?, ?>) actual.get("transport_security_summary")).get("recommended_exposure"), is("local_stdio_session"));
    }
    
    @Test
    void assertHandleWithEmptyRuntimeDatabase() {
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(ResourceTestDataFactory.createRuntimeContext(List.of(), MCPTransportType.STREAMABLE_HTTP),
                new MCPSessionIdentity("session-1", "", "", Map.of()));
        Map<String, Object> actual = new RuntimeStatusHandler().handle(requestContext, new MCPUriVariables(Map.of())).toPayload();
        assertThat(actual.get("server_status"), is("configuration_required"));
        assertThat(actual.get("summary"), is("Runtime requires at least one configured logical database before metadata discovery or SQL execution."));
        assertThat(actual.get("status"), is("configuration_required"));
        assertThat(actual.get("configured_database_count"), is(0));
        assertThat(((List<?>) actual.get("databases")).size(), is(0));
        Map<?, ?> readiness = (Map<?, ?>) actual.get("readiness");
        assertFalse((Boolean) readiness.get("ready"));
        assertThat(readiness.get("reason"), is("No runtime databases are configured."));
        assertRuntimeDiagnostics(actual, "invalid_configuration");
        assertRuntimeProtection(actual);
        assertThat(extractResourceUris((List<?>) actual.get("resources_to_read")), is(List.of("shardingsphere://capabilities")));
        List<?> nextActions = (List<?>) actual.get("next_actions");
        assertThat(((Map<?, ?>) nextActions.get(0)).get("type"), is("resource_read"));
        assertThat(((Map<?, ?>) nextActions.get(1)).get("type"), is("ask_user"));
        assertThat(((Map<?, ?>) nextActions.get(1)).get("required_inputs"), is(List.of("runtimeDatabases")));
    }
    
    private void assertRuntimeDiagnostics(final Map<String, Object> payload, final String expectedCategory) {
        Map<?, ?> actualDiagnostics = (Map<?, ?>) payload.get("diagnostics");
        assertThat(actualDiagnostics.get("current_category"), is(expectedCategory));
        List<?> actualSafeCategories = (List<?>) actualDiagnostics.get("safe_categories");
        assertTrue(actualSafeCategories.contains("missing_jdbc_driver"));
        assertTrue(actualSafeCategories.contains("authentication_failed"));
        assertTrue(actualSafeCategories.contains("authorization_failed"));
        assertTrue(actualSafeCategories.contains("connection_timeout"));
        assertTrue(actualSafeCategories.contains("invalid_configuration"));
        assertTrue(actualSafeCategories.contains("database_unavailable"));
        assertTrue(actualSafeCategories.contains("connection_failed"));
        assertTrue(actualSafeCategories.contains("database_not_visible"));
        List<?> actualOperatorNextActions = (List<?>) actualDiagnostics.get("operator_next_actions");
        assertThat(actualOperatorNextActions.size(), is(8));
        Map<?, ?> actualInvalidConfigurationAction = (Map<?, ?>) actualOperatorNextActions.get(4);
        assertThat(actualInvalidConfigurationAction.get("category"), is("invalid_configuration"));
        assertThat(actualInvalidConfigurationAction.get("operator_action"), is("Fix runtimeDatabases JDBC URL, driver, or binding configuration."));
        assertTrue((Boolean) actualInvalidConfigurationAction.get("secret_safe"));
        assertThat(((Map<?, ?>) actualOperatorNextActions.get(7)).get("category"), is("database_not_visible"));
    }
    
    private void assertRuntimeProtection(final Map<String, Object> payload) {
        Map<?, ?> actualRuntimeProtection = (Map<?, ?>) payload.get("runtime_protection");
        Map<?, ?> actualToolCallLimit = (Map<?, ?>) actualRuntimeProtection.get("tool_call_limit");
        assertThat(actualToolCallLimit.get("scope"), is("session"));
        assertThat(actualToolCallLimit.get("property"), is(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY));
        Map<?, ?> actualSQLExecutionLimits = (Map<?, ?>) actualRuntimeProtection.get("sql_execution_limits");
        Map<?, ?> actualMaxRows = (Map<?, ?>) actualSQLExecutionLimits.get("max_rows");
        assertThat(actualMaxRows.get("default_value"), is(MCPRuntimeProtectionPolicy.DEFAULT_MAX_ROWS));
        assertThat(actualMaxRows.get("maximum_value"), is(MCPRuntimeProtectionPolicy.MAX_ROWS_LIMIT));
        assertThat(actualMaxRows.get("applied_field"), is("applied_max_rows"));
        assertThat(actualMaxRows.get("truncation_field"), is("truncated"));
        Map<?, ?> actualTimeout = (Map<?, ?>) actualSQLExecutionLimits.get("timeout_ms");
        assertThat(actualTimeout.get("default_value"), is(MCPRuntimeProtectionPolicy.DEFAULT_TIMEOUT_MILLISECONDS));
        assertThat(actualTimeout.get("maximum_value"), is(MCPRuntimeProtectionPolicy.MAX_TIMEOUT_MILLISECONDS));
        assertThat(actualTimeout.get("applied_field"), is("applied_timeout_ms"));
        assertThat(actualTimeout.get("zero_means"), is("server_default"));
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

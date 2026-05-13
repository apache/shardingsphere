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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionH2CapabilityDiscoveryE2ETest extends ProductionH2RuntimeSmokeE2ETest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadDatabasesResource(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource("shardingsphere://databases"), "database", "logic_db");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadDatabaseCapabilitiesResource(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource("shardingsphere://databases/logic_db/capabilities");
            assertThat(String.valueOf(actual.get("databaseType")), is("H2"));
            assertThat(String.valueOf(actual.get("supportsExplainAnalyze")), is("true"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertServiceCapabilitiesResource(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertOfficialToolNames(((List<?>) interactionClient.readResource("shardingsphere://capabilities").get("supportedTools")).stream().map(String::valueOf).toList());
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertListTools(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> actual = interactionClient.listTools();
            assertOfficialToolNames(actual.stream().map(each -> String.valueOf(each.get("name"))).toList());
            assertToolDefinition(actual, "database_gateway_search_metadata", "Search Metadata", "", "object_types", "array");
            assertToolDefinition(actual, "database_gateway_execute_query", "Execute Read-Only SQL", "sql", "timeout_ms", "integer");
            assertToolDefinition(actual, "database_gateway_execute_update", "Execute Update SQL", "sql", "timeout_ms", "integer");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertListResources(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.listResources();
            assertTrue(getResources(actual).stream().anyMatch(each -> "shardingsphere://capabilities".equals(each.get("uri"))));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertListResourceTemplates(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<String> actualTemplates = getResourceTemplates(interactionClient.listResourceTemplates()).stream()
                    .map(each -> String.valueOf(each.get("uriTemplate"))).toList();
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}"));
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}/schemas/{schema}"));
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectUnsupportedResourceUri(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource("unsupported://resource");
            assertThat(String.valueOf(actual.get("error_code")), is("json_rpc_error"));
            assertThat(String.valueOf(actual.get("message")), is("Resource not found"));
        }
    }
}

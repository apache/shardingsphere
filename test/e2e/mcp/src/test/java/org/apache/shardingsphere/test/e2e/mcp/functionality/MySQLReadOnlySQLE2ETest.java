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

package org.apache.shardingsphere.test.e2e.mcp.functionality;

import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
class MySQLReadOnlySQLE2ETest extends AbstractMySQLRuntimeE2ETest {
    
    @Override
    protected boolean useSharedRuntimeFixture() {
        return true;
    }
    
    @Test
    void assertExecuteSelect() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }
    
    @Test
    void assertExecuteSelectWithTruncation() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(((List<?>) actual.get("rows")).size(), is(1));
            assertThat(String.valueOf(actual.get("truncated")), is("true"));
        }
    }
    
    @Test
    void assertExecuteExplainSelect() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_explain_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT * FROM orders WHERE order_id = 1",
                            "explain_sql", "EXPLAIN SELECT * FROM orders WHERE order_id = 1", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(String.valueOf(actual.get("statement_type")), is("EXPLAIN"));
            assertFalse(((List<?>) actual.get("rows")).isEmpty());
        }
    }
    
    @Test
    void assertExecuteQueryTimeout() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT SLEEP(2)", "timeout_ms", 1));
            assertRecoveryResponse(actual);
        }
    }
    
    @Test
    void assertRejectSequenceResource() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/sequences", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME));
            assertThat(String.valueOf(actual.get("error_code")), is("json_rpc_error"));
            assertThat(String.valueOf(actual.get("message")), is("Sequence resources are not supported for the current database."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("allTransportCases")
    void assertAiNativeDeterministicInteractionLoop(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertAiNativeGuidance(interactionClient.readResource("shardingsphere://guidance"));
            assertAiNativeDiscovery(interactionClient);
            Map<String, Object> searchMetadataPayload = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "orders", "object_types", List.of("table")));
            Map<String, Object> tableHit = MCPPayloadAssertions.findItem(searchMetadataPayload, "name", "orders");
            String tableResourceUri = String.valueOf(getObjectOrEmpty(tableHit.get("resource")).get("uri"));
            assertThat(tableResourceUri, is("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders"));
            assertFalse(getRequiredObjectList(tableHit.get("next_resources")).isEmpty());
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(tableResourceUri), "table", "orders");
            assertAiNativeSqlPreview(interactionClient);
            assertAiNativeSqlResult(interactionClient);
        }
    }
}

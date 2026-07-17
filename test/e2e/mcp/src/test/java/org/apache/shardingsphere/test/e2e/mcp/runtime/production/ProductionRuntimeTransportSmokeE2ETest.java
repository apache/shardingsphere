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
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@EnabledIf("isEnabled")
class ProductionRuntimeTransportSmokeE2ETest extends AbstractProductionMySQLRuntimeE2ETest {
    
    @Override
    protected boolean useSharedRuntimeFixture() {
        return true;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dualTransports")
    void assertTransportLifecycleWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.sendRawNotification("notifications/cancelled", Map.of("requestId", "unknown-request", "reason", "race tolerance"));
            assertThat(getObjectOrEmpty(interactionClient.sendRawRequest("ping-1", "ping", Map.of()).get("result")), is(Map.of()));
            interactionClient.sendRawNotification("notifications/cancelled", Map.of("requestId", "ping-1", "reason", "request already completed"));
            assertThat(getObjectOrEmpty(interactionClient.sendRawRequest("ping-2", "ping", Map.of()).get("result")), is(Map.of()));
            assertOfficialToolNames(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList());
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource("shardingsphere://databases"), "database", LOGICAL_DATABASE_NAME);
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders WHERE order_id = 1", "max_rows", 1));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }
}

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
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("isEnabled")
class ProductionH2AiNativeInteractionE2ETest extends ProductionH2RuntimeSmokeE2ETest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertAiNativeDeterministicInteractionLoop(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertAiNativeCapabilities(interactionClient.readResource("shardingsphere://capabilities"));
            assertAiNativeDiscovery(interactionClient);
            Map<String, Object> searchMetadataPayload = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", "logic_db", "schema", "public", "query", "orders", "object_types", List.of("table")));
            Map<String, Object> tableHit = MCPPayloadAssertions.findItem(searchMetadataPayload, "name", "orders");
            String tableResourceUri = String.valueOf(getMap(tableHit.get("resource")).get("uri"));
            assertThat(tableResourceUri, is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
            assertFalse(getMapList(tableHit.get("next_resources")).isEmpty());
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(tableResourceUri), "table", "orders");
            assertAiNativeSqlPreview(interactionClient);
            assertAiNativeSqlResult(interactionClient);
        }
    }
}

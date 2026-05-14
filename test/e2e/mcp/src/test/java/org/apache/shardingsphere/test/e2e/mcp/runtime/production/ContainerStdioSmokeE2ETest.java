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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.DockerImageStdioInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs({OS.LINUX, OS.MAC, OS.WINDOWS})
@EnabledIf("isEnabled")
class ContainerStdioSmokeE2ETest {

    private static final String IMAGE_PROPERTY = "mcp.e2e.container.image";

    private static boolean isEnabled() {
        return !System.getProperty(IMAGE_PROPERTY, "").isBlank();
    }

    @Test
    void assertLaunchContainerOverStdio() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = new DockerImageStdioInteractionClient(System.getProperty(IMAGE_PROPERTY))) {
            interactionClient.open();
            assertSupportedTools(interactionClient.listTools());
            assertTrue(interactionClient.readResource("shardingsphere://capabilities").containsKey("supportedTools"));
            Map<String, Object> actualResult = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", "orders", "query", "order", "object_types", List.of("table")));
            assertThat(getItemNames(actualResult), hasItems("orders"));
        }
    }

    private void assertSupportedTools(final List<Map<String, Object>> actualTools) {
        assertThat(actualTools.stream().map(each -> String.valueOf(each.get("name"))).toList(),
                hasItems("database_gateway_search_metadata", "database_gateway_execute_query"));
    }

    private List<String> getItemNames(final Map<String, Object> payload) {
        return MCPInteractionPayloads.castToList(payload.get("items")).stream().map(each -> String.valueOf(each.get("name"))).toList();
    }
}

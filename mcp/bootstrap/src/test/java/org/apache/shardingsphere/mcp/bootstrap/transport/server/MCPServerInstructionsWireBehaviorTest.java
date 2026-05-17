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

package org.apache.shardingsphere.mcp.bootstrap.transport.server;

import org.apache.shardingsphere.mcp.bootstrap.transport.AbstractMCPWireBehaviorTest;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.support.markdown.MCPMarkdownResourceLoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MCPServerInstructionsWireBehaviorTest extends AbstractMCPWireBehaviorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertInitializeExposesMarkdownInstructions(final String name, final MCPWireClientFactory clientFactory) throws Exception {
        try (MCPWireClient client = clientFactory.create()) {
            Map<String, Object> actualResult = getMap(client.getInitializePayload().get("result"));
            String actualInstructions = String.valueOf(actualResult.get("instructions"));
            assertThat(actualInstructions, is(MCPMarkdownResourceLoader.loadRequired(MCPTransportConstants.SERVER_INSTRUCTIONS_RESOURCE, "server instruction")));
            assertThat(actualInstructions.lines().findFirst().orElse(""), is("Apache ShardingSphere MCP."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertServerInstructionsAreNotListedAsResource(final String name, final MCPWireClientFactory clientFactory) throws Exception {
        try (MCPWireClient client = clientFactory.create()) {
            Object actualResources = getMap(client.sendRawRequest("resources-list-instructions-1", "resources/list", Map.of()).get("result")).get("resources");
            assertThat(actualResources, isA(List.class));
            assertFalse(containsServerInstructionsResource((List<?>) actualResources));
        }
    }
    
    private boolean containsServerInstructionsResource(final List<?> resources) {
        for (Object each : resources) {
            if (each instanceof Map && MCPTransportConstants.SERVER_INSTRUCTIONS_RESOURCE.equals(String.valueOf(((Map<?, ?>) each).get("uri")))) {
                return true;
            }
        }
        return false;
    }
}

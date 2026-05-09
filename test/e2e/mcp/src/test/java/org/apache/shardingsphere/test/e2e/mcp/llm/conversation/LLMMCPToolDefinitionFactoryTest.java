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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LLMMCPToolDefinitionFactoryTest {

    @Test
    void assertOfficialToolDefinitionsUseProductionDescriptors() {
        List<Map<String, Object>> actual = new LLMMCPToolDefinitionFactory().create(List.of("execute_update", "apply_workflow"));
        assertApprovalField(findTool(actual, "execute_update"));
        assertApprovalField(findTool(actual, "apply_workflow"));
    }

    @Test
    void assertProtocolBridgeToolDefinitionsKeepBridgeSchemas() {
        List<Map<String, Object>> actual = new LLMMCPToolDefinitionFactory().create(List.of(MCPInteractionActionNames.LIST_RESOURCES, MCPInteractionActionNames.READ_RESOURCE));
        Map<?, ?> listResourcesParameters = getParameters(findTool(actual, MCPInteractionActionNames.LIST_RESOURCES));
        assertThat(listResourcesParameters.get("properties"), is(Map.of()));
        Map<?, ?> readResourceParameters = getParameters(findTool(actual, MCPInteractionActionNames.READ_RESOURCE));
        assertThat(((Map<?, ?>) ((Map<?, ?>) readResourceParameters.get("properties")).get("uri")).get("type"), is("string"));
        assertThat(readResourceParameters.get("required"), is(List.of("uri")));
    }

    private void assertApprovalField(final Map<?, ?> toolDefinition) {
        Map<?, ?> parameters = getParameters(toolDefinition);
        Map<?, ?> approvedByUser = (Map<?, ?>) ((Map<?, ?>) parameters.get("properties")).get("approved_by_user");
        assertThat(approvedByUser.get("type"), is("boolean"));
        assertFalse(((List<?>) parameters.get("required")).contains("approved_by_user"));
    }

    private Map<?, ?> getParameters(final Map<?, ?> toolDefinition) {
        return (Map<?, ?>) ((Map<?, ?>) toolDefinition.get("function")).get("parameters");
    }

    private Map<?, ?> findTool(final List<Map<String, Object>> toolDefinitions, final String toolName) {
        return toolDefinitions.stream()
                .filter(each -> toolName.equals(((Map<?, ?>) each.get("function")).get("name")))
                .findFirst()
                .orElseThrow();
    }
}

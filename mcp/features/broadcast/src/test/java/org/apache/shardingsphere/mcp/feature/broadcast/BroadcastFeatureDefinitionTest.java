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

package org.apache.shardingsphere.mcp.feature.broadcast;

import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BroadcastFeatureDefinitionTest {
    
    @Test
    void assertConstants() {
        assertThat(BroadcastFeatureDefinition.WORKFLOW_KIND.getValue(), is("broadcast.rule"));
        assertThat(BroadcastFeatureDefinition.PLAN_TOOL_NAME, is("database_gateway_plan_broadcast_rule"));
        assertThat(BroadcastFeatureDefinition.PLAN_PROMPT_NAME, is("plan_broadcast_rule"));
        assertThat(BroadcastFeatureDefinition.TABLES_FIELD, is("tables"));
        assertThat(BroadcastFeatureDefinition.RULES_RESOURCE_URI, is("shardingsphere://features/broadcast/databases/{database}/rules"));
        assertThat(BroadcastFeatureDefinition.RULE_COUNT_RESOURCE_URI, is("shardingsphere://features/broadcast/databases/{database}/rule-count"));
    }
    
    @Test
    void assertPromptCompletionArguments() {
        MCPCompletionTargetDescriptor actual = MCPDescriptorCatalogLoader.load().getShardingSphereDescriptors().getCompletionTargetDescriptors().stream()
                .filter(each -> "prompt".equals(each.getReferenceType()) && BroadcastFeatureDefinition.PLAN_PROMPT_NAME.equals(each.getReference())).findFirst().orElseThrow();
        assertThat(actual.getArguments(), is(List.of("database", "table", "plan_id")));
    }
}

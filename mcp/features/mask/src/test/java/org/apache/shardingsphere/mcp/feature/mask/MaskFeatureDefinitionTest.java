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

package org.apache.shardingsphere.mcp.feature.mask;

import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MaskFeatureDefinitionTest {
    
    @Test
    void assertConstants() {
        assertThat(MaskFeatureDefinition.WORKFLOW_KIND.getValue(), is("mask.rule"));
        assertThat(MaskFeatureDefinition.PLAN_TOOL_NAME, is("database_gateway_plan_mask_rule"));
        assertThat(MaskFeatureDefinition.PLAN_PROMPT_NAME, is("plan_mask_rule"));
        assertThat(MaskFeatureDefinition.ALGORITHMS_RESOURCE_URI, is("shardingsphere://features/mask/algorithms"));
        assertThat(MaskFeatureDefinition.RULES_RESOURCE_URI, is("shardingsphere://features/mask/databases/{database}/rules"));
        assertThat(MaskFeatureDefinition.RULE_RESOURCE_URI, is("shardingsphere://features/mask/databases/{database}/tables/{table}/rules"));
    }
    
    @Test
    void assertPromptCompletionArguments() {
        MCPCompletionTargetDescriptor actual = MCPDescriptorCatalogLoader.load().getShardingSphereDescriptors().getCompletionTargetDescriptors().stream()
                .filter(each -> "prompt".equals(each.getReferenceType()) && MaskFeatureDefinition.PLAN_PROMPT_NAME.equals(each.getReference())).findFirst().orElseThrow();
        assertThat(actual.getArguments(), is(List.of("database", "schema", "table", "column", "algorithm_type", "plan_id")));
    }
}

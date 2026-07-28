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

package org.apache.shardingsphere.mcp.feature.readwritesplitting;

import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReadwriteSplittingFeatureDefinitionTest {
    
    @Test
    void assertWorkflowKinds() {
        assertThat(ReadwriteSplittingFeatureDefinition.RULE_WORKFLOW_KIND.getValue(), is("readwrite.rule"));
        assertThat(ReadwriteSplittingFeatureDefinition.STATUS_WORKFLOW_KIND.getValue(), is("readwrite.status"));
    }
    
    @Test
    void assertPromptCompletionArguments() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        assertCompletionTargetArguments(catalog, ReadwriteSplittingFeatureDefinition.PLAN_RULE_PROMPT_NAME, "database", "write_storage_unit", "load_balancer_type", "plan_id");
        assertCompletionTargetArguments(catalog, ReadwriteSplittingFeatureDefinition.PLAN_STATUS_PROMPT_NAME, "database", "storage_unit", "plan_id");
    }
    
    private void assertCompletionTargetArguments(final MCPDescriptorCatalog catalog, final String promptName, final String... expectedArguments) {
        MCPCompletionTargetDescriptor actual = catalog.getShardingSphereDescriptors().getCompletionTargetDescriptors().stream()
                .filter(each -> "prompt".equals(each.getReferenceType()) && promptName.equals(each.getReference())).findFirst().orElseThrow();
        assertThat(actual.getArguments(), is(List.of(expectedArguments)));
    }
}

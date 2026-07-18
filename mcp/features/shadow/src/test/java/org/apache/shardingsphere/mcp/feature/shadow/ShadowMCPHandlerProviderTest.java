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

package org.apache.shardingsphere.mcp.feature.shadow;

import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

class ShadowMCPHandlerProviderTest {
    
    @Test
    void assertGetResourceHandlers() {
        assertThat(new ShadowMCPHandlerProvider().getResourceHandlers().size(), is(8));
    }
    
    @Test
    void assertGetToolHandlers() {
        assertThat(new ShadowMCPHandlerProvider().getToolHandlers().stream().map(MCPToolHandler::getToolName).toList(),
                containsInAnyOrder(ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME,
                        ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_TOOL_NAME, ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_TOOL_NAME));
    }
    
    @Test
    void assertGetWorkflowDefinitions() {
        Collection<WorkflowRuntimeDefinition> actual = new ShadowMCPHandlerProvider().getWorkflowDefinitions();
        assertThat(actual.stream().map(each -> each.getWorkflowKind().getValue()).toList(),
                containsInAnyOrder("shadow.rule", "shadow.default", "shadow.cleanup"));
    }
}

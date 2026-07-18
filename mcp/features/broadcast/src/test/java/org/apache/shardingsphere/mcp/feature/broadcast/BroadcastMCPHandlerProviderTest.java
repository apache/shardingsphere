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

import org.apache.shardingsphere.mcp.spi.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.service.BroadcastWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcastMCPHandlerProviderTest {
    
    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new BroadcastMCPHandlerProvider().getResourceHandlers();
        assertThat(actual.stream().map(MCPResourceHandler::getResourceUriTemplate).toList(), is(List.of(
                BroadcastFeatureDefinition.RULES_RESOURCE_URI,
                BroadcastFeatureDefinition.TABLE_RULE_RESOURCE_URI,
                BroadcastFeatureDefinition.RULE_COUNT_RESOURCE_URI)));
        assertTrue(actual.stream().allMatch(each -> MCPFeatureRequestContext.class.equals(each.getContextType())));
    }
    
    @Test
    void assertGetToolHandlers() {
        MCPToolHandler<?> actual = new BroadcastMCPHandlerProvider().getToolHandlers().iterator().next();
        assertThat(actual.getToolName(), is(BroadcastFeatureDefinition.PLAN_TOOL_NAME));
    }
    
    @Test
    void assertGetCompletionHandlers() {
        assertTrue(new BroadcastMCPHandlerProvider().getCompletionHandlers().isEmpty());
    }
    
    @Test
    void assertGetWorkflowDefinitions() {
        WorkflowRuntimeDefinition actual = new BroadcastMCPHandlerProvider().getWorkflowDefinitions().iterator().next();
        assertThat(actual.getWorkflowKind(), is(BroadcastFeatureDefinition.WORKFLOW_KIND));
        assertThat(actual.getApplySynchronizationHandler(), isA(BroadcastWorkflowValidationService.class));
        assertThat(actual.getValidationHandler(), isA(BroadcastWorkflowValidationService.class));
    }
    
    @Test
    void assertSPIRegistration() {
        assertTrue(ServiceLoader.load(MCPHandlerProvider.class).stream().anyMatch(each -> BroadcastMCPHandlerProvider.class.equals(each.type())));
    }
}

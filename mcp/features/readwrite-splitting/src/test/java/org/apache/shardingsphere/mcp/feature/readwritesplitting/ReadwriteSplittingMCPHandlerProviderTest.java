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

import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingRuleWorkflowValidationService;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingStatusWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingMCPHandlerProviderTest {
    
    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new ReadwriteSplittingMCPHandlerProvider().getResourceHandlers();
        assertThat(actual.stream().map(MCPResourceHandler::getResourceUriTemplate).toList(), containsInAnyOrder(
                ReadwriteSplittingFeatureDefinition.RULES_RESOURCE_URI,
                ReadwriteSplittingFeatureDefinition.RULE_RESOURCE_URI,
                ReadwriteSplittingFeatureDefinition.STATUS_RESOURCE_URI,
                ReadwriteSplittingFeatureDefinition.RULE_STATUS_RESOURCE_URI,
                ReadwriteSplittingFeatureDefinition.RULE_COUNT_RESOURCE_URI,
                ReadwriteSplittingFeatureDefinition.LOAD_BALANCE_ALGORITHM_PLUGINS_RESOURCE_URI));
        assertTrue(actual.stream().allMatch(each -> MCPDatabaseHandlerContext.class.equals(each.getContextType())));
    }
    
    @Test
    void assertGetToolHandlers() {
        List<String> actual = new ReadwriteSplittingMCPHandlerProvider().getToolHandlers().stream().map(MCPToolHandler::getToolName).toList();
        assertThat(actual, is(List.of(ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME, ReadwriteSplittingFeatureDefinition.PLAN_STATUS_TOOL_NAME)));
    }
    
    @Test
    void assertGetWorkflowDefinitions() {
        List<WorkflowRuntimeDefinition> actual = new ReadwriteSplittingMCPHandlerProvider().getWorkflowDefinitions().stream().toList();
        assertThat(actual.get(0).getWorkflowKind(), is(ReadwriteSplittingFeatureDefinition.RULE_WORKFLOW_KIND));
        assertThat(actual.get(0).getValidationHandler(), isA(ReadwriteSplittingRuleWorkflowValidationService.class));
        assertThat(actual.get(1).getWorkflowKind(), is(ReadwriteSplittingFeatureDefinition.STATUS_WORKFLOW_KIND));
        assertThat(actual.get(1).getValidationHandler(), isA(ReadwriteSplittingStatusWorkflowValidationService.class));
    }
    
    @Test
    void assertSPIRegistration() {
        assertTrue(ServiceLoader.load(MCPHandlerProvider.class).stream().anyMatch(each -> ReadwriteSplittingMCPHandlerProvider.class.equals(each.type())));
    }
}

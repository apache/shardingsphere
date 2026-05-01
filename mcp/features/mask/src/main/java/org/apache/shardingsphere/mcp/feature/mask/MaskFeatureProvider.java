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

import org.apache.shardingsphere.mcp.feature.mask.resource.handler.MaskAlgorithmsHandler;
import org.apache.shardingsphere.mcp.feature.mask.resource.handler.MaskRuleHandler;
import org.apache.shardingsphere.mcp.feature.mask.resource.handler.MaskRulesHandler;
import org.apache.shardingsphere.mcp.feature.mask.tool.handler.PlanMaskRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowValidationService;
import org.apache.shardingsphere.mcp.feature.spi.MCPContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectResourceContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.workflow.spi.MCPWorkflowToolContribution;
import org.apache.shardingsphere.mcp.resource.ResourceHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Mask MCP feature provider.
 */
public final class MaskFeatureProvider implements MCPFeatureProvider {
    
    @Override
    public Collection<MCPContribution> getContributions() {
        Collection<MCPContribution> result = new LinkedList<>();
        result.add(createWorkflowContribution());
        result.addAll(createResourceContributions());
        return List.copyOf(result);
    }
    
    private static MCPWorkflowToolContribution createWorkflowContribution() {
        PlanMaskRuleToolHandler planToolHandler = new PlanMaskRuleToolHandler();
        MaskWorkflowValidationService workflowValidationService = new MaskWorkflowValidationService();
        return new MCPWorkflowToolContribution(planToolHandler.getToolDescriptor(), planToolHandler::handle,
                MaskFeatureDefinition.APPLY_TOOL_NAME, MaskFeatureDefinition.VALIDATE_TOOL_NAME, workflowValidationService, workflowValidationService);
    }
    
    private static Collection<MCPContribution> createResourceContributions() {
        return List.of(
                createResourceContribution(new MaskAlgorithmsHandler()),
                createResourceContribution(new MaskRulesHandler()),
                createResourceContribution(new MaskRuleHandler()));
    }
    
    private static MCPDirectResourceContribution createResourceContribution(final ResourceHandler resourceHandler) {
        return new MCPDirectResourceContribution(resourceHandler.getUriPattern(), resourceHandler::handle);
    }
}

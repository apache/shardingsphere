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

package org.apache.shardingsphere.mcp.feature.encrypt;

import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.spi.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.completion.EncryptAlgorithmCompletionHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.resource.handler.EncryptAlgorithmsHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.resource.handler.EncryptRuleHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.resource.handler.EncryptRulesHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.handler.PlanEncryptRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.Collection;
import java.util.List;

/**
 * Encrypt MCP handler provider.
 */
public final class EncryptMCPHandlerProvider implements MCPHandlerProvider, MCPWorkflowDefinitionProvider {
    
    @Override
    public Collection<MCPResourceHandler<?>> getResourceHandlers() {
        return List.of(new EncryptAlgorithmsHandler(), new EncryptRulesHandler(), new EncryptRuleHandler());
    }
    
    @Override
    public Collection<MCPToolHandler<?>> getToolHandlers() {
        return List.of(new PlanEncryptRuleToolHandler());
    }
    
    @Override
    public Collection<MCPCompletionHandler<?>> getCompletionHandlers() {
        return List.of(new EncryptAlgorithmCompletionHandler());
    }
    
    @Override
    public Collection<WorkflowRuntimeDefinition> getWorkflowDefinitions() {
        return List.of(new WorkflowRuntimeDefinition(EncryptFeatureDefinition.WORKFLOW_KIND, new EncryptWorkflowValidationService()));
    }
}

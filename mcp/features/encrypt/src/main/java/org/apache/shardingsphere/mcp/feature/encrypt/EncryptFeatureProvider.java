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

import org.apache.shardingsphere.mcp.feature.encrypt.resource.handler.EncryptAlgorithmsHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.resource.handler.EncryptRuleHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.resource.handler.EncryptRulesHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.handler.PlanEncryptRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowValidationService;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.workflow.WorkflowExecutionToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.workflow.WorkflowValidationToolHandler;

import java.util.Collection;
import java.util.List;

/**
 * Encrypt MCP feature provider.
 */
public final class EncryptFeatureProvider implements MCPFeatureProvider {
    
    @Override
    public Collection<ToolHandler> getToolHandlers() {
        return List.of(new PlanEncryptRuleToolHandler(),
                new WorkflowExecutionToolHandler(EncryptFeatureDefinition.APPLY_TOOL_NAME),
                new WorkflowValidationToolHandler(EncryptFeatureDefinition.VALIDATE_TOOL_NAME, new EncryptWorkflowValidationService()::validate));
    }
    
    @Override
    public Collection<ResourceHandler> getResourceHandlers() {
        return List.of(new EncryptAlgorithmsHandler(), new EncryptRulesHandler(), new EncryptRuleHandler());
    }
}

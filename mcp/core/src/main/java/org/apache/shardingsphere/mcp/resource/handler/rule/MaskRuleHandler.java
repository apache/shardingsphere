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

package org.apache.shardingsphere.mcp.resource.handler.rule;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;
import org.apache.shardingsphere.mcp.tool.service.workflow.RuleInspectionService;

/**
 * Handler for one logical table mask rule resource URI.
 */
public final class MaskRuleHandler implements ResourceHandler {
    
    private final RuleInspectionService ruleInspectionService = new RuleInspectionService();
    
    @Override
    public String getUriPattern() {
        return "shardingsphere://databases/{database}/mask-rules/{table}";
    }
    
    @Override
    public MCPResponse handle(final MCPRuntimeContext runtimeContext, final MCPUriVariables uriVariables) {
        return new MCPMetadataResponse(ruleInspectionService.queryMaskRules(runtimeContext, uriVariables.getVariable("database"), uriVariables.getVariable("table")));
    }
}

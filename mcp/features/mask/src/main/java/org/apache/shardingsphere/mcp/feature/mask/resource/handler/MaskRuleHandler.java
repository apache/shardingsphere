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

package org.apache.shardingsphere.mcp.feature.mask.resource.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPItemsResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskRuleInspectionService;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;

/**
 * Mask rule handler.
 */
public final class MaskRuleHandler implements MCPResourceHandler<MCPDatabaseHandlerContext> {
    
    private final MaskRuleInspectionService ruleInspectionService = new MaskRuleInspectionService();
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public MCPResourceDescriptor getResourceDescriptor() {
        return MCPDescriptorRegistry.getRequiredResourceDescriptor(MaskFeatureDefinition.RULE_RESOURCE_URI);
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        return new MCPItemsResponse(ruleInspectionService.queryMaskRules(databaseContext.getQueryFacade(), uriVariables.getVariable("database"), uriVariables.getVariable("table")));
    }
}

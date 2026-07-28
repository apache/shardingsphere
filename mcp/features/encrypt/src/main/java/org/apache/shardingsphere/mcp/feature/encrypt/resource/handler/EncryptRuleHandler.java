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

package org.apache.shardingsphere.mcp.feature.encrypt.resource.handler;

import org.apache.shardingsphere.mcp.support.protocol.payload.MCPItemsPayload;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptRuleInspectionService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationPayloadBuilder;

/**
 * Encrypt rule handler.
 */
public final class EncryptRuleHandler implements MCPResourceHandler<MCPFeatureRequestContext> {
    
    private final EncryptRuleInspectionService ruleInspectionService = new EncryptRuleInspectionService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return EncryptFeatureDefinition.RULE_RESOURCE_URI;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final MCPResourceURIVariables uriVariables) {
        return new MCPItemsPayload(ruleInspectionService.queryEncryptRules(requestContext.getQueryFacade(), uriVariables.getValue("database"), uriVariables.getValue("table")),
                MCPResourceNavigationPayloadBuilder.create(
                        MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(getResourceUriTemplate()), uriVariables, EncryptFeatureDefinition.RULES_RESOURCE_URI));
    }
}

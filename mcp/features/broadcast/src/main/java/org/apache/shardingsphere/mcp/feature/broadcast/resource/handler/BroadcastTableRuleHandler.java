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

package org.apache.shardingsphere.mcp.feature.broadcast.resource.handler;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.feature.broadcast.BroadcastFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.service.BroadcastRuleInspectionService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationPayloadBuilder;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPItemsPayload;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRuleValueUtils;

import java.util.List;
import java.util.Map;

/**
 * Broadcast table rule handler.
 */
public final class BroadcastTableRuleHandler implements MCPResourceHandler<MCPFeatureRequestContext> {
    
    private final BroadcastRuleInspectionService ruleInspectionService = new BroadcastRuleInspectionService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return BroadcastFeatureDefinition.TABLE_RULE_RESOURCE_URI;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final MCPResourceURIVariables uriVariables) {
        String database = uriVariables.getValue("database");
        String table = uriVariables.getValue("table");
        MCPFeatureQueryFacade queryFacade = requestContext.getQueryFacade();
        queryFacade.checkDatabaseCapability(database);
        List<Map<String, Object>> items = ruleInspectionService.queryBroadcastRules(queryFacade, database).stream()
                .filter(each -> queryFacade.isSameIdentifier(database, IdentifierScope.TABLE, table, WorkflowRuleValueUtils.getRuleValue(each, "broadcast_table"))).toList();
        return new MCPItemsPayload(items, MCPResourceNavigationPayloadBuilder.create(
                MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(getResourceUriTemplate()), uriVariables, BroadcastFeatureDefinition.RULES_RESOURCE_URI));
    }
}

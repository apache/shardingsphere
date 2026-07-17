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

package org.apache.shardingsphere.mcp.feature.shadow.resource.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowInspectionService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationPayloadBuilder;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPItemsPayload;

import java.util.List;
import java.util.Map;

/**
 * Shadow resource handler.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowResourceHandler implements MCPResourceHandler<MCPFeatureRequestContext> {
    
    private final String resourceUriTemplate;
    
    private final ResourceKind resourceKind;
    
    private final ShadowInspectionService inspectionService = new ShadowInspectionService();
    
    /**
     * Create rules resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler rules() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.RULES_RESOURCE_URI, ResourceKind.RULES);
    }
    
    /**
     * Create single rule resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler rule() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.RULE_RESOURCE_URI, ResourceKind.RULE);
    }
    
    /**
     * Create table rules resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler tableRules() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.TABLE_RULES_RESOURCE_URI, ResourceKind.TABLE_RULES);
    }
    
    /**
     * Create table-specific rules resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler tableRule() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.TABLE_RULE_RESOURCE_URI, ResourceKind.TABLE_RULE);
    }
    
    /**
     * Create algorithms resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler algorithms() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.ALGORITHMS_RESOURCE_URI, ResourceKind.ALGORITHMS);
    }
    
    /**
     * Create default algorithm resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler defaultAlgorithm() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.DEFAULT_ALGORITHM_RESOURCE_URI, ResourceKind.DEFAULT_ALGORITHM);
    }
    
    /**
     * Create rule count resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler ruleCount() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.RULE_COUNT_RESOURCE_URI, ResourceKind.RULE_COUNT);
    }
    
    /**
     * Create algorithm plugins resource handler.
     *
     * @return resource handler
     */
    public static ShadowResourceHandler algorithmPlugins() {
        return new ShadowResourceHandler(ShadowFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI, ResourceKind.ALGORITHM_PLUGINS);
    }
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return resourceUriTemplate;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final MCPUriVariables uriVariables) {
        return new MCPItemsPayload(query(requestContext, uriVariables),
                MCPResourceNavigationPayloadBuilder.create(MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(getResourceUriTemplate()), uriVariables));
    }
    
    private List<Map<String, Object>> query(final MCPFeatureRequestContext requestContext, final MCPUriVariables uriVariables) {
        return switch (resourceKind) {
            case RULES -> inspectionService.queryRules(requestContext.getQueryFacade(), uriVariables.getValue("database"));
            case RULE -> inspectionService.queryRule(requestContext.getQueryFacade(), uriVariables.getValue("database"), uriVariables.getValue(ShadowFeatureDefinition.RULE_FIELD));
            case TABLE_RULES -> inspectionService.queryTableRules(requestContext.getQueryFacade(), uriVariables.getValue("database"));
            case TABLE_RULE -> inspectionService.queryTableRule(requestContext.getQueryFacade(), uriVariables.getValue("database"), uriVariables.getValue(ShadowFeatureDefinition.TABLE_FIELD));
            case ALGORITHMS -> inspectionService.queryAlgorithms(requestContext.getQueryFacade(), uriVariables.getValue("database"));
            case DEFAULT_ALGORITHM -> inspectionService.queryDefaultAlgorithm(requestContext.getQueryFacade(), uriVariables.getValue("database"));
            case RULE_COUNT -> inspectionService.queryRuleCount(requestContext.getQueryFacade(), uriVariables.getValue("database"));
            case ALGORITHM_PLUGINS -> inspectionService.queryAlgorithmPlugins(requestContext.getQueryFacade());
        };
    }
    
    enum ResourceKind {
        
        RULES, RULE, TABLE_RULES, TABLE_RULE, ALGORITHMS, DEFAULT_ALGORITHM, RULE_COUNT, ALGORITHM_PLUGINS
    }
}

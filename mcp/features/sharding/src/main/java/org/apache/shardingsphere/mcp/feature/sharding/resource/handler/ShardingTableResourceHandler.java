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

package org.apache.shardingsphere.mcp.feature.sharding.resource.handler;

import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;

import java.util.List;
import java.util.Map;

/**
 * Sharding table resource handler.
 */
public final class ShardingTableResourceHandler extends AbstractShardingResourceHandler {
    
    private final ResourceKind resourceKind;
    
    private ShardingTableResourceHandler(final String resourceUriTemplate, final ResourceKind resourceKind) {
        super(resourceUriTemplate);
        this.resourceKind = resourceKind;
    }
    
    /**
     * Create handler for sharding table rule resources.
     *
     * @return sharding table resource handler
     */
    public static ShardingTableResourceHandler tableRules() {
        return new ShardingTableResourceHandler(ShardingFeatureDefinition.TABLE_RULES_RESOURCE_URI, ResourceKind.TABLE_RULES);
    }
    
    /**
     * Create handler for a sharding table rule resource.
     *
     * @return sharding table resource handler
     */
    public static ShardingTableResourceHandler tableRule() {
        return new ShardingTableResourceHandler(ShardingFeatureDefinition.TABLE_RULE_RESOURCE_URI, ResourceKind.TABLE_RULE);
    }
    
    /**
     * Create handler for sharding table node resources.
     *
     * @return sharding table resource handler
     */
    public static ShardingTableResourceHandler tableNodes() {
        return new ShardingTableResourceHandler(ShardingFeatureDefinition.TABLE_NODES_RESOURCE_URI, ResourceKind.TABLE_NODES);
    }
    
    /**
     * Create handler for a sharding table node resource.
     *
     * @return sharding table resource handler
     */
    public static ShardingTableResourceHandler tableNode() {
        return new ShardingTableResourceHandler(ShardingFeatureDefinition.TABLE_NODE_RESOURCE_URI, ResourceKind.TABLE_NODE);
    }
    
    /**
     * Create handler for sharding table reference rule resources.
     *
     * @return sharding table resource handler
     */
    public static ShardingTableResourceHandler tableReferenceRules() {
        return new ShardingTableResourceHandler(ShardingFeatureDefinition.TABLE_REFERENCE_RULES_RESOURCE_URI, ResourceKind.TABLE_REFERENCE_RULES);
    }
    
    /**
     * Create handler for a sharding table reference rule resource.
     *
     * @return sharding table resource handler
     */
    public static ShardingTableResourceHandler tableReferenceRule() {
        return new ShardingTableResourceHandler(ShardingFeatureDefinition.TABLE_REFERENCE_RULE_RESOURCE_URI, ResourceKind.TABLE_REFERENCE_RULE);
    }
    
    @Override
    protected List<Map<String, Object>> query(final MCPFeatureRequestContext requestContext, final MCPUriVariables uriVariables) {
        String databaseName = uriVariables.getValue("database");
        return switch (resourceKind) {
            case TABLE_RULES -> getInspectionService().queryTableRules(requestContext.getQueryFacade(), databaseName);
            case TABLE_RULE -> getInspectionService().queryTableRule(requestContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.TABLE_FIELD));
            case TABLE_NODES -> getInspectionService().queryTableNodes(requestContext.getQueryFacade(), databaseName);
            case TABLE_NODE -> getInspectionService().queryTableNode(requestContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.TABLE_FIELD));
            case TABLE_REFERENCE_RULES -> getInspectionService().queryTableReferenceRules(requestContext.getQueryFacade(), databaseName);
            case TABLE_REFERENCE_RULE -> getInspectionService().queryTableReferenceRule(requestContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.RULE_FIELD));
        };
    }
    
    enum ResourceKind {
        
        TABLE_RULES, TABLE_RULE, TABLE_NODES, TABLE_NODE, TABLE_REFERENCE_RULES, TABLE_REFERENCE_RULE
    }
}

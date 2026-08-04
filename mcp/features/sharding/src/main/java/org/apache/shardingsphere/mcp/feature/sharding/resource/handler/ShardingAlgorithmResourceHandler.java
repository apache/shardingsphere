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

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;

import java.util.List;
import java.util.Map;

/**
 * Sharding algorithm resource handler.
 */
public final class ShardingAlgorithmResourceHandler extends AbstractShardingResourceHandler {
    
    private final ResourceKind resourceKind;
    
    private ShardingAlgorithmResourceHandler(final String resourceUriTemplate, final ResourceKind resourceKind) {
        super(resourceUriTemplate);
        this.resourceKind = resourceKind;
    }
    
    /**
     * Create handler for sharding algorithm plugin resources.
     *
     * @return sharding algorithm resource handler
     */
    public static ShardingAlgorithmResourceHandler algorithmPlugins() {
        return new ShardingAlgorithmResourceHandler(ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI, ResourceKind.ALGORITHM_PLUGINS);
    }
    
    /**
     * Create handler for key generate algorithm plugin resources.
     *
     * @return sharding algorithm resource handler
     */
    public static ShardingAlgorithmResourceHandler keyGenerateAlgorithmPlugins() {
        return new ShardingAlgorithmResourceHandler(ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI, ResourceKind.KEY_GENERATE_ALGORITHM_PLUGINS);
    }
    
    /**
     * Create handler for sharding algorithm resources.
     *
     * @return sharding algorithm resource handler
     */
    public static ShardingAlgorithmResourceHandler algorithms() {
        return new ShardingAlgorithmResourceHandler(ShardingFeatureDefinition.ALGORITHMS_RESOURCE_URI, ResourceKind.ALGORITHMS);
    }
    
    /**
     * Create handler for unused sharding algorithm resources.
     *
     * @return sharding algorithm resource handler
     */
    public static ShardingAlgorithmResourceHandler unusedAlgorithms() {
        return new ShardingAlgorithmResourceHandler(ShardingFeatureDefinition.UNUSED_ALGORITHMS_RESOURCE_URI, ResourceKind.UNUSED_ALGORITHMS);
    }
    
    /**
     * Create handler for table rules that use a sharding algorithm.
     *
     * @return sharding algorithm resource handler
     */
    public static ShardingAlgorithmResourceHandler algorithmUsedTableRules() {
        return new ShardingAlgorithmResourceHandler(ShardingFeatureDefinition.ALGORITHM_USED_TABLE_RULES_RESOURCE_URI, ResourceKind.ALGORITHM_USED_TABLE_RULES);
    }
    
    @Override
    protected List<Map<String, Object>> query(final MCPFeatureRequestContext requestContext, final MCPResourceURIVariables uriVariables) {
        return switch (resourceKind) {
            case ALGORITHM_PLUGINS -> getInspectionService().queryAlgorithmPlugins(requestContext.getQueryFacade());
            case KEY_GENERATE_ALGORITHM_PLUGINS -> getInspectionService().queryKeyGenerateAlgorithmPlugins(requestContext.getQueryFacade());
            case ALGORITHMS -> getInspectionService().queryAlgorithms(requestContext.getQueryFacade(), uriVariables.getValue("database"));
            case UNUSED_ALGORITHMS -> getInspectionService().queryUnusedAlgorithms(requestContext.getQueryFacade(), uriVariables.getValue("database"));
            case ALGORITHM_USED_TABLE_RULES -> getInspectionService().queryTableRulesUsedAlgorithm(
                    requestContext.getQueryFacade(), uriVariables.getValue("database"), uriVariables.getValue(ShardingFeatureDefinition.ALGORITHM_FIELD));
        };
    }
    
    enum ResourceKind {
        
        ALGORITHM_PLUGINS, KEY_GENERATE_ALGORITHM_PLUGINS, ALGORITHMS, UNUSED_ALGORITHMS, ALGORITHM_USED_TABLE_RULES
    }
}

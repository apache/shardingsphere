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

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;

import java.util.List;
import java.util.Map;

/**
 * Sharding strategy resource handler.
 */
public final class ShardingStrategyResourceHandler extends AbstractShardingResourceHandler {
    
    private final ResourceKind resourceKind;
    
    private ShardingStrategyResourceHandler(final String resourceUriTemplate, final ResourceKind resourceKind) {
        super(resourceUriTemplate);
        this.resourceKind = resourceKind;
    }
    
    /**
     * Create handler for default sharding strategy resources.
     *
     * @return sharding strategy resource handler
     */
    public static ShardingStrategyResourceHandler defaultStrategy() {
        return new ShardingStrategyResourceHandler(ShardingFeatureDefinition.DEFAULT_STRATEGY_RESOURCE_URI, ResourceKind.DEFAULT_STRATEGY);
    }
    
    /**
     * Create handler for sharding key generator resources.
     *
     * @return sharding strategy resource handler
     */
    public static ShardingStrategyResourceHandler keyGenerators() {
        return new ShardingStrategyResourceHandler(ShardingFeatureDefinition.KEY_GENERATORS_RESOURCE_URI, ResourceKind.KEY_GENERATORS);
    }
    
    /**
     * Create handler for a sharding key generator resource.
     *
     * @return sharding strategy resource handler
     */
    public static ShardingStrategyResourceHandler keyGenerator() {
        return new ShardingStrategyResourceHandler(ShardingFeatureDefinition.KEY_GENERATOR_RESOURCE_URI, ResourceKind.KEY_GENERATOR);
    }
    
    /**
     * Create handler for sharding key generate strategy resources.
     *
     * @return sharding strategy resource handler
     */
    public static ShardingStrategyResourceHandler keyGenerateStrategies() {
        return new ShardingStrategyResourceHandler(ShardingFeatureDefinition.KEY_GENERATE_STRATEGIES_RESOURCE_URI, ResourceKind.KEY_GENERATE_STRATEGIES);
    }
    
    /**
     * Create handler for a sharding key generate strategy resource.
     *
     * @return sharding strategy resource handler
     */
    public static ShardingStrategyResourceHandler keyGenerateStrategy() {
        return new ShardingStrategyResourceHandler(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_RESOURCE_URI, ResourceKind.KEY_GENERATE_STRATEGY);
    }
    
    /**
     * Create handler for unused sharding key generator resources.
     *
     * @return sharding strategy resource handler
     */
    public static ShardingStrategyResourceHandler unusedKeyGenerators() {
        return new ShardingStrategyResourceHandler(ShardingFeatureDefinition.UNUSED_KEY_GENERATORS_RESOURCE_URI, ResourceKind.UNUSED_KEY_GENERATORS);
    }
    
    /**
     * Create handler for table rules that use a sharding key generator.
     *
     * @return sharding strategy resource handler
     */
    public static ShardingStrategyResourceHandler keyGeneratorUsedTableRules() {
        return new ShardingStrategyResourceHandler(ShardingFeatureDefinition.KEY_GENERATOR_USED_TABLE_RULES_RESOURCE_URI, ResourceKind.KEY_GENERATOR_USED_TABLE_RULES);
    }
    
    @Override
    protected List<Map<String, Object>> query(final MCPFeatureRequestContext requestContext, final MCPUriVariables uriVariables) {
        String databaseName = uriVariables.getValue("database");
        return switch (resourceKind) {
            case DEFAULT_STRATEGY -> getInspectionService().queryDefaultStrategy(requestContext.getQueryFacade(), databaseName);
            case KEY_GENERATORS -> getInspectionService().queryKeyGenerators(requestContext.getQueryFacade(), databaseName);
            case KEY_GENERATOR -> getInspectionService().queryKeyGenerator(requestContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.KEY_GENERATOR_FIELD));
            case KEY_GENERATE_STRATEGIES -> getInspectionService().queryKeyGenerateStrategies(requestContext.getQueryFacade(), databaseName);
            case KEY_GENERATE_STRATEGY -> getInspectionService().queryKeyGenerateStrategy(
                    requestContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.STRATEGY_FIELD));
            case UNUSED_KEY_GENERATORS -> getInspectionService().queryUnusedKeyGenerators(requestContext.getQueryFacade(), databaseName);
            case KEY_GENERATOR_USED_TABLE_RULES -> getInspectionService().queryTableRulesUsedKeyGenerator(
                    requestContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.KEY_GENERATOR_FIELD));
        };
    }
    
    enum ResourceKind {
        
        DEFAULT_STRATEGY, KEY_GENERATORS, KEY_GENERATOR, KEY_GENERATE_STRATEGIES, KEY_GENERATE_STRATEGY, UNUSED_KEY_GENERATORS, KEY_GENERATOR_USED_TABLE_RULES
    }
}

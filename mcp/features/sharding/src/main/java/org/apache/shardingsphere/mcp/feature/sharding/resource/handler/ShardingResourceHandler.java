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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingInspectionService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPResourceNavigationPayloadBuilder;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPItemsResponse;

import java.util.List;
import java.util.Map;

/**
 * Sharding resource handler.
 */
public final class ShardingResourceHandler implements MCPResourceHandler<MCPDatabaseHandlerContext> {
    
    private final String resourceUriTemplate;
    
    private final ResourceKind resourceKind;
    
    private final ShardingInspectionService inspectionService;
    
    private ShardingResourceHandler(final String resourceUriTemplate, final ResourceKind resourceKind) {
        this(resourceUriTemplate, resourceKind, new ShardingInspectionService());
    }
    
    ShardingResourceHandler(final String resourceUriTemplate, final ResourceKind resourceKind, final ShardingInspectionService inspectionService) {
        this.resourceUriTemplate = resourceUriTemplate;
        this.resourceKind = resourceKind;
        this.inspectionService = inspectionService;
    }
    
    /**
     * Create handler for sharding algorithm plugin resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler algorithmPlugins() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI, ResourceKind.ALGORITHM_PLUGINS);
    }
    
    /**
     * Create handler for key generate algorithm plugin resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler keyGenerateAlgorithmPlugins() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI, ResourceKind.KEY_GENERATE_ALGORITHM_PLUGINS);
    }
    
    /**
     * Create handler for sharding algorithm resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler algorithms() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.ALGORITHMS_RESOURCE_URI, ResourceKind.ALGORITHMS);
    }
    
    /**
     * Create handler for sharding table rule resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler tableRules() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.TABLE_RULES_RESOURCE_URI, ResourceKind.TABLE_RULES);
    }
    
    /**
     * Create handler for a sharding table rule resource.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler tableRule() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.TABLE_RULE_RESOURCE_URI, ResourceKind.TABLE_RULE);
    }
    
    /**
     * Create handler for sharding table node resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler tableNodes() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.TABLE_NODES_RESOURCE_URI, ResourceKind.TABLE_NODES);
    }
    
    /**
     * Create handler for a sharding table node resource.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler tableNode() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.TABLE_NODE_RESOURCE_URI, ResourceKind.TABLE_NODE);
    }
    
    /**
     * Create handler for sharding table reference rule resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler tableReferenceRules() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.TABLE_REFERENCE_RULES_RESOURCE_URI, ResourceKind.TABLE_REFERENCE_RULES);
    }
    
    /**
     * Create handler for a sharding table reference rule resource.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler tableReferenceRule() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.TABLE_REFERENCE_RULE_RESOURCE_URI, ResourceKind.TABLE_REFERENCE_RULE);
    }
    
    /**
     * Create handler for default sharding strategy resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler defaultStrategy() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.DEFAULT_STRATEGY_RESOURCE_URI, ResourceKind.DEFAULT_STRATEGY);
    }
    
    /**
     * Create handler for sharding key generator resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler keyGenerators() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.KEY_GENERATORS_RESOURCE_URI, ResourceKind.KEY_GENERATORS);
    }
    
    /**
     * Create handler for a sharding key generator resource.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler keyGenerator() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.KEY_GENERATOR_RESOURCE_URI, ResourceKind.KEY_GENERATOR);
    }
    
    /**
     * Create handler for sharding key generate strategy resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler keyGenerateStrategies() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.KEY_GENERATE_STRATEGIES_RESOURCE_URI, ResourceKind.KEY_GENERATE_STRATEGIES);
    }
    
    /**
     * Create handler for a sharding key generate strategy resource.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler keyGenerateStrategy() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_RESOURCE_URI, ResourceKind.KEY_GENERATE_STRATEGY);
    }
    
    /**
     * Create handler for sharding auditor resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler auditors() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.AUDITORS_RESOURCE_URI, ResourceKind.AUDITORS);
    }
    
    /**
     * Create handler for unused sharding algorithm resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler unusedAlgorithms() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.UNUSED_ALGORITHMS_RESOURCE_URI, ResourceKind.UNUSED_ALGORITHMS);
    }
    
    /**
     * Create handler for unused sharding key generator resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler unusedKeyGenerators() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.UNUSED_KEY_GENERATORS_RESOURCE_URI, ResourceKind.UNUSED_KEY_GENERATORS);
    }
    
    /**
     * Create handler for unused sharding auditor resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler unusedAuditors() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.UNUSED_AUDITORS_RESOURCE_URI, ResourceKind.UNUSED_AUDITORS);
    }
    
    /**
     * Create handler for table rules that use a sharding algorithm.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler algorithmUsedTableRules() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.ALGORITHM_USED_TABLE_RULES_RESOURCE_URI, ResourceKind.ALGORITHM_USED_TABLE_RULES);
    }
    
    /**
     * Create handler for table rules that use a sharding key generator.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler keyGeneratorUsedTableRules() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.KEY_GENERATOR_USED_TABLE_RULES_RESOURCE_URI, ResourceKind.KEY_GENERATOR_USED_TABLE_RULES);
    }
    
    /**
     * Create handler for table rules that use a sharding auditor.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler auditorUsedTableRules() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.AUDITOR_USED_TABLE_RULES_RESOURCE_URI, ResourceKind.AUDITOR_USED_TABLE_RULES);
    }
    
    /**
     * Create handler for sharding rule count resources.
     *
     * @return sharding resource handler
     */
    public static ShardingResourceHandler ruleCount() {
        return new ShardingResourceHandler(ShardingFeatureDefinition.RULE_COUNT_RESOURCE_URI, ResourceKind.RULE_COUNT);
    }
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public String getResourceUriTemplate() {
        return resourceUriTemplate;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        return new MCPItemsResponse(query(databaseContext, uriVariables),
                MCPResourceNavigationPayloadBuilder.create(MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(getResourceUriTemplate()), uriVariables));
    }
    
    private List<Map<String, Object>> query(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        return switch (resourceKind) {
            case ALGORITHM_PLUGINS -> inspectionService.queryAlgorithmPlugins(databaseContext.getQueryFacade());
            case KEY_GENERATE_ALGORITHM_PLUGINS -> inspectionService.queryKeyGenerateAlgorithmPlugins(databaseContext.getQueryFacade());
            default -> queryDatabaseScoped(databaseContext, uriVariables);
        };
    }
    
    private List<Map<String, Object>> queryDatabaseScoped(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        String databaseName = uriVariables.getValue("database");
        return switch (resourceKind) {
            case ALGORITHMS -> inspectionService.queryAlgorithms(databaseContext.getQueryFacade(), databaseName);
            case TABLE_RULES -> inspectionService.queryTableRules(databaseContext.getQueryFacade(), databaseName);
            case TABLE_RULE -> inspectionService.queryTableRule(databaseContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.TABLE_FIELD));
            case TABLE_NODES -> inspectionService.queryTableNodes(databaseContext.getQueryFacade(), databaseName);
            case TABLE_NODE -> inspectionService.queryTableNode(databaseContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.TABLE_FIELD));
            case TABLE_REFERENCE_RULES -> inspectionService.queryTableReferenceRules(databaseContext.getQueryFacade(), databaseName);
            case TABLE_REFERENCE_RULE -> inspectionService.queryTableReferenceRule(databaseContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.RULE_FIELD));
            case DEFAULT_STRATEGY -> inspectionService.queryDefaultStrategy(databaseContext.getQueryFacade(), databaseName);
            case KEY_GENERATORS -> inspectionService.queryKeyGenerators(databaseContext.getQueryFacade(), databaseName);
            case KEY_GENERATOR -> inspectionService.queryKeyGenerator(databaseContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.KEY_GENERATOR_FIELD));
            case KEY_GENERATE_STRATEGIES -> inspectionService.queryKeyGenerateStrategies(databaseContext.getQueryFacade(), databaseName);
            case KEY_GENERATE_STRATEGY -> inspectionService.queryKeyGenerateStrategy(databaseContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.STRATEGY_FIELD));
            case AUDITORS -> inspectionService.queryAuditors(databaseContext.getQueryFacade(), databaseName);
            case UNUSED_ALGORITHMS -> inspectionService.queryUnusedAlgorithms(databaseContext.getQueryFacade(), databaseName);
            case UNUSED_KEY_GENERATORS -> inspectionService.queryUnusedKeyGenerators(databaseContext.getQueryFacade(), databaseName);
            case UNUSED_AUDITORS -> inspectionService.queryUnusedAuditors(databaseContext.getQueryFacade(), databaseName);
            case ALGORITHM_USED_TABLE_RULES -> inspectionService.queryTableRulesUsedAlgorithm(databaseContext.getQueryFacade(), databaseName,
                    uriVariables.getValue(ShardingFeatureDefinition.ALGORITHM_FIELD));
            case KEY_GENERATOR_USED_TABLE_RULES -> inspectionService.queryTableRulesUsedKeyGenerator(databaseContext.getQueryFacade(), databaseName,
                    uriVariables.getValue(ShardingFeatureDefinition.KEY_GENERATOR_FIELD));
            case AUDITOR_USED_TABLE_RULES -> inspectionService.queryTableRulesUsedAuditor(databaseContext.getQueryFacade(), databaseName,
                    uriVariables.getValue(ShardingFeatureDefinition.AUDITOR_FIELD));
            case RULE_COUNT -> inspectionService.queryRuleCount(databaseContext.getQueryFacade(), databaseName);
            default -> throw new IllegalStateException("Unsupported sharding resource kind.");
        };
    }
    
    enum ResourceKind {
        
        ALGORITHM_PLUGINS, KEY_GENERATE_ALGORITHM_PLUGINS, ALGORITHMS, TABLE_RULES, TABLE_RULE, TABLE_NODES, TABLE_NODE, TABLE_REFERENCE_RULES,
        TABLE_REFERENCE_RULE, DEFAULT_STRATEGY, KEY_GENERATORS, KEY_GENERATOR, KEY_GENERATE_STRATEGIES, KEY_GENERATE_STRATEGY, AUDITORS, UNUSED_ALGORITHMS,
        UNUSED_KEY_GENERATORS, UNUSED_AUDITORS, ALGORITHM_USED_TABLE_RULES, KEY_GENERATOR_USED_TABLE_RULES, AUDITOR_USED_TABLE_RULES, RULE_COUNT
    }
}

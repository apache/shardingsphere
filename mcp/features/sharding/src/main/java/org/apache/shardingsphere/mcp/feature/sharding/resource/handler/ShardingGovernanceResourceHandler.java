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
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;

import java.util.List;
import java.util.Map;

/**
 * Sharding governance resource handler.
 */
public final class ShardingGovernanceResourceHandler extends AbstractShardingResourceHandler {
    
    private final ResourceKind resourceKind;
    
    private ShardingGovernanceResourceHandler(final String resourceUriTemplate, final ResourceKind resourceKind) {
        super(resourceUriTemplate);
        this.resourceKind = resourceKind;
    }
    
    /**
     * Create handler for sharding auditor resources.
     *
     * @return sharding governance resource handler
     */
    public static ShardingGovernanceResourceHandler auditors() {
        return new ShardingGovernanceResourceHandler(ShardingFeatureDefinition.AUDITORS_RESOURCE_URI, ResourceKind.AUDITORS);
    }
    
    /**
     * Create handler for unused sharding auditor resources.
     *
     * @return sharding governance resource handler
     */
    public static ShardingGovernanceResourceHandler unusedAuditors() {
        return new ShardingGovernanceResourceHandler(ShardingFeatureDefinition.UNUSED_AUDITORS_RESOURCE_URI, ResourceKind.UNUSED_AUDITORS);
    }
    
    /**
     * Create handler for table rules that use a sharding auditor.
     *
     * @return sharding governance resource handler
     */
    public static ShardingGovernanceResourceHandler auditorUsedTableRules() {
        return new ShardingGovernanceResourceHandler(ShardingFeatureDefinition.AUDITOR_USED_TABLE_RULES_RESOURCE_URI, ResourceKind.AUDITOR_USED_TABLE_RULES);
    }
    
    /**
     * Create handler for sharding rule count resources.
     *
     * @return sharding governance resource handler
     */
    public static ShardingGovernanceResourceHandler ruleCount() {
        return new ShardingGovernanceResourceHandler(ShardingFeatureDefinition.RULE_COUNT_RESOURCE_URI, ResourceKind.RULE_COUNT);
    }
    
    @Override
    protected List<Map<String, Object>> query(final MCPDatabaseHandlerContext databaseContext, final MCPUriVariables uriVariables) {
        String databaseName = uriVariables.getValue("database");
        return switch (resourceKind) {
            case AUDITORS -> getInspectionService().queryAuditors(databaseContext.getQueryFacade(), databaseName);
            case UNUSED_AUDITORS -> getInspectionService().queryUnusedAuditors(databaseContext.getQueryFacade(), databaseName);
            case AUDITOR_USED_TABLE_RULES -> getInspectionService().queryTableRulesUsedAuditor(
                    databaseContext.getQueryFacade(), databaseName, uriVariables.getValue(ShardingFeatureDefinition.AUDITOR_FIELD));
            case RULE_COUNT -> getInspectionService().queryRuleCount(databaseContext.getQueryFacade(), databaseName);
        };
    }
    
    enum ResourceKind {
        
        AUDITORS, UNUSED_AUDITORS, AUDITOR_USED_TABLE_RULES, RULE_COUNT
    }
}

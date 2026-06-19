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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingDefaultStrategyWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingKeyGenerateStrategyWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingKeyGeneratorWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingRuleComponentCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingTableReferenceRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingTableRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

/**
 * Sharding workflow planning service.
 */
public final class ShardingWorkflowPlanningService {
    
    private final ShardingTableRuleWorkflowPlanningService tableRulePlanningService;
    
    private final ShardingTableReferenceRuleWorkflowPlanningService tableReferenceRulePlanningService;
    
    private final ShardingDefaultStrategyWorkflowPlanningService defaultStrategyPlanningService;
    
    private final ShardingKeyGeneratorWorkflowPlanningService keyGeneratorPlanningService;
    
    private final ShardingKeyGenerateStrategyWorkflowPlanningService keyGenerateStrategyPlanningService;
    
    private final ShardingRuleComponentCleanupWorkflowPlanningService ruleComponentCleanupPlanningService;
    
    /**
     * Create sharding workflow planning service.
     */
    public ShardingWorkflowPlanningService() {
        this(new ShardingWorkflowPlanningKernel());
    }
    
    ShardingWorkflowPlanningService(final ShardingInspectionService inspectionService, final ShardingDistSQLPlanningService distSQLPlanningService) {
        this(new ShardingWorkflowPlanningKernel(inspectionService, distSQLPlanningService));
    }
    
    private ShardingWorkflowPlanningService(final ShardingWorkflowPlanningKernel kernel) {
        tableRulePlanningService = new ShardingTableRuleWorkflowPlanningService(kernel);
        tableReferenceRulePlanningService = new ShardingTableReferenceRuleWorkflowPlanningService(kernel);
        defaultStrategyPlanningService = new ShardingDefaultStrategyWorkflowPlanningService(kernel);
        keyGeneratorPlanningService = new ShardingKeyGeneratorWorkflowPlanningService(kernel);
        keyGenerateStrategyPlanningService = new ShardingKeyGenerateStrategyWorkflowPlanningService(kernel);
        ruleComponentCleanupPlanningService = new ShardingRuleComponentCleanupWorkflowPlanningService(kernel);
    }
    
    /**
     * Plan sharding table rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planTableRule(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                 final String sessionId, final ShardingWorkflowRequest request) {
        return tableRulePlanningService.plan(workflowSessionContext, queryFacade, sessionId, new ShardingTableRuleWorkflowRequest(request));
    }
    
    /**
     * Plan sharding table reference rule workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planTableReferenceRule(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                          final String sessionId, final ShardingWorkflowRequest request) {
        return tableReferenceRulePlanningService.plan(workflowSessionContext, queryFacade, sessionId, new ShardingTableReferenceRuleWorkflowRequest(request));
    }
    
    /**
     * Plan default sharding strategy workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planDefaultStrategy(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                       final String sessionId, final ShardingWorkflowRequest request) {
        return defaultStrategyPlanningService.plan(workflowSessionContext, queryFacade, sessionId, new ShardingDefaultStrategyWorkflowRequest(request));
    }
    
    /**
     * Plan sharding key generator workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planKeyGenerator(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                    final String sessionId, final ShardingWorkflowRequest request) {
        return keyGeneratorPlanningService.plan(workflowSessionContext, queryFacade, sessionId, new ShardingKeyGeneratorWorkflowRequest(request));
    }
    
    /**
     * Plan sharding key generate strategy workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planKeyGenerateStrategy(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                           final String sessionId, final ShardingWorkflowRequest request) {
        return keyGenerateStrategyPlanningService.plan(workflowSessionContext, queryFacade, sessionId, new ShardingKeyGenerateStrategyWorkflowRequest(request));
    }
    
    /**
     * Plan unused sharding rule component cleanup workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot planComponentCleanup(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                                        final String sessionId, final ShardingWorkflowRequest request) {
        return ruleComponentCleanupPlanningService.plan(workflowSessionContext, queryFacade, sessionId, new ShardingRuleComponentCleanupWorkflowRequest(request));
    }
}

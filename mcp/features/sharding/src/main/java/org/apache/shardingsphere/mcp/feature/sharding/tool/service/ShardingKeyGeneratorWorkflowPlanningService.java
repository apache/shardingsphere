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

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingKeyGeneratorWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

/**
 * Sharding key generator workflow planning service.
 */
public final class ShardingKeyGeneratorWorkflowPlanningService {
    
    private final Planner planner;
    
    public ShardingKeyGeneratorWorkflowPlanningService() {
        this(new ShardingWorkflowPlanningKernel());
    }
    
    public ShardingKeyGeneratorWorkflowPlanningService(final ShardingWorkflowPlanningService delegate) {
        this(delegate::planKeyGenerator);
    }
    
    ShardingKeyGeneratorWorkflowPlanningService(final ShardingWorkflowPlanningKernel kernel) {
        this(kernel::planKeyGenerator);
    }
    
    private ShardingKeyGeneratorWorkflowPlanningService(final Planner planner) {
        this.planner = planner;
    }
    
    /**
     * Plan sharding key generator workflow.
     *
     * @param workflowSessionContext workflow session context
     * @param queryFacade feature query facade
     * @param sessionId MCP session ID
     * @param request sharding key generator workflow request
     * @return workflow context snapshot
     */
    public WorkflowContextSnapshot plan(final WorkflowSessionContext workflowSessionContext, final MCPFeatureQueryFacade queryFacade,
                                        final String sessionId, final ShardingKeyGeneratorWorkflowRequest request) {
        return planner.plan(workflowSessionContext, queryFacade, sessionId, request.toWorkflowRequest());
    }
    
    @FunctionalInterface
    private interface Planner {
        
        WorkflowContextSnapshot plan(WorkflowSessionContext workflowSessionContext, MCPFeatureQueryFacade queryFacade, String sessionId, ShardingWorkflowRequest request);
    }
}

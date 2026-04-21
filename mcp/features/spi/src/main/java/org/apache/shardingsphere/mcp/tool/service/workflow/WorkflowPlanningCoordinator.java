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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.List;
import java.util.Map;

/**
 * Workflow planning coordinator.
 */
public final class WorkflowPlanningCoordinator {
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    /**
     * Coordinate the shared workflow-planning lifecycle.
     *
     * @param requestContext request context
     * @param sessionId session id
     * @param request workflow request
     * @param contextStore workflow context store
     * @param planningScenario feature planning scenario
     * @param <R> request type
     * @param <S> feature state type
     * @return workflow snapshot
     */
    public <R extends WorkflowRequest, S> WorkflowContextSnapshot plan(final MCPFeatureContext requestContext, final String sessionId,
                                                                       final R request, final WorkflowContextStore contextStore,
                                                                       final WorkflowPlanningScenario<R, S> planningScenario) {
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(contextStore, requestContext);
        WorkflowContextSnapshot result = actualContextStore.getOrCreate(sessionId, request.getPlanId());
        R mergedRequest = planningScenario.prepareSnapshot(result, request);
        ClarifiedIntent clarifiedIntent = result.getClarifiedIntent();
        planningScenario.applyResolvedIntent(mergedRequest, clarifiedIntent);
        MCPMetadataQueryFacade metadataQueryFacade = requestContext.getMetadataQueryFacade();
        if (!planningSupport.ensurePlanningContext(metadataQueryFacade, mergedRequest, clarifiedIntent, result)) {
            return actualContextStore.persist(result,
                    WorkflowLifecycle.STATUS_FAILED.equals(result.getStatus()) ? WorkflowLifecycle.STEP_FAILED : WorkflowLifecycle.STEP_CLARIFYING, result.getStatus());
        }
        List<Map<String, Object>> existingRules = planningScenario.queryRules(requestContext, mergedRequest);
        if (!planningScenario.ensureLifecycleState(clarifiedIntent, mergedRequest, existingRules, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        }
        S workflowState = planningScenario.getWorkflowState(result);
        if (planningScenario.isDropWorkflow(clarifiedIntent)) {
            planningScenario.planDrop(requestContext, metadataQueryFacade, workflowState, clarifiedIntent, mergedRequest, existingRules, result);
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
        }
        if (!planningScenario.planNonDrop(requestContext, metadataQueryFacade, workflowState, clarifiedIntent, mergedRequest, existingRules, result)) {
            return actualContextStore.persist(result, WorkflowLifecycle.STEP_CLARIFYING, WorkflowLifecycle.STATUS_CLARIFYING);
        }
        return actualContextStore.persist(result, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PLANNED);
    }
    
    /**
     * Feature workflow-planning scenario.
     *
     * @param <R> request type
     * @param <S> feature state type
     */
    public interface WorkflowPlanningScenario<R extends WorkflowRequest, S> {
        
        /**
         * Prepare the persisted snapshot for planning.
         *
         * @param snapshot persisted snapshot
         * @param request incoming request
         * @return merged request
         */
        R prepareSnapshot(WorkflowContextSnapshot snapshot, R request);
        
        /**
         * Apply resolved intent to the request before planning.
         *
         * @param request workflow request
         * @param clarifiedIntent clarified intent
         */
        void applyResolvedIntent(R request, ClarifiedIntent clarifiedIntent);
        
        /**
         * Query existing rules relevant to the workflow request.
         *
         * @param requestContext request context
         * @param request workflow request
         * @return existing rules
         */
        List<Map<String, Object>> queryRules(MCPFeatureContext requestContext, R request);
        
        /**
         * Ensure the workflow lifecycle can proceed with the current request and rules.
         *
         * @param clarifiedIntent clarified intent
         * @param request workflow request
         * @param existingRules existing rules
         * @param snapshot workflow snapshot
         * @return whether planning can continue
         */
        boolean ensureLifecycleState(ClarifiedIntent clarifiedIntent, R request, List<Map<String, Object>> existingRules, WorkflowContextSnapshot snapshot);
        
        /**
         * Resolve the feature-specific planning state from the snapshot.
         *
         * @param snapshot workflow snapshot
         * @return feature state
         */
        S getWorkflowState(WorkflowContextSnapshot snapshot);
        
        /**
         * Determine whether the clarified intent represents a drop workflow.
         *
         * @param clarifiedIntent clarified intent
         * @return whether the workflow is a drop operation
         */
        boolean isDropWorkflow(ClarifiedIntent clarifiedIntent);
        
        /**
         * Plan the artifacts for a drop workflow.
         *
         * @param requestContext request context
         * @param metadataQueryFacade metadata query facade
         * @param workflowState feature state
         * @param clarifiedIntent clarified intent
         * @param request workflow request
         * @param existingRules existing rules
         * @param snapshot workflow snapshot
         */
        void planDrop(MCPFeatureContext requestContext, MCPMetadataQueryFacade metadataQueryFacade, S workflowState,
                      ClarifiedIntent clarifiedIntent, R request, List<Map<String, Object>> existingRules, WorkflowContextSnapshot snapshot);
        
        /**
         * Plan the artifacts for a non-drop workflow.
         *
         * @param requestContext request context
         * @param metadataQueryFacade metadata query facade
         * @param workflowState feature state
         * @param clarifiedIntent clarified intent
         * @param request workflow request
         * @param existingRules existing rules
         * @param snapshot workflow snapshot
         * @return whether planning completed without further clarification
         */
        boolean planNonDrop(MCPFeatureContext requestContext, MCPMetadataQueryFacade metadataQueryFacade, S workflowState,
                            ClarifiedIntent clarifiedIntent, R request, List<Map<String, Object>> existingRules, WorkflowContextSnapshot snapshot);
    }
}

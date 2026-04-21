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
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationReport;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationSection;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;

import java.util.List;
import java.util.Map;

/**
 * Workflow validation coordinator.
 */
public final class WorkflowValidationCoordinator {
    
    private final WorkflowValidationSupport validationSupport = new WorkflowValidationSupport();
    
    /**
     * Coordinate the shared workflow-validation lifecycle.
     *
     * @param requestContext request context
     * @param sessionId session id
     * @param planId plan identifier
     * @param contextStore workflow context store
     * @param validationScenario feature validation scenario
     * @param <S> feature state type
     * @return validation payload
     */
    public <S> Map<String, Object> validate(final MCPFeatureContext requestContext, final String sessionId, final String planId,
                                            final WorkflowContextStore contextStore, final WorkflowValidationScenario<S> validationScenario) {
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(contextStore, requestContext);
        WorkflowContextSnapshot snapshot = actualContextStore.getRequired(planId);
        Map<String, Object> rejectedResponse = validationSupport.checkValidatePreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        ValidationReport validationReport = new ValidationReport();
        snapshot.setValidationReport(validationReport);
        S workflowState = validationScenario.getWorkflowState(snapshot);
        List<Map<String, Object>> existingRules = validationScenario.queryRules(requestContext, snapshot);
        MCPMetadataQueryFacade metadataQueryFacade = requestContext.getMetadataQueryFacade();
        validationReport.setDdlValidation(validationScenario.validateDdl(requestContext, sessionId, snapshot, workflowState, existingRules, validationReport));
        validationReport.setRuleValidation(validationScenario.validateRules(requestContext, sessionId, snapshot, workflowState, existingRules, validationReport));
        validationReport.setLogicalMetadataValidation(validationSupport.validateLogicalMetadata(snapshot, metadataQueryFacade, validationReport));
        validationReport.setSqlExecutabilityValidation(validationScenario.validateSqlExecutability(requestContext, sessionId, snapshot, workflowState, validationReport));
        validationReport.setOverallStatus(validationSupport.resolveOverallStatus(validationReport.getDdlValidation(), validationReport.getRuleValidation(),
                validationReport.getLogicalMetadataValidation(), validationReport.getSqlExecutabilityValidation()));
        return validationSupport.finalizeValidation(actualContextStore, snapshot, validationReport);
    }
    
    /**
     * Feature workflow-validation scenario.
     *
     * @param <S> feature state type
     */
    public interface WorkflowValidationScenario<S> {
        
        /**
         * Resolve the feature-specific validation state from the snapshot.
         *
         * @param snapshot workflow snapshot
         * @return feature state
         */
        S getWorkflowState(WorkflowContextSnapshot snapshot);
        
        /**
         * Query existing rules relevant to the workflow snapshot.
         *
         * @param requestContext request context
         * @param snapshot workflow snapshot
         * @return existing rules
         */
        List<Map<String, Object>> queryRules(MCPFeatureContext requestContext, WorkflowContextSnapshot snapshot);
        
        /**
         * Validate physical DDL state for the workflow.
         *
         * @param requestContext request context
         * @param sessionId session id
         * @param snapshot workflow snapshot
         * @param workflowState feature state
         * @param existingRules existing rules
         * @param validationReport validation report
         * @return DDL validation section
         */
        ValidationSection validateDdl(MCPFeatureContext requestContext, String sessionId, WorkflowContextSnapshot snapshot,
                                      S workflowState, List<Map<String, Object>> existingRules, ValidationReport validationReport);
        
        /**
         * Validate logical rule state for the workflow.
         *
         * @param requestContext request context
         * @param sessionId session id
         * @param snapshot workflow snapshot
         * @param workflowState feature state
         * @param existingRules existing rules
         * @param validationReport validation report
         * @return rule validation section
         */
        ValidationSection validateRules(MCPFeatureContext requestContext, String sessionId, WorkflowContextSnapshot snapshot,
                                        S workflowState, List<Map<String, Object>> existingRules, ValidationReport validationReport);
        
        /**
         * Validate SQL executability for the workflow.
         *
         * @param requestContext request context
         * @param sessionId session id
         * @param snapshot workflow snapshot
         * @param workflowState feature state
         * @param validationReport validation report
         * @return SQL executability validation section
         */
        ValidationSection validateSqlExecutability(MCPFeatureContext requestContext, String sessionId, WorkflowContextSnapshot snapshot,
                                                   S workflowState, ValidationReport validationReport);
    }
}

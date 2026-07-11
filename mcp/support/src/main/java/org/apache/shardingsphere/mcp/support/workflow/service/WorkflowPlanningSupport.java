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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Workflow planning support.
 */
public final class WorkflowPlanningSupport {
    
    private final WorkflowPlanningContextValidator contextValidator = new WorkflowPlanningContextValidator();
    
    private final WorkflowAlgorithmRequirementCollector requirementCollector = new WorkflowAlgorithmRequirementCollector();
    
    /**
     * Apply resolved intent fields to the workflow request.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     */
    public void applyResolvedIntent(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        request.setOperationType(clarifiedIntent.getOperationType());
        request.setFieldSemantics(clarifiedIntent.getFieldSemantics());
    }
    
    /**
     * Prepare workflow snapshot for planning.
     *
     * @param snapshot workflow snapshot
     * @param workflowKind workflow kind
     * @param request merged request
     * @param featureData feature-scoped workflow data
     * @param clarifiedIntent clarified intent
     * @param summary interaction summary
     * @param interactionSteps interaction steps
     * @param validationLayers validation layers
     * @param <T> request type
     * @return prepared request
     */
    public <T extends WorkflowRequest> T prepareSnapshot(final WorkflowContextSnapshot snapshot, final WorkflowKind workflowKind, final T request, final WorkflowFeatureData featureData,
                                                         final ClarifiedIntent clarifiedIntent, final String summary,
                                                         final List<String> interactionSteps, final List<String> validationLayers) {
        request.setExecutionMode(WorkflowIntentResolverSupport.resolveExecutionMode(request, clarifiedIntent));
        snapshot.setWorkflowKind(workflowKind);
        snapshot.setRequest(request);
        snapshot.setFeatureData(featureData);
        snapshot.setInteractionPlan(InteractionPlan.create(snapshot.getPlanId(), request, summary, interactionSteps, validationLayers));
        snapshot.clearPlanningState();
        snapshot.setClarifiedIntent(clarifiedIntent);
        return request;
    }
    
    /**
     * Ensure lifecycle state matches the requested workflow operation.
     *
     * @param ruleLabel rule label for user-facing issues
     * @param clarifiedIntent clarified intent
     * @param ruleExists whether the target rule already exists
     * @param snapshot workflow snapshot
     * @return whether lifecycle state is valid
     */
    public boolean ensureLifecycleState(final String ruleLabel, final ClarifiedIntent clarifiedIntent,
                                        final boolean ruleExists, final WorkflowContextSnapshot snapshot) {
        String actualOperationType = clarifiedIntent.getOperationType().toLowerCase(Locale.ENGLISH);
        if ("create".equals(actualOperationType) && ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("%s already exists for the target column.", ruleLabel), "Use a supported change path for the existing rule, or drop it before creating a replacement.", false,
                    Map.of()));
            return false;
        }
        if ("alter".equals(actualOperationType) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.RULE_STATE_MISMATCH, "error", "discovering",
                    String.format("%s does not exist for the target column.", ruleLabel), "Use create or confirm the target column.", false, Map.of()));
            return false;
        }
        if ("drop".equals(actualOperationType) && !ruleExists) {
            snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND, "error", "discovering",
                    String.format("%s does not exist for the target column.", ruleLabel), "Confirm target table and column or skip the drop request.", false, Map.of()));
            return false;
        }
        return true;
    }
    
    /**
     * Ensure workflow operation type is exposed by the feature contract.
     *
     * @param clarifiedIntent clarified intent
     * @param supportedOperationTypes supported operation types
     * @param snapshot workflow snapshot
     * @return whether the operation type is supported
     */
    public boolean ensureSupportedOperationType(final ClarifiedIntent clarifiedIntent, final Collection<String> supportedOperationTypes, final WorkflowContextSnapshot snapshot) {
        if (containsOperationType(supportedOperationTypes, clarifiedIntent.getOperationType())) {
            return true;
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.WORKFLOW_STATUS_INVALID, "error", "intaking",
                "Unsupported workflow operation type.", String.format("Use one of: %s.", String.join(", ", supportedOperationTypes)), false,
                Map.of("supported_operation_types", supportedOperationTypes)));
        clarifiedIntent.getInferredValues().remove(WorkflowFieldNames.OPERATION_TYPE);
        clarifiedIntent.setOperationType("");
        if (null != snapshot.getRequest()) {
            snapshot.getRequest().setOperationType("");
        }
        snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
        return false;
    }
    
    private boolean containsOperationType(final Collection<String> supportedOperationTypes, final String actualOperationType) {
        for (String each : supportedOperationTypes) {
            if (each.equalsIgnoreCase(actualOperationType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Add one fallback clarification question when algorithm selection is blocked.
     *
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @param fallbackQuestion fallback question
     * @return whether there is any blocking algorithm issue
     */
    public boolean hasBlockingAlgorithmIssues(final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot, final String fallbackQuestion) {
        return requirementCollector.hasBlockingAlgorithmIssues(clarifiedIntent, snapshot, fallbackQuestion);
    }
    
    /**
     * Collect required algorithm properties and emit missing-property clarification prompts.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @param propertyRequirements property requirements
     * @return whether all required properties are present
     */
    public boolean collectPropertyRequirements(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent,
                                               final WorkflowContextSnapshot snapshot, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        return requirementCollector.collectPropertyRequirements(request, clarifiedIntent, snapshot, propertyRequirements);
    }
    
    /**
     * Judge whether workflow planning can continue to artifact generation.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @param propertyRequirements property requirements
     * @param fallbackQuestion fallback question
     * @return whether artifact planning can continue
     */
    public boolean isReadyForArtifactPlanning(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot,
                                              final List<AlgorithmPropertyRequirement> propertyRequirements, final String fallbackQuestion) {
        return requirementCollector.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, propertyRequirements, fallbackQuestion);
    }
    
    /**
     * Ensure workflow identifiers can be rendered into reviewable DistSQL.
     *
     * @param fieldName field name for issue details
     * @param identifiers identifiers to check
     * @param snapshot workflow snapshot
     * @param issueStage issue stage
     * @return whether all identifiers are supported
     */
    public boolean ensureSupportedIdentifiers(final String fieldName, final Collection<String> identifiers, final WorkflowContextSnapshot snapshot,
                                              final String issueStage) {
        return contextValidator.ensureSupportedIdentifiers(fieldName, identifiers, snapshot, issueStage);
    }
    
    /**
     * Ensure optional workflow identifiers can be rendered into reviewable DistSQL when present.
     *
     * @param fieldName field name for issue details
     * @param identifiers identifiers to check
     * @param snapshot workflow snapshot
     * @param issueStage issue stage
     * @return whether all present identifiers are supported
     */
    public boolean ensureOptionalSupportedIdentifiers(final String fieldName, final Collection<String> identifiers, final WorkflowContextSnapshot snapshot,
                                                      final String issueStage) {
        return contextValidator.ensureOptionalSupportedIdentifiers(fieldName, identifiers, snapshot, issueStage);
    }
    
    /**
     * Ensure workflow planning context is complete and valid.
     *
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade for database capability resolution
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @return whether planning context is ready
     * @throws DatabaseCapabilityNotFoundException when database profile or capability does not exist
     */
    public boolean ensurePlanningContext(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade, final WorkflowRequest request,
                                         final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot) {
        return contextValidator.ensurePlanningContext(metadataQueryFacade, queryFacade, request, clarifiedIntent, snapshot);
    }
}

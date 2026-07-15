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

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Collector for workflow algorithm property requirements.
 */
public final class WorkflowAlgorithmRequirementCollector {
    
    /**
     * Add one fallback clarification question when algorithm selection is blocked.
     *
     * @param clarifiedIntent clarified intent
     * @param snapshot workflow snapshot
     * @param fallbackQuestion fallback question
     * @return whether there is any blocking algorithm issue
     */
    public boolean hasBlockingAlgorithmIssues(final ClarifiedIntent clarifiedIntent, final WorkflowContextSnapshot snapshot, final String fallbackQuestion) {
        boolean result = snapshot.getIssues().stream().anyMatch(each -> WorkflowLifecycle.STEP_SELECTING_ALGORITHM.equals(each.getStage()) && "error".equals(each.getSeverity()));
        if (result && clarifiedIntent.getClarificationMessages().isEmpty()) {
            clarifiedIntent.getClarificationMessages().add(fallbackQuestion);
        }
        return result;
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
        snapshot.getPropertyRequirements().addAll(propertyRequirements);
        applyDefaultProperties(request, propertyRequirements);
        List<String> missingRequiredProperties = findMissingRequiredProperties(request, propertyRequirements);
        if (missingRequiredProperties.isEmpty()) {
            return true;
        }
        for (String each : missingRequiredProperties) {
            clarifiedIntent.getClarificationMessages().add(String.format("Please provide property `%s`.", each));
        }
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING, "error", WorkflowLifecycle.STEP_COLLECTING_PROPERTIES,
                "Required algorithm properties are still missing.", "Provide all required algorithm properties.", true, Map.of("missing_properties", missingRequiredProperties)));
        return false;
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
        if (hasBlockingAlgorithmIssues(clarifiedIntent, snapshot, fallbackQuestion) || !clarifiedIntent.getClarificationMessages().isEmpty()) {
            return false;
        }
        return collectPropertyRequirements(request, clarifiedIntent, snapshot, propertyRequirements);
    }
    
    private void applyDefaultProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            if (!each.getDefaultValue().isEmpty()) {
                request.getAlgorithmProperties(each.getAlgorithmRole()).putIfAbsent(each.getPropertyKey(), each.getDefaultValue());
            }
        }
    }
    
    private List<String> findMissingRequiredProperties(final WorkflowRequest request, final List<AlgorithmPropertyRequirement> propertyRequirements) {
        List<String> result = new LinkedList<>();
        for (AlgorithmPropertyRequirement each : propertyRequirements) {
            String actualValue = request.getAlgorithmProperties(each.getAlgorithmRole()).get(each.getPropertyKey());
            if (each.isRequired() && (null == actualValue || actualValue.isBlank())) {
                result.add(each.getPropertyKey());
            }
        }
        return result;
    }
}

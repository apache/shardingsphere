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

package org.apache.shardingsphere.mcp.tool.model.workflow;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Interaction plan.
 */
@Getter
@Setter
public final class InteractionPlan {
    
    private String planId;
    
    private String summary;
    
    private String currentStep;
    
    private String deliveryMode;
    
    private String executionMode;
    
    private final List<String> steps = new LinkedList<>();
    
    private final Map<String, Object> validationStrategy = new LinkedHashMap<>(8, 1F);
    
    /**
     * Create interaction plan.
     *
     * @param planId workflow plan identifier
     * @param request workflow request
     * @param summary interaction summary
     * @param steps interaction steps
     * @param validationLayers validation layers
     * @return interaction plan
     */
    public static InteractionPlan create(final String planId, final WorkflowRequest request, final String summary,
                                         final List<String> steps, final List<String> validationLayers) {
        InteractionPlan result = new InteractionPlan();
        result.setPlanId(planId);
        result.setSummary(summary);
        result.setCurrentStep(WorkflowLifecycle.STEP_INTAKING);
        result.setDeliveryMode(resolveMode(request.getDeliveryMode(), "all-at-once"));
        result.setExecutionMode(resolveMode(request.getExecutionMode(), "review-then-execute"));
        result.getSteps().addAll(steps);
        result.getValidationStrategy().put("layers", validationLayers);
        return result;
    }
    
    private static String resolveMode(final String requestedMode, final String defaultMode) {
        return null == requestedMode || requestedMode.isBlank() ? defaultMode : requestedMode;
    }
}

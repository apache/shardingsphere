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

import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.List;

/**
 * Interaction plan factory.
 */
public final class InteractionPlanFactory {
    
    private InteractionPlanFactory() {
    }
    
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
        result.setCurrentStep("intaking");
        result.setDeliveryMode(WorkflowSqlUtils.trimToEmpty(request.getDeliveryMode()).isEmpty() ? "all-at-once" : request.getDeliveryMode());
        result.setExecutionMode(WorkflowSqlUtils.trimToEmpty(request.getExecutionMode()).isEmpty() ? "review-then-execute" : request.getExecutionMode());
        result.getSteps().addAll(steps);
        result.getValidationStrategy().put("layers", validationLayers);
        return result;
    }
}

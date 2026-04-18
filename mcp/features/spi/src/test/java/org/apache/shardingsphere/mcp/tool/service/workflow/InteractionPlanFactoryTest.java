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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class InteractionPlanFactoryTest {
    
    @Test
    void assertCreateUsesExplicitModes() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDeliveryMode("step-by-step");
        request.setExecutionMode("manual-only");
        InteractionPlan actual = InteractionPlanFactory.create("plan-1", request, "Encrypt workflow plan.", List.of("inspect", "plan"), List.of("rules", "ddl"));
        assertThat(actual.getPlanId(), is("plan-1"));
        assertThat(actual.getSummary(), is("Encrypt workflow plan."));
        assertThat(actual.getCurrentStep(), is("intaking"));
        assertThat(actual.getDeliveryMode(), is("step-by-step"));
        assertThat(actual.getExecutionMode(), is("manual-only"));
        assertThat(actual.getSteps(), is(List.of("inspect", "plan")));
        assertThat(actual.getValidationStrategy().get("layers"), is(List.of("rules", "ddl")));
    }
    
    @Test
    void assertCreateUsesDefaultModesWhenBlank() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDeliveryMode(" ");
        request.setExecutionMode("");
        InteractionPlan actual = InteractionPlanFactory.create("plan-2", request, "Mask workflow plan.", List.of("inspect"), List.of("rules"));
        assertThat(actual.getDeliveryMode(), is("all-at-once"));
        assertThat(actual.getExecutionMode(), is("review-then-execute"));
    }
}

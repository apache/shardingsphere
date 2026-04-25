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

import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowLifecycleUtilsTest {
    
    @Test
    void assertResolveContextStoreUsesRequestContextStore() {
        WorkflowContextStore expectedContextStore = new WorkflowContextStore();
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(null, expectedContextStore);
        assertThat(actualContextStore, is(expectedContextStore));
    }
    
    @Test
    void assertResolveContextStorePrefersConfiguredStore() {
        WorkflowContextStore expectedContextStore = new WorkflowContextStore();
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(expectedContextStore, new WorkflowContextStore());
        assertThat(actualContextStore, is(expectedContextStore));
    }
    
    @Test
    void assertIsOwnedBySessionWithBlankSnapshotSession() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        assertTrue(WorkflowLifecycleUtils.isOwnedBySession("session-1", snapshot));
    }
    
    @Test
    void assertIsOwnedBySessionWithDifferentSession() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setSessionId("session-1");
        assertFalse(WorkflowLifecycleUtils.isOwnedBySession("session-2", snapshot));
    }
    
    @Test
    void assertResolveCurrentStep() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        snapshot.setInteractionPlan(interactionPlan);
        assertThat(WorkflowLifecycleUtils.resolveCurrentStep(snapshot), is("review"));
    }
    
    @Test
    void assertResolveCurrentStepWithoutInteractionPlan() {
        assertThat(WorkflowLifecycleUtils.resolveCurrentStep(new WorkflowContextSnapshot()), is(""));
    }
    
    @Test
    void assertResolveOperationType() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("drop");
        snapshot.setClarifiedIntent(clarifiedIntent);
        assertThat(WorkflowLifecycleUtils.resolveOperationType(snapshot), is("drop"));
    }
    
    @Test
    void assertResolveOperationTypeWithoutClarifiedIntent() {
        assertThat(WorkflowLifecycleUtils.resolveOperationType(new WorkflowContextSnapshot()), is(""));
    }
    
    @Test
    void assertIsDropWorkflow() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("drop");
        snapshot.setClarifiedIntent(clarifiedIntent);
        assertTrue(WorkflowLifecycleUtils.isDropWorkflow(snapshot));
    }
    
    @Test
    void assertIsDropWorkflowWithNonDropOperation() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        snapshot.setClarifiedIntent(clarifiedIntent);
        assertFalse(WorkflowLifecycleUtils.isDropWorkflow(snapshot));
    }
}

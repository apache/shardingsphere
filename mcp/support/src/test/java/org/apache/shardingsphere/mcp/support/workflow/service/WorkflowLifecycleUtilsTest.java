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

import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WorkflowLifecycleUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dropWorkflowCases")
    void assertIsDropWorkflow(final String name, final WorkflowContextSnapshot snapshot, final boolean expected) {
        assertThat(WorkflowLifecycleUtils.isDropWorkflow(snapshot), is(expected));
    }
    
    private static Stream<Arguments> dropWorkflowCases() {
        return Stream.of(
                Arguments.of("before clarification", new WorkflowContextSnapshot(), false),
                Arguments.of("drop operation", createSnapshot("drop"), true),
                Arguments.of("create operation", createSnapshot("create"), false));
    }
    
    private static WorkflowContextSnapshot createSnapshot(final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    @Test
    void assertIsNotOwnedBySessionWithBlankSnapshotSession() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        assertFalse(WorkflowLifecycleUtils.isOwnedBySession("session-1", snapshot));
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
}

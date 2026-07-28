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
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowAlgorithmRequirementCollectorTest {
    
    private final WorkflowAlgorithmRequirementCollector collector = new WorkflowAlgorithmRequirementCollector();
    
    @Test
    void assertHasBlockingAlgorithmIssuesAddsFallbackQuestion() {
        WorkflowRequest request = new WorkflowRequest();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "message", "action", false, Map.of()));
        boolean actual = collector.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, List.of(), "Please use an algorithm visible in the current Proxy.");
        assertFalse(actual);
        assertThat(clarifiedIntent.getClarificationMessages(), is(List.of("Please use an algorithm visible in the current Proxy.")));
    }
    
    @Test
    void assertCollectPropertyRequirementsAppliesDefaultsAndReportsMissingValues() {
        WorkflowRequest request = new WorkflowRequest();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        boolean actual = collector.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, List.of(
                new AlgorithmPropertyRequirement("primary", "mask-char", false, false, "mask char", "*"),
                new AlgorithmPropertyRequirement("primary", "from-x", true, false, "from x", "")), "fallback");
        assertFalse(actual);
        assertThat(request.getPrimaryAlgorithmProperties().get("mask-char"), is("*"));
        assertThat(clarifiedIntent.getClarificationMessages(), is(List.of("Please provide property `from-x`.")));
        assertThat(snapshot.getPropertyRequirements().size(), is(2));
        assertThat(snapshot.getIssues().getFirst().getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
    }
    
    @Test
    void assertIsReadyForArtifactPlanningStopsOnExistingClarification() {
        WorkflowRequest request = new WorkflowRequest();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.getClarificationMessages().add("Please provide sharding column.");
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        boolean actual = collector.isReadyForArtifactPlanning(request, clarifiedIntent, snapshot, List.of(
                new AlgorithmPropertyRequirement("primary", "mask-char", false, false, "mask char", "*")), "fallback");
        assertFalse(actual);
        assertTrue(snapshot.getPropertyRequirements().isEmpty());
    }
}

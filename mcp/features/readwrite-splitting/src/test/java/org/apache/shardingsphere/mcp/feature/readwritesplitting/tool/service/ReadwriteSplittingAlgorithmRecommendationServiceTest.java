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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingAlgorithmRecommendationServiceTest {
    
    private final ReadwriteSplittingAlgorithmRecommendationService service = new ReadwriteSplittingAlgorithmRecommendationService();
    
    @Test
    void assertRecommendSpecifiedAlgorithm() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setLoadBalancerType("round_robin");
        List<AlgorithmCandidate> actual = service.recommendLoadBalanceAlgorithms(request, List.of(Map.of("type", "ROUND_ROBIN")), new LinkedList<>());
        assertThat(actual.getFirst().getAlgorithmType(), is("ROUND_ROBIN"));
        assertThat(actual.getFirst().getAlgorithmRole(), is("primary"));
    }
    
    @Test
    void assertRecommendDefaultAlgorithm() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        List<AlgorithmCandidate> actual = service.recommendLoadBalanceAlgorithms(request, List.of(Map.of("type", "RANDOM"), Map.of("type", "ROUND_ROBIN")), new LinkedList<>());
        assertThat(actual.getFirst().getAlgorithmType(), is("ROUND_ROBIN"));
    }
    
    @Test
    void assertRecommendRejectsInvisibleAlgorithm() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setLoadBalancerType("WEIGHT");
        List<WorkflowIssue> issues = new LinkedList<>();
        assertTrue(service.recommendLoadBalanceAlgorithms(request, List.of(Map.of("type", "RANDOM")), issues).isEmpty());
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
}

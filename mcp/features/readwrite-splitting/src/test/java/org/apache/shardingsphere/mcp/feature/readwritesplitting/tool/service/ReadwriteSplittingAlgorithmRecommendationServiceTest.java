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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingAlgorithmRecommendationServiceTest {
    
    private final ReadwriteSplittingAlgorithmRecommendationService service = new ReadwriteSplittingAlgorithmRecommendationService();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRecommendAlgorithmCases")
    void assertRecommendAlgorithm(final String name, final String algorithmType, final List<Map<String, Object>> algorithmRows, final String expectedType, final String expectedReason) {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setLoadBalancerType(algorithmType);
        List<AlgorithmCandidate> actual = service.recommendLoadBalanceAlgorithms(request, algorithmRows, new LinkedList<>());
        assertThat(actual.getFirst().getAlgorithmType(), is(expectedType));
        assertThat(actual.getFirst().getAlgorithmRole(), is("primary"));
        assertThat(actual.getFirst().getRecommendationReason(), is(expectedReason));
    }
    
    private static Stream<Arguments> assertRecommendAlgorithmCases() {
        return Stream.of(
                Arguments.of("specified algorithm", "round_robin", List.of(Map.of("type", "ROUND_ROBIN")), "ROUND_ROBIN", "User specified load-balance algorithm."),
                Arguments.of("default algorithm", "", List.of(Map.of("type", "RANDOM"), Map.of("type", "ROUND_ROBIN")), "ROUND_ROBIN",
                        "Selected from algorithms reported by the current Proxy using MCP's built-in load-balance preference order."),
                Arguments.of("first available algorithm", "", List.of(Map.of("type", "FOO_ALGORITHM")), "FOO_ALGORITHM",
                        "Selected from algorithms reported by the current Proxy using MCP's built-in load-balance preference order."));
    }
    
    @Test
    void assertRecommendRejectsInvisibleAlgorithm() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setLoadBalancerType("WEIGHT");
        List<WorkflowIssue> issues = new LinkedList<>();
        assertTrue(service.recommendLoadBalanceAlgorithms(request, List.of(Map.of("type", "RANDOM")), issues).isEmpty());
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @Test
    void assertRecommendWithoutAlgorithms() {
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendLoadBalanceAlgorithms(new ReadwriteSplittingRuleWorkflowRequest(), List.of(), issues);
        assertTrue(actual.isEmpty());
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
        assertThat(issues.getFirst().getMessage(), is("No load-balance algorithm is available from the current Proxy."));
    }
}

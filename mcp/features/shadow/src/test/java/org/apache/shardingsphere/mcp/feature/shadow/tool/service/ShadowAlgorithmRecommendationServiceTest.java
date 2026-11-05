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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
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

class ShadowAlgorithmRecommendationServiceTest {
    
    private final ShadowAlgorithmRecommendationService service = new ShadowAlgorithmRecommendationService();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRecommendAlgorithmCases")
    void assertRecommendAlgorithm(final String name, final String algorithmType, final List<Map<String, Object>> algorithmRows, final String expectedType, final String expectedReason) {
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType(algorithmType);
        List<AlgorithmCandidate> actual = service.recommendShadowAlgorithms(request, algorithmRows, new LinkedList<>());
        assertThat(actual.getFirst().getAlgorithmType(), is(expectedType));
        assertThat(actual.getFirst().getRecommendationReason(), is(expectedReason));
    }
    
    private static Stream<Arguments> assertRecommendAlgorithmCases() {
        return Stream.of(
                Arguments.of("specified algorithm", "value_match", List.of(Map.of("type", "VALUE_MATCH")), "VALUE_MATCH", "User specified shadow algorithm."),
                Arguments.of("default algorithm", "", List.of(Map.of("type", "VALUE_MATCH"), Map.of("type", "SQL_HINT")), "SQL_HINT",
                        "Selected from algorithms reported by the current Proxy using MCP's built-in shadow preference order."),
                Arguments.of("first available algorithm", "", List.of(Map.of("type", "FOO_ALGORITHM")), "FOO_ALGORITHM",
                        "Selected from algorithms reported by the current Proxy using MCP's built-in shadow preference order."));
    }
    
    @Test
    void assertRecommendRejectsInvisibleAlgorithm() {
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("REGEX_MATCH");
        List<WorkflowIssue> issues = new LinkedList<>();
        assertTrue(service.recommendShadowAlgorithms(request, List.of(Map.of("type", "SQL_HINT")), issues).isEmpty());
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
}

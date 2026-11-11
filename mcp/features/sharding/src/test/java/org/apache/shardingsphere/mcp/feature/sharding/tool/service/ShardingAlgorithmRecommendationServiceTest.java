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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingAlgorithmRecommendationServiceTest {
    
    private final ShardingAlgorithmRecommendationService service = new ShardingAlgorithmRecommendationService();
    
    @Test
    void assertRecommendSpecifiedAlgorithms() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setAlgorithmType("inline");
        request.setKeyGeneratorType("snowflake");
        List<AlgorithmCandidate> actual = service.recommend(request, List.of(Map.of("type", "INLINE")), List.of(Map.of("type", "SNOWFLAKE")), true, true, new LinkedList<>());
        assertThat(actual.get(0).getAlgorithmType(), is("INLINE"));
        assertThat(actual.get(1).getAlgorithmRole(), is("key_generator"));
        assertThat(actual.get(1).getAlgorithmType(), is("SNOWFLAKE"));
    }
    
    @Test
    void assertRecommendDefaultAlgorithms() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        List<AlgorithmCandidate> actual = service.recommend(request, List.of(Map.of("type", "MOD"), Map.of("type", "INLINE")),
                List.of(Map.of("type", "UUID"), Map.of("type", "SNOWFLAKE")), true, true, new LinkedList<>());
        assertThat(actual.get(0).getAlgorithmType(), is("INLINE"));
        assertThat(actual.get(0).getRecommendationReason(), is("Selected from algorithms reported by the current Proxy using MCP's built-in sharding preference order."));
        assertThat(actual.get(1).getAlgorithmType(), is("SNOWFLAKE"));
        assertThat(actual.get(1).getRecommendationReason(), is("Selected from algorithms reported by the current Proxy using MCP's built-in key-generator preference order."));
    }
    
    @Test
    void assertRecommendRejectsInvisibleAlgorithm() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setAlgorithmType("CLASS_BASED");
        List<WorkflowIssue> issues = new LinkedList<>();
        assertTrue(service.recommend(request, List.of(Map.of("type", "INLINE")), List.of(), true, false, issues).isEmpty());
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRecommendMissingOrFallbackAlgorithmsArguments")
    void assertRecommendMissingOrFallbackAlgorithms(final String name, final String keyGeneratorType, final List<Map<String, Object>> algorithms,
                                                    final List<Map<String, Object>> keyGenerators, final boolean includeSharding, final List<String> expectedTypes,
                                                    final List<String> expectedIssueCodes) {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setKeyGeneratorType(keyGeneratorType);
        List<WorkflowIssue> issues = new ArrayList<>(expectedIssueCodes.size());
        List<AlgorithmCandidate> actual = service.recommend(request, algorithms, keyGenerators, includeSharding, true, issues);
        assertThat(actual.stream().map(AlgorithmCandidate::getAlgorithmType).toList(), is(expectedTypes));
        assertThat(issues.stream().map(WorkflowIssue::getCode).toList(), is(expectedIssueCodes));
    }
    
    private static Stream<Arguments> assertRecommendMissingOrFallbackAlgorithmsArguments() {
        return Stream.of(
                Arguments.of("empty catalogs", "", List.of(), List.of(), true, List.of(), List.of(WorkflowIssueCode.ALGORITHM_NOT_FOUND, WorkflowIssueCode.ALGORITHM_NOT_FOUND)),
                Arguments.of("fallback algorithms", "", List.of(Map.of("type", "FOO_ALGORITHM")), List.of(Map.of("type", "FOO_GENERATOR")), true, List.of("FOO_ALGORITHM", "FOO_GENERATOR"), List.of()),
                Arguments.of("unavailable key generator", "FOO_GENERATOR", List.of(), List.of(Map.of("type", "SNOWFLAKE")), false, List.of(), List.of(WorkflowIssueCode.ALGORITHM_NOT_FOUND)));
    }
}

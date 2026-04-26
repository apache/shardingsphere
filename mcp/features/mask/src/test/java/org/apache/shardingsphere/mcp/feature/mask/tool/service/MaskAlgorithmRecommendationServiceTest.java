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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
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

class MaskAlgorithmRecommendationServiceTest {
    
    private final MaskAlgorithmRecommendationService service = new MaskAlgorithmRecommendationService();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertIsKnownMaskAlgorithmArguments")
    void assertIsKnownMaskAlgorithm(final String name, final String algorithmType, final boolean expectedKnown) {
        assertThat(MaskAlgorithmRecommendationService.isKnownMaskAlgorithm(algorithmType), is(expectedKnown));
    }
    
    @Test
    void assertRecommendMaskAlgorithmsWithSpecifiedAlgorithm() {
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("MD5");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendMaskAlgorithms(new ClarifiedIntent(), request, List.of(Map.of("type", "MD5")), issues);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getAlgorithmType(), is("MD5"));
        assertThat(actual.get(0).getSource(), is("builtin"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendMaskAlgorithmsWithUnknownSpecifiedAlgorithm() {
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("CUSTOM");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendMaskAlgorithms(new ClarifiedIntent(), request, List.of(Map.of("type", "MD5")), issues);
        assertTrue(actual.isEmpty());
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @Test
    void assertRecommendMaskAlgorithmsWithPhoneSemantics() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setFieldSemantics("phone");
        WorkflowRequest request = new WorkflowRequest();
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendMaskAlgorithms(clarifiedIntent, request, List.of(
                Map.of("type", "MASK_FROM_X_TO_Y"),
                Map.of("type", "KEEP_FIRST_N_LAST_M")), issues);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getAlgorithmType(), is("MASK_FROM_X_TO_Y"));
    }
    
    @Test
    void assertRecommendMaskAlgorithmsWithoutAvailableAlgorithms() {
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendMaskAlgorithms(new ClarifiedIntent(), new WorkflowRequest(), List.of(), issues);
        assertTrue(actual.isEmpty());
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    private static Stream<Arguments> assertIsKnownMaskAlgorithmArguments() {
        return Stream.of(
                Arguments.of("md5 is built in", "MD5", true),
                Arguments.of("keep first and last is built in", "KEEP_FIRST_N_LAST_M", true),
                Arguments.of("custom is not built in", "CUSTOM", false));
    }
}

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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptAlgorithmRecommendationServiceTest {
    
    private final EncryptAlgorithmRecommendationService service = new EncryptAlgorithmRecommendationService();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertIsKnownEncryptAlgorithmArguments")
    void assertIsKnownEncryptAlgorithm(final String name, final String algorithmType, final boolean expectedKnown) {
        assertThat(EncryptAlgorithmRecommendationService.isKnownEncryptAlgorithm(algorithmType), is(expectedKnown));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertFindEncryptCapabilityArguments")
    void assertFindEncryptCapability(final String name, final String algorithmType, final Boolean expectedDecrypt,
                                     final Boolean expectedEquivalentFilter, final Boolean expectedLike) {
        Map<String, Boolean> actual = EncryptAlgorithmRecommendationService.findEncryptCapability(algorithmType);
        assertThat(actual.get("supports_decrypt"), is(expectedDecrypt));
        assertThat(actual.get("supports_equivalent_filter"), is(expectedEquivalentFilter));
        assertThat(actual.get("supports_like"), is(expectedLike));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithSpecifiedBuiltinPrimary() {
        EncryptWorkflowState workflowState = new EncryptWorkflowState();
        workflowState.getOptions().setRequiresDecrypt(true);
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("AES");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(workflowState, request, List.of(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getAlgorithmRole(), is("primary"));
        assertThat(actual.get(0).getAlgorithmType(), is("AES"));
        assertThat(actual.get(0).getSource(), is("builtin"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithUnknownSpecifiedPrimary() {
        EncryptWorkflowState workflowState = new EncryptWorkflowState();
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("SM4");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(workflowState, request, List.of(createAlgorithmRow("AES", true, true, false)), issues);
        assertTrue(actual.isEmpty());
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithMissingAssistedQueryAlgorithm() {
        EncryptWorkflowState workflowState = new EncryptWorkflowState();
        workflowState.getOptions().setRequiresEqualityFilter(true);
        WorkflowRequest request = new WorkflowRequest();
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(workflowState, request, List.of(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getAlgorithmType(), is("AES"));
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithCustomPrimaryWarning() {
        EncryptWorkflowState workflowState = new EncryptWorkflowState();
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("FPE");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(workflowState, request, List.of(createAlgorithmRow("FPE", null, null, null)), issues);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSource(), is("custom-spi"));
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.CUSTOM_ALGORITHM_CAPABILITY_UNCONFIRMED));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithLikeRequirementUsesLikeCapableAlgorithms() {
        EncryptWorkflowState workflowState = new EncryptWorkflowState();
        workflowState.getOptions().setRequiresLikeQuery(true);
        WorkflowRequest request = new WorkflowRequest();
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(workflowState, request, List.of(
                createAlgorithmRow("AES", true, true, false),
                createAlgorithmRow("FPE", null, null, true)), issues);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getAlgorithmType(), is("FPE"));
        assertThat(actual.get(1).getAlgorithmRole(), is("like_query"));
        assertThat(actual.get(1).getAlgorithmType(), is("FPE"));
        assertFalse(issues.isEmpty());
    }
    
    private static Stream<Arguments> assertIsKnownEncryptAlgorithmArguments() {
        return Stream.of(
                Arguments.of("aes is built in", "AES", true),
                Arguments.of("md5 is built in", "MD5", true),
                Arguments.of("sm4 is custom", "SM4", false));
    }
    
    private static Stream<Arguments> assertFindEncryptCapabilityArguments() {
        return Stream.of(
                Arguments.of("aes capability", "AES", true, true, false),
                Arguments.of("md5 capability", "MD5", false, true, false),
                Arguments.of("unknown capability", "CUSTOM", null, null, null));
    }
    
    private Map<String, Object> createAlgorithmRow(final String type, final Boolean decrypt, final Boolean equivalentFilter, final Boolean like) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", type);
        result.put("supports_decrypt", decrypt);
        result.put("supports_equivalent_filter", equivalentFilter);
        result.put("supports_like", like);
        return result;
    }
}

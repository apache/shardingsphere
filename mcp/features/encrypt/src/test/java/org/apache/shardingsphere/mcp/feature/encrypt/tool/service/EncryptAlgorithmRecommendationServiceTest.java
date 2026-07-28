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

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowQueryResult;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptAlgorithmRecommendationServiceTest {
    
    private final EncryptAlgorithmRecommendationService service = new EncryptAlgorithmRecommendationService();
    
    @Test
    void assertRecommendEncryptAlgorithmsWithSpecifiedPrimary() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresDecrypt(true);
        request.setAlgorithmType("AES");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getAlgorithmRole(), is("primary"));
        assertThat(actual.getFirst().getAlgorithmType(), is("AES"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsRejectsDecryptUnsupportedPrimary() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresDecrypt(true);
        request.setAlgorithmType("MD5");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("MD5", false, true, false)), issues);
        assertTrue(actual.isEmpty());
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithUnknownSpecifiedPrimary() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("SM4");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("AES", true, true, false)), issues);
        assertTrue(actual.isEmpty());
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithoutAvailableAlgorithm() {
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(new EncryptWorkflowRequest(), WorkflowQueryResult.confirmed(List.of()), issues);
        assertTrue(actual.isEmpty());
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsFallsBackToFirstAvailableAlgorithm() {
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(
                new EncryptWorkflowRequest(), confirmed(createAlgorithmRow("FPE", null, null, null)), issues);
        assertThat(actual.getFirst().getAlgorithmType(), is("FPE"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithMissingAssistedQueryAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresEqualityFilter(true);
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getAlgorithmType(), is("AES"));
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithDefaultAssistedQueryAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresEqualityFilter(true);
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(
                createAlgorithmRow("AES", true, true, false), createAlgorithmRow("MD5", false, true, false)), issues);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(1).getAlgorithmRole(), is("assisted_query"));
        assertThat(actual.get(1).getAlgorithmType(), is("MD5"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithSpecifiedAssistedQueryAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresEqualityFilter(true);
        request.getOptions().setAssistedQueryAlgorithmType("CUSTOM_HASH");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(
                createAlgorithmRow("AES", true, true, false), createAlgorithmRow("CUSTOM_HASH", null, true, false)), issues);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(1).getAlgorithmType(), is("CUSTOM_HASH"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsRejectsUnavailableSpecifiedAssistedQueryAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresEqualityFilter(true);
        request.getOptions().setAssistedQueryAlgorithmType("MD5");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(issues.size(), is(1));
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithUnconfirmedCapability() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("FPE");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("FPE", null, null, null)), issues);
        assertThat(actual.size(), is(1));
        assertFalse(actual.getFirst().getRiskNotes().isEmpty());
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithLikeRequirementUsesLikeCapableAlgorithms() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresLikeQuery(true);
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(
                createAlgorithmRow("AES", true, true, false),
                createAlgorithmRow("FPE", null, null, true)), issues);
        assertThat(actual.size(), is(2));
        assertThat(actual.getFirst().getAlgorithmType(), is("FPE"));
        assertThat(actual.get(1).getAlgorithmRole(), is("like_query"));
        assertThat(actual.get(1).getAlgorithmType(), is("FPE"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsRejectsKnownUnsupportedLikeAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresLikeQuery(true);
        request.getOptions().setLikeQueryAlgorithmType("AES");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithoutLikeCapableAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresLikeQuery(true);
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsRejectsUnavailableSpecifiedLikeAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresLikeQuery(true);
        request.getOptions().setLikeQueryAlgorithmType("FPE");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(createAlgorithmRow("AES", true, true, false)), issues);
        assertThat(actual.size(), is(1));
        assertThat(issues.size(), is(1));
        assertThat(issues.getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithSpecifiedUnconfirmedLikeAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.getOptions().setRequiresLikeQuery(true);
        request.getOptions().setLikeQueryAlgorithmType("FPE");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, confirmed(
                createAlgorithmRow("AES", true, true, false), createAlgorithmRow("FPE", null, null, true)), issues);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(1).getAlgorithmType(), is("FPE"));
        assertThat(actual.get(1).getRecommendationScore(), is(70));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendEncryptAlgorithmsWithUnconfirmedAvailability() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("CUSTOM");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(request, WorkflowQueryResult.fallback(List.of()), issues);
        assertThat(actual.getFirst().getAlgorithmType(), is("CUSTOM"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendAssistedQueryAlgorithmWithUnconfirmedAvailability() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("AES");
        request.getOptions().setRequiresEqualityFilter(true);
        request.getOptions().setAssistedQueryAlgorithmType("CUSTOM_HASH");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(
                request, WorkflowQueryResult.fallback(List.of(createAlgorithmRow("AES", true, true, false))), issues);
        assertThat(actual.get(1).getAlgorithmType(), is("CUSTOM_HASH"));
        assertTrue(issues.isEmpty());
    }
    
    @Test
    void assertRecommendLikeQueryAlgorithmWithUnconfirmedAvailability() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("AES");
        request.getOptions().setRequiresLikeQuery(true);
        request.getOptions().setLikeQueryAlgorithmType("CUSTOM_LIKE");
        List<WorkflowIssue> issues = new LinkedList<>();
        List<AlgorithmCandidate> actual = service.recommendEncryptAlgorithms(
                request, WorkflowQueryResult.fallback(List.of(createAlgorithmRow("AES", true, true, false))), issues);
        assertThat(actual.get(1).getAlgorithmType(), is("CUSTOM_LIKE"));
        assertTrue(issues.isEmpty());
    }
    
    @SafeVarargs
    private WorkflowQueryResult confirmed(final Map<String, Object>... rows) {
        return WorkflowQueryResult.confirmed(List.of(rows));
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

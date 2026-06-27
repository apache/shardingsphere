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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowAlgorithmRecommendationServiceTest {
    
    private final ShadowAlgorithmRecommendationService service = new ShadowAlgorithmRecommendationService();
    
    @Test
    void assertRecommendSpecifiedAlgorithm() {
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("value_match");
        List<AlgorithmCandidate> actual = service.recommendShadowAlgorithms(request, List.of(Map.of("type", "VALUE_MATCH")), new LinkedList<>());
        assertThat(actual.getFirst().getAlgorithmType(), is("VALUE_MATCH"));
    }
    
    @Test
    void assertRecommendSqlHintDefault() {
        WorkflowRequest request = new WorkflowRequest();
        List<AlgorithmCandidate> actual = service.recommendShadowAlgorithms(request, List.of(Map.of("type", "VALUE_MATCH"), Map.of("type", "SQL_HINT")), new LinkedList<>());
        assertThat(actual.getFirst().getAlgorithmType(), is("SQL_HINT"));
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

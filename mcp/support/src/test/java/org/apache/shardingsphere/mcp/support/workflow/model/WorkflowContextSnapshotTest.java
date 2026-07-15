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

package org.apache.shardingsphere.mcp.support.workflow.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowContextSnapshotTest {
    
    @Test
    void assertCopyCreatesDetachedSnapshot() {
        WorkflowContextSnapshot originalSnapshot = createSnapshot();
        WorkflowContextSnapshot actualSnapshot = originalSnapshot.copy();
        assertThat(actualSnapshot.getPlanId(), is("plan-1"));
        assertThat(actualSnapshot.getWorkflowKind(), is(WorkflowKind.valueOf("encrypt.rule")));
        originalSnapshot.getRequest().setTable("archived_orders");
        originalSnapshot.getClarifiedIntent().getClarificationMessages().add("another question");
        originalSnapshot.getClarifiedIntent().getUnresolvedFields().add("schema");
        originalSnapshot.getClarifiedIntent().getInferredValues().put("requires_decrypt", false);
        originalSnapshot.getFeatureData().getAlgorithmProperties("primary").put("mode", "changed");
        originalSnapshot.getInteractionPlan().getValidationStrategy().put("layers", List.of("rule"));
        getStringList(originalSnapshot.getIssues().get(0).getDetails(), "missing_fields").add("schema");
        getStringList(originalSnapshot.getValidationReport().getMismatches().get(0), "layers").add("rule");
        originalSnapshot.getAlgorithmCandidates().clear();
        originalSnapshot.getPropertyRequirements().clear();
        originalSnapshot.getDdlArtifacts().clear();
        originalSnapshot.getRuleArtifacts().clear();
        originalSnapshot.getIndexPlans().clear();
        assertThat(actualSnapshot.getRequest().getTable(), is("orders"));
        assertThat(actualSnapshot.getClarifiedIntent().getClarificationMessages(), is(List.of("provide schema")));
        assertTrue(actualSnapshot.getClarifiedIntent().getUnresolvedFields().isEmpty());
        assertTrue(actualSnapshot.getClarifiedIntent().getInferredValues().isEmpty());
        assertThat(actualSnapshot.getFeatureData().getAlgorithmProperties("primary").get("mode"), is("strict"));
        assertThat(actualSnapshot.getInteractionPlan().getValidationStrategy().get("layers"), is(List.of("ddl")));
        assertThat(actualSnapshot.getIssues().get(0).getDetails().get("missing_fields"), is(List.of("column")));
        assertThat(actualSnapshot.getValidationReport().getMismatches().get(0).get("layers"), is(List.of("ddl")));
        assertThat(actualSnapshot.getAlgorithmCandidates().size(), is(1));
        assertThat(actualSnapshot.getPropertyRequirements().size(), is(1));
        assertThat(actualSnapshot.getDdlArtifacts().size(), is(1));
        assertThat(actualSnapshot.getRuleArtifacts().size(), is(1));
        assertThat(actualSnapshot.getIndexPlans().size(), is(1));
    }
    
    @Test
    void assertClearPlanningStateRemovesTransientArtifacts() {
        WorkflowContextSnapshot snapshot = createSnapshot();
        WorkflowFeatureData featureData = snapshot.getFeatureData();
        assertThat(snapshot.getFeatureData(), is(featureData));
        snapshot.clearPlanningState();
        assertTrue(snapshot.getIssues().isEmpty());
        assertTrue(snapshot.getAlgorithmCandidates().isEmpty());
        assertTrue(snapshot.getPropertyRequirements().isEmpty());
        assertTrue(snapshot.getDdlArtifacts().isEmpty());
        assertTrue(snapshot.getRuleArtifacts().isEmpty());
        assertTrue(snapshot.getIndexPlans().isEmpty());
        assertThat(snapshot.getFeatureData(), is(featureData));
        assertNull(snapshot.getValidationReport());
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        result.setStatus("planned");
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("phone");
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.getClarificationMessages().add("provide schema");
        result.setClarifiedIntent(clarifiedIntent);
        StubWorkflowFeatureData featureData = new StubWorkflowFeatureData();
        featureData.getAlgorithmProperties("primary").put("mode", "strict");
        result.setFeatureData(featureData);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        interactionPlan.getValidationStrategy().put("layers", List.of("ddl"));
        result.setInteractionPlan(interactionPlan);
        Map<String, Object> issueDetails = new LinkedHashMap<>(1, 1F);
        issueDetails.put("missing_fields", new LinkedList<>(List.of("column")));
        result.getIssues().add(new WorkflowIssue("code", "error", "stage", "message", "action", true, issueDetails));
        result.getAlgorithmCandidates().add(AlgorithmCandidate.builder().algorithmRole("primary").algorithmType("AES")
                .supportsDecrypt(true).supportsEquivalentFilter(true).supportsLike(false).recommendationScore(90).recommendationReason("reason").riskNotes("").build());
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "key", true, true, "desc", ""));
        result.getDdlArtifacts().add(new DDLArtifact("ddl", "ALTER TABLE t ADD c VARCHAR(32)", 1));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t"));
        result.getIndexPlans().add(new IndexPlan("idx", "c", "reason", "CREATE INDEX idx ON t(c)"));
        ValidationReport validationReport = new ValidationReport();
        Map<String, Object> mismatch = new LinkedHashMap<>(2, 1F);
        mismatch.put("code", "mismatch");
        mismatch.put("layers", new LinkedList<>(List.of("ddl")));
        validationReport.getMismatches().add(mismatch);
        result.setValidationReport(validationReport);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getStringList(final Map<String, Object> source, final String key) {
        return (List<String>) source.get(key);
    }
    
    private static final class StubWorkflowFeatureData implements WorkflowFeatureData {
        
        private final Map<String, String> algorithmProperties = new LinkedHashMap<>(4, 1F);
        
        @Override
        public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
            return "primary".equals(algorithmRole) ? algorithmProperties : Collections.emptyMap();
        }
        
        @Override
        public WorkflowFeatureData copy() {
            StubWorkflowFeatureData result = new StubWorkflowFeatureData();
            result.algorithmProperties.putAll(algorithmProperties);
            return result;
        }
    }
}

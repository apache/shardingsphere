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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowIntentResolverTest {
    
    @Test
    void assertResolveUsesNaturalLanguageHintsForEncryptWorkflow() {
        WorkflowRequest request = new WorkflowRequest();
        request.setColumn("phone");
        request.setNaturalLanguageIntent("给手机号加密，可逆，不需要等值，需要模糊");
        ClarifiedIntent actual = new WorkflowIntentResolver().resolve(request);
        assertThat(actual.getFeatureType(), is("encrypt"));
        assertThat(actual.getOperationType(), is("create"));
        assertThat(actual.getFieldSemantics(), is("phone"));
        assertTrue(actual.getRequiresDecrypt());
        assertFalse(actual.getRequiresEqualityFilter());
        assertTrue(actual.getRequiresLikeQuery());
    }
    
    @Test
    void assertResolvePrefersStructuredEvidenceOverNaturalLanguageHints() {
        WorkflowRequest request = new WorkflowRequest();
        request.setFeatureType("encrypt");
        request.setFieldSemantics("id_card");
        request.setRequiresDecrypt(false);
        request.setRequiresEqualityFilter(true);
        request.setRequiresLikeQuery(false);
        request.setNaturalLanguageIntent("给手机号加密，可逆，不需要等值，需要模糊");
        ClarifiedIntent actual = new WorkflowIntentResolver().resolve(request);
        assertThat(actual.getFeatureType(), is("encrypt"));
        assertThat(actual.getFieldSemantics(), is("id_card"));
        assertFalse(actual.getRequiresDecrypt());
        assertTrue(actual.getRequiresEqualityFilter());
        assertFalse(actual.getRequiresLikeQuery());
    }
    
    @Test
    void assertResolveSkipsEncryptCapabilityQuestionsForDropWorkflow() {
        WorkflowRequest request = new WorkflowRequest();
        request.setFeatureType("encrypt");
        request.setOperationType("drop");
        request.setColumn("phone");
        ClarifiedIntent actual = new WorkflowIntentResolver().resolve(request);
        assertThat(actual.getFeatureType(), is("encrypt"));
        assertThat(actual.getOperationType(), is("drop"));
        assertThat(actual.getFieldSemantics(), is("phone"));
        assertNull(actual.getRequiresDecrypt());
        assertNull(actual.getRequiresEqualityFilter());
        assertNull(actual.getRequiresLikeQuery());
        assertTrue(actual.getPendingQuestions().isEmpty());
    }
}

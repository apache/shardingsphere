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
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptWorkflowIntentResolverTest {
    
    @Test
    void assertResolveUsesExplicitArgumentsWithoutInference() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setOperationType("alter");
        request.setFieldSemantics("email");
        request.getOptions().setRequiresDecrypt(true);
        request.getOptions().setRequiresEqualityFilter(false);
        request.getOptions().setRequiresLikeQuery(false);
        ClarifiedIntent actual = new EncryptWorkflowIntentResolver().resolve(request);
        assertThat(actual.getOperationType(), is("alter"));
        assertThat(actual.getFieldSemantics(), is("email"));
        assertThat(actual.getInferredValues(), is(Map.of()));
        assertThat(actual.getUnresolvedFields(), is(List.of()));
        assertThat(actual.getClarificationMessages(), is(List.of()));
        assertThat(actual.getReasoningNotes(), is("Resolved from explicit arguments."));
    }
    
    @Test
    void assertResolveRecordsHeuristicInference() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setColumn("customer_phone");
        request.setNaturalLanguageIntent("create reversible encryption with equality and LIKE query");
        ClarifiedIntent actual = new EncryptWorkflowIntentResolver().resolve(request);
        assertThat(actual.getOperationType(), is("create"));
        assertThat(actual.getFieldSemantics(), is("phone"));
        assertThat(actual.getInferredValues().get("operation_type"), is("create"));
        assertThat(actual.getInferredValues().get("field_semantics"), is("phone"));
        assertTrue((Boolean) actual.getInferredValues().get("requires_decrypt"));
        assertTrue((Boolean) actual.getInferredValues().get("requires_equality_filter"));
        assertTrue((Boolean) actual.getInferredValues().get("requires_like_query"));
        assertThat(actual.getUnresolvedFields(), is(List.of()));
    }
    
    @Test
    void assertResolveRecordsChineseHeuristicInference() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setNaturalLanguageIntent("给手机号配置可逆加密，支持等值查询和模糊查询");
        ClarifiedIntent actual = new EncryptWorkflowIntentResolver().resolve(request);
        assertThat(actual.getFieldSemantics(), is("phone"));
        assertTrue((Boolean) actual.getInferredValues().get("requires_decrypt"));
        assertTrue((Boolean) actual.getInferredValues().get("requires_equality_filter"));
        assertTrue((Boolean) actual.getInferredValues().get("requires_like_query"));
        assertThat(actual.getUnresolvedFields(), is(List.of()));
    }
    
    @Test
    void assertResolveAddsUnresolvedFieldsWhenRequirementsNeedClarification() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setColumn("customer_phone");
        request.setNaturalLanguageIntent("create encrypt rule");
        ClarifiedIntent actual = new EncryptWorkflowIntentResolver().resolve(request);
        assertThat(actual.getClarificationMessages(), is(List.of("Do you need reversible decryption?", "Do you need equality query?", "Do you need LIKE query?")));
        assertThat(actual.getUnresolvedFields(), is(List.of("requires_decrypt", "requires_equality_filter", "requires_like_query")));
        assertThat(actual.getReasoningNotes(),
                is("Resolved from explicit arguments, heuristic inference for operation_type, field_semantics, unresolved fields: requires_decrypt, requires_equality_filter, requires_like_query."));
    }
}

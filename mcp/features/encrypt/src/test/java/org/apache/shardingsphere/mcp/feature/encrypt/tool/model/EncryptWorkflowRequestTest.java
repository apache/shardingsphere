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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.model;

import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptWorkflowRequestTest {
    
    @Test
    void assertCopyCreatesDetachedRequest() {
        EncryptWorkflowRequest originalRequest = new EncryptWorkflowRequest();
        originalRequest.setAlgorithmType("AES");
        originalRequest.setRequiresDecrypt(true);
        originalRequest.setAssistedQueryAlgorithmType("MD5");
        originalRequest.getAssistedQueryAlgorithmProperties().put("digest-algorithm-name", "SHA-256");
        EncryptWorkflowRequest actualRequest = originalRequest.copy();
        originalRequest.setAssistedQueryAlgorithmType("SM3");
        originalRequest.getAssistedQueryAlgorithmProperties().put("salt", "abc");
        assertThat(actualRequest.getAlgorithmType(), is("AES"));
        assertTrue(actualRequest.getRequiresDecrypt());
        assertThat(actualRequest.getAssistedQueryAlgorithmType(), is("MD5"));
        assertThat(actualRequest.getAssistedQueryAlgorithmProperties().size(), is(1));
    }
    
    @Test
    void assertMergeKeepsPreviousRequestStateAndOverlaysCurrentInputs() {
        WorkflowRequest previousRequest = new WorkflowRequest();
        previousRequest.setPlanId("plan-1");
        previousRequest.setDatabase("logic_db");
        previousRequest.setTable("orders");
        previousRequest.setColumn("phone");
        previousRequest.getPrimaryAlgorithmProperties().put("aes-key-value", "previous-key");
        EncryptWorkflowState previousState = new EncryptWorkflowState();
        previousState.setRequiresDecrypt(true);
        previousState.setAssistedQueryAlgorithmType("MD5");
        previousState.getAssistedQueryAlgorithmProperties().put("digest-algorithm-name", "SHA-256");
        EncryptWorkflowRequest currentRequest = new EncryptWorkflowRequest();
        currentRequest.setOperationType("alter");
        currentRequest.setRequiresLikeQuery(true);
        currentRequest.setLikeQueryAlgorithmType("CHAR_DIGEST_LIKE");
        currentRequest.getPrimaryAlgorithmProperties().put("aes-key-value", "current-key");
        EncryptWorkflowRequest actualRequest = EncryptWorkflowRequest.merge(previousRequest, previousState, currentRequest);
        assertThat(actualRequest.getPlanId(), is("plan-1"));
        assertThat(actualRequest.getDatabase(), is("logic_db"));
        assertThat(actualRequest.getTable(), is("orders"));
        assertThat(actualRequest.getColumn(), is("phone"));
        assertThat(actualRequest.getOperationType(), is("alter"));
        assertTrue(actualRequest.getRequiresDecrypt());
        assertTrue(actualRequest.getRequiresLikeQuery());
        assertThat(actualRequest.getAssistedQueryAlgorithmType(), is("MD5"));
        assertThat(actualRequest.getLikeQueryAlgorithmType(), is("CHAR_DIGEST_LIKE"));
        assertThat(actualRequest.getPrimaryAlgorithmProperties().get("aes-key-value"), is("current-key"));
        assertThat(actualRequest.getAssistedQueryAlgorithmProperties().get("digest-algorithm-name"), is("SHA-256"));
    }
}

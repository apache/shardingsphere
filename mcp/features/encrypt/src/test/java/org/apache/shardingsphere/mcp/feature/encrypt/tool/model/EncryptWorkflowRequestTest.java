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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptWorkflowRequestTest {
    
    @Test
    void assertCopyCreatesDetachedRequest() {
        EncryptWorkflowRequest originalRequest = new EncryptWorkflowRequest();
        originalRequest.setAlgorithmType("AES");
        originalRequest.getOptions().setRequiresDecrypt(true);
        originalRequest.getOptions().setAssistedQueryAlgorithmType("MD5");
        originalRequest.getOptions().getAssistedQueryAlgorithmProperties().put("digest-algorithm-name", "SHA-256");
        EncryptWorkflowRequest actualRequest = originalRequest.copy();
        originalRequest.getOptions().setAssistedQueryAlgorithmType("SM3");
        originalRequest.getOptions().getAssistedQueryAlgorithmProperties().put("salt", "abc");
        assertThat(actualRequest.getAlgorithmType(), is("AES"));
        assertTrue(actualRequest.getOptions().getRequiresDecrypt());
        assertThat(actualRequest.getOptions().getAssistedQueryAlgorithmType(), is("MD5"));
        assertThat(actualRequest.getOptions().getAssistedQueryAlgorithmProperties().size(), is(1));
    }
    
    @Test
    void assertMergeKeepsPreviousRequestStateAndOverlaysCurrentInputs() {
        EncryptWorkflowRequest previousRequest = new EncryptWorkflowRequest();
        previousRequest.setPlanId("plan-1");
        previousRequest.setDatabase("logic_db");
        previousRequest.setTable("orders");
        previousRequest.setColumn("phone");
        previousRequest.getPrimaryAlgorithmProperties().put("aes-key-value", "previous-key");
        previousRequest.getOptions().setRequiresDecrypt(true);
        previousRequest.getOptions().setAssistedQueryAlgorithmType("MD5");
        previousRequest.getOptions().getAssistedQueryAlgorithmProperties().put("digest-algorithm-name", "SHA-256");
        EncryptWorkflowRequest currentRequest = new EncryptWorkflowRequest();
        currentRequest.setOperationType("alter");
        currentRequest.getOptions().setRequiresLikeQuery(true);
        currentRequest.getOptions().setLikeQueryAlgorithmType("CHAR_DIGEST_LIKE");
        currentRequest.getPrimaryAlgorithmProperties().put("aes-key-value", "current-key");
        EncryptWorkflowRequest actualRequest = EncryptWorkflowRequest.merge(previousRequest, currentRequest);
        assertThat(actualRequest.getPlanId(), is("plan-1"));
        assertThat(actualRequest.getDatabase(), is("logic_db"));
        assertThat(actualRequest.getTable(), is("orders"));
        assertThat(actualRequest.getColumn(), is("phone"));
        assertThat(actualRequest.getOperationType(), is("alter"));
        assertTrue(actualRequest.getOptions().getRequiresDecrypt());
        assertTrue(actualRequest.getOptions().getRequiresLikeQuery());
        assertThat(actualRequest.getOptions().getAssistedQueryAlgorithmType(), is("MD5"));
        assertThat(actualRequest.getOptions().getLikeQueryAlgorithmType(), is("CHAR_DIGEST_LIKE"));
        assertThat(actualRequest.getPrimaryAlgorithmProperties().get("aes-key-value"), is("current-key"));
        assertThat(actualRequest.getOptions().getAssistedQueryAlgorithmProperties().get("digest-algorithm-name"), is("SHA-256"));
    }
}

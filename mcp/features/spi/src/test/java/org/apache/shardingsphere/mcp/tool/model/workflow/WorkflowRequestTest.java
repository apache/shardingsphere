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

package org.apache.shardingsphere.mcp.tool.model.workflow;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowRequestTest {
    
    @Test
    void assertMergeKeepsPreviousContextAndOverlaysCurrentInputs() {
        WorkflowRequest previous = new WorkflowRequest();
        previous.setPlanId("plan-1");
        previous.setDatabase("logic_db");
        previous.setTable("orders");
        previous.setColumn("phone");
        previous.setNaturalLanguageIntent("mask phone");
        previous.setExecutionMode("review-then-execute");
        previous.getPrimaryAlgorithmProperties().put("first-n", "1");
        previous.getApprovedSteps().add("ddl");
        WorkflowRequest current = new WorkflowRequest();
        current.setOperationType("alter");
        current.setNaturalLanguageIntent("mask mobile");
        current.getPrimaryAlgorithmProperties().put("replace-char", "*");
        current.getApprovedSteps().add("rule_distsql");
        WorkflowRequest actualRequest = WorkflowRequest.merge(previous, current);
        assertThat(actualRequest.getPlanId(), is("plan-1"));
        assertThat(actualRequest.getDatabase(), is("logic_db"));
        assertThat(actualRequest.getTable(), is("orders"));
        assertThat(actualRequest.getColumn(), is("phone"));
        assertThat(actualRequest.getNaturalLanguageIntent(), is("mask mobile"));
        assertThat(actualRequest.getOperationType(), is("alter"));
        assertThat(actualRequest.getPrimaryAlgorithmProperties().get("first-n"), is("1"));
        assertThat(actualRequest.getPrimaryAlgorithmProperties().get("replace-char"), is("*"));
        assertThat(actualRequest.getApprovedSteps(), is(List.of("rule_distsql")));
    }
    
    @Test
    void assertCopyCreatesDetachedRequest() {
        WorkflowRequest originalRequest = new WorkflowRequest();
        originalRequest.setPlanId("plan-1");
        originalRequest.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        originalRequest.getApprovedSteps().add("ddl");
        WorkflowRequest actualRequest = originalRequest.copy();
        originalRequest.getPrimaryAlgorithmProperties().put("salt", "abc");
        originalRequest.getApprovedSteps().add("rule_distsql");
        assertThat(actualRequest.getPlanId(), is("plan-1"));
        assertThat(actualRequest.getPrimaryAlgorithmProperties().size(), is(1));
        assertThat(actualRequest.getPrimaryAlgorithmProperties().get("aes-key-value"), is("123456"));
        assertThat(actualRequest.getApprovedSteps(), is(List.of("ddl")));
    }
}

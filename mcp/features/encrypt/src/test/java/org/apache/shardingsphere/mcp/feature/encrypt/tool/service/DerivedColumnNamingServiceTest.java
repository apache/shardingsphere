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
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DerivedColumnNamingServiceTest {
    
    private final DerivedColumnNamingService service = new DerivedColumnNamingService();
    
    @Test
    void assertCreatePlanWithDefaultNames() {
        WorkflowRequest request = new WorkflowRequest();
        request.setColumn("phone");
        EncryptWorkflowState workflowState = createWorkflowState(true, true);
        List<WorkflowIssue> issues = new LinkedList<>();
        DerivedColumnPlan actual = service.createPlan(request, workflowState, new LinkedHashSet<>(), issues);
        assertThat(actual.getCipherColumnName(), is("phone_cipher"));
        assertThat(actual.getAssistedQueryColumnName(), is("phone_assisted_query"));
        assertThat(actual.getLikeQueryColumnName(), is("phone_like_query"));
        assertThat(actual.getNameCollisions().size(), is(0));
        assertThat(issues.size(), is(0));
    }
    
    @Test
    void assertCreatePlanWithUnsafeOverride() {
        WorkflowRequest request = new WorkflowRequest();
        request.setColumn("phone");
        EncryptWorkflowState workflowState = createWorkflowState(false, false);
        workflowState.getOptions().setCipherColumnName("phone-cipher");
        List<WorkflowIssue> issues = new LinkedList<>();
        DerivedColumnPlan actual = service.createPlan(request, workflowState, new LinkedHashSet<>(), issues);
        assertThat(actual.getCipherColumnName(), is("phone_cipher"));
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.USER_OVERRIDE_NAME_UNSAFE));
    }
    
    @Test
    void assertCreatePlanWithNameCollision() {
        WorkflowRequest request = new WorkflowRequest();
        request.setColumn("phone");
        EncryptWorkflowState workflowState = createWorkflowState(false, false);
        Set<String> existingNames = new LinkedHashSet<>(List.of("phone_cipher", "phone_cipher_1"));
        List<WorkflowIssue> issues = new LinkedList<>();
        DerivedColumnPlan actual = service.createPlan(request, workflowState, existingNames, issues);
        assertThat(actual.getCipherColumnName(), is("phone_cipher_2"));
        assertThat(actual.getNameCollisions().size(), is(1));
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.AUTO_RENAMED_DUE_TO_CONFLICT));
        assertFalse(existingNames.isEmpty());
    }
    
    private EncryptWorkflowState createWorkflowState(final boolean equalityFilter, final boolean likeQuery) {
        EncryptWorkflowState result = new EncryptWorkflowState();
        result.getOptions().setRequiresEqualityFilter(equalityFilter);
        result.getOptions().setRequiresLikeQuery(likeQuery);
        return result;
    }
}

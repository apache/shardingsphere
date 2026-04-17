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

import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DerivedColumnNamingServiceTest {
    
    @Test
    void assertCreatePlanAddsNumericSuffixWhenDerivedColumnConflicts() {
        DerivedColumnNamingService service = new DerivedColumnNamingService();
        WorkflowRequest request = new WorkflowRequest();
        request.setColumn("status");
        List<WorkflowIssue> issues = new LinkedList<>();
        DerivedColumnPlan actual = service.createPlan(request, false, false, new LinkedHashSet<>(List.of("status_cipher", "status_cipher_1")), issues);
        assertThat(actual.getCipherColumnName(), is("status_cipher_2"));
        assertThat(actual.getNameCollisions().size(), is(1));
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.AUTO_RENAMED_DUE_TO_CONFLICT));
    }
    
    @Test
    void assertCreatePlanFallsBackToDefaultWhenOverrideNameIsUnsafe() {
        DerivedColumnNamingService service = new DerivedColumnNamingService();
        WorkflowRequest request = new WorkflowRequest();
        request.setColumn("status");
        request.setCipherColumnName("bad name");
        List<WorkflowIssue> issues = new LinkedList<>();
        DerivedColumnPlan actual = service.createPlan(request, false, false, new LinkedHashSet<>(), issues);
        assertThat(actual.getCipherColumnName(), is("status_cipher"));
        assertThat(issues.get(0).getCode(), is(WorkflowIssueCode.USER_OVERRIDE_NAME_UNSAFE));
    }
}

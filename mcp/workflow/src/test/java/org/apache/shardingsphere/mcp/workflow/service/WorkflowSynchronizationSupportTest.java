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

package org.apache.shardingsphere.mcp.workflow.service;

import org.apache.shardingsphere.mcp.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowSynchronizationSupportTest {
    
    @Test
    void assertSynchronize() {
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus(WorkflowLifecycle.STATUS_PASSED);
        WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport(1, 0L);
        workflowSynchronizationSupport.synchronize(() -> validationReport);
    }
    
    @Test
    void assertSynchronizeWhenValidationFails() {
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus(WorkflowLifecycle.STATUS_FAILED);
        validationReport.getMismatches().add(Map.of("code", WorkflowIssueCode.DDL_STATE_MISMATCH,
                "impact", "Derived column is not visible from Proxy information_schema."));
        WorkflowSynchronizationSupport workflowSynchronizationSupport = new WorkflowSynchronizationSupport(1, 0L);
        WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class, () -> workflowSynchronizationSupport.synchronize(() -> validationReport));
        assertThat(actual.getIssueCode(), is(WorkflowIssueCode.DDL_STATE_MISMATCH));
        assertThat(actual.getMessage(), is("Derived column is not visible from Proxy information_schema."));
    }
}

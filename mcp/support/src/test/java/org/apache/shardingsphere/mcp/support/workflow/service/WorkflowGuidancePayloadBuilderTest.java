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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowGuidancePayloadBuilderTest {
    
    @Test
    void assertAppendApplyGuidanceForSecretReferenceRecovery() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("category", MCPDiagnosticCategory.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED);
        WorkflowGuidancePayloadBuilder.appendApplyGuidance(payload, WorkflowLifecycle.STATUS_FAILED);
        Map<?, ?> actual = (Map<?, ?>) ((List<?>) payload.get("next_actions")).getFirst();
        assertThat(actual.get("type"), is("ask_user"));
        assertThat(actual.get("required_inputs"), is(List.of("manual_artifacts_executed")));
    }
    
    @Test
    void assertAppendApplyGuidanceForGenericFailure() {
        Map<String, Object> payload = new LinkedHashMap<>();
        WorkflowGuidancePayloadBuilder.appendApplyGuidance(payload, WorkflowLifecycle.STATUS_FAILED);
        Map<?, ?> actual = (Map<?, ?>) ((List<?>) payload.get("next_actions")).getFirst();
        assertThat(actual.get("type"), is("ask_user"));
        assertThat(actual.get("required_inputs"), is(List.of("issues")));
    }
}

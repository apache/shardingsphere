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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRequestBinderTest {
    
    @Test
    void assertBindPlanningRequestBindsCommonFieldsAndFeatureCallbacks() {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("plan_id", "plan-1");
        arguments.put("database", "logic_db");
        arguments.put("schema", "public");
        arguments.put("table", "t_order");
        arguments.put("column", "user_id");
        arguments.put("operation_type", "create");
        arguments.put("natural_language_intent", "encrypt the user id");
        arguments.put("delivery_mode", "step-by-step");
        arguments.put("execution_mode", "manual-only");
        arguments.put("algorithm_type", "MD5");
        arguments.put("structured_intent_evidence", Map.of("field_semantics", "identifier"));
        AtomicReference<String> actualFeatureAlgorithm = new AtomicReference<>();
        AtomicReference<Map<String, Object>> actualStructuredIntentEvidence = new AtomicReference<>();
        WorkflowRequest actual = WorkflowRequestBinder.bindPlanningRequest(arguments, (request, workflowPlanningArguments) -> {
            request.setAlgorithmType(workflowPlanningArguments.getStringArgument("algorithm_type"));
            actualFeatureAlgorithm.set(workflowPlanningArguments.getStringArgument("algorithm_type"));
        }, (request, structuredIntentEvidence) -> {
            request.setFieldSemantics(String.valueOf(structuredIntentEvidence.get("field_semantics")));
            actualStructuredIntentEvidence.set(structuredIntentEvidence);
        });
        assertThat(actual.getPlanId(), is("plan-1"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getSchema(), is("public"));
        assertThat(actual.getTable(), is("t_order"));
        assertThat(actual.getColumn(), is("user_id"));
        assertThat(actual.getOperationType(), is("create"));
        assertThat(actual.getNaturalLanguageIntent(), is("encrypt the user id"));
        assertThat(actual.getDeliveryMode(), is("step-by-step"));
        assertThat(actual.getExecutionMode(), is("manual-only"));
        assertThat(actual.getFieldSemantics(), is("identifier"));
        assertThat(actual.getAlgorithmType(), is("MD5"));
        assertThat(actualFeatureAlgorithm.get(), is("MD5"));
        assertThat(actualStructuredIntentEvidence.get(), is(Map.of("field_semantics", "identifier")));
    }
    
    @Test
    void assertBindPlanningRequestKeepsPlanIdValue() {
        WorkflowRequest actual = WorkflowRequestBinder.bindPlanningRequest(Map.of("plan_id", "plan_id", "database", "logic_db"),
                (request, workflowPlanningArguments) -> {
                }, (request, structuredIntentEvidence) -> {
                });
        assertThat(actual.getPlanId(), is("plan_id"));
        assertThat(actual.getDatabase(), is("logic_db"));
    }
    
    @Test
    void assertBindPlanningRequestRejectsInvalidStructuredIntentEvidence() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> WorkflowRequestBinder.bindPlanningRequest(Map.of(
                "database", "logic_db",
                "structured_intent_evidence", "invalid"), (request, workflowPlanningArguments) -> request.setAlgorithmType("AES"),
                (request, structuredIntentEvidence) -> {
                }));
        assertThat(actual.getMessage(), is("structured_intent_evidence must be an object."));
    }
    
    @Test
    void assertApplyStringField() {
        AtomicReference<String> actual = new AtomicReference<>();
        WorkflowRequestBinder.applyStringField(Map.of("field", 42), "field", actual::set);
        assertThat(actual.get(), is("42"));
    }
    
    @Test
    void assertApplyStringFieldIgnoresMissingField() {
        AtomicReference<String> actual = new AtomicReference<>("unchanged");
        WorkflowRequestBinder.applyStringField(Map.of(), "field", actual::set);
        assertThat(actual.get(), is("unchanged"));
    }
}

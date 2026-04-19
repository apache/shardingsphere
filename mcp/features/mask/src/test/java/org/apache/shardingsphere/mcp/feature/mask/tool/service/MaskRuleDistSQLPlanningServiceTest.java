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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleDistSQLPlanningServiceTest {
    
    private final MaskRuleDistSQLPlanningService service = new MaskRuleDistSQLPlanningService();
    
    @Test
    void assertPlanMaskRuleWithCreate() {
        RuleArtifact actual = service.planMaskRule(createRequest("create"), List.of());
        assertThat(actual.getOperationType(), is("create"));
        assertTrue(actual.getSql().startsWith("CREATE MASK RULE orders"));
        assertTrue(actual.getSql().contains("TYPE(NAME='mask_from_x_to_y'"));
    }
    
    @Test
    void assertPlanMaskRuleWithExistingRules() {
        RuleArtifact actual = service.planMaskRule(createRequest("alter"), List.of(
                Map.of("column", "phone", "algorithm_type", "MD5"),
                Map.of("column", "email", "algorithm_type", "KEEP_FIRST_N_LAST_M", "algorithm_props", "first-n=1")));
        assertThat(actual.getOperationType(), is("alter"));
        assertTrue(actual.getSql().startsWith("ALTER MASK RULE orders"));
        assertTrue(actual.getSql().contains("NAME=email"));
        assertTrue(actual.getSql().contains("NAME=phone"));
    }
    
    @Test
    void assertPlanMaskDropRuleWithoutRemainingColumns() {
        RuleArtifact actual = service.planMaskDropRule(createRequest("drop"), List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        assertThat(actual.getOperationType(), is("drop"));
        assertThat(actual.getSql(), is("DROP MASK RULE orders"));
    }
    
    @Test
    void assertPlanMaskDropRuleWithRemainingColumns() {
        RuleArtifact actual = service.planMaskDropRule(createRequest("drop"), List.of(
                Map.of("column", "phone", "algorithm_type", "MD5"),
                Map.of("column", "email", "algorithm_type", "KEEP_FIRST_N_LAST_M")));
        assertThat(actual.getOperationType(), is("drop"));
        assertTrue(actual.getSql().startsWith("ALTER MASK RULE orders"));
        assertTrue(actual.getSql().contains("NAME=email"));
    }
    
    private WorkflowRequest createRequest(final String operationType) {
        WorkflowRequest result = new WorkflowRequest();
        result.setOperationType(operationType);
        result.setTable("orders");
        result.setColumn("phone");
        result.setAlgorithmType("MASK_FROM_X_TO_Y");
        result.getPrimaryAlgorithmProperties().put("from-x", "4");
        result.getPrimaryAlgorithmProperties().put("to-y", "7");
        result.getPrimaryAlgorithmProperties().put("replace-char", "*");
        return result;
    }
}

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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleDistSQLPlanningServiceTest {
    
    private final MaskRuleDistSQLPlanningService service = new MaskRuleDistSQLPlanningService();
    
    @Test
    void assertPlanMaskRuleWithCreate() {
        List<RuleArtifact> actual = service.planMaskRule(createRequest("create"), List.of(), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getOperationType(), is("create"));
        assertTrue(actual.getFirst().getSql().startsWith("CREATE MASK RULE orders"));
        assertTrue(actual.getFirst().getSql().contains("TYPE(NAME='mask_from_x_to_y'"));
    }
    
    @Test
    void assertPlanMaskRuleWithExistingRules() {
        List<RuleArtifact> actual = service.planMaskRule(createRequest("alter"), List.of(
                Map.of("column", "phone", "algorithm_type", "MD5"),
                Map.of("column", "email", "algorithm_type", "KEEP_FIRST_N_LAST_M", "algorithm_props", "first-n=1")), "MySQL");
        assertThat(actual.size(), is(2));
        assertThat(actual.getFirst().getOperationType(), is("drop"));
        assertThat(actual.getFirst().getSql(), is("DROP MASK RULE orders"));
        assertThat(actual.getLast().getOperationType(), is("create"));
        assertTrue(actual.getLast().getSql().startsWith("CREATE MASK RULE orders"));
        assertTrue(actual.getLast().getSql().contains("NAME=email"));
        assertTrue(actual.getLast().getSql().contains("NAME=phone"));
    }
    
    @Test
    void assertPlanMaskRuleEscapesAlgorithmLiterals() {
        WorkflowRequest request = createRequest("create");
        request.setAlgorithmType("KEEP'X");
        request.getPrimaryAlgorithmProperties().put("replace-char", "x'");
        List<RuleArtifact> actual = service.planMaskRule(request, List.of(), "MySQL");
        assertTrue(actual.getFirst().getSql().contains("TYPE(NAME='keep''x'"));
        assertTrue(actual.getFirst().getSql().contains("'replace-char'='x'''"));
    }
    
    @Test
    void assertPlanMaskRuleFormatsSpecialCharacterIdentifiers() {
        WorkflowRequest request = createRequest("create");
        request.setTable("order detail");
        request.setColumn("Phone Number");
        List<RuleArtifact> actual = service.planMaskRule(request, List.of(), "MySQL");
        assertTrue(actual.getFirst().getSql().startsWith("CREATE MASK RULE `order detail`"));
        assertTrue(actual.getFirst().getSql().contains("NAME=`Phone Number`"));
    }
    
    @Test
    void assertPlanMaskRuleFormatsReservedIdentifiers() {
        WorkflowRequest request = createRequest("create");
        request.setTable("key");
        List<RuleArtifact> actual = service.planMaskRule(request, List.of(), "MySQL");
        assertTrue(actual.getFirst().getSql().startsWith("CREATE MASK RULE `key`"));
        assertTrue(actual.getFirst().getSql().contains("NAME=phone"));
    }
    
    @Test
    void assertPlanMaskRuleRejectsLineTerminatorExistingColumn() {
        MCPInvalidRequestException actualException = assertThrows(MCPInvalidRequestException.class,
                () -> service.planMaskRule(createRequest("alter"), List.of(Map.of("column", "bad\ncolumn", "algorithm_type", "MD5")), "MySQL"));
        assertThat(actualException.getMessage(), is("column `bad\ncolumn` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertPlanMaskDropRuleWithoutRemainingColumns() {
        List<RuleArtifact> actual = service.planMaskDropRule(createRequest("drop"), List.of(Map.of("column", "phone", "algorithm_type", "MD5")), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getOperationType(), is("drop"));
        assertThat(actual.getFirst().getSql(), is("DROP MASK RULE orders"));
    }
    
    @Test
    void assertPlanMaskDropRuleWithRemainingColumns() {
        List<RuleArtifact> actual = service.planMaskDropRule(createRequest("drop"), List.of(
                Map.of("column", "phone", "algorithm_type", "MD5"),
                Map.of("column", "email", "algorithm_type", "KEEP_FIRST_N_LAST_M")), "MySQL");
        assertThat(actual.size(), is(2));
        assertThat(actual.getFirst().getOperationType(), is("drop"));
        assertThat(actual.getFirst().getSql(), is("DROP MASK RULE orders"));
        assertThat(actual.getLast().getOperationType(), is("create"));
        assertTrue(actual.getLast().getSql().startsWith("CREATE MASK RULE orders"));
        assertTrue(actual.getLast().getSql().contains("NAME=email"));
    }
    
    @Test
    void assertPlanMaskDropRulePreservesCaseSensitiveSiblingColumn() {
        WorkflowRequest request = createRequest("drop");
        request.setColumn("\"Phone\"");
        List<RuleArtifact> actual = service.planMaskDropRule(request, List.of(
                Map.of("column", "Phone", "algorithm_type", "MD5"),
                Map.of("column", "phone", "algorithm_type", "KEEP_FIRST_N_LAST_M")), "PostgreSQL");
        assertThat(actual.size(), is(2));
        assertThat(actual.getFirst().getOperationType(), is("drop"));
        assertThat(actual.getLast().getOperationType(), is("create"));
        assertTrue(actual.getLast().getSql().startsWith("CREATE MASK RULE orders"));
        assertTrue(actual.getLast().getSql().contains("NAME=phone"));
    }
    
    @Test
    void assertPlanMaskDropRuleMatchesPostgreSQLUnquotedColumn() {
        WorkflowRequest request = createRequest("drop");
        request.setColumn("Phone");
        List<RuleArtifact> actual = service.planMaskDropRule(request, List.of(Map.of("column", "phone", "algorithm_type", "MD5")), "PostgreSQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getOperationType(), is("drop"));
        assertThat(actual.getFirst().getSql(), is("DROP MASK RULE orders"));
    }
    
    @Test
    void assertPlanMaskDropRuleMatchesCaseInsensitiveColumn() {
        WorkflowRequest request = createRequest("drop");
        request.setColumn("Phone");
        List<RuleArtifact> actual = service.planMaskDropRule(request, List.of(Map.of("column", "phone", "algorithm_type", "MD5")), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getOperationType(), is("drop"));
        assertThat(actual.getFirst().getSql(), is("DROP MASK RULE orders"));
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

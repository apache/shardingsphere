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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaskRuleDistSQLPlanningServiceTest {
    
    private final MaskRuleDistSQLPlanningService service = new MaskRuleDistSQLPlanningService();
    
    @Test
    void assertPlanMaskRuleWithCreate() {
        RuleArtifact actual = service.planMaskRule(createRequest("create"));
        assertThat(actual.getOperationType(), is("create"));
        assertTrue(actual.getSql().startsWith("CREATE MASK RULE orders"));
        assertTrue(actual.getSql().contains("TYPE(NAME='mask_from_x_to_y'"));
    }
    
    @Test
    void assertPlanMaskRuleEscapesAlgorithmLiterals() {
        WorkflowRequest request = createRequest("create");
        request.setAlgorithmType("KEEP'X");
        request.getPrimaryAlgorithmProperties().put("replace-char", "x'");
        RuleArtifact actual = service.planMaskRule(request);
        assertTrue(actual.getSql().contains("TYPE(NAME='keep''x'"));
        assertTrue(actual.getSql().contains("'replace-char'='x'''"));
    }
    
    @Test
    void assertPlanMaskRuleFormatsSpecialCharacterIdentifiers() {
        WorkflowRequest request = createRequest("create");
        request.setTable("order detail");
        request.setColumn("Phone Number");
        RuleArtifact actual = service.planMaskRule(request);
        assertTrue(actual.getSql().startsWith("CREATE MASK RULE `order detail`"));
        assertTrue(actual.getSql().contains("NAME=`Phone Number`"));
    }
    
    @Test
    void assertPlanMaskRuleFormatsReservedIdentifiers() {
        WorkflowRequest request = createRequest("create");
        request.setTable("key");
        RuleArtifact actual = service.planMaskRule(request);
        assertTrue(actual.getSql().startsWith("CREATE MASK RULE `key`"));
        assertTrue(actual.getSql().contains("NAME=phone"));
    }
    
    @Test
    void assertPlanMaskRuleFormatsReservedColumnIdentifier() {
        WorkflowRequest request = createRequest("create");
        request.setTable("table");
        request.setColumn("from");
        RuleArtifact actual = service.planMaskRule(request);
        assertTrue(actual.getSql().startsWith("CREATE MASK RULE `table`"));
        assertTrue(actual.getSql().contains("NAME=`from`"));
    }
    
    @Test
    void assertPlanMaskRuleRejectsLineTerminatorColumn() {
        MCPInvalidRequestException actualException = assertThrows(MCPInvalidRequestException.class,
                () -> service.planMaskRule(createRequestWithColumn("bad\ncolumn")));
        assertThat(actualException.getMessage(), is("column `bad\ncolumn` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertPlanMaskDropRuleWithoutRemainingColumns() {
        RuleArtifact actual = service.planMaskDropRule(createRequest("drop"));
        assertThat(actual.getOperationType(), is("drop"));
        assertThat(actual.getSql(), is("DROP MASK RULE orders"));
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
    
    private WorkflowRequest createRequestWithColumn(final String columnName) {
        WorkflowRequest result = createRequest("create");
        result.setColumn(columnName);
        return result;
    }
}

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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadwriteSplittingStatusDistSQLPlanningServiceTest {
    
    @Test
    void assertPlanStatus() {
        ReadwriteSplittingStatusWorkflowRequest request = createRequest("enable");
        assertThat(new ReadwriteSplittingStatusDistSQLPlanningService().planStatus(request).getSql(),
                is("ALTER READWRITE_SPLITTING RULE `readwrite_ds` ENABLE `read_ds_0` FROM `logic_db`"));
    }
    
    @Test
    void assertResolveStatusOperationUsesTargetStatus() {
        assertThat(new ReadwriteSplittingStatusDistSQLPlanningService().resolveStatusOperation(createRequest("disable")), is("DISABLE"));
    }
    
    @Test
    void assertResolveStatusOperationIgnoresOperationType() {
        ReadwriteSplittingStatusWorkflowRequest request = createRequest("");
        request.setOperationType("enable");
        assertThat(new ReadwriteSplittingStatusDistSQLPlanningService().resolveStatusOperation(request), is(""));
    }
    
    @Test
    void assertPlanStatusRejectsMissingTargetStatus() {
        assertThrows(MCPInvalidRequestException.class, () -> new ReadwriteSplittingStatusDistSQLPlanningService().planStatus(createRequest("")));
    }
    
    private ReadwriteSplittingStatusWorkflowRequest createRequest(final String targetStatus) {
        ReadwriteSplittingStatusWorkflowRequest result = new ReadwriteSplittingStatusWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("readwrite_ds");
        result.setStorageUnit("read_ds_0");
        result.setTargetStatus(targetStatus);
        return result;
    }
}

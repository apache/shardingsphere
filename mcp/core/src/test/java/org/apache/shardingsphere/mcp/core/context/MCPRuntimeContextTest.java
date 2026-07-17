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

package org.apache.shardingsphere.mcp.core.context;

import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPRuntimeContextTest {
    
    @Test
    void assertWorkflowContextsAreSessionBound() {
        MCPRuntimeContext runtimeContext = createRuntimeContext(new MCPSessionManager(Map.of()));
        runtimeContext.getWorkflowSessionContext("session-1").save(createSnapshot("plan-1"));
        assertThrows(MCPInvalidRequestException.class, () -> runtimeContext.getWorkflowSessionContext("session-2").getRequired("plan-1"));
    }
    
    @Test
    void assertSessionCloseRemovesWorkflowState() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession("session-1");
        sessionManager.createSession("session-2");
        MCPRuntimeContext runtimeContext = createRuntimeContext(sessionManager);
        WorkflowSessionContext firstContext = runtimeContext.getWorkflowSessionContext("session-1");
        firstContext.save(createSnapshot("plan-1"));
        runtimeContext.getWorkflowSessionContext("session-2").save(createSnapshot("plan-2"));
        new MCPSessionExecutionCoordinator(sessionManager).closeSession("session-1");
        assertThrows(MCPInvalidRequestException.class, () -> firstContext.getRequired("plan-1"));
        assertThat(runtimeContext.getWorkflowSessionContext("session-2").getRequired("plan-2").getPlanId(), is("plan-2"));
    }
    
    private MCPRuntimeContext createRuntimeContext(final MCPSessionManager sessionManager) {
        return new MCPRuntimeContext(sessionManager, new MCPDatabaseCapabilityProvider(Map.of()), "http");
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        return result;
    }
}

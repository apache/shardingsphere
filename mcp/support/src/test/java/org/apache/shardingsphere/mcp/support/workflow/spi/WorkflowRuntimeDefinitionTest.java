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

package org.apache.shardingsphere.mcp.support.workflow.spi;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

class WorkflowRuntimeDefinitionTest {
    
    @Test
    void assertCreateWithRuntimeHandlerUsesApplyArtifactValidator() {
        MCPWorkflowRuntimeHandler runtimeHandler = mock(MCPWorkflowRuntimeHandler.class, withSettings().extraInterfaces(MCPWorkflowApplyArtifactValidator.class));
        WorkflowRuntimeDefinition actual = new WorkflowRuntimeDefinition(WorkflowKind.valueOf("encrypt.rule"), runtimeHandler);
        assertThat(actual.getApplyArtifactValidator(), is((MCPWorkflowApplyArtifactValidator) runtimeHandler));
    }
    
    @Test
    void assertCreateWithRuntimeHandlerUsesNoOpApplyArtifactValidator() {
        WorkflowRuntimeDefinition actual = new WorkflowRuntimeDefinition(WorkflowKind.valueOf("encrypt.rule"), mock(MCPWorkflowRuntimeHandler.class));
        assertThat(actual.getApplyArtifactValidator(), is(MCPWorkflowApplyArtifactValidator.NO_OP));
    }
}

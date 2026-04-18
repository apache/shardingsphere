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

package org.apache.shardingsphere.mcp.tool.handler.workflow;

import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowExecutionService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplyEncryptMaskRuleToolHandlerTest {
    
    @Test
    void assertHandle() {
        WorkflowExecutionService executionService = mock(WorkflowExecutionService.class);
        MCPRequestContext requestContext = mock(MCPRequestContext.class);
        when(executionService.apply(org.mockito.ArgumentMatchers.same(requestContext), anyString(), anyString(), anyList(), anyString())).thenReturn(Map.of("status", "completed"));
        ApplyEncryptMaskRuleToolHandler handler = new ApplyEncryptMaskRuleToolHandler(executionService);
        Map<String, Object> actual = handler.handle(requestContext, "session-1", Map.of("plan_id", "plan-1", "approved_steps", List.of("ddl"))).toPayload();
        assertThat(actual.get("status"), is("completed"));
    }
    
    @Test
    void assertGetToolDescriptor() {
        assertThat(new ApplyEncryptMaskRuleToolHandler().getToolDescriptor().getName(), is("apply_encrypt_mask_rule"));
    }
}

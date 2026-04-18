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
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowValidationService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidateEncryptMaskRuleToolHandlerTest {
    
    @Test
    void assertHandle() {
        WorkflowValidationService validationService = mock(WorkflowValidationService.class);
        MCPRequestContext requestContext = mock(MCPRequestContext.class);
        when(validationService.validate(org.mockito.ArgumentMatchers.same(requestContext), anyString(), anyString())).thenReturn(Map.of("overall_status", "passed"));
        ValidateEncryptMaskRuleToolHandler handler = new ValidateEncryptMaskRuleToolHandler(validationService);
        Map<String, Object> actual = handler.handle(requestContext, "session-1", Map.of("plan_id", "plan-1")).toPayload();
        assertThat(actual.get("overall_status"), is("passed"));
    }
    
    @Test
    void assertGetToolDescriptor() {
        assertThat(new ValidateEncryptMaskRuleToolHandler().getToolDescriptor().getName(), is("validate_encrypt_mask_rule"));
    }
}

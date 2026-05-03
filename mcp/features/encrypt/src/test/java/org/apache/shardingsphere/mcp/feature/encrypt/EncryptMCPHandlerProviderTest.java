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

package org.apache.shardingsphere.mcp.feature.encrypt;

import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class EncryptMCPHandlerProviderTest {
    
    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new EncryptMCPHandlerProvider().getResourceHandlers();
        assertThat(actual.stream().map(MCPResourceHandler::getUriPattern).toList(), is(List.of(
                "shardingsphere://features/encrypt/algorithms",
                "shardingsphere://features/encrypt/databases/{database}/rules",
                "shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules")));
    }
    
    @Test
    void assertGetToolHandlers() {
        Collection<MCPToolHandler<?>> actual = new EncryptMCPHandlerProvider().getToolHandlers();
        assertThat(actual.stream().map(each -> each.getToolDescriptor().getName()).toList(), is(List.of("plan_encrypt_rule")));
    }
    
    @Test
    void assertGetWorkflowDefinitions() {
        WorkflowRuntimeDefinition actual = new EncryptMCPHandlerProvider().getWorkflowDefinitions().iterator().next();
        assertThat(actual.getWorkflowKind(), is(EncryptFeatureDefinition.WORKFLOW_KIND));
        assertThat(actual.getApplySynchronizationHandler(), isA(EncryptWorkflowValidationService.class));
        assertThat(actual.getValidationHandler(), isA(EncryptWorkflowValidationService.class));
    }
}

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

import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandler;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.feature.encrypt.completion.EncryptAlgorithmCompletionHandler;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptMCPHandlerProviderTest {
    
    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new EncryptMCPHandlerProvider().getResourceHandlers();
        assertThat(actual.stream().map(MCPResourceHandler::getResourceUriTemplate).toList(), is(List.of(
                "shardingsphere://features/encrypt/algorithms",
                "shardingsphere://features/encrypt/databases/{database}/rules",
                "shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules")));
        assertTrue(actual.stream().allMatch(each -> MCPFeatureRequestContext.class.equals(each.getContextType())));
    }
    
    @Test
    void assertGetToolHandlers() {
        MCPToolHandler<?> actual = new EncryptMCPHandlerProvider().getToolHandlers().iterator().next();
        assertThat(actual.getToolName(), is("database_gateway_plan_encrypt_rule"));
    }
    
    @Test
    void assertGetCompletionHandlers() {
        MCPCompletionHandler<?> actual = new EncryptMCPHandlerProvider().getCompletionHandlers().iterator().next();
        assertThat(actual, isA(EncryptAlgorithmCompletionHandler.class));
    }
    
    @Test
    void assertLoadByServiceLoader() {
        assertTrue(ServiceLoader.load(MCPHandlerProvider.class).stream().map(ServiceLoader.Provider::type).anyMatch(EncryptMCPHandlerProvider.class::equals));
    }
    
    @Test
    void assertGetWorkflowDefinitions() {
        WorkflowRuntimeDefinition actual = new EncryptMCPHandlerProvider().getWorkflowDefinitions().iterator().next();
        assertThat(actual.getWorkflowKind(), is(EncryptFeatureDefinition.WORKFLOW_KIND));
        assertThat(actual.getApplySynchronizationHandler(), isA(EncryptWorkflowValidationService.class));
        assertThat(actual.getApplyArtifactValidator(), isA(EncryptWorkflowApplyArtifactValidator.class));
        assertThat(actual.getValidationHandler(), isA(EncryptWorkflowValidationService.class));
    }
}

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

package org.apache.shardingsphere.mcp.feature.mask;

import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MaskMCPHandlerProviderTest {

    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new MaskMCPHandlerProvider().getResourceHandlers();
        assertThat(actual.stream().map(each -> each.getResourceDescriptor().getUriTemplate()).toList(), is(List.of(
                "shardingsphere://features/mask/algorithms",
                "shardingsphere://features/mask/databases/{database}/rules",
                "shardingsphere://features/mask/databases/{database}/tables/{table}/rules")));
    }

    @Test
    void assertGetToolHandlers() {
        MCPToolDescriptor actual = new MaskMCPHandlerProvider().getToolHandlers().iterator().next().getToolDescriptor();
        assertThat(actual.getName(), is("database_gateway_plan_mask_rule"));
        assertFalse(actual.getAnnotations().isReadOnlyHint());
        assertFalse(actual.getAnnotations().isDestructiveHint());
        assertFalse(actual.getAnnotations().isIdempotentHint());
    }

    @Test
    void assertGetWorkflowDefinitions() {
        WorkflowRuntimeDefinition actual = new MaskMCPHandlerProvider().getWorkflowDefinitions().iterator().next();
        assertThat(actual.getWorkflowKind(), is(MaskFeatureDefinition.WORKFLOW_KIND));
        assertThat(actual.getApplySynchronizationHandler(), isA(MaskWorkflowValidationService.class));
        assertThat(actual.getValidationHandler(), isA(MaskWorkflowValidationService.class));
    }
}

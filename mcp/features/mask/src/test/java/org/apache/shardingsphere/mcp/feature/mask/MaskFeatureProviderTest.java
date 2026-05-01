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

import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowValidationService;
import org.apache.shardingsphere.mcp.resource.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.is;

class MaskFeatureProviderTest {
    
    @Test
    void assertGetResourceHandlers() {
        Collection<ResourceHandler> actual = new MaskFeatureProvider().getResourceHandlers();
        assertThat(actual.stream().map(ResourceHandler::getUriPattern).toList(), is(List.of(
                "shardingsphere://features/mask/algorithms",
                "shardingsphere://features/mask/databases/{database}/rules",
                "shardingsphere://features/mask/databases/{database}/tables/{table}/rules")));
    }
    
    @Test
    void assertGetToolHandlers() {
        Collection<ToolHandler> actual = new MaskFeatureProvider().getToolHandlers();
        assertThat(actual.stream().map(each -> each.getToolDescriptor().getName()).toList(), is(List.of("plan_mask_rule")));
    }
    
    @Test
    void assertGetWorkflowDefinitions() {
        WorkflowRuntimeDefinition actual = new MaskFeatureProvider().getWorkflowDefinitions().iterator().next();
        assertThat(actual.getWorkflowKind(), is(MaskFeatureDefinition.WORKFLOW_KIND));
        assertThat(actual.getApplySynchronizationHandler(), isA(MaskWorkflowValidationService.class));
        assertThat(actual.getValidationHandler(), isA(MaskWorkflowValidationService.class));
    }
}

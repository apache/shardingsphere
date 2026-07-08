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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDescriptorCatalogIndexTest {
    
    @Test
    void assertGetResourceDescriptors() {
        Collection<MCPResourceDescriptor> actualDescriptors = MCPDescriptorCatalogIndex.getResourceDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "shardingsphere://workflows/{plan_id}".equals(each.getUriTemplate())));
    }
    
    @Test
    void assertGetRequiredResourceDescriptor() {
        MCPResourceDescriptor actual = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor("shardingsphere://workflows/{plan_id}");
        assertThat(actual.getName(), is("workflow-plan"));
    }
    
    @Test
    void assertGetRequiredResourceDescriptorWithUnknownResource() {
        assertThrows(IllegalStateException.class, () -> MCPDescriptorCatalogIndex.getRequiredResourceDescriptor("shardingsphere://unknown"));
    }
    
    @Test
    void assertGetRequiredShardingSphereResourceMetadata() {
        ShardingSphereMCPResourceMetadata actual = MCPDescriptorCatalogIndex.getRequiredShardingSphereResourceMetadata("shardingsphere://workflows/{plan_id}");
        assertThat(actual.getResourceKind(), is("detail"));
    }
    
    @Test
    void assertGetToolDescriptors() {
        Collection<MCPToolDescriptor> actualDescriptors = MCPDescriptorCatalogIndex.getToolDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "database_gateway_apply_workflow".equals(each.getName())));
    }
    
    @Test
    void assertGetRequiredToolDescriptor() {
        MCPToolDescriptor actual = MCPDescriptorCatalogIndex.getRequiredToolDescriptor("database_gateway_apply_workflow");
        assertThat(actual.getTitle(), is("Apply Workflow"));
    }
    
    @Test
    void assertGetRequiredToolDescriptorWithUnknownTool() {
        assertThrows(IllegalStateException.class, () -> MCPDescriptorCatalogIndex.getRequiredToolDescriptor("unknown_tool"));
    }
    
    @Test
    void assertGetPromptDescriptors() {
        Collection<MCPPromptDescriptor> actualDescriptors = MCPDescriptorCatalogIndex.getPromptDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "inspect_metadata".equals(each.getName())));
    }
    
    @Test
    void assertGetRequiredPromptTemplateBinding() {
        MCPPromptTemplateBinding actual = MCPDescriptorCatalogIndex.getRequiredPromptTemplateBinding("inspect_metadata");
        assertThat(actual.getTemplateResource(), is("META-INF/shardingsphere-mcp/prompts/inspect-metadata.md"));
    }
    
    @Test
    void assertFindToolRuntimeDescriptor() {
        assertTrue(MCPDescriptorCatalogIndex.findToolRuntimeDescriptor("database_gateway_apply_workflow")
                .filter(optional -> "apply".equals(optional.getWorkflowRole()) && optional.getSideEffectScope().contains("rule-metadata")).isPresent());
    }
    
    @Test
    void assertFindToolRuntimeDescriptorWithUnknownTool() {
        assertFalse(MCPDescriptorCatalogIndex.findToolRuntimeDescriptor("unknown_tool").isPresent());
    }
    
    @Test
    void assertFindPlanningToolNameByWorkflowKind() {
        assertThat(MCPDescriptorCatalogIndex.findPlanningToolNameByWorkflowKind("encrypt.rule").orElseThrow(), is("database_gateway_plan_encrypt_rule"));
    }
    
    @Test
    void assertFindPlanningToolNameByUnknownWorkflowKind() {
        assertFalse(MCPDescriptorCatalogIndex.findPlanningToolNameByWorkflowKind("unknown.rule").isPresent());
    }
    
    @Test
    void assertFindWorkflowKindsByGenericPromptCompletionTarget() {
        MCPCompletionTargetDescriptor descriptor = new MCPCompletionTargetDescriptor("prompt", "recover_workflow", List.of("plan_id"), 50, Map.of());
        assertTrue(MCPDescriptorCatalogIndex.findWorkflowKindsByCompletionTarget(descriptor).isEmpty());
    }
    
    @Test
    void assertFindWorkflowKindsByResourceCompletionTarget() {
        MCPCompletionTargetDescriptor descriptor = new MCPCompletionTargetDescriptor("resource", "shardingsphere://workflows/{plan_id}", List.of("plan_id"), 50, Map.of());
        assertTrue(MCPDescriptorCatalogIndex.findWorkflowKindsByCompletionTarget(descriptor).isEmpty());
    }
    
    @Test
    void assertGetCompletionTargetDescriptors() {
        Collection<MCPCompletionTargetDescriptor> actualDescriptors = MCPDescriptorCatalogIndex.getCompletionTargetDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "prompt".equals(each.getReferenceType()) && "inspect_metadata".equals(each.getReference())));
        assertTrue(actualDescriptors.stream().filter(each -> "resource".equals(each.getReferenceType()))
                .allMatch(each -> MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(each.getReference()).isTemplated()));
    }
    
    @Test
    void assertGetResourceNavigationDescriptors() {
        Collection<MCPResourceNavigationDescriptor> actualDescriptors = MCPDescriptorCatalogIndex.getResourceNavigationDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "database_gateway_apply_workflow".equals(each.getFrom()) && "database_gateway_validate_workflow".equals(each.getTo())));
    }
    
    @Test
    void assertGetResourceNavigationDescriptorsByFrom() {
        Collection<MCPResourceNavigationDescriptor> actualDescriptors = MCPDescriptorCatalogIndex.getResourceNavigationDescriptors("database_gateway_apply_workflow");
        assertFalse(actualDescriptors.isEmpty());
        assertTrue(actualDescriptors.stream().allMatch(each -> "database_gateway_apply_workflow".equals(each.getFrom())));
        assertTrue(actualDescriptors.stream().anyMatch(each -> "database_gateway_validate_workflow".equals(each.getTo())));
    }
    
    @Test
    void assertGetResourceNavigationDescriptorsByUnknownFrom() {
        Collection<MCPResourceNavigationDescriptor> actualDescriptors = MCPDescriptorCatalogIndex.getResourceNavigationDescriptors("shardingsphere://unknown");
        assertTrue(actualDescriptors.isEmpty());
    }
    
    @Test
    void assertCreateCapabilityPayload() {
        Map<String, Object> actual = MCPDescriptorCatalogIndex.createCapabilityPayload(List.of("shardingsphere://workflows/{plan_id}"), List.of("database_gateway_apply_workflow"), List.of("SELECT"));
        assertThat(actual.get("supportedResources"), is(List.of("shardingsphere://workflows/{plan_id}")));
        assertThat(actual.get("supportedTools"), is(List.of("database_gateway_apply_workflow")));
        assertThat(actual.get("supportedStatementClasses"), is(List.of("SELECT")));
        assertFalse(actual.containsKey("fingerprints"));
    }
    
    @Test
    void assertCreateGuidancePayload() {
        Map<String, Object> actual = MCPDescriptorCatalogIndex.createGuidancePayload();
        assertThat(actual.get("response_mode"), is("guidance"));
        assertThat(actual.get("guidance_resource"), is("shardingsphere://guidance"));
        assertTrue(actual.containsKey("model_contract"));
    }
}

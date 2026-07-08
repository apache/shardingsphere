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

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPDescriptorCatalogValidatorTest {
    
    @Test
    void assertValidateResourceAnnotations() {
        MCPDescriptorCatalog actual = createCatalog(List.of(createResourceDescriptor(new MCPResourceAnnotations(List.of("assistant"), 0D, "2026-05-13T00:00:00Z"))), List.of());
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(actual));
    }
    
    @Test
    void assertValidateScopedProtocolNonGoalFieldsAreNotRequired() {
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(createCatalog(
                List.of(createResourceDescriptor(MCPResourceAnnotations.EMPTY)),
                List.of(createToolDescriptor(new MCPToolAnnotations("Test Tool", true, false, true, true))))));
    }
    
    @Test
    void assertValidateRejectsRemovedModelFacingOutputField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", new MCPToolAnnotations("Test Tool", true, false, true, true),
                createOutputSchema(Map.of("recommended_next_tool", Map.of("type", "string", "description", "Removed alias.")))))),
                "Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `recommended_next_tool`.");
    }
    
    @Test
    void assertValidateRejectsRemovedModelFacingInputField() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.",
                createInputSchema(Map.of("query", Map.of("type", "string", "description", "Query."),
                        "user_overrides", Map.of("type", "object", "description", "Removed duplicate input."))),
                createOutputSchema(), new MCPToolAnnotations("Test Tool", true, false, true, true), Map.of()))),
                "Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `user_overrides`.");
    }
    
    @Test
    void assertValidateRejectsRemovedModelFacingRequiredInputField() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.",
                createInputSchema(Map.of("query", Map.of("type", "string", "description", "Query.")), List.of("query", "required_arguments")),
                createOutputSchema(), new MCPToolAnnotations("Test Tool", true, false, true, true), Map.of()))),
                "Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `required_arguments`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedInputSchemaTopLevelField() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("oneOf", List.of(Map.of("required", List.of("query"))));
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema,
                createOutputSchema(), new MCPToolAnnotations("Test Tool", true, false, true, true), Map.of()))),
                "Tool `database_gateway_test_tool` inputSchema contains unsupported top-level field `oneOf`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedNestedInputSchemaField() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.",
                createInputSchema(Map.of("query", Map.of("type", "string", "description", "Query.", "pattern", ".+"))),
                createOutputSchema(), new MCPToolAnnotations("Test Tool", true, false, true, true), Map.of()))),
                "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` contains unsupported field `pattern`.");
    }
    
    @Test
    void assertValidateRejectsUnknownRelatedResourceUri() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", createInputSchema(),
                createOutputSchema(), new MCPToolAnnotations("Test Tool", true, false, true, true),
                Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://unknown"))))),
                "Tool `database_gateway_test_tool` metadata `org.apache.shardingsphere/related-resource-uris` references unknown resource `shardingsphere://unknown`.");
    }
    
    @Test
    void assertValidateRejectsPlanningRuntimeWithoutWorkflowKind() {
        String toolName = "database_gateway_test_plan_rule";
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(createToolDescriptor(toolName, new MCPToolAnnotations("Test Tool", true, false, true, true),
                createWorkflowPlanOutputSchema(), createPlanningToolMetaWithoutWorkflowKind())),
                List.of(new MCPToolRuntimeDescriptor(toolName, "plan", List.of()))),
                "Tool `database_gateway_test_plan_rule` metadata must declare `org.apache.shardingsphere/workflow-kind`.");
    }
    
    @Test
    void assertValidateRejectsDuplicatePlanningWorkflowKind() {
        MCPToolDescriptor firstTool = createToolDescriptor("database_gateway_test_plan_rule", new MCPToolAnnotations("Test Tool", true, false, true, true),
                createWorkflowPlanOutputSchema(), createPlanningToolMeta("test.rule"));
        MCPToolDescriptor secondTool = createToolDescriptor("database_gateway_test_plan_rule_again", new MCPToolAnnotations("Test Tool", true, false, true, true),
                createWorkflowPlanOutputSchema(), createPlanningToolMeta("test.rule"));
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(firstTool, secondTool), List.of(
                new MCPToolRuntimeDescriptor(firstTool.getName(), "plan", List.of()),
                new MCPToolRuntimeDescriptor(secondTool.getName(), "plan", List.of()))),
                "Planning workflow kind `test.rule` is used by both `database_gateway_test_plan_rule` and `database_gateway_test_plan_rule_again`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedNextActionSchemaField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", new MCPToolAnnotations("Test Tool", true, false, true, true),
                createOutputSchema(Map.of("next_actions", createNextActionsSchema("extra_context")))))),
                "Tool `database_gateway_test_tool` next_actions item contains unsupported field `extra_context`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedNextActionExampleField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", new MCPToolAnnotations("Test Tool", true, false, true, true),
                createOutputSchema(Map.of("next_actions", createNextActionsSchema()), List.of(Map.of("next_actions", List.of(
                        Map.of("order", 1, "type", "tool_call", "title", "Retry", "tool_name", "database_gateway_test_tool", "arguments", Map.of(), "extra_context", "bad")))))))),
                "Tool `database_gateway_test_tool` next_actions example `tool_call` contains unsupported field `extra_context`.");
    }
    
    @Test
    void assertValidateRejectsMissingNextActionExampleField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", new MCPToolAnnotations("Test Tool", true, false, true, true),
                createOutputSchema(Map.of("next_actions", createNextActionsSchema()), List.of(Map.of("next_actions", List.of(
                        Map.of("order", 1, "type", "tool_call", "title", "Retry", "tool_name", "database_gateway_test_tool")))))))),
                "Tool `database_gateway_test_tool` next_actions example `tool_call` must contain `arguments`.");
    }
    
    @Test
    void assertValidateAcceptsFeatureOwnedToolDescriptorWithoutExtensionMarker() {
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_extension_test_tool", new MCPToolAnnotations("Extension Tool", true, false, true, true), createOutputSchema())))));
    }
    
    @Test
    void assertValidateRejectsIncompleteCoreToolDescriptor() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_search_metadata", new MCPToolAnnotations("Search Metadata", true, false, true, true), createOutputSchema()))),
                "Tool `database_gateway_search_metadata` outputSchema must declare `response_mode`.");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertValidateRejectsApplyDescriptorWithoutSecretReferenceSummary() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(WorkflowToolDescriptors.APPLY_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove("secret_reference_summary");
        outputSchema.put("properties", properties);
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta()))),
                "Tool `database_gateway_apply_workflow` outputSchema must declare `secret_reference_summary`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedModelFacingPromptPlaceholder() {
        assertValidationError(createPromptCatalog(List.of(createPromptDescriptor("test_prompt", List.of(), Map.of())),
                List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md"))),
                "Prompt template `META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md` contains unsupported model-facing placeholder `{database}`.");
    }
    
    @Test
    void assertValidatePreservesResourceUriTemplatesInPrompt() {
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(createPromptCatalog(List.of(createPromptDescriptor("test_prompt", List.of(), Map.of())),
                List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-uri-template.md")))));
    }
    
    @Test
    void assertValidateRejectsModelFacingPromptPlaceholderNearResourceUriTemplate() {
        assertValidationError(createPromptCatalog(List.of(createPromptDescriptor("test_prompt", List.of(), Map.of())),
                List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-mixed-placeholder-and-uri-template.md"))),
                "Prompt template `META-INF/shardingsphere-mcp/prompts/fixture-mixed-placeholder-and-uri-template.md` contains unsupported model-facing placeholder `{database}`.");
    }
    
    @Test
    void assertValidateRejectsUnrenderedPromptArgument() {
        assertValidationError(createPromptCatalog(List.of(createPromptDescriptor("test_prompt", Map.of())),
                List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-uri-template.md"))),
                "Prompt `test_prompt` declares argument `database` but template `META-INF/shardingsphere-mcp/prompts/fixture-uri-template.md` does not render it.");
    }
    
    @Test
    void assertValidateAcceptsClientFormOnlyPromptArgument() {
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(createPromptCatalog(List.of(createPromptDescriptor("test_prompt",
                Map.of("org.apache.shardingsphere/client-form-only-arguments", List.of("database")))),
                List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-uri-template.md")))));
    }
    
    @Test
    void assertValidateRejectsUnknownCompletionRequiredContextArgument() {
        MCPPromptDescriptor prompt = createPromptDescriptor("test_prompt", List.of(
                new MCPPromptArgumentDescriptor("database", "Database", "Logical database.", false),
                new MCPPromptArgumentDescriptor("schema", "Schema", "Schema.", false)),
                Map.of("org.apache.shardingsphere/client-form-only-arguments", List.of("database", "schema")));
        MCPCompletionTargetDescriptor completion = new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database", "schema"), 50,
                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("schema", List.of("tenant"))));
        assertValidationError(createPromptCatalog(List.of(prompt),
                List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-uri-template.md")), List.of(completion)),
                "Completion target `prompt:test_prompt` context argument `tenant` for `schema` is not declared by the target.");
    }
    
    private void assertValidationError(final MCPDescriptorCatalog catalog, final String expectedMessage) {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPToolDescriptor> toolDescriptors) {
        return createCatalog(resourceDescriptors, List.of(), toolDescriptors);
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPResourceDescriptor> resourceTemplateDescriptors,
                                               final List<MCPToolDescriptor> toolDescriptors) {
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(resourceDescriptors, resourceTemplateDescriptors, toolDescriptors, List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
    }
    
    private MCPDescriptorCatalog createToolRuntimeCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPToolDescriptor> toolDescriptors,
                                                          final List<MCPToolRuntimeDescriptor> runtimeDescriptors) {
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(resourceDescriptors, List.of(), toolDescriptors, List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), runtimeDescriptors));
    }
    
    private MCPDescriptorCatalog createPromptCatalog(final List<MCPPromptDescriptor> promptDescriptors, final List<MCPPromptTemplateBinding> promptTemplateBindings) {
        return createPromptCatalog(promptDescriptors, promptTemplateBindings, List.of());
    }
    
    private MCPDescriptorCatalog createPromptCatalog(final List<MCPPromptDescriptor> promptDescriptors, final List<MCPPromptTemplateBinding> promptTemplateBindings,
                                                     final List<MCPCompletionTargetDescriptor> completionTargetDescriptors) {
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(), promptDescriptors),
                new MCPShardingSphereDescriptorCatalog(List.of(), promptTemplateBindings, completionTargetDescriptors, List.of(), List.of()));
    }
    
    private MCPResourceDescriptor createResourceDescriptor(final MCPResourceAnnotations annotations) {
        return new MCPResourceDescriptor("shardingsphere://capabilities", "server-capability-catalog", "Server Capability Catalog",
                "Read the model-facing capability catalog.", "application/json", annotations, Map.of());
    }
    
    private MCPPromptDescriptor createPromptDescriptor(final String name, final Map<String, Object> meta) {
        return createPromptDescriptor(name, List.of(new MCPPromptArgumentDescriptor("database", "Database", "Logical database.", false)), meta);
    }
    
    private MCPPromptDescriptor createPromptDescriptor(final String name, final List<MCPPromptArgumentDescriptor> args, final Map<String, Object> meta) {
        return new MCPPromptDescriptor(name, "Test Prompt", "Guide the model through a test prompt.", args, meta);
    }
    
    private MCPToolDescriptor createToolDescriptor(final MCPToolAnnotations annotations) {
        return createToolDescriptor("database_gateway_test_tool", annotations, createOutputSchema());
    }
    
    private MCPToolDescriptor createToolDescriptor(final String toolName, final MCPToolAnnotations annotations, final Map<String, Object> outputSchema) {
        return createToolDescriptor(toolName, annotations, outputSchema, Map.of());
    }
    
    private MCPToolDescriptor createToolDescriptor(final String toolName, final MCPToolAnnotations annotations, final Map<String, Object> outputSchema, final Map<String, Object> meta) {
        return new MCPToolDescriptor(toolName, "Test Tool", "Run a test tool.", createInputSchema(), outputSchema, annotations, meta);
    }
    
    private Map<String, Object> createInputSchema() {
        return createInputSchema(Map.of("query", Map.of("type", "string", "description", "Query.")));
    }
    
    private Map<String, Object> createInputSchema(final Map<String, Object> properties) {
        return createInputSchema(properties, List.of("query"));
    }
    
    private Map<String, Object> createInputSchema(final Map<String, Object> properties, final List<String> requiredFields) {
        return Map.of("type", "object", "properties", properties, "required", requiredFields, "additionalProperties", false);
    }
    
    private Map<String, Object> createOutputSchema() {
        return createOutputSchema(Map.of("status", Map.of("type", "string", "description", "Status.")));
    }
    
    private Map<String, Object> createOutputSchema(final Map<String, Object> properties) {
        return Map.of("type", "object", "properties", properties, "examples", List.of(Map.of("status", "ok")));
    }
    
    private Map<String, Object> createOutputSchema(final Map<String, Object> properties, final List<Map<String, Object>> examples) {
        return Map.of("type", "object", "properties", properties, "examples", examples);
    }
    
    private Map<String, Object> createWorkflowPlanOutputSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String each : List.of(
                "response_mode", MCPPayloadFieldNames.SUMMARY, WorkflowFieldNames.PLAN_ID, "workflow_kind", "status", "missing_required_inputs", "clarification_questions",
                "elicitation_support", "fallback_reason", "issues", "global_steps", "current_step", "algorithm_recommendations", "property_requirements", "validation_strategy",
                "delivery_mode", "execution_mode", "intent_inference", "argument_provenance", "review_focus", "proxy_topology_hint", "distsql_artifacts",
                MCPPayloadFieldNames.RESOURCES_TO_READ)) {
            properties.put(each, Map.of("type", "object", "description", "Workflow plan field."));
        }
        properties.put(MCPPayloadFieldNames.NEXT_ACTIONS, createNextActionsSchema());
        return createOutputSchema(properties, List.of(Map.of("response_mode", "planning")));
    }
    
    private Map<String, Object> createPlanningToolMeta(final String workflowKind) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.putAll(createPlanningToolMetaWithoutWorkflowKind());
        result.put(MCPShardingSphereMetadataKeys.WORKFLOW_KIND, workflowKind);
        return result;
    }
    
    private Map<String, Object> createPlanningToolMetaWithoutWorkflowKind() {
        return Map.of(
                "org.apache.shardingsphere/artifact-categories", List.of("rule_distsql"),
                "org.apache.shardingsphere/side-effect-scope", List.of("rule-metadata"),
                "org.apache.shardingsphere/related-resource-uris", List.of(),
                "org.apache.shardingsphere/follow-up-tools", List.of("database_gateway_apply_workflow"));
    }
    
    private Map<String, Object> createNextActionsSchema() {
        return createNextActionsSchema("");
    }
    
    private Map<String, Object> createNextActionsSchema(final String additionalFieldName) {
        Map<String, Object> actionProperties = new LinkedHashMap<>();
        actionProperties.put("order", Map.of("type", "integer", "description", "1-based action order."));
        actionProperties.put("type", Map.of("type", "string", "description", "Canonical action type."));
        actionProperties.put("title", Map.of("type", "string", "description", "Action title."));
        actionProperties.put("tool_name", Map.of("type", "string", "description", "Canonical tool name."));
        actionProperties.put("arguments", Map.of("type", "object", "description", "Canonical tool arguments."));
        if (!additionalFieldName.isEmpty()) {
            actionProperties.put(additionalFieldName, Map.of("type", "string", "description", "Unsupported field."));
        }
        return Map.of("type", "array", "description", "Structured follow-up actions.", "items", Map.of("type", "object", "properties", actionProperties));
    }
}

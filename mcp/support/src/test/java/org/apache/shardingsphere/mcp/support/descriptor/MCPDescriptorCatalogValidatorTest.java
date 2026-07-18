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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
                List.of(createToolDescriptor(createReadOnlyToolAnnotations())))));
    }
    
    @Test
    void assertValidateRejectsRemovedModelFacingOutputField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", createReadOnlyToolAnnotations(),
                createOutputSchema(Map.of("recommended_next_tool", Map.of("type", "string", "description", "Removed alias.")))))),
                "Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `recommended_next_tool`.");
    }
    
    @Test
    void assertValidateRejectsRemovedModelFacingInputField() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.",
                createInputSchema(Map.of("query", Map.of("type", "string", "description", "Query."),
                        "user_overrides", Map.of("type", "object", "description", "Removed duplicate input."))),
                createOutputSchema(), createReadOnlyToolAnnotations(), Map.of()))),
                "Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `user_overrides`.");
    }
    
    @Test
    void assertValidateRejectsRemovedModelFacingRequiredInputField() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.",
                createInputSchema(Map.of("query", Map.of("type", "string", "description", "Query.")), List.of("query", "required_arguments")),
                createOutputSchema(), createReadOnlyToolAnnotations(), Map.of()))),
                "Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `required_arguments`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedInputSchemaTopLevelField() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("oneOf", List.of(Map.of("required", List.of("query"))));
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema,
                createOutputSchema(), createReadOnlyToolAnnotations(), Map.of()))),
                "Tool `database_gateway_test_tool` inputSchema contains unsupported top-level field `oneOf`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedNestedInputSchemaField() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.",
                createInputSchema(Map.of("query", Map.of("type", "string", "description", "Query.", "pattern", ".+"))),
                createOutputSchema(), createReadOnlyToolAnnotations(), Map.of()))),
                "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` contains unsupported field `pattern`.");
    }
    
    @Test
    void assertValidateRejectsUnknownRelatedResourceUri() {
        assertValidationError(createCatalog(List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", createInputSchema(),
                createOutputSchema(), createReadOnlyToolAnnotations(),
                Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://unknown"))))),
                "Tool `database_gateway_test_tool` metadata `org.apache.shardingsphere/related-resource-uris` references unknown resource `shardingsphere://unknown`.");
    }
    
    @Test
    void assertValidateRejectsPlanningRuntimeWithoutWorkflowKind() {
        String toolName = "database_gateway_test_plan_rule";
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(createToolDescriptor(toolName, createPlanningToolAnnotations(),
                createWorkflowPlanOutputSchema(), createPlanningToolMetaWithoutWorkflowKind())),
                List.of(new MCPToolRuntimeDescriptor(toolName, "plan", List.of()))),
                "Tool `database_gateway_test_plan_rule` metadata must declare `org.apache.shardingsphere/workflow-kind`.");
    }
    
    @Test
    void assertValidateRejectsDuplicatePlanningWorkflowKind() {
        MCPToolDescriptor firstTool = createToolDescriptor("database_gateway_test_plan_rule", createPlanningToolAnnotations(),
                createWorkflowPlanOutputSchema(), createPlanningToolMeta("test.rule"));
        MCPToolDescriptor secondTool = createToolDescriptor("database_gateway_test_plan_rule_again", createPlanningToolAnnotations(),
                createWorkflowPlanOutputSchema(), createPlanningToolMeta("test.rule"));
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(firstTool, secondTool), List.of(
                new MCPToolRuntimeDescriptor(firstTool.getName(), "plan", List.of()),
                new MCPToolRuntimeDescriptor(secondTool.getName(), "plan", List.of()))),
                "Planning workflow kind `test.rule` is used by both `database_gateway_test_plan_rule` and `database_gateway_test_plan_rule_again`.");
    }
    
    @Test
    void assertValidateRejectsPlanningToolReadOnlyHint() {
        String toolName = "database_gateway_test_plan_rule";
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(createToolDescriptor(toolName, MCPToolAnnotations.builder()
                .title("Test Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(false).openWorldHint(true).build(),
                createWorkflowPlanOutputSchema(), createPlanningToolMeta("test.rule"))), List.of(new MCPToolRuntimeDescriptor(toolName, "plan", List.of()))),
                "Planning tool `database_gateway_test_plan_rule` annotations.readOnlyHint must be false.");
    }
    
    @Test
    void assertValidateRejectsPlanningToolDestructiveHint() {
        String toolName = "database_gateway_test_plan_rule";
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(createToolDescriptor(toolName, MCPToolAnnotations.builder()
                .title("Test Tool").readOnlyHint(false).destructiveHint(true).idempotentHint(false).openWorldHint(true).build(),
                createWorkflowPlanOutputSchema(), createPlanningToolMeta("test.rule"))), List.of(new MCPToolRuntimeDescriptor(toolName, "plan", List.of()))),
                "Planning tool `database_gateway_test_plan_rule` annotations.destructiveHint must be false.");
    }
    
    @Test
    void assertValidateRejectsPlanningToolIdempotentHint() {
        String toolName = "database_gateway_test_plan_rule";
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(createToolDescriptor(toolName, MCPToolAnnotations.builder()
                .title("Test Tool").readOnlyHint(false).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(),
                createWorkflowPlanOutputSchema(), createPlanningToolMeta("test.rule"))), List.of(new MCPToolRuntimeDescriptor(toolName, "plan", List.of()))),
                "Planning tool `database_gateway_test_plan_rule` annotations.idempotentHint must be false.");
    }
    
    @Test
    void assertValidateRejectsDestructiveToolWithoutExecutionMode() {
        assertDestructiveToolValidationError(false, List.of(), false, List.of("database"),
                "Destructive tool `database_gateway_test_tool` must declare execution_mode.");
    }
    
    @Test
    void assertValidateRejectsDestructiveToolWithOptionalExecutionMode() {
        assertDestructiveToolValidationError(true, List.of("preview"), false, List.of("database"),
                "Destructive tool `database_gateway_test_tool` execution_mode must be required.");
    }
    
    @Test
    void assertValidateRejectsDestructiveToolWithoutPreview() {
        assertDestructiveToolValidationError(true, List.of("review-then-execute"), true, List.of("database"),
                "Destructive tool `database_gateway_test_tool` execution_mode must allow preview.");
    }
    
    @Test
    void assertValidateRejectsDestructiveToolWithAutoExecute() {
        assertDestructiveToolValidationError(true, List.of("preview", "auto-execute"), true, List.of("database"),
                "Destructive tool `database_gateway_test_tool` execution_mode must not expose auto-execute.");
    }
    
    @Test
    void assertValidateRejectsDestructiveToolWithoutSideEffectScope() {
        assertDestructiveToolValidationError(true, List.of("preview"), true, List.of(),
                "Destructive tool `database_gateway_test_tool` must declare sideEffectScope in internal runtime.");
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"preview", "auto-execute"})
    void assertValidateRejectsNonDestructiveExecutionMode(final String executionMode) {
        MCPToolDescriptor descriptor = createExecutionModeTool(false, true, List.of(executionMode), true);
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(descriptor), List.of()),
                String.format("Non-destructive tool `database_gateway_test_tool` execution_mode must not expose %s.", executionMode));
    }
    
    @Test
    void assertValidateRejectsUnsupportedNextActionSchemaField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", createReadOnlyToolAnnotations(),
                createOutputSchema(Map.of("next_actions", createNextActionsSchema("extra_context")))))),
                "Tool `database_gateway_test_tool` next_actions item contains unsupported field `extra_context`.");
    }
    
    @Test
    void assertValidateRejectsUnsupportedNextActionExampleField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", createReadOnlyToolAnnotations(),
                createOutputSchema(Map.of("next_actions", createNextActionsSchema()), List.of(Map.of("next_actions", List.of(
                        Map.of("order", 1, "type", "tool_call", "title", "Retry", "tool_name", "database_gateway_test_tool", "arguments", Map.of(), "extra_context", "bad")))))))),
                "Tool `database_gateway_test_tool` next_actions example `tool_call` contains unsupported field `extra_context`.");
    }
    
    @Test
    void assertValidateRejectsMissingNextActionExampleField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", createReadOnlyToolAnnotations(),
                createOutputSchema(Map.of("next_actions", createNextActionsSchema()), List.of(Map.of("next_actions", List.of(
                        Map.of("order", 1, "type", "tool_call", "title", "Retry", "tool_name", "database_gateway_test_tool")))))))),
                "Tool `database_gateway_test_tool` next_actions example `tool_call` must contain `arguments`.");
    }
    
    @Test
    void assertValidateAcceptsFeatureOwnedToolDescriptorWithoutExtensionMarker() {
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_extension_test_tool", createReadOnlyToolAnnotations("Extension Tool"), createOutputSchema())))));
    }
    
    @Test
    void assertValidateRejectsIncompleteCoreToolDescriptor() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_search_metadata", createReadOnlyToolAnnotations("Search Metadata"), createOutputSchema()))),
                "Tool `database_gateway_search_metadata` outputSchema must declare `response_mode`.");
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"columns", "rows"})
    void assertValidateRejectsExecuteUpdateDescriptorWithoutResultSetField(final String fieldName) {
        assertValidationError(createCatalog(List.of(), List.of(createExecuteUpdateDescriptorWithout(fieldName))),
                String.format("Tool `database_gateway_execute_update` outputSchema must declare `%s`.", fieldName));
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
    
    private void assertDestructiveToolValidationError(final boolean executionModePresent, final List<String> executionModes, final boolean executionModeRequired,
                                                      final List<String> sideEffectScope, final String expectedMessage) {
        MCPToolDescriptor descriptor = createExecutionModeTool(true, executionModePresent, executionModes, executionModeRequired);
        assertValidationError(createToolRuntimeCatalog(List.of(), List.of(descriptor),
                List.of(new MCPToolRuntimeDescriptor(descriptor.getName(), "execute", sideEffectScope))), expectedMessage);
    }
    
    private MCPToolDescriptor createExecutionModeTool(final boolean destructive, final boolean executionModePresent, final List<String> executionModes,
                                                      final boolean executionModeRequired) {
        Map<String, Object> properties = executionModePresent
                ? Map.of(MCPPayloadFieldNames.EXECUTION_MODE, Map.of("type", "string", "description", "Execution mode.", "enum", executionModes))
                : Map.of();
        List<String> required = executionModeRequired ? List.of(MCPPayloadFieldNames.EXECUTION_MODE) : List.of();
        MCPToolAnnotations annotations = MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(false).destructiveHint(destructive)
                .idempotentHint(false).openWorldHint(true).build();
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", createInputSchema(properties, required),
                createOutputSchema(), annotations, Map.of());
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
    
    private MCPToolDescriptor createExecuteUpdateDescriptorWithout(final String fieldName) {
        Map<String, Object> outputProperties = new LinkedHashMap<>();
        for (String each : List.of("response_mode", "result_kind", "statement_class", "statement_type", "status")) {
            outputProperties.put(each, Map.of("type", "string", "description", "Execution result field."));
        }
        for (String each : List.of("columns", "rows")) {
            outputProperties.put(each, Map.of("type", "array", "description", "Result-set field."));
        }
        for (String each : List.of("returned_row_count", "applied_max_rows", "applied_timeout_ms")) {
            outputProperties.put(each, Map.of("type", "integer", "description", "Execution limit field."));
        }
        outputProperties.put("suggested_arguments", Map.of("type", "object", "description", "Suggested execution arguments."));
        outputProperties.put(MCPPayloadFieldNames.NEXT_ACTIONS, createNextActionsSchema());
        outputProperties.remove(fieldName);
        Map<String, Object> inputSchema = createInputSchema(Map.of(MCPPayloadFieldNames.EXECUTION_MODE,
                Map.of("type", "string", "description", "Execution mode.", "enum", List.of("execute", "preview"))), List.of(MCPPayloadFieldNames.EXECUTION_MODE));
        return new MCPToolDescriptor("database_gateway_execute_update", "Execute Update", "Execute a side-effecting statement.", inputSchema,
                createOutputSchema(outputProperties, List.of(Map.of("response_mode", "preview"))), createReadOnlyToolAnnotations("Execute Update"), Map.of());
    }
    
    private MCPToolAnnotations createPlanningToolAnnotations() {
        return MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(false).destructiveHint(false).idempotentHint(false).openWorldHint(true).build();
    }
    
    private MCPToolAnnotations createReadOnlyToolAnnotations() {
        return createReadOnlyToolAnnotations("Test Tool");
    }
    
    private MCPToolAnnotations createReadOnlyToolAnnotations(final String title) {
        return MCPToolAnnotations.builder().title(title).readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build();
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
        Map<String, Object> result = new LinkedHashMap<>(createOutputSchema(properties, List.of(Map.of("response_mode", "planning"))));
        result.put("required", List.of(
                "response_mode", WorkflowFieldNames.PLAN_ID, "workflow_kind", "status", "missing_required_inputs",
                MCPPayloadFieldNames.RESOURCES_TO_READ, MCPPayloadFieldNames.NEXT_ACTIONS));
        return result;
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

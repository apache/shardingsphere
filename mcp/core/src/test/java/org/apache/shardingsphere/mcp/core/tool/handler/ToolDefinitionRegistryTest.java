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

package org.apache.shardingsphere.mcp.core.tool.handler;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidApprovedStepsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolArgumentContractViolationException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ToolDefinitionRegistryTest {
    
    @Test
    void assertGetSupportedTools() {
        assertThat(ToolDefinitionRegistry.getSupportedTools(), contains("database_gateway_search_metadata", "database_gateway_execute_query",
                "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow"));
    }
    
    @Test
    void assertGetSupportedToolDescriptors() {
        List<MCPToolDescriptor> actual = ToolDefinitionRegistry.getSupportedToolDescriptors();
        assertThat(actual.stream().map(MCPToolDescriptor::getName).toList(),
                is(List.of("database_gateway_search_metadata", "database_gateway_execute_query", "database_gateway_execute_update",
                        "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
        assertToolFields(actual.get(0), List.of("database", "schema", "query", "object_types"));
        assertToolFields(actual.get(1), List.of("database", "schema", "sql", "max_rows", "timeout_ms"));
        assertRequiredFields(actual.get(1), List.of("database", "sql"));
        assertToolFields(actual.get(2), List.of("database", "schema", "sql", "execution_mode", "max_rows", "timeout_ms"));
        assertRequiredFields(actual.get(2), List.of("database", "sql", "execution_mode"));
        assertField(actual.get(2), "execution_mode", "string", List.of("execute", "preview"), true);
        assertField(actual.get(2), "max_rows", "integer", List.of(), false);
        assertField(actual.get(2), "timeout_ms", "integer", List.of(), false);
        assertToolFields(actual.get(3), List.of("plan_id", "execution_mode", "approved_steps"));
        assertRequiredFields(actual.get(3), List.of("plan_id", "execution_mode"));
        assertField(actual.get(3), "execution_mode", "string", List.of("preview", "review-then-execute", "manual-only"), true);
        assertField(actual.get(3), "approved_steps", "array", List.of(), false);
    }
    
    @Test
    void assertGetToolDefinition() {
        assertThat(ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata").getDescriptor().getName(), is("database_gateway_search_metadata"));
    }
    
    @Test
    void assertDispatch() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        runtimeContext.getSessionManager().createSession("session-1");
        try (MCPRequestScope requestContext = new MCPRequestScope(runtimeContext)) {
            MCPToolDefinition toolDefinition = ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata");
            MCPResponse actual = ToolDefinitionRegistry.dispatch(requestContext, toolDefinition, "session-1", Map.of("query", "order", "object_types", List.of("index")));
            assertThat(toolDefinition.getDescriptor().getName(), is("database_gateway_search_metadata"));
            assertThat(((List<?>) actual.toPayload().get("items")).size(), is(1));
        }
    }
    
    @Test
    void assertGetToolDefinitionWithUnknownToolName() {
        assertThrows(UnsupportedToolException.class, () -> ToolDefinitionRegistry.getToolDefinition("unknown_tool"));
    }
    
    @Test
    void assertDispatchWithMissingRequiredArgument() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> dispatch("database_gateway_execute_query", Map.of("database", "logic_db")));
        assertThat(actual.getMessage(), is("sql is required."));
    }
    
    @Test
    void assertDispatchWithBlankRequiredTextArgument() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> dispatch("database_gateway_execute_query", Map.of("database", "logic_db", "sql", "   ")));
        assertThat(actual.getMessage(), is("sql is required."));
    }
    
    @Test
    void assertDispatchWithMissingExecutionMode() {
        MCPExecutionModeRequiredException actual = assertThrows(MCPExecutionModeRequiredException.class,
                () -> dispatch("database_gateway_execute_update", Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1")));
        assertThat(actual.getMessage(), is("database_gateway_execute_update execution_mode is required."));
        assertThat(actual.getToolName(), is("database_gateway_execute_update"));
        assertThat(actual.getAllowedValues(), is(List.of("execute", "preview")));
        assertThat(actual.getSuggestedArguments(), is(Map.of("database", "logic_db", "schema", "public",
                "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1", "execution_mode", "preview")));
    }
    
    @Test
    void assertDispatchWithInvalidArgumentType() {
        MCPToolArgumentContractViolationException actual = assertThrows(MCPToolArgumentContractViolationException.class,
                () -> dispatch("database_gateway_search_metadata", Map.of("query", "order", "object_types", "table")));
        assertThat(actual.getMessage(), is("object_types must be an array."));
        assertThat(actual.getToolName(), is("database_gateway_search_metadata"));
        assertThat(actual.getArgumentPath(), is("object_types"));
        assertThat(actual.getCategory(), is("invalid_argument_type"));
        assertThat(actual.getExpectedType(), is("array"));
        assertThat(actual.getSuggestedArguments(), is(Map.of("query", "order")));
    }
    
    @Test
    void assertDispatchWithInvalidEnumArgument() {
        MCPToolArgumentContractViolationException actual = assertThrows(MCPToolArgumentContractViolationException.class,
                () -> dispatch("database_gateway_search_metadata", Map.of("query", "order", "object_types", List.of("TABLE"))));
        assertThat(actual.getMessage(), is("object_types[0] must be one of [database, schema, table, view, column, index, sequence]."));
        assertThat(actual.getArgumentPath(), is("object_types[0]"));
        assertThat(actual.getCategory(), is("invalid_enum_value"));
        assertThat(actual.getAllowedValues(), is(List.of("database", "schema", "table", "view", "column", "index", "sequence")));
        assertThat(actual.getSuggestedArguments(), is(Map.of("query", "order")));
    }
    
    @Test
    void assertDispatchWithInvalidExecutionMode() {
        MCPInvalidExecutionModeException actual = assertThrows(MCPInvalidExecutionModeException.class,
                () -> dispatch("database_gateway_execute_update", Map.of("database", "logic_db", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1", "execution_mode", "RUN")));
        assertThat(actual.getMessage(), is("database_gateway_execute_update execution_mode must be one of [execute, preview]."));
        assertThat(actual.getAllowedValues(), is(List.of("execute", "preview")));
        assertThat(actual.getSuggestedArguments(), is(Map.of("database", "logic_db", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1", "execution_mode", "preview")));
    }
    
    @Test
    void assertDispatchWithInvalidApprovedSteps() {
        MCPInvalidApprovedStepsException actual = assertThrows(MCPInvalidApprovedStepsException.class,
                () -> dispatch("database_gateway_apply_workflow", Map.of("plan_id", "plan-1", "execution_mode", "preview", "approved_steps", List.of("all"))));
        assertThat(actual.getMessage(), is("approved_steps must contain only [ddl, index_ddl, rule_distsql]."));
        assertThat(actual.getAllowedValues(), is(List.of("ddl", "index_ddl", "rule_distsql")));
        assertThat(actual.getSuggestedArguments(), is(Map.of("plan_id", "plan-1", "execution_mode", "preview")));
    }
    
    @Test
    void assertDispatchWithUnknownArgument() {
        MCPToolArgumentContractViolationException actual = assertThrows(MCPToolArgumentContractViolationException.class,
                () -> dispatch("database_gateway_execute_query", Map.of("database", "logic_db", "sql", "SELECT 1", "limit", 10)));
        assertThat(actual.getMessage(), is("limit is not a supported argument for database_gateway_execute_query."));
        assertThat(actual.getCategory(), is("unknown_argument"));
        assertThat(actual.getArgumentPath(), is("limit"));
        assertThat(actual.getSuggestedArguments(), is(Map.of("database", "logic_db", "sql", "SELECT 1")));
    }
    
    @Test
    void assertDispatchWithRemovedSearchPageSizeArgument() {
        MCPToolArgumentContractViolationException actual = assertThrows(MCPToolArgumentContractViolationException.class,
                () -> dispatch("database_gateway_search_metadata", Map.of("query", "order", "page_size", 10)));
        assertThat(actual.getMessage(), is("page_size is not a supported argument for database_gateway_search_metadata."));
        assertThat(actual.getCategory(), is("unknown_argument"));
        assertThat(actual.getArgumentPath(), is("page_size"));
        assertThat(actual.getSuggestedArguments(), is(Map.of("query", "order")));
    }
    
    @Test
    void assertDispatchWithRemovedSearchPageTokenArgument() {
        MCPToolArgumentContractViolationException actual = assertThrows(MCPToolArgumentContractViolationException.class,
                () -> dispatch("database_gateway_search_metadata", Map.of("query", "order", "page_token", "offset")));
        assertThat(actual.getMessage(), is("page_token is not a supported argument for database_gateway_search_metadata."));
        assertThat(actual.getCategory(), is("unknown_argument"));
        assertThat(actual.getArgumentPath(), is("page_token"));
        assertThat(actual.getSuggestedArguments(), is(Map.of("query", "order")));
    }
    
    @Test
    void assertValidateWithUnknownNestedArgument() {
        MCPToolDescriptor descriptor = createNestedFixtureToolDescriptor();
        MCPToolArgumentContractViolationException actual = assertThrows(MCPToolArgumentContractViolationException.class,
                () -> new MCPToolArgumentContract(descriptor.getName(), descriptor.getInputSchema()).validate(Map.of("options", Map.of("mode", "preview", "limit", 10))));
        assertThat(actual.getMessage(), is("options.limit is not a supported argument for fixture_tool."));
        assertThat(actual.getToolName(), is("fixture_tool"));
        assertThat(actual.getArgumentPath(), is("options.limit"));
        assertThat(actual.getCategory(), is("unknown_argument"));
        assertThat(actual.getSuggestedArguments(), is(Map.of()));
    }
    
    @Test
    void assertGetSupportedToolsWithNoToolHandlers() {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(Collections.emptyList());
            Class<?> registryClass = assertDoesNotThrow(() -> Class.forName(ToolDefinitionRegistry.class.getName(), false, createIsolatedToolDefinitionRegistryClassLoader()));
            InvocationTargetException actual = assertThrows(InvocationTargetException.class,
                    () -> Plugins.getMemberAccessor().invoke(registryClass.getMethod("getSupportedTools"), null));
            assertThat(actual.getCause().getClass(), is(ExceptionInInitializerError.class));
            Throwable actualCause = actual.getCause().getCause();
            assertThat(actualCause.getClass(), is(IllegalStateException.class));
            assertThat(actualCause.getMessage(), is("No tool handlers are registered."));
        }
    }
    
    private static MCPResponse dispatch(final String toolName, final Map<String, Object> arguments) {
        return ToolDefinitionRegistry.dispatch(mock(MCPRequestScope.class), ToolDefinitionRegistry.getToolDefinition(toolName), "session-1", arguments);
    }
    
    private static MCPToolDescriptor createNestedFixtureToolDescriptor() {
        Map<String, Object> optionSchema = Map.of("type", "object", "properties", Map.of("mode", Map.of("type", "string")), "required", List.of(), "additionalProperties", false);
        return new MCPToolDescriptor("fixture_tool", "Fixture Tool", "Fixture tool.", Map.of("type", "object", "properties", Map.of("options", optionSchema), "required", List.of(),
                "additionalProperties", false), Collections.emptyMap(), new MCPToolAnnotations("Fixture Tool", true, false, true, true), Collections.emptyMap());
    }
    
    private static void assertToolFields(final MCPToolDescriptor descriptor, final List<String> expectedFieldNames) {
        assertThat(getInputProperties(descriptor).keySet().stream().map(Object::toString).toList(), is(expectedFieldNames));
    }
    
    private static void assertRequiredFields(final MCPToolDescriptor descriptor, final List<String> expectedRequiredFieldNames) {
        assertThat((List<?>) descriptor.getInputSchema().get("required"), is(expectedRequiredFieldNames));
    }
    
    private static void assertField(final MCPToolDescriptor descriptor, final String fieldName, final String expectedType, final List<String> expectedEnumValues, final boolean expectedRequired) {
        Map<?, ?> actual = findField(descriptor, fieldName);
        Object enumValues = actual.get("enum");
        assertThat(actual.get("type"), is(expectedType));
        assertThat(enumValues instanceof List<?> ? (List<?>) enumValues : List.of(), is(expectedEnumValues));
        assertThat(((List<?>) descriptor.getInputSchema().get("required")).contains(fieldName), is(expectedRequired));
    }
    
    private static Map<?, ?> findField(final MCPToolDescriptor descriptor, final String fieldName) {
        return (Map<?, ?>) getInputProperties(descriptor).get(fieldName);
    }
    
    private static Map<?, ?> getInputProperties(final MCPToolDescriptor descriptor) {
        return (Map<?, ?>) descriptor.getInputSchema().get("properties");
    }
    
    private static ClassLoader createIsolatedToolDefinitionRegistryClassLoader() {
        return new ClassLoader(ToolDefinitionRegistry.class.getClassLoader()) {
            
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (!ToolDefinitionRegistry.class.getName().equals(name)) {
                    return super.loadClass(name, resolve);
                }
                synchronized (getClassLoadingLock(name)) {
                    Class<?> result = findLoadedClass(name);
                    if (null == result) {
                        byte[] bytes = readToolDefinitionRegistryClass(name);
                        result = defineClass(name, bytes, 0, bytes.length, ToolDefinitionRegistry.class.getProtectionDomain());
                    }
                    if (resolve) {
                        resolveClass(result);
                    }
                    return result;
                }
            }
        };
    }
    
    private static byte[] readToolDefinitionRegistryClass(final String name) throws ClassNotFoundException {
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream inputStream = ToolDefinitionRegistry.class.getClassLoader().getResourceAsStream(resourceName)) {
            ShardingSpherePreconditions.checkNotNull(inputStream, () -> new ClassNotFoundException(name));
            return inputStream.readAllBytes();
        } catch (final IOException ex) {
            throw new ClassNotFoundException(name, ex);
        }
    }
}

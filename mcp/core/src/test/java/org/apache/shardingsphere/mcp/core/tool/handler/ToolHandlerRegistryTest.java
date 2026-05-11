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
import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.context.MCPServiceHandlerContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ToolHandlerRegistryTest {
    
    @Test
    void assertGetSupportedTools() {
        assertThat(ToolHandlerRegistry.getSupportedTools(), contains("search_metadata", "execute_query", "execute_update", "apply_workflow", "validate_workflow"));
    }
    
    @Test
    void assertGetSupportedToolDescriptors() {
        List<MCPToolDescriptor> actual = ToolHandlerRegistry.getSupportedToolDescriptors();
        assertThat(actual.stream().map(MCPToolDescriptor::getName).toList(), is(List.of("search_metadata", "execute_query", "execute_update", "apply_workflow", "validate_workflow")));
        assertToolFields(actual.get(1), List.of("database", "schema", "sql", "max_rows", "timeout_ms"));
        assertRequiredFields(actual.get(1), List.of("database", "sql"));
        assertToolFields(actual.get(2), List.of("database", "schema", "sql", "execution_mode", "approved_by_user", "max_rows", "timeout_ms"));
        assertRequiredFields(actual.get(2), List.of("database", "sql", "execution_mode"));
        assertField(actual.get(2), "execution_mode", MCPToolValueDefinition.Type.STRING, List.of("execute", "preview"), true);
        assertField(actual.get(2), "approved_by_user", MCPToolValueDefinition.Type.BOOLEAN, List.of(), false);
        assertField(actual.get(2), "max_rows", MCPToolValueDefinition.Type.INTEGER, List.of(), false);
        assertField(actual.get(2), "timeout_ms", MCPToolValueDefinition.Type.INTEGER, List.of(), false);
        assertToolFields(actual.get(3), List.of("plan_id", "execution_mode", "approved_steps", "approved_by_user"));
        assertRequiredFields(actual.get(3), List.of("plan_id", "execution_mode"));
        assertField(actual.get(3), "execution_mode", MCPToolValueDefinition.Type.STRING, List.of("preview", "review-then-execute", "manual-only"), true);
        assertField(actual.get(3), "approved_steps", MCPToolValueDefinition.Type.ARRAY, List.of(), false);
        assertField(actual.get(3), "approved_by_user", MCPToolValueDefinition.Type.BOOLEAN, List.of(), false);
    }
    
    @Test
    void assertFindRegisteredTool() {
        Optional<MCPToolHandler<?>> actual = ToolHandlerRegistry.findRegisteredTool("search_metadata");
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow().getToolDescriptor().getName(), is("search_metadata"));
    }
    
    @Test
    void assertDispatch() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        runtimeContext.getSessionManager().createSession("session-1");
        try (MCPRequestScope requestContext = new MCPRequestScope(runtimeContext)) {
            Optional<MCPResponse> actual = ToolHandlerRegistry.dispatch(requestContext, "session-1", "search_metadata", Map.of("query", "order", "object_types", List.of("index")));
            assertTrue(actual.isPresent());
            assertThat(((List<?>) actual.orElseThrow().toPayload().get("items")).size(), is(1));
        }
    }
    
    @Test
    void assertFindRegisteredToolWithUnknownToolName() {
        assertFalse(ToolHandlerRegistry.findRegisteredTool("unknown_tool").isPresent());
    }
    
    @Test
    void assertDispatchWithUnknownToolName() {
        assertFalse(ToolHandlerRegistry.dispatch(mock(MCPRequestScope.class), "session-1", "unknown_tool", Map.of()).isPresent());
    }
    
    @Test
    void assertDispatchWithMissingRequiredArgument() {
        MCPInvalidRequestException actual =
                assertThrows(MCPInvalidRequestException.class, () -> ToolHandlerRegistry.dispatch(mock(MCPRequestScope.class), "session-1", "execute_query", Map.of("database", "logic_db")));
        assertThat(actual.getMessage(), is("sql is required."));
    }
    
    @Test
    void assertDispatchWithMissingExecutionMode() {
        MCPExecutionModeRequiredException actual = assertThrows(MCPExecutionModeRequiredException.class,
                () -> ToolHandlerRegistry.dispatch(mock(MCPRequestScope.class), "session-1", "execute_update",
                        Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1")));
        assertThat(actual.getMessage(), is("execute_update execution_mode is required."));
        assertThat(actual.getToolName(), is("execute_update"));
        assertThat(actual.getAllowedValues(), is(List.of("execute", "preview")));
        assertThat(actual.getSuggestedArguments(), is(Map.of("database", "logic_db", "schema", "public",
                "sql", "UPDATE orders SET status = 'PAID' WHERE order_id = 1", "execution_mode", "preview")));
    }
    
    @Test
    void assertGetSupportedToolsWithNoToolHandlers() {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(Collections.emptyList());
            Class<?> registryClass = assertDoesNotThrow(() -> Class.forName(ToolHandlerRegistry.class.getName(), false, createIsolatedToolHandlerRegistryClassLoader()));
            InvocationTargetException actual = assertThrows(InvocationTargetException.class,
                    () -> Plugins.getMemberAccessor().invoke(registryClass.getMethod("getSupportedTools"), null));
            assertThat(actual.getCause().getClass(), is(ExceptionInInitializerError.class));
            Throwable actualCause = actual.getCause().getCause();
            assertThat(actualCause.getClass(), is(IllegalStateException.class));
            assertThat(actualCause.getMessage(), is("No tool handlers are registered."));
        }
    }
    
    @Test
    void assertCreateRegisteredToolsWithInvalidHandlers() {
        MCPToolHandler<?> firstHandler = createToolHandler("search_metadata");
        MCPToolHandler<?> secondHandler = createToolHandler("search_metadata");
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> ToolHandlerRegistry.createRegisteredTools(List.of(firstHandler, secondHandler)));
        assertThat(actual.getMessage(), is(String.format("Duplicate tool name `search_metadata` with `%s` and `%s`.",
                firstHandler.getClass().getName(), secondHandler.getClass().getName())));
        MCPToolHandler<?> handler = mock(MCPToolHandler.class);
        actual = assertThrows(IllegalArgumentException.class, () -> ToolHandlerRegistry.createRegisteredTools(List.of(handler)));
        assertThat(actual.getMessage(), is(String.format("Tool descriptor is required for `%s`.", handler.getClass().getName())));
        MCPToolHandler<?> nullNameHandler = createToolHandler(null);
        actual = assertThrows(IllegalArgumentException.class, () -> ToolHandlerRegistry.createRegisteredTools(List.of(nullNameHandler)));
        assertThat(actual.getMessage(), is(String.format("Tool name is required for `%s`.", nullNameHandler.getClass().getName())));
        MCPToolHandler<?> blankNameHandler = createToolHandler("   ");
        actual = assertThrows(IllegalArgumentException.class, () -> ToolHandlerRegistry.createRegisteredTools(List.of(blankNameHandler)));
        assertThat(actual.getMessage(), is(String.format("Tool name is required for `%s`.", blankNameHandler.getClass().getName())));
        MCPToolHandler<MCPHandlerContext> unsupportedHandler = mock(MCPToolHandler.class);
        when(unsupportedHandler.getContextType()).thenReturn(MCPHandlerContext.class);
        when(unsupportedHandler.getToolDescriptor()).thenReturn(new MCPToolDescriptor("unsupported", "Unsupported", "Unsupported tool.", List.of()));
        actual = assertThrows(IllegalArgumentException.class, () -> ToolHandlerRegistry.createRegisteredTools(List.of(unsupportedHandler)));
        assertThat(actual.getMessage(), is(String.format("Unsupported handler context type `%s` for `%s`.", MCPHandlerContext.class.getName(), unsupportedHandler.getClass().getName())));
    }
    
    @Test
    void assertCreateRegisteredTools() {
        MCPToolHandler<?> firstHandler = createToolHandler("search_metadata");
        MCPToolHandler<?> secondHandler = createToolHandler("execute_query");
        Map<String, MCPToolHandler<?>> actual = ToolHandlerRegistry.createRegisteredTools(List.of(firstHandler, secondHandler));
        assertThat(actual.size(), is(2));
        assertThat(actual.keySet().stream().toList(), is(List.of("search_metadata", "execute_query")));
    }
    
    private static MCPToolHandler<?> createToolHandler(final String toolName) {
        MCPToolDescriptor descriptor = mock(MCPToolDescriptor.class);
        when(descriptor.getName()).thenReturn(toolName);
        MCPToolHandler<MCPServiceHandlerContext> result = mock(MCPToolHandler.class);
        when(result.getContextType()).thenReturn(MCPServiceHandlerContext.class);
        when(result.getToolDescriptor()).thenReturn(descriptor);
        return result;
    }
    
    private static void assertToolFields(final MCPToolDescriptor descriptor, final List<String> expectedFieldNames) {
        assertThat(descriptor.getFields().stream().map(MCPToolFieldDefinition::getName).toList(), is(expectedFieldNames));
    }
    
    private static void assertRequiredFields(final MCPToolDescriptor descriptor, final List<String> expectedRequiredFieldNames) {
        assertThat(descriptor.getFields().stream().filter(MCPToolFieldDefinition::isRequired).map(MCPToolFieldDefinition::getName).toList(), is(expectedRequiredFieldNames));
    }
    
    private static void assertField(final MCPToolDescriptor descriptor, final String fieldName, final MCPToolValueDefinition.Type expectedType, final List<String> expectedEnumValues,
                                    final boolean expectedRequired) {
        MCPToolFieldDefinition actual = findField(descriptor, fieldName);
        assertThat(actual.getValueDefinition().getType(), is(expectedType));
        assertThat(actual.getValueDefinition().getEnumValues(), is(expectedEnumValues));
        assertThat(actual.isRequired(), is(expectedRequired));
    }
    
    private static MCPToolFieldDefinition findField(final MCPToolDescriptor descriptor, final String fieldName) {
        return descriptor.getFields().stream().filter(each -> fieldName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private static ClassLoader createIsolatedToolHandlerRegistryClassLoader() {
        return new ClassLoader(ToolHandlerRegistry.class.getClassLoader()) {
            
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (!ToolHandlerRegistry.class.getName().equals(name)) {
                    return super.loadClass(name, resolve);
                }
                synchronized (getClassLoadingLock(name)) {
                    Class<?> result = findLoadedClass(name);
                    if (null == result) {
                        byte[] bytes = readToolHandlerRegistryClass(name);
                        result = defineClass(name, bytes, 0, bytes.length, ToolHandlerRegistry.class.getProtectionDomain());
                    }
                    if (resolve) {
                        resolveClass(result);
                    }
                    return result;
                }
            }
        };
    }
    
    private static byte[] readToolHandlerRegistryClass(final String name) throws ClassNotFoundException {
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream inputStream = ToolHandlerRegistry.class.getClassLoader().getResourceAsStream(resourceName)) {
            ShardingSpherePreconditions.checkNotNull(inputStream, () -> new ClassNotFoundException(name));
            return inputStream.readAllBytes();
        } catch (final IOException ex) {
            throw new ClassNotFoundException(name, ex);
        }
    }
}

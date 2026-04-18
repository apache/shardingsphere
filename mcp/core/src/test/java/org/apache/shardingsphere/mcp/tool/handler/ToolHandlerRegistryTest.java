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

package org.apache.shardingsphere.mcp.tool.handler;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
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
        assertThat(ToolHandlerRegistry.getSupportedTools(), is(List.of(
                "search_metadata", "execute_query", "plan_encrypt_mask_rule", "apply_encrypt_mask_rule", "validate_encrypt_mask_rule")));
    }
    
    @Test
    void assertGetSupportedToolDescriptors() {
        List<MCPToolDescriptor> actual = ToolHandlerRegistry.getSupportedToolDescriptors();
        assertThat(actual.stream().map(MCPToolDescriptor::getName).toList(), is(List.of(
                "search_metadata", "execute_query", "plan_encrypt_mask_rule", "apply_encrypt_mask_rule", "validate_encrypt_mask_rule")));
        assertThat(actual.stream().map(each -> each.getFields().size()).toList(), is(List.of(6, 5, 24, 3, 1)));
    }
    
    @Test
    void assertFindRegisteredHandler() {
        Optional<ToolHandler> actual = ToolHandlerRegistry.findRegisteredHandler("search_metadata");
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow().getToolDescriptor().getName(), is("search_metadata"));
    }
    
    @Test
    void assertFindRegisteredHandlerWithUnknownToolName() {
        assertFalse(ToolHandlerRegistry.findRegisteredHandler("unknown_tool").isPresent());
    }
    
    @Test
    void assertGetSupportedToolsWithNoToolHandlers() {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(ToolHandler.class)).thenReturn(Collections.emptyList());
            Class<?> registryClass = assertDoesNotThrow(
                    () -> Class.forName(ToolHandlerRegistry.class.getName(), false, createIsolatedToolHandlerRegistryClassLoader()));
            ExceptionInInitializerError actual = assertThrows(ExceptionInInitializerError.class,
                    () -> MethodHandles.publicLookup().findStatic(registryClass, "getSupportedTools", MethodType.methodType(List.class))
                            .invokeWithArguments());
            assertThat(actual.getCause().getClass(), is(IllegalStateException.class));
            assertThat(actual.getCause().getMessage(), is("No tool handlers are registered."));
        }
    }
    
    @Test
    void assertGetSupportedToolsWithDuplicateToolNames() {
        ToolHandler firstHandler = createToolHandler("search_metadata");
        ToolHandler secondHandler = createToolHandler("search_metadata");
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(ToolHandler.class)).thenReturn(List.of(firstHandler, secondHandler));
            Class<?> registryClass = assertDoesNotThrow(() -> Class.forName(ToolHandlerRegistry.class.getName(), false, createIsolatedToolHandlerRegistryClassLoader()));
            ExceptionInInitializerError actual = assertThrows(ExceptionInInitializerError.class,
                    () -> MethodHandles.publicLookup().findStatic(registryClass, "getSupportedTools", MethodType.methodType(List.class)).invokeWithArguments());
            assertThat(actual.getCause().getClass(), is(IllegalArgumentException.class));
            assertThat(actual.getCause().getMessage(),
                    is(String.format("Duplicate tool name `search_metadata` with `%s` and `%s`.", firstHandler.getClass().getName(), secondHandler.getClass().getName())));
        }
    }
    
    private static ToolHandler createToolHandler(final String toolName) {
        MCPToolDescriptor descriptor = mock(MCPToolDescriptor.class);
        when(descriptor.getName()).thenReturn(toolName);
        ToolHandler result = mock(ToolHandler.class);
        when(result.getToolDescriptor()).thenReturn(descriptor);
        return result;
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

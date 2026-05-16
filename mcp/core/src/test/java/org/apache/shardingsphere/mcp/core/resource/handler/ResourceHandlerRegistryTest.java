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

package org.apache.shardingsphere.mcp.core.resource.handler;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.MCPHandlerContext;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPServiceHandlerContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ResourceHandlerRegistryTest {
    
    @Test
    void assertDispatch() {
        Optional<MCPResponse> actual = ResourceHandlerRegistry.dispatch(mock(MCPRequestScope.class), "shardingsphere://capabilities");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().toPayload().containsKey("supportedTools"));
    }
    
    @Test
    void assertDispatchWithoutMatchedHandler() {
        assertFalse(ResourceHandlerRegistry.dispatch(mock(MCPRequestScope.class), "unsupported://resource").isPresent());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getRegisteredResourcesFailureCases")
    void assertGetRegisteredResourcesFailure(final String name, final Collection<MCPResourceHandler<?>> handlers,
                                             final Class<? extends Throwable> expectedCauseType, final String expectedMessage) throws ReflectiveOperationException {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            MCPHandlerProvider provider = createHandlerProvider(handlers);
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(provider));
            Class<?> registryClass = Class.forName(ResourceHandlerRegistry.class.getName(), false, createIsolatedResourceHandlerRegistryClassLoader());
            var getRegisteredResourcesMethod = registryClass.getDeclaredMethod("getRegisteredResources");
            InvocationTargetException actual = assertThrows(InvocationTargetException.class,
                    () -> Plugins.getMemberAccessor().invoke(getRegisteredResourcesMethod, null));
            assertThat(actual.getCause().getClass(), is(ExceptionInInitializerError.class));
            Throwable actualCause = actual.getCause().getCause();
            assertThat(actualCause.getClass(), is(expectedCauseType));
            assertThat(actualCause.getMessage(), is(expectedMessage));
        }
    }
    
    @Test
    void assertGetSupportedResources() {
        Collection<String> actual = ResourceHandlerRegistry.getSupportedResources();
        assertThat(actual.size(), is(20));
        assertTrue(actual.contains("shardingsphere://capabilities"));
        assertTrue(actual.contains("shardingsphere://runtime"));
        assertTrue(actual.contains("shardingsphere://workflows/{plan_id}"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}"));
    }
    
    private static Stream<Arguments> getRegisteredResourcesFailureCases() {
        MCPResourceHandler<?> nullUriHandler = createResourceHandler(null);
        MCPResourceHandler<?> emptyUriHandler = createResourceHandler("");
        MCPResourceHandler<?> blankUriHandler = createResourceHandler("   ");
        MCPResourceHandler<?> duplicateUriHandler = createResourceHandler("shardingsphere://foo/{id}");
        MCPResourceHandler<?> otherDuplicateUriHandler = createResourceHandler("shardingsphere://foo/{id}");
        MCPResourceHandler<?> overlappingUriHandler = createResourceHandler("shardingsphere://foo/{id}");
        MCPResourceHandler<?> otherOverlappingUriHandler = createResourceHandler("shardingsphere://foo/bar");
        return Stream.of(
                Arguments.of("no resource handlers", Collections.emptyList(), IllegalStateException.class, "No resource handlers are registered."),
                Arguments.of("null resource URI", List.of(nullUriHandler), IllegalArgumentException.class,
                        getRequiredUriMessage()),
                Arguments.of("empty resource URI", List.of(emptyUriHandler), IllegalArgumentException.class,
                        getRequiredUriMessage()),
                Arguments.of("blank resource URI", List.of(blankUriHandler), IllegalArgumentException.class,
                        getRequiredUriMessage()),
                Arguments.of("duplicate resource URI template",
                        List.of(duplicateUriHandler, otherDuplicateUriHandler), IllegalArgumentException.class,
                        getDuplicateUriTemplateMessage()),
                Arguments.of("overlapping resource URI template",
                        List.of(overlappingUriHandler, otherOverlappingUriHandler), IllegalArgumentException.class,
                        getOverlappingUriTemplateMessage()),
                Arguments.of("unsupported resource handler context", List.of(createUnsupportedResourceHandler()), IllegalArgumentException.class,
                        getUnsupportedResourceHandlerMessage()));
    }
    
    private static MCPResourceHandler<?> createResourceHandler(final String uriTemplate) {
        MCPResourceHandler<MCPServiceHandlerContext> result = mock(MCPResourceHandler.class);
        when(result.getContextType()).thenReturn(MCPServiceHandlerContext.class);
        when(result.getResourceDescriptor()).thenReturn(createResourceDescriptor(uriTemplate, "foo", "Foo"));
        return result;
    }
    
    private static MCPResourceHandler<?> createUnsupportedResourceHandler() {
        MCPResourceHandler<MCPHandlerContext> result = mock(MCPResourceHandler.class);
        when(result.getContextType()).thenReturn(MCPHandlerContext.class);
        when(result.getResourceDescriptor()).thenReturn(createResourceDescriptor("shardingsphere://unsupported", "unsupported", "Unsupported"));
        return result;
    }
    
    private static MCPResourceDescriptor createResourceDescriptor(final String uriOrTemplate, final String name, final String title) {
        return new MCPResourceDescriptor(uriOrTemplate, name, title, String.format("Read the %s fixture resource.", name), "application/json", MCPResourceAnnotations.EMPTY,
                Collections.emptyMap());
    }
    
    private static MCPHandlerProvider createHandlerProvider(final Collection<MCPResourceHandler<?>> resourceHandlers) {
        MCPHandlerProvider result = mock(MCPHandlerProvider.class);
        when(result.getResourceHandlers()).thenReturn(resourceHandlers);
        return result;
    }
    
    private static ClassLoader createIsolatedResourceHandlerRegistryClassLoader() {
        return new ClassLoader(ResourceHandlerRegistry.class.getClassLoader()) {
            
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (!ResourceHandlerRegistry.class.getName().equals(name)) {
                    return super.loadClass(name, resolve);
                }
                synchronized (getClassLoadingLock(name)) {
                    Class<?> result = findLoadedClass(name);
                    if (null == result) {
                        byte[] bytes = readResourceHandlerRegistryClass(name);
                        result = defineClass(name, bytes, 0, bytes.length, ResourceHandlerRegistry.class.getProtectionDomain());
                    }
                    if (resolve) {
                        resolveClass(result);
                    }
                    return result;
                }
            }
        };
    }
    
    private static byte[] readResourceHandlerRegistryClass(final String name) throws ClassNotFoundException {
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream inputStream = ResourceHandlerRegistry.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (null == inputStream) {
                throw new ClassNotFoundException(name);
            }
            return inputStream.readAllBytes();
        } catch (final IOException ex) {
            throw new ClassNotFoundException(name, ex);
        }
    }
    
    private static String getRequiredUriMessage() {
        MCPResourceHandler<?> handler = createResourceHandler(null);
        return String.format("Resource URI or URI template is required for `%s`.", handler.getClass().getName());
    }
    
    private static String getDuplicateUriTemplateMessage() {
        MCPResourceHandler<?> firstHandler = createResourceHandler("shardingsphere://foo/{id}");
        MCPResourceHandler<?> secondHandler = createResourceHandler("shardingsphere://foo/{id}");
        return String.format("Duplicate resource URI template `shardingsphere://foo/{id}` with `%s` and `%s`.",
                firstHandler.getClass().getName(), secondHandler.getClass().getName());
    }
    
    private static String getOverlappingUriTemplateMessage() {
        MCPResourceHandler<?> firstHandler = createResourceHandler("shardingsphere://foo/{id}");
        MCPResourceHandler<?> secondHandler = createResourceHandler("shardingsphere://foo/bar");
        return String.format("Overlapping resource URI templates `shardingsphere://foo/{id}` with `%s` and `%s`.",
                firstHandler.getClass().getName(), secondHandler.getClass().getName());
    }
    
    private static String getUnsupportedResourceHandlerMessage() {
        MCPResourceHandler<?> handler = createUnsupportedResourceHandler();
        return String.format("Unsupported handler context type `%s` for `%s`.", MCPHandlerContext.class.getName(), handler.getClass().getName());
    }
}

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
import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ResourceDefinitionRegistryTest {
    
    @Test
    void assertDispatch() {
        Optional<MCPSuccessPayload> actual = ResourceDefinitionRegistry.dispatch(mock(MCPFeatureRuntimeRequestContext.class), "shardingsphere://capabilities");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().toPayload().containsKey("supportedStatementClasses"));
    }
    
    @Test
    void assertDispatchWithoutMatchedHandler() {
        assertFalse(ResourceDefinitionRegistry.dispatch(mock(MCPFeatureRuntimeRequestContext.class), "unsupported://resource").isPresent());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSupportedResourcesFailureCases")
    void assertGetSupportedResourcesFailure(final String name, final Collection<MCPResourceHandler<?>> handlers,
                                            final Class<? extends Throwable> expectedCauseType, final String expectedMessage) {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            MCPHandlerProvider provider = createHandlerProvider(handlers);
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(provider));
            Class<?> registryClass = assertDoesNotThrow(() -> Class.forName(ResourceDefinitionRegistry.class.getName(), false, createIsolatedResourceDefinitionRegistryClassLoader()));
            InvocationTargetException actual = assertThrows(InvocationTargetException.class,
                    () -> Plugins.getMemberAccessor().invoke(registryClass.getMethod("getSupportedResources"), null));
            assertThat(actual.getCause().getClass(), is(ExceptionInInitializerError.class));
            Throwable actualCause = actual.getCause().getCause();
            assertThat(actualCause.getClass(), is(expectedCauseType));
            assertThat(actualCause.getMessage(), is(expectedMessage));
        }
    }
    
    @Test
    void assertGetSupportedResources() {
        Collection<String> actual = ResourceDefinitionRegistry.getSupportedResources();
        assertThat(actual, is(List.of(
                "shardingsphere://capabilities",
                "shardingsphere://guidance",
                "shardingsphere://runtime",
                "shardingsphere://workflows/{plan_id}",
                "shardingsphere://databases/{database}/capabilities",
                "shardingsphere://databases",
                "shardingsphere://databases/{database}",
                "shardingsphere://databases/{database}/storage-units",
                "shardingsphere://databases/{database}/storage-units/{storageUnit}",
                "shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules",
                "shardingsphere://databases/{database}/single-tables",
                "shardingsphere://databases/{database}/single-tables/{table}",
                "shardingsphere://databases/{database}/single-table/default-storage-unit",
                "shardingsphere://databases/{database}/schemas",
                "shardingsphere://databases/{database}/schemas/{schema}",
                "shardingsphere://databases/{database}/schemas/{schema}/sequences",
                "shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                "shardingsphere://databases/{database}/schemas/{schema}/tables",
                "shardingsphere://databases/{database}/schemas/{schema}/views",
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}")));
    }
    
    @Test
    void assertGetSupportedResourceDescriptors() {
        Collection<String> actual = ResourceDefinitionRegistry.getSupportedResourceDescriptors().stream().map(MCPResourceDescriptor::getUriTemplate).toList();
        assertThat(actual, is(ResourceDefinitionRegistry.getSupportedResources()));
    }
    
    private static Stream<Arguments> getSupportedResourcesFailureCases() {
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
        MCPResourceHandler<MCPRequestContext> result = mock(MCPResourceHandler.class);
        when(result.getContextType()).thenReturn(MCPRequestContext.class);
        when(result.getResourceUriTemplate()).thenReturn(uriTemplate);
        return result;
    }
    
    private static MCPResourceHandler<?> createUnsupportedResourceHandler() {
        MCPResourceHandler<MCPFeatureRuntimeRequestContext> result = mock(MCPResourceHandler.class);
        when(result.getContextType()).thenReturn(MCPFeatureRuntimeRequestContext.class);
        when(result.getResourceUriTemplate()).thenReturn("shardingsphere://unsupported");
        return result;
    }
    
    private static MCPHandlerProvider createHandlerProvider(final Collection<MCPResourceHandler<?>> resourceHandlers) {
        MCPHandlerProvider result = mock(MCPHandlerProvider.class);
        when(result.getResourceHandlers()).thenReturn(resourceHandlers);
        return result;
    }
    
    private static ClassLoader createIsolatedResourceDefinitionRegistryClassLoader() {
        return new ClassLoader(ResourceDefinitionRegistry.class.getClassLoader()) {
            
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                if (!ResourceDefinitionRegistry.class.getName().equals(name)) {
                    return super.loadClass(name, resolve);
                }
                synchronized (getClassLoadingLock(name)) {
                    Class<?> result = findLoadedClass(name);
                    if (null == result) {
                        byte[] bytes = readResourceDefinitionRegistryClass(name);
                        result = defineClass(name, bytes, 0, bytes.length, ResourceDefinitionRegistry.class.getProtectionDomain());
                    }
                    if (resolve) {
                        resolveClass(result);
                    }
                    return result;
                }
            }
        };
    }
    
    private static byte[] readResourceDefinitionRegistryClass(final String name) throws ClassNotFoundException {
        String resourceName = name.replace('.', '/') + ".class";
        try (InputStream inputStream = ResourceDefinitionRegistry.class.getClassLoader().getResourceAsStream(resourceName)) {
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
        return String.format("Resource URI template is required for `%s`.", handler.getClass().getName());
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
        return String.format("Unsupported request context type `%s` for `%s`.", MCPFeatureRuntimeRequestContext.class.getName(), handler.getClass().getName());
    }
}

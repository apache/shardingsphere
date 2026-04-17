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

package org.apache.shardingsphere.mcp.resource.handler;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
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
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ResourceHandlerRegistryTest {
    
    @Test
    void assertGetRegisteredHandlers() {
        assertThat(ResourceHandlerRegistry.getRegisteredHandlers().size(), is(24));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getRegisteredHandlersFailureCases")
    void assertGetRegisteredHandlersFailure(final String name, final Collection<ResourceHandler> handlers,
                                            final Class<? extends Throwable> expectedCauseType, final String expectedMessage) throws ReflectiveOperationException {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(ResourceHandler.class)).thenReturn(handlers);
            Class<?> registryClass = Class.forName(ResourceHandlerRegistry.class.getName(), false, createIsolatedResourceHandlerRegistryClassLoader());
            InvocationTargetException actual = assertThrows(InvocationTargetException.class,
                    () -> Plugins.getMemberAccessor().invoke(registryClass.getMethod("getRegisteredHandlers"), null));
            assertThat(actual.getCause().getClass(), is(ExceptionInInitializerError.class));
            Throwable actualCause = actual.getCause().getCause();
            assertThat(actualCause.getClass(), is(expectedCauseType));
            assertThat(actualCause.getMessage(), is(expectedMessage));
        }
    }
    
    @Test
    void assertGetSupportedResources() {
        List<String> actual = ResourceHandlerRegistry.getSupportedResources();
        assertThat(actual.size(), is(24));
        assertTrue(actual.contains("shardingsphere://capabilities"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/encrypt-rules"));
        assertTrue(actual.contains("shardingsphere://plugins/encrypt-algorithms"));
    }
    
    private static Stream<Arguments> getRegisteredHandlersFailureCases() {
        ResourceHandler nullPatternHandler = createResourceHandler(null);
        ResourceHandler emptyPatternHandler = createResourceHandler("");
        ResourceHandler duplicatePatternHandler = createResourceHandler("shardingsphere://foo/{id}");
        ResourceHandler otherDuplicatePatternHandler = createResourceHandler("shardingsphere://foo/{id}");
        ResourceHandler overlappingPatternHandler = createResourceHandler("shardingsphere://foo/{id}");
        ResourceHandler otherOverlappingPatternHandler = createResourceHandler("shardingsphere://foo/bar");
        return Stream.of(
                Arguments.of("no resource handlers", Collections.emptyList(), IllegalStateException.class, "No resource handlers are registered."),
                Arguments.of("null resource URI pattern", List.of(nullPatternHandler), IllegalArgumentException.class,
                        getRequiredUriPatternMessage(nullPatternHandler)),
                Arguments.of("empty resource URI pattern", List.of(emptyPatternHandler), IllegalArgumentException.class,
                        getRequiredUriPatternMessage(emptyPatternHandler)),
                Arguments.of("duplicate resource URI pattern",
                        List.of(duplicatePatternHandler, otherDuplicatePatternHandler), IllegalArgumentException.class,
                        getDuplicateUriPatternMessage(duplicatePatternHandler, otherDuplicatePatternHandler)),
                Arguments.of("overlapping resource URI pattern",
                        List.of(overlappingPatternHandler, otherOverlappingPatternHandler), IllegalArgumentException.class,
                        getOverlappingUriPatternMessage(overlappingPatternHandler, otherOverlappingPatternHandler)));
    }
    
    private static ResourceHandler createResourceHandler(final String uriPattern) {
        ResourceHandler result = mock(ResourceHandler.class);
        when(result.getUriPattern()).thenReturn(uriPattern);
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
    
    private static String getRequiredUriPatternMessage(final ResourceHandler handler) {
        return String.format("Resource URI pattern is required for `%s`.", handler.getClass().getName());
    }
    
    private static String getDuplicateUriPatternMessage(final ResourceHandler handler, final ResourceHandler otherHandler) {
        return String.format("Duplicate resource URI pattern `shardingsphere://foo/{id}` with `%s` and `%s`.", handler.getClass().getName(), otherHandler.getClass().getName());
    }
    
    private static String getOverlappingUriPatternMessage(final ResourceHandler handler, final ResourceHandler otherHandler) {
        return String.format("Overlapping resource URI patterns `shardingsphere://foo/{id}` with `%s` and `%s`.", handler.getClass().getName(), otherHandler.getClass().getName());
    }
}

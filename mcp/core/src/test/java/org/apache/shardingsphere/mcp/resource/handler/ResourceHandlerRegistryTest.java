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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceContribution;
import org.apache.shardingsphere.mcp.api.resource.handler.ServerResourceHandler;
import org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider;
import org.apache.shardingsphere.mcp.context.MCPRequestScope;
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
    void assertGetRegisteredResources() {
        assertThat(ResourceHandlerRegistry.getRegisteredResources().size(), is(18));
        assertThrows(UnsupportedOperationException.class, () -> ResourceHandlerRegistry.getRegisteredResources().clear());
    }
    
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
    void assertGetRegisteredResourcesFailure(final String name, final Collection<MCPResourceContribution> contributions,
                                             final Class<? extends Throwable> expectedCauseType, final String expectedMessage) throws ReflectiveOperationException {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            MCPContributionProvider provider = createContributionProvider(contributions);
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPContributionProvider.class)).thenReturn(List.of(provider));
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
        List<String> actual = ResourceHandlerRegistry.getSupportedResources();
        assertThat(actual.size(), is(18));
        assertTrue(actual.contains("shardingsphere://capabilities"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns"));
        assertTrue(actual.contains("shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}"));
        assertThrows(UnsupportedOperationException.class, () -> ResourceHandlerRegistry.getSupportedResources().clear());
    }
    
    private static Stream<Arguments> getRegisteredResourcesFailureCases() {
        MCPResourceContribution nullPatternContribution = createResourceContribution(null);
        MCPResourceContribution emptyPatternContribution = createResourceContribution("");
        MCPResourceContribution blankPatternContribution = createResourceContribution("   ");
        MCPResourceContribution duplicatePatternContribution = createResourceContribution("shardingsphere://foo/{id}");
        MCPResourceContribution otherDuplicatePatternContribution = createResourceContribution("shardingsphere://foo/{id}");
        MCPResourceContribution overlappingPatternContribution = createResourceContribution("shardingsphere://foo/{id}");
        MCPResourceContribution otherOverlappingPatternContribution = createResourceContribution("shardingsphere://foo/bar");
        return Stream.of(
                Arguments.of("no resource contributions", Collections.emptyList(), IllegalStateException.class, "No resource contributions are registered."),
                Arguments.of("null resource URI pattern", List.of(nullPatternContribution), IllegalArgumentException.class,
                        getRequiredUriPatternMessage()),
                Arguments.of("empty resource URI pattern", List.of(emptyPatternContribution), IllegalArgumentException.class,
                        getRequiredUriPatternMessage()),
                Arguments.of("blank resource URI pattern", List.of(blankPatternContribution), IllegalArgumentException.class,
                        getRequiredUriPatternMessage()),
                Arguments.of("duplicate resource URI pattern",
                        List.of(duplicatePatternContribution, otherDuplicatePatternContribution), IllegalArgumentException.class,
                        getDuplicateUriPatternMessage()),
                Arguments.of("overlapping resource URI pattern",
                        List.of(overlappingPatternContribution, otherOverlappingPatternContribution), IllegalArgumentException.class,
                        getOverlappingUriPatternMessage()),
                Arguments.of("unsupported resource contribution type", List.of(createUnsupportedResourceContribution()), IllegalArgumentException.class,
                        getUnsupportedResourceContributionMessage()));
    }
    
    private static MCPResourceContribution createResourceContribution(final String uriPattern) {
        MCPResourceContribution result = mock(ServerResourceHandler.class);
        when(result.getUriPattern()).thenReturn(uriPattern);
        return result;
    }
    
    private static MCPResourceContribution createUnsupportedResourceContribution() {
        MCPResourceContribution result = mock(MCPResourceContribution.class);
        when(result.getUriPattern()).thenReturn("shardingsphere://unsupported");
        return result;
    }
    
    private static MCPContributionProvider createContributionProvider(final Collection<MCPResourceContribution> resourceContributions) {
        MCPContributionProvider result = mock(MCPContributionProvider.class);
        when(result.getResourceContributions()).thenReturn(resourceContributions);
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
    
    private static String getRequiredUriPatternMessage() {
        MCPResourceContribution contribution = createResourceContribution(null);
        return String.format("Resource URI pattern is required for `%s`.", contribution.getClass().getName());
    }
    
    private static String getDuplicateUriPatternMessage() {
        MCPResourceContribution firstContribution = createResourceContribution("shardingsphere://foo/{id}");
        MCPResourceContribution secondContribution = createResourceContribution("shardingsphere://foo/{id}");
        return String.format("Duplicate resource URI pattern `shardingsphere://foo/{id}` with `%s` and `%s`.",
                firstContribution.getClass().getName(), secondContribution.getClass().getName());
    }
    
    private static String getOverlappingUriPatternMessage() {
        MCPResourceContribution firstContribution = createResourceContribution("shardingsphere://foo/{id}");
        MCPResourceContribution secondContribution = createResourceContribution("shardingsphere://foo/bar");
        return String.format("Overlapping resource URI patterns `shardingsphere://foo/{id}` with `%s` and `%s`.",
                firstContribution.getClass().getName(), secondContribution.getClass().getName());
    }
    
    private static String getUnsupportedResourceContributionMessage() {
        MCPResourceContribution contribution = createUnsupportedResourceContribution();
        return String.format("Unsupported resource contribution type `%s`.", contribution.getClass().getName());
    }
}

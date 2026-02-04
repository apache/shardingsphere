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

package org.apache.shardingsphere.proxy.frontend.connection;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ConnectionLimitContextTest {
    
    @BeforeEach
    @AfterEach
    void resetActiveConnections() {
        setActiveConnections(0);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("connectionAllowedArguments")
    void assertConnectionAllowed(final String name, final int maxConnections, final int initialActiveConnections, final boolean expectedAllowed, final int expectedActiveAfterCall) {
        mockMaxConnections(maxConnections);
        setActiveConnections(initialActiveConnections);
        assertThat(ConnectionLimitContext.getInstance().connectionAllowed(), is(expectedAllowed));
        assertThat(getActiveConnections(), is(expectedActiveAfterCall));
    }
    
    @Test
    void assertConnectionInactiveDecrementsCounter() {
        setActiveConnections(1);
        ConnectionLimitContext.getInstance().connectionInactive();
        assertThat(getActiveConnections(), is(0));
    }
    
    @Test
    void assertLimitsMaxConnectionsEnabled() {
        mockMaxConnections(1);
        assertTrue(ConnectionLimitContext.getInstance().limitsMaxConnections());
    }
    
    @Test
    void assertLimitsMaxConnectionsDisabled() {
        mockMaxConnections(0);
        assertFalse(ConnectionLimitContext.getInstance().limitsMaxConnections());
    }
    
    private void mockMaxConnections(final int maxConnections) {
        ContextManager contextManager = mockContextManager(maxConnections);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private ContextManager mockContextManager(final int maxConnections) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS)).thenReturn(maxConnections);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setActiveConnections(final int value) {
        ((AtomicInteger) Plugins.getMemberAccessor().get(ConnectionLimitContext.class.getDeclaredField("activeConnections"), ConnectionLimitContext.getInstance())).set(value);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private int getActiveConnections() {
        return ((AtomicInteger) Plugins.getMemberAccessor().get(ConnectionLimitContext.class.getDeclaredField("activeConnections"), ConnectionLimitContext.getInstance())).get();
    }
    
    private static Stream<Arguments> connectionAllowedArguments() {
        return Stream.of(
                Arguments.of("allowed", 2, 1, true, 2),
                Arguments.of("rejected", 2, 2, false, 3),
                Arguments.of("unlimited", 0, 0, true, 1));
    }
}

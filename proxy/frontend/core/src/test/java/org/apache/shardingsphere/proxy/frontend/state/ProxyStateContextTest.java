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

package org.apache.shardingsphere.proxy.frontend.state;

import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.state.impl.CircuitBreakProxyState;
import org.apache.shardingsphere.proxy.frontend.state.impl.OKProxyState;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ProxyStateContextTest {
    
    @Test
    void assertExecuteWithOKState() {
        try (
                MockedConstruction<OKProxyState> ignoredOkStateConstruction = mockConstruction(OKProxyState.class);
                MockedConstruction<CircuitBreakProxyState> ignoredCircuitBreakConstruction = mockConstruction(CircuitBreakProxyState.class)) {
            when(ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getInstance().getState().getCurrentState()).thenReturn(InstanceState.OK);
            ProxyState okState = new OKProxyState();
            ProxyState circuitBreakState = new CircuitBreakProxyState();
            replaceStates(okState, circuitBreakState);
            ChannelHandlerContext context = mock(ChannelHandlerContext.class);
            Object message = new Object();
            DatabaseProtocolFrontendEngine frontendEngine = mock(DatabaseProtocolFrontendEngine.class);
            ConnectionSession connectionSession = mock(ConnectionSession.class);
            ProxyStateContext.execute(context, message, frontendEngine, connectionSession);
            verify(okState).execute(context, message, frontendEngine, connectionSession);
        }
    }
    
    @Test
    void assertExecuteWithCircuitBreakState() {
        try (
                MockedConstruction<OKProxyState> ignoredOkStateConstruction = mockConstruction(OKProxyState.class);
                MockedConstruction<CircuitBreakProxyState> ignoredCircuitBreakConstruction = mockConstruction(CircuitBreakProxyState.class)) {
            when(ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getInstance().getState().getCurrentState()).thenReturn(InstanceState.CIRCUIT_BREAK);
            ProxyState okState = new OKProxyState();
            ProxyState circuitBreakState = new CircuitBreakProxyState();
            replaceStates(okState, circuitBreakState);
            ChannelHandlerContext context = mock(ChannelHandlerContext.class);
            Object message = new Object();
            DatabaseProtocolFrontendEngine frontendEngine = mock(DatabaseProtocolFrontendEngine.class);
            ConnectionSession connectionSession = mock(ConnectionSession.class);
            ProxyStateContext.execute(context, message, frontendEngine, connectionSession);
            verify(circuitBreakState).execute(context, message, frontendEngine, connectionSession);
        }
    }
    
    private void replaceStates(final ProxyState okState, final ProxyState circuitBreakState) {
        Map<InstanceState, ProxyState> states = getStates();
        states.clear();
        states.put(InstanceState.OK, okState);
        states.put(InstanceState.CIRCUIT_BREAK, circuitBreakState);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<InstanceState, ProxyState> getStates() {
        return (Map<InstanceState, ProxyState>) Plugins.getMemberAccessor().get(ProxyStateContext.class.getDeclaredField("STATES"), null);
    }
}

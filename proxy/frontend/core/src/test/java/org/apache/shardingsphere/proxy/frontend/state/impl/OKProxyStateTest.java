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

package org.apache.shardingsphere.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.BackendExecutorType;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
public final class OKProxyStateTest {
    
    @Test
    public void assertExecuteWithProxyHintEnabled() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(true);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getConnectionId()).thenReturn(1);
        ExecutorService executorService = registerMockExecutorService(1);
        new OKProxyState().execute(mock(ChannelHandlerContext.class), null, mock(DatabaseProtocolFrontendEngine.class), connectionSession);
        verify(executorService).execute(any(CommandExecutorTask.class));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(1);
    }
    
    @Test
    public void assertExecuteWithDistributedTransaction() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(false);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.XA);
        when(connectionSession.getConnectionId()).thenReturn(1);
        ExecutorService executorService = registerMockExecutorService(1);
        new OKProxyState().execute(mock(ChannelHandlerContext.class), null, mock(DatabaseProtocolFrontendEngine.class), connectionSession);
        verify(executorService).execute(any(CommandExecutorTask.class));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(1);
    }
    
    @Test
    public void assertExecuteWithProxyBackendExecutorSuitableForOLTP() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(false);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<BackendExecutorType>getValue(
                ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE)).thenReturn(BackendExecutorType.OLTP);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        EventExecutor eventExecutor = mock(EventExecutor.class);
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.executor()).thenReturn(eventExecutor);
        new OKProxyState().execute(context, null, mock(DatabaseProtocolFrontendEngine.class), mock(ConnectionSession.class, RETURNS_DEEP_STUBS));
        verify(eventExecutor).execute(any(CommandExecutorTask.class));
    }
    
    @Test
    public void assertExecuteWithProxyBackendExecutorSuitableForOLAPAndRequiredSameThreadForConnection() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(false);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<BackendExecutorType>getValue(
                ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE)).thenReturn(BackendExecutorType.OLAP);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getConnectionId()).thenReturn(1);
        DatabaseProtocolFrontendEngine frontendEngine = mock(DatabaseProtocolFrontendEngine.class, RETURNS_DEEP_STUBS);
        when(frontendEngine.getFrontendContext().isRequiredSameThreadForConnection(null)).thenReturn(true);
        ExecutorService executorService = registerMockExecutorService(1);
        new OKProxyState().execute(mock(ChannelHandlerContext.class), null, frontendEngine, connectionSession);
        verify(executorService).execute(any(CommandExecutorTask.class));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(1);
    }
    
    @SuppressWarnings({"unchecked", "SameParameterValue"})
    @SneakyThrows(ReflectiveOperationException.class)
    private ExecutorService registerMockExecutorService(final int connectionId) {
        Map<Integer, ExecutorService> executorServices = (Map<Integer, ExecutorService>) Plugins.getMemberAccessor()
                .get(ConnectionThreadExecutorGroup.class.getDeclaredField("executorServices"), ConnectionThreadExecutorGroup.getInstance());
        ExecutorService result = mock(ExecutorService.class);
        executorServices.put(connectionId, result);
        return result;
    }
}

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
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCOKProxyStateTest extends ProxyContextRestorer {
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseProtocolFrontendEngine frontendEngine;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Before
    public void setup() {
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(connectionSession.getBackendConnection()).thenReturn(mock(JDBCBackendConnection.class));
        ProxyContext.init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    public void assertExecuteWithProxyHintEnabled() {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(true);
        ExecutorService executorService = registerMockExecutorService(1);
        new JDBCOKProxyState().execute(context, null, frontendEngine, connectionSession);
        verify(executorService).execute(any(CommandExecutorTask.class));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(1);
    }
    
    @Test
    public void assertExecuteWithDistributedTransaction() {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(false);
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.XA);
        ExecutorService executorService = registerMockExecutorService(1);
        new JDBCOKProxyState().execute(context, null, frontendEngine, connectionSession);
        verify(executorService).execute(any(CommandExecutorTask.class));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(1);
    }
    
    @Test
    public void assertExecuteWithProxyBackendExecutorSuitableForOLTP() {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(false);
        when(ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaData().getProps().<String>getValue(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE)).thenReturn("OLTP");
        EventExecutor eventExecutor = mock(EventExecutor.class);
        when(context.executor()).thenReturn(eventExecutor);
        new JDBCOKProxyState().execute(context, null, frontendEngine, connectionSession);
        verify(eventExecutor).execute(any(CommandExecutorTask.class));
    }
    
    @Test
    public void assertExecuteWithProxyBackendExecutorSuitableForOLAPAndRequiredSameThreadForConnection() {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(false);
        when(ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaData().getProps().<String>getValue(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE)).thenReturn("OLAP");
        when(frontendEngine.getFrontendContext().isRequiredSameThreadForConnection(null)).thenReturn(true);
        ExecutorService executorService = registerMockExecutorService(1);
        new JDBCOKProxyState().execute(context, null, frontendEngine, connectionSession);
        verify(executorService).execute(any(CommandExecutorTask.class));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertExecuteWithProxyBackendExecutorSuitableForInvalidValue() {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(false);
        when(ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getMetaData().getProps().<String>getValue(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE)).thenReturn("invalid value");
        new JDBCOKProxyState().execute(context, null, frontendEngine, connectionSession);
    }
    
    @SuppressWarnings({"unchecked", "SameParameterValue"})
    @SneakyThrows(ReflectiveOperationException.class)
    private ExecutorService registerMockExecutorService(final int connectionId) {
        Field executorServicesField = ConnectionThreadExecutorGroup.class.getDeclaredField("executorServices");
        executorServicesField.setAccessible(true);
        Map<Integer, ExecutorService> executorServices = (Map<Integer, ExecutorService>) executorServicesField.get(ConnectionThreadExecutorGroup.getInstance());
        ExecutorService result = mock(ExecutorService.class);
        executorServices.put(connectionId, result);
        return result;
    }
}

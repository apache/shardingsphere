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
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class OKProxyStateTest {
    
    private ChannelHandlerContext context;
    
    @BeforeEach
    void setup() {
        context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(new EmbeddedChannel());
    }
    
    @AfterEach
    void tearDown() {
        context.channel().close().syncUninterruptibly();
    }
    
    @Test
    void assertExecuteWithDistributedTransaction() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getTransactionStatus().getTransactionType()).thenReturn(TransactionType.XA);
        when(connectionSession.getConnectionId()).thenReturn(1);
        ExecutorService executorService = registerMockExecutorService(1);
        new OKProxyState().execute(context, null, mock(DatabaseProtocolFrontendEngine.class), connectionSession);
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

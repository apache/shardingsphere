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

package org.apache.shardingsphere.proxy.frontend.netty;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.exception.core.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionLimitContext;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class FrontendChannelLimitationInboundHandlerTest {
    
    @BeforeEach
    void resetActiveConnections() {
        getActiveConnectionsCounter().set(0);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private AtomicInteger getActiveConnectionsCounter() {
        return (AtomicInteger) Plugins.getMemberAccessor().get(ConnectionLimitContext.class.getDeclaredField("activeConnections"), ConnectionLimitContext.getInstance());
    }
    
    @Test
    void assertChannelActiveWhenAllowed() {
        mockMaxConnections(2);
        DatabaseProtocolFrontendEngine frontendEngine = mock(DatabaseProtocolFrontendEngine.class);
        FrontendChannelLimitationInboundHandler handler = new FrontendChannelLimitationInboundHandler(frontendEngine);
        ChannelHandlerContext context = mockChannelHandlerContext();
        handler.channelActive(context);
        verify(context).fireChannelActive();
    }
    
    @Test
    void assertChannelActiveRejected() {
        mockMaxConnections(1);
        DatabaseProtocolFrontendEngine frontendEngine = mock(DatabaseProtocolFrontendEngine.class, RETURNS_DEEP_STUBS);
        DatabasePacket errorPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getErrorPacket(any(TooManyConnectionsException.class))).thenReturn(errorPacket);
        FrontendChannelLimitationInboundHandler handler = new FrontendChannelLimitationInboundHandler(frontendEngine);
        ChannelHandlerContext context = mockChannelHandlerContext();
        ConnectionLimitContext.getInstance().connectionAllowed();
        handler.channelActive(context);
        verify(context).writeAndFlush(errorPacket);
        verify(context).close();
    }
    
    @Test
    void assertChannelInactiveReleasesConnection() {
        mockMaxConnections(1);
        ConnectionLimitContext.getInstance().connectionAllowed();
        ChannelHandlerContext context = mockChannelHandlerContext();
        new FrontendChannelLimitationInboundHandler(mock()).channelInactive(context);
        verify(context).fireChannelInactive();
        assertThat(getActiveConnectionsCounter().get(), is(0));
    }
    
    private void mockMaxConnections(final int maxConnections) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS)).thenReturn(maxConnections);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private ChannelHandlerContext mockChannelHandlerContext() {
        ChannelHandlerContext result = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        ChannelConfig config = result.channel().config();
        when(config.setAutoRead(false)).thenReturn(config);
        return result;
    }
}

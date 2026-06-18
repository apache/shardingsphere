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

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.codec.PacketCodec;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.ConstructionMockSettings;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@ConstructionMockSettings(FrontendChannelInboundHandler.class)
@StaticMockSettings(ProxyContext.class)
class ServerHandlerInitializerTest {
    
    @Test
    void assertInitChannel() {
        SocketChannel channel = mock(SocketChannel.class);
        ChannelPipeline pipeline = mock(ChannelPipeline.class);
        when(channel.pipeline()).thenReturn(pipeline);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_CONNECTION_IDLE_TIMEOUT)).thenReturn(28800L);
        new ServerHandlerInitializer(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")).initChannel(channel);
        verify(pipeline).addLast(any(ChannelAttrInitializer.class));
        verify(pipeline).addLast(any(PacketCodec.class));
        verify(pipeline).addLast(any(FrontendChannelLimitationInboundHandler.class));
        verify(pipeline).addLast(any(IdleStateHandler.class));
        verify(pipeline).addLast(eq(ProxyFlowControlHandler.class.getSimpleName()), any(ProxyFlowControlHandler.class));
        verify(pipeline).addLast(eq(FrontendChannelInboundHandler.class.getSimpleName()), any(FrontendChannelInboundHandler.class));
    }
}

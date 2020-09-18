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

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.auth.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.auth.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.executor.ChannelThreadExecutorGroup;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class FrontendChannelInboundHandlerTest {
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private Channel channel;
    
    @Mock
    private AuthenticationEngine authenticationEngine;
    
    @Mock
    private DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    @Mock
    private BackendConnection backendConnectionMock;
    
    @Mock
    private PacketPayload payload;
    
    @Mock
    private DatabasePacketCodecEngine databasePacketCodecEngine;
    
    @Mock
    private AuthenticationResult authenticationResult;
    
    @Test
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertChannelActive() {
        ChannelId channelId = mock(ChannelId.class);
        when(channel.id()).thenReturn(channelId);
        when(context.channel()).thenReturn(channel);
        when(authenticationEngine.handshake(eq(context))).thenReturn(Integer.MAX_VALUE);
        when(databaseProtocolFrontendEngine.getAuthEngine()).thenReturn(authenticationEngine);
        FrontendChannelInboundHandler frontendChannelInboundHandler = new FrontendChannelInboundHandler(databaseProtocolFrontendEngine);
        Field backendConnection = frontendChannelInboundHandler.getClass().getDeclaredField("backendConnection");
        backendConnection.setAccessible(true);
        backendConnection.set(frontendChannelInboundHandler, backendConnectionMock);
        frontendChannelInboundHandler.channelActive(context);
        assertNotNull(ChannelThreadExecutorGroup.getInstance().get(channelId));
        assertNotNull(backendConnectionMock.getConnectionId());
    }
    
    @Test
    public void assertChannelRead() {
        ChannelId channelId = mock(ChannelId.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        when(channel.id()).thenReturn(channelId);
        when(context.channel()).thenReturn(channel);
        when(authenticationResult.isFinished()).thenReturn(false);
        when(databasePacketCodecEngine.createPacketPayload(byteBuf)).thenReturn(payload);
        when(databaseProtocolFrontendEngine.getCodecEngine()).thenReturn(databasePacketCodecEngine);
        when(authenticationEngine.auth(eq(context), eq(payload))).thenReturn(authenticationResult);
        when(databaseProtocolFrontendEngine.getAuthEngine()).thenReturn(authenticationEngine);
        FrontendChannelInboundHandler frontendChannelInboundHandler = new FrontendChannelInboundHandler(databaseProtocolFrontendEngine);
        frontendChannelInboundHandler.channelRead(context, byteBuf);
        assertFalse(authenticationResult.isFinished());
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertChannelInactive() {
        ChannelId channelId = mock(ChannelId.class);
        when(channel.id()).thenReturn(channelId);
        when(context.channel()).thenReturn(channel);
        ChannelThreadExecutorGroup.getInstance().register(channelId);
        FrontendChannelInboundHandler frontendChannelInboundHandler = new FrontendChannelInboundHandler(databaseProtocolFrontendEngine);
        frontendChannelInboundHandler.channelInactive(context);
        assertNull(ChannelThreadExecutorGroup.getInstance().get(channelId));
    }
    
}

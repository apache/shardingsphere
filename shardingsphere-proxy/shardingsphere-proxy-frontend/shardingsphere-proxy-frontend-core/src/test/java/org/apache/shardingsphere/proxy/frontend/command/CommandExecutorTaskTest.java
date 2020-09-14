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

package org.apache.shardingsphere.proxy.frontend.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.ConnectionStatusManager;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.context.FrontendContext;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CommandExecutorTaskTest {
    
    @Mock
    private DatabaseProtocolFrontendEngine engine;
    
    @Mock
    private DatabasePacketCodecEngine codecEngine;
    
    @Mock
    private PacketPayload payload;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private ChannelHandlerContext handlerContext;
    
    @Mock
    private ConnectionStatusManager statusHandler;
    
    @Mock
    private CommandExecuteEngine executeEngine;
    
    @Mock
    private ByteBuf message;
    
    @Mock
    private CommandPacketType commandPacketType;
    
    @Mock
    private CommandPacket commandPacket;
    
    @Mock
    private QueryCommandExecutor queryCommandExecutor;
    
    @Mock
    private CommandExecutor commandExecutor;
    
    @Mock
    private DatabasePacket databasePacket;
    
    @Mock
    private FrontendContext frontendContext;
    
    @Test
    public void assertRunNeedFlushByFalse() throws SQLException {
        when(backendConnection.getConnectionSize()).thenReturn(1);
        when(queryCommandExecutor.execute()).thenReturn(Collections.emptyList());
        when(executeEngine.getCommandPacket(eq(payload), eq(commandPacketType), eq(backendConnection))).thenReturn(commandPacket);
        when(executeEngine.getCommandExecutor(eq(commandPacketType), eq(commandPacket), eq(backendConnection))).thenReturn(queryCommandExecutor);
        when(executeEngine.getCommandPacketType(eq(payload))).thenReturn(commandPacketType);
        when(engine.getCommandExecuteEngine()).thenReturn(executeEngine);
        when(backendConnection.getConnectionStatusManager()).thenReturn(statusHandler);
        when(codecEngine.createPacketPayload(eq(message))).thenReturn(payload);
        when(engine.getCodecEngine()).thenReturn(codecEngine);
        CommandExecutorTask actual = new CommandExecutorTask(engine, backendConnection, handlerContext, message);
        actual.run();
        verify(statusHandler).waitUntilConnectionRelease();
        verify(statusHandler).switchToUsing();
    }
    
    @Test
    public void assertRunNeedFlushByTrue() throws SQLException {
        when(backendConnection.getConnectionSize()).thenReturn(1);
        when(queryCommandExecutor.execute()).thenReturn(Collections.singletonList(databasePacket));
        when(executeEngine.getCommandPacket(eq(payload), eq(commandPacketType), eq(backendConnection))).thenReturn(commandPacket);
        when(executeEngine.getCommandExecutor(eq(commandPacketType), eq(commandPacket), eq(backendConnection))).thenReturn(queryCommandExecutor);
        when(executeEngine.getCommandPacketType(eq(payload))).thenReturn(commandPacketType);
        when(engine.getCommandExecuteEngine()).thenReturn(executeEngine);
        when(backendConnection.getConnectionStatusManager()).thenReturn(statusHandler);
        when(codecEngine.createPacketPayload(eq(message))).thenReturn(payload);
        when(engine.getCodecEngine()).thenReturn(codecEngine);
        CommandExecutorTask actual = new CommandExecutorTask(engine, backendConnection, handlerContext, message);
        actual.run();
        verify(statusHandler).waitUntilConnectionRelease();
        verify(statusHandler).switchToUsing();
        verify(handlerContext).write(databasePacket);
        verify(handlerContext).flush();
        verify(executeEngine).writeQueryData(handlerContext, backendConnection, queryCommandExecutor, 1);
    }
    
    @Test
    public void assertRunByCommandExecutor() throws SQLException {
        when(frontendContext.isFlushForPerCommandPacket()).thenReturn(true);
        when(engine.getFrontendContext()).thenReturn(frontendContext);
        when(backendConnection.getConnectionSize()).thenReturn(1);
        when(commandExecutor.execute()).thenReturn(Collections.singletonList(databasePacket));
        when(executeEngine.getCommandPacket(eq(payload), eq(commandPacketType), eq(backendConnection))).thenReturn(commandPacket);
        when(executeEngine.getCommandExecutor(eq(commandPacketType), eq(commandPacket), eq(backendConnection))).thenReturn(commandExecutor);
        when(executeEngine.getCommandPacketType(eq(payload))).thenReturn(commandPacketType);
        when(engine.getCommandExecuteEngine()).thenReturn(executeEngine);
        when(backendConnection.getConnectionStatusManager()).thenReturn(statusHandler);
        when(codecEngine.createPacketPayload(eq(message))).thenReturn(payload);
        when(engine.getCodecEngine()).thenReturn(codecEngine);
        CommandExecutorTask actual = new CommandExecutorTask(engine, backendConnection, handlerContext, message);
        actual.run();
        verify(statusHandler).waitUntilConnectionRelease();
        verify(statusHandler).switchToUsing();
        verify(handlerContext).write(databasePacket);
        verify(handlerContext).flush();
    }
    
    @Test
    public void assertRunWithError() {
        RuntimeException mockException = new RuntimeException("mock");
        when(backendConnection.getConnectionStatusManager()).thenThrow(mockException);
        when(codecEngine.createPacketPayload(message)).thenReturn(payload);
        when(engine.getCodecEngine()).thenReturn(codecEngine);
        when(executeEngine.getErrorPacket(eq(mockException))).thenReturn(databasePacket);
        when(executeEngine.getOtherPacket()).thenReturn(Optional.of(databasePacket));
        when(engine.getCommandExecuteEngine()).thenReturn(executeEngine);
        CommandExecutorTask actual = new CommandExecutorTask(engine, backendConnection, handlerContext, message);
        actual.run();
        verify(handlerContext, atLeast(2)).writeAndFlush(databasePacket);
    }
}

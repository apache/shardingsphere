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
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.ConnectionStatus;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.context.FrontendContext;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public final class CommandExecutorTaskTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseProtocolFrontendEngine engine;
    
    @Mock
    private PacketPayload payload;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext handlerContext;
    
    @Mock
    private ConnectionStatus connectionStatus;
    
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
    
    @Before
    public void setup() {
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(handlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
    }
    
    @Test
    public void assertRunNeedFlushByFalse() throws SQLException, BackendConnectionException {
        when(queryCommandExecutor.execute()).thenReturn(Collections.emptyList());
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(queryCommandExecutor);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(queryCommandExecutor).close();
        verify(backendConnection).closeExecutionResources();
    }
    
    @Test
    public void assertRunNeedFlushByTrue() throws SQLException, BackendConnectionException {
        when(queryCommandExecutor.execute()).thenReturn(Collections.singletonList(databasePacket));
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(queryCommandExecutor);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCommandExecuteEngine().writeQueryData(any(ChannelHandlerContext.class), any(JDBCBackendConnection.class), any(QueryCommandExecutor.class), anyInt())).thenReturn(true);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(handlerContext).write(databasePacket);
        verify(handlerContext).flush();
        verify(engine.getCommandExecuteEngine()).writeQueryData(handlerContext, backendConnection, queryCommandExecutor, 1);
        verify(queryCommandExecutor).close();
        verify(backendConnection).closeExecutionResources();
    }
    
    @Test
    public void assertRunByCommandExecutor() throws SQLException, BackendConnectionException {
        when(frontendContext.isFlushForPerCommandPacket()).thenReturn(true);
        when(engine.getFrontendContext()).thenReturn(frontendContext);
        when(commandExecutor.execute()).thenReturn(Collections.singletonList(databasePacket));
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(handlerContext).write(databasePacket);
        verify(handlerContext).flush();
        verify(commandExecutor).close();
        verify(backendConnection).closeExecutionResources();
    }
    
    @Test
    public void assertRunWithError() throws BackendConnectionException {
        RuntimeException mockException = new RuntimeException("mock");
        doThrow(mockException).when(backendConnection).prepareForTaskExecution();
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        when(engine.getCommandExecuteEngine().getErrorPacket(mockException, connectionSession)).thenReturn(databasePacket);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.of(databasePacket));
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(handlerContext, times(2)).write(databasePacket);
        verify(handlerContext).flush();
        verify(backendConnection).closeExecutionResources();
    }
}

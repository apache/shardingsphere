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
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class CommandExecutorTaskTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseProtocolFrontendEngine engine;
    
    @Mock
    private PacketPayload payload;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext handlerContext;
    
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
    
    @BeforeEach
    void setup() {
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(handlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        when(metaDataContexts.getMetaData()).thenReturn(new ShardingSphereMetaData());
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertRunNeedFlushByFalse() throws SQLException, BackendConnectionException {
        when(queryCommandExecutor.execute()).thenReturn(Collections.emptyList());
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(queryCommandExecutor);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(queryCommandExecutor).close();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    @Test
    void assertRunNeedFlushByTrue() throws SQLException, BackendConnectionException {
        when(queryCommandExecutor.execute()).thenReturn(Collections.singleton(databasePacket));
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(queryCommandExecutor);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(handlerContext).flush();
        verify(engine.getCommandExecuteEngine()).writeQueryData(handlerContext, databaseConnectionManager, queryCommandExecutor, 1);
        verify(queryCommandExecutor).close();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    @Test
    void assertRunByCommandExecutor() throws SQLException, BackendConnectionException {
        when(commandExecutor.execute()).thenReturn(Collections.singleton(databasePacket));
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(handlerContext).flush();
        verify(commandExecutor).close();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    @Test
    void assertRunWithException() throws BackendConnectionException, SQLException {
        RuntimeException mockException = new RuntimeException("mock");
        doThrow(mockException).when(commandExecutor).execute();
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        when(engine.getCommandExecuteEngine().getErrorPacket(mockException)).thenReturn(databasePacket);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.of(databasePacket));
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(handlerContext, times(2)).write(databasePacket);
        verify(handlerContext).flush();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    @Test
    void assertRunWithOOMError() throws BackendConnectionException, SQLException {
        doThrow(OutOfMemoryError.class).when(commandExecutor).execute();
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        when(engine.getCommandExecuteEngine().getErrorPacket(any(RuntimeException.class))).thenReturn(databasePacket);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.of(databasePacket));
        CommandExecutorTask actual = new CommandExecutorTask(engine, connectionSession, handlerContext, message);
        actual.run();
        verify(handlerContext, times(2)).write(databasePacket);
        verify(handlerContext).flush();
        verify(databaseConnectionManager).closeExecutionResources();
    }
}

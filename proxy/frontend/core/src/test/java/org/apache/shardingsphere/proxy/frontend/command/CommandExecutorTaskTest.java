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
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
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
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext handlerContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseProtocolFrontendEngine engine;
    
    @Mock
    private PacketPayload payload;
    
    @Mock
    private CommandPacketType commandPacketType;
    
    @Mock
    private CommandPacket commandPacket;
    
    @Mock
    private ByteBuf message;
    
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
        when(engine.getCommandExecuteEngine().getCommandPacket(payload, commandPacketType, connectionSession)).thenReturn(commandPacket);
        when(engine.getCommandExecuteEngine().getCommandPacketType(payload)).thenReturn(commandPacketType);
    }
    
    @Test
    void assertRunNeedFlushByFalse() throws SQLException, BackendConnectionException {
        mockProxyContext(false);
        when(queryCommandExecutor.execute()).thenReturn(Collections.emptyList());
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(queryCommandExecutor);
        CompositeByteBuf compositeMessage = mock(CompositeByteBuf.class);
        when(compositeMessage.readableBytes()).thenReturn(0);
        when(engine.getCodecEngine().createPacketPayload(compositeMessage, StandardCharsets.UTF_8)).thenReturn(payload);
        BackendConnectionException backendConnectionException = new BackendConnectionException(Collections.singleton(new SQLException("foo_close")));
        doThrow(backendConnectionException).when(databaseConnectionManager).closeExecutionResources();
        when(engine.getCommandExecuteEngine().getErrorPacket(any(Exception.class))).thenReturn(databasePacket);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.empty());
        new CommandExecutorTask(engine, connectionSession, handlerContext, compositeMessage).run();
        verify(queryCommandExecutor).close();
        verify(handlerContext).write(databasePacket);
        verify(handlerContext).flush();
        verify(databaseConnectionManager).closeExecutionResources();
        verify(compositeMessage).discardReadComponents();
        verify(compositeMessage).release();
    }
    
    @Test
    void assertRunNeedFlushByTrue() throws SQLException, BackendConnectionException {
        mockProxyContext(false);
        when(queryCommandExecutor.execute()).thenReturn(Collections.singleton(databasePacket));
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(queryCommandExecutor);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        new CommandExecutorTask(engine, connectionSession, handlerContext, message).run();
        verify(handlerContext).flush();
        verify(engine.getCommandExecuteEngine()).writeQueryData(handlerContext, databaseConnectionManager, queryCommandExecutor, 1);
        verify(queryCommandExecutor).close();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    @Test
    void assertRunByCommandExecutor() throws SQLException, BackendConnectionException {
        mockProxyContext(false);
        when(commandExecutor.execute()).thenReturn(Collections.singleton(databasePacket));
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        new CommandExecutorTask(engine, connectionSession, handlerContext, message).run();
        verify(handlerContext).flush();
        verify(commandExecutor).close();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    @Test
    void assertRunWithSQLDialectException() throws SQLException, BackendConnectionException {
        mockProxyContext(true);
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(new Grantee("foo_user", "bar_host"));
        SQLDialectException expectedException = mock(SQLDialectException.class);
        doThrow(expectedException).when(commandExecutor).execute();
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        CompositeByteBuf compositeMessage = mock(CompositeByteBuf.class);
        when(compositeMessage.readableBytes()).thenReturn(2);
        when(engine.getCodecEngine().createPacketPayload(compositeMessage, StandardCharsets.UTF_8)).thenReturn(payload);
        when(engine.getCommandExecuteEngine().getErrorPacket(expectedException)).thenReturn(databasePacket);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.empty());
        new CommandExecutorTask(engine, connectionSession, handlerContext, compositeMessage).run();
        verify(engine).handleException(connectionSession, expectedException);
        verify(handlerContext).write(databasePacket);
        verify(handlerContext).flush();
        verify(commandExecutor).close();
        verify(databaseConnectionManager).closeExecutionResources();
        verify(compositeMessage).skipBytes(2);
        verify(compositeMessage).discardReadComponents();
        verify(compositeMessage).release();
    }
    
    @Test
    void assertRunWithException() throws BackendConnectionException, SQLException {
        mockProxyContext(false);
        RuntimeException mockException = new RuntimeException("foo_mock");
        doThrow(mockException).when(commandExecutor).execute();
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        when(engine.getCommandExecuteEngine().getErrorPacket(mockException)).thenReturn(databasePacket);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.of(databasePacket));
        new CommandExecutorTask(engine, connectionSession, handlerContext, message).run();
        verify(handlerContext, times(2)).write(databasePacket);
        verify(handlerContext).flush();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    @Test
    void assertRunWithOOMError() throws BackendConnectionException, SQLException {
        mockProxyContext(false);
        doThrow(OutOfMemoryError.class).when(commandExecutor).execute();
        when(engine.getCodecEngine().createPacketPayload(message, StandardCharsets.UTF_8)).thenReturn(payload);
        when(engine.getCommandExecuteEngine().getCommandExecutor(commandPacketType, commandPacket, connectionSession)).thenReturn(commandExecutor);
        when(engine.getCommandExecuteEngine().getErrorPacket(any(RuntimeException.class))).thenReturn(databasePacket);
        when(engine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.empty());
        new CommandExecutorTask(engine, connectionSession, handlerContext, message).run();
        verify(handlerContext).write(databasePacket);
        verify(handlerContext).flush();
        verify(databaseConnectionManager).closeExecutionResources();
    }
    
    private void mockProxyContext(final boolean sqlShowEnabled) {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        when(metaDataContexts.getMetaData()).thenReturn(new ShardingSphereMetaData(Collections.emptyList(), mock(),
                mock(), new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.toString(sqlShowEnabled))))));
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
}

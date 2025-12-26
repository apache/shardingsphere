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

package org.apache.shardingsphere.proxy.frontend.opengauss.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.OpenGaussCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.generic.OpenGaussErrorResponsePacket;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.err.OpenGaussErrorPacketFactory;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLPortalContextRegistry;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({OpenGaussCommandPacketFactory.class, OpenGaussCommandExecutorFactory.class, OpenGaussErrorPacketFactory.class, PostgreSQLPortalContextRegistry.class})
class OpenGaussCommandExecuteEngineTest {
    
    private final OpenGaussCommandExecuteEngine engine = new OpenGaussCommandExecuteEngine();
    
    @Mock
    private PostgreSQLCommandExecuteEngine delegated;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(OpenGaussCommandExecuteEngine.class.getDeclaredField("delegated"), engine, delegated);
    }
    
    @Test
    void assertGetCommandPacketType() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(OpenGaussCommandPacketType.BATCH_BIND_COMMAND.getValue());
        PacketPayload payload = mock(PacketPayload.class, RETURNS_DEEP_STUBS);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        assertThat(engine.getCommandPacketType(payload), is(OpenGaussCommandPacketType.BATCH_BIND_COMMAND));
    }
    
    @Test
    void assertGetCommandPacket() {
        PostgreSQLCommandPacket expectedPacket = mock(PostgreSQLCommandPacket.class);
        when(OpenGaussCommandPacketFactory.newInstance(any(), any(PostgreSQLPacketPayload.class))).thenReturn(expectedPacket);
        assertThat(engine.getCommandPacket(mock(PostgreSQLPacketPayload.class), OpenGaussCommandPacketType.BATCH_BIND_COMMAND, mock(ConnectionSession.class)), is(expectedPacket));
    }
    
    @Test
    void assertGetCommandExecutor() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getConnectionId()).thenReturn(1);
        CommandExecutor expectedExecutor = mock(CommandExecutor.class);
        when(PostgreSQLPortalContextRegistry.getInstance()).thenReturn(mock(PostgreSQLPortalContextRegistry.class));
        when(OpenGaussCommandExecutorFactory.newInstance(any(), any(PostgreSQLCommandPacket.class), any(), any())).thenReturn(expectedExecutor);
        assertThat(engine.getCommandExecutor(OpenGaussCommandPacketType.BATCH_BIND_COMMAND, mock(PostgreSQLCommandPacket.class), connectionSession), is(expectedExecutor));
    }
    
    @Test
    void assertGetErrorPacket() {
        OpenGaussErrorResponsePacket expectedPacket = mock(OpenGaussErrorResponsePacket.class);
        when(OpenGaussErrorPacketFactory.newInstance(any(Exception.class))).thenReturn(expectedPacket);
        assertThat(engine.getErrorPacket(new Exception("err")), is(expectedPacket));
    }
    
    @Test
    void assertGetOtherPacket() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        engine.getOtherPacket(connectionSession);
        verify(delegated).getOtherPacket(connectionSession);
    }
    
    @Test
    void assertWriteQueryData() throws SQLException {
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        QueryCommandExecutor queryCommandExecutor = mock(QueryCommandExecutor.class);
        engine.writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 1);
        verify(delegated).writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 1);
    }
}

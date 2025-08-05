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

package org.apache.shardingsphere.proxy.frontend.firebird.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        FirebirdCommandPacketFactory.class,
        FirebirdConnectionProtocolVersion.class,
        FirebirdCommandExecutorFactory.class,
        FirebirdErrorPacketFactory.class,
        ProxyContext.class
})
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdCommandExecuteEngineTest {

    private FirebirdCommandExecuteEngine engine;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;

    @Mock
    private ChannelHandlerContext context;

    @Mock
    private Channel channel;

    @Mock
    private QueryCommandExecutor queryCommandExecutor;

    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;

    @BeforeEach
    void setUp() {
        engine = new FirebirdCommandExecuteEngine();
        when(context.channel()).thenReturn(channel);
    }

    @Test
    void assertGetCommandPacketType() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(FirebirdCommandPacketType.EXECUTE.getValue());
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(engine.getCommandPacketType(payload), is(FirebirdCommandPacketType.EXECUTE));
    }

    @Test
    void assertGetCommandPacket() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class);
        when(connectionSession.getConnectionId()).thenReturn(1);
        FirebirdConnectionProtocolVersion connectionProtocolVersion = mock(FirebirdConnectionProtocolVersion.class);
        when(FirebirdConnectionProtocolVersion.getInstance()).thenReturn(connectionProtocolVersion);
        when(connectionProtocolVersion.getProtocolVersion(1)).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        FirebirdCommandPacket commandPacket = mock(FirebirdCommandPacket.class);
        when(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.EXECUTE, payload, FirebirdProtocolVersion.PROTOCOL_VERSION10)).thenReturn(commandPacket);
        assertThat(engine.getCommandPacket(payload, FirebirdCommandPacketType.EXECUTE, connectionSession), is(commandPacket));
    }

    @Test
    void assertGetCommandExecutor() throws SQLException {
        CommandExecutor executor = mock(CommandExecutor.class);
        CommandPacket packet = mock(CommandPacket.class);
        when(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE, packet, connectionSession)).thenReturn(executor);
        assertThat(engine.getCommandExecutor(FirebirdCommandPacketType.EXECUTE, packet, connectionSession), is(executor));
    }

    @Test
    void assertGetErrorPacket() {
        Exception cause = new Exception("error");
        FirebirdPacket errorPacket = mock(FirebirdPacket.class);
        when(FirebirdErrorPacketFactory.newInstance(cause)).thenReturn(errorPacket);
        assertThat(engine.getErrorPacket(cause), is(errorPacket));
    }

    @Test
    void assertWriteQueryData() throws SQLException {
        ConnectionResourceLock connectionResourceLock = mock(ConnectionResourceLock.class);
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        DatabasePacket rowPacket = mock(DatabasePacket.class);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(rowPacket);
        ContextManager mockContextManager = mock(ContextManager.class);
        MetaDataContexts metaDataContexts =
                new MetaDataContexts(new ShardingSphereMetaData(), new ShardingSphereStatistics());
        when(mockContextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mockContextManager);
        engine.writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock).doAwait(context);
        verify(context).write(rowPacket);
        verify(context).flush();
    }
}

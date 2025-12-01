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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings({
        FirebirdCommandPacketFactory.class,
        FirebirdConnectionProtocolVersion.class,
        FirebirdCommandExecutorFactory.class,
        FirebirdErrorPacketFactory.class,
        ProxyContext.class
})
class FirebirdCommandExecuteEngineTest {
    
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
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private FirebirdConnectionProtocolVersion connectionProtocolVersion;
    
    @Mock
    private CommandExecutor executor;
    
    @Mock
    private CommandPacket packet;
    
    @Mock
    private ConnectionResourceLock connectionResourceLock;
    
    @Mock
    private ContextManager mockContextManager;
    
    @Mock
    private FirebirdPacket errorPacket;
    
    @Mock
    private FirebirdCommandPacket commandPacket;
    
    @Mock
    private DatabasePacket rowPacket;
    
    private FirebirdCommandExecuteEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new FirebirdCommandExecuteEngine();
        when(context.channel()).thenReturn(channel);
    }
    
    @Test
    void assertGetCommandPacketType() {
        when(byteBuf.readerIndex()).thenReturn(0);
        when(byteBuf.getInt(0)).thenReturn(FirebirdCommandPacketType.EXECUTE.getValue());
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(engine.getCommandPacketType(payload), is(FirebirdCommandPacketType.EXECUTE));
    }
    
    @Test
    void assertGetCommandPacket() {
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(FirebirdConnectionProtocolVersion.getInstance()).thenReturn(connectionProtocolVersion);
        when(connectionProtocolVersion.getProtocolVersion(1)).thenReturn(FirebirdProtocolVersion.PROTOCOL_VERSION10);
        when(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.EXECUTE, payload, FirebirdProtocolVersion.PROTOCOL_VERSION10)).thenReturn(commandPacket);
        assertThat(engine.getCommandPacket(payload, FirebirdCommandPacketType.EXECUTE, connectionSession), is(commandPacket));
    }
    
    @Test
    void assertGetCommandExecutor() {
        when(FirebirdCommandExecutorFactory.newInstance(FirebirdCommandPacketType.EXECUTE, packet, connectionSession)).thenReturn(executor);
        assertThat(engine.getCommandExecutor(FirebirdCommandPacketType.EXECUTE, packet, connectionSession), is(executor));
    }
    
    @Test
    void assertGetErrorPacket() {
        Exception cause = new Exception("error");
        when(FirebirdErrorPacketFactory.newInstance(cause)).thenReturn(errorPacket);
        assertThat(engine.getErrorPacket(cause), is(errorPacket));
    }
    
    @Test
    void assertWriteQueryData() throws SQLException {
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(rowPacket);
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

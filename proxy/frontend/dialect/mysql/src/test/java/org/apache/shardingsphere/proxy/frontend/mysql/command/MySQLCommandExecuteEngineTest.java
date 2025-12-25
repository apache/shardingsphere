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

package org.apache.shardingsphere.proxy.frontend.mysql.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.err.MySQLErrorPacketFactory;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({MySQLCommandPacketFactory.class, MySQLCommandExecutorFactory.class, MySQLErrorPacketFactory.class, ProxyContext.class})
class MySQLCommandExecuteEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext context;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private QueryCommandExecutor queryCommandExecutor;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Test
    void assertGetCommandPacketType() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(MySQLCommandPacketType.COM_QUERY.getValue());
        assertThat(new MySQLCommandExecuteEngine().getCommandPacketType(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8)), is(MySQLCommandPacketType.COM_QUERY));
    }
    
    @Test
    void assertGetCommandPacketAndExecutorAndErrorPacket() throws SQLException {
        MySQLPacketPayload payload = mock(MySQLPacketPayload.class);
        MySQLCommandPacket expectedCommandPacket = mock(MySQLCommandPacket.class);
        CommandExecutor expectedCommandExecutor = mock(CommandExecutor.class);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(MySQLCommandPacketFactory.newInstance(MySQLCommandPacketType.COM_QUERY, payload, connectionSession)).thenReturn(expectedCommandPacket);
        when(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUERY, expectedCommandPacket, connectionSession)).thenReturn(expectedCommandExecutor);
        MySQLCommandExecuteEngine commandExecuteEngine = new MySQLCommandExecuteEngine();
        assertThat(commandExecuteEngine.getCommandPacket(payload, MySQLCommandPacketType.COM_QUERY, connectionSession), is(expectedCommandPacket));
        assertThat(commandExecuteEngine.getCommandExecutor(MySQLCommandPacketType.COM_QUERY, expectedCommandPacket, connectionSession), is(expectedCommandExecutor));
        Exception cause = new Exception("error");
        MySQLErrPacket mysqlPacket = mock(MySQLErrPacket.class);
        when(MySQLErrorPacketFactory.newInstance(cause)).thenReturn(mysqlPacket);
        assertThat(commandExecuteEngine.getErrorPacket(cause), is(mysqlPacket));
    }
    
    @Test
    void assertWriteQueryDataFlushesAndAppendsEof() throws SQLException {
        when(context.channel().isActive()).thenReturn(true);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(queryCommandExecutor.next()).thenReturn(true, true, false);
        DatabasePacket rowPacket = mock(DatabasePacket.class);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(rowPacket);
        ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
        when(proxyContext.getContextManager().getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD.getKey(), 2))));
        ConnectionResourceLock connectionResourceLock = mock(ConnectionResourceLock.class);
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        new MySQLCommandExecuteEngine().writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock, times(2)).doAwait(context);
        verify(context, times(2)).write(rowPacket);
        verify(context).flush();
        verify(context).write(isA(MySQLEofPacket.class));
    }
    
    @Test
    void assertWriteQueryDataReturnsWhenResponseIsNotQuery() throws SQLException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        new MySQLCommandExecuteEngine().writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(queryCommandExecutor, never()).next();
        verify(context, never()).write(any(DatabasePacket.class));
        verify(context, never()).flush();
    }
    
    @Test
    void assertWriteQueryDataReturnsWhenChannelInactive() throws SQLException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        new MySQLCommandExecuteEngine().writeQueryData(context, databaseConnectionManager, queryCommandExecutor, 0);
        verify(queryCommandExecutor, never()).next();
        verify(context, never()).write(any());
        verify(context, never()).flush();
    }
}

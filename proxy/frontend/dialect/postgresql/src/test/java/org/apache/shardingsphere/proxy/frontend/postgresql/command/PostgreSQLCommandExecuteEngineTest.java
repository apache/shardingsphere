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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.err.PostgreSQLErrorPacketFactory;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.plugins.MemberAccessor;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        ProxyContext.class,
        PostgreSQLCommandPacketFactory.class,
        PostgreSQLCommandExecutorFactory.class,
        PostgreSQLErrorPacketFactory.class,
        PostgreSQLPortalContextRegistry.class
})
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLCommandExecuteEngineTest {
    
    private final PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private QueryCommandExecutor queryCommandExecutor;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ConnectionResourceLock connectionResourceLock;
    
    private TransactionStatus transactionStatus;
    
    @BeforeEach
    void setUp() {
        when(channelHandlerContext.channel()).thenReturn(channel);
        transactionStatus = new TransactionStatus();
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
    }
    
    @Test
    void assertGetCommandPacketType() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(PostgreSQLCommandPacketType.SIMPLE_QUERY.getValue());
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        assertThat(commandExecuteEngine.getCommandPacketType(payload), is(PostgreSQLCommandPacketType.SIMPLE_QUERY));
    }
    
    @Test
    void assertGetCommandPacket() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        PostgreSQLCommandPacket expectedPacket = mock(PostgreSQLCommandPacket.class);
        when(PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.SIMPLE_QUERY, payload)).thenReturn(expectedPacket);
        assertThat(commandExecuteEngine.getCommandPacket(payload, PostgreSQLCommandPacketType.SIMPLE_QUERY, connectionSession), is(expectedPacket));
    }
    
    @Test
    void assertGetCommandExecutor() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(1);
        PostgreSQLPortalContextRegistry registry = mock(PostgreSQLPortalContextRegistry.class);
        when(PostgreSQLPortalContextRegistry.getInstance()).thenReturn(registry);
        PortalContext portalContext = mock(PortalContext.class);
        when(registry.get(1)).thenReturn(portalContext);
        PostgreSQLCommandPacket commandPacket = mock(PostgreSQLCommandPacket.class);
        CommandExecutor expectedExecutor = mock(CommandExecutor.class);
        when(PostgreSQLCommandExecutorFactory.newInstance(PostgreSQLCommandPacketType.SIMPLE_QUERY, commandPacket, connectionSession, portalContext)).thenReturn(expectedExecutor);
        assertThat(commandExecuteEngine.getCommandExecutor(PostgreSQLCommandPacketType.SIMPLE_QUERY, commandPacket, connectionSession), is(expectedExecutor));
    }
    
    @Test
    void assertGetErrorPacket() {
        Exception cause = new Exception("error");
        PostgreSQLErrorResponsePacket expectedPacket = PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, "state", "message").build();
        when(PostgreSQLErrorPacketFactory.newInstance(cause)).thenReturn(expectedPacket);
        assertThat(commandExecuteEngine.getErrorPacket(cause), is(expectedPacket));
    }
    
    @Test
    void assertGetOtherPacketWhenInTransaction() {
        transactionStatus.setInTransaction(true);
        Optional<DatabasePacket> actual = commandExecuteEngine.getOtherPacket(connectionSession);
        assertThat(actual.isPresent(), is(true));
        assertThat(actual.get(), is(PostgreSQLReadyForQueryPacket.TRANSACTION_FAILED));
    }
    
    @Test
    void assertGetOtherPacketWhenNotInTransaction() {
        Optional<DatabasePacket> actual = commandExecuteEngine.getOtherPacket(connectionSession);
        assertThat(actual.isPresent(), is(true));
        assertThat(actual.get(), is(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION));
    }
    
    @Test
    void assertWriteQueryDataWhenChannelInactiveForQuery() throws SQLException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        verify(queryCommandExecutor, never()).next();
        verify(channelHandlerContext).write(isA(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    void assertWriteQueryDataForUpdateWhenInTransaction() throws SQLException {
        transactionStatus.setInTransaction(true);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        verify(channelHandlerContext).write(PostgreSQLReadyForQueryPacket.IN_TRANSACTION);
    }
    
    @Test
    void assertWriteQueryDataForQueryFlushAndCountDataRows() throws SQLException, NoSuchFieldException, IllegalAccessException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        PostgreSQLDataRowPacket dataRowPacket = mock(PostgreSQLDataRowPacket.class);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(dataRowPacket);
        mockProxyContextFlushThreshold(1);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock).doAwait(channelHandlerContext);
        verify(channelHandlerContext).write(dataRowPacket);
        verify(channelHandlerContext).flush();
        PostgreSQLCommandCompletePacket commandCompletePacket = captureCommandCompletePacket(3);
        assertThat(getCommandCompleteRowCount(commandCompletePacket), is(1L));
        verify(channelHandlerContext).write(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
    }
    
    @Test
    void assertWriteQueryDataForQueryWithoutFlushAndNonDataRow() throws SQLException, NoSuchFieldException, IllegalAccessException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        PostgreSQLPacket rowPacket = mock(PostgreSQLPacket.class);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(rowPacket);
        mockProxyContextFlushThreshold(2);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock).doAwait(channelHandlerContext);
        verify(channelHandlerContext).write(rowPacket);
        verify(channelHandlerContext, never()).flush();
        PostgreSQLCommandCompletePacket commandCompletePacket = captureCommandCompletePacket(3);
        assertThat(getCommandCompleteRowCount(commandCompletePacket), is(0L));
        verify(channelHandlerContext).write(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
    }
    
    @Test
    void assertWriteQueryDataForQueryWithoutRows() throws SQLException, NoSuchFieldException, IllegalAccessException {
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(false);
        mockProxyContextFlushThreshold(2);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock, never()).doAwait(any());
        verify(channelHandlerContext, never()).write(isA(PostgreSQLDataRowPacket.class));
        PostgreSQLCommandCompletePacket commandCompletePacket = captureCommandCompletePacket(2);
        assertThat(getCommandCompleteRowCount(commandCompletePacket), is(0L));
        verify(channelHandlerContext).write(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
    }
    
    @Test
    void assertWriteQueryDataForQueryInTransaction() throws SQLException, NoSuchFieldException, IllegalAccessException {
        transactionStatus.setInTransaction(true);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(false);
        mockProxyContextFlushThreshold(2);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        PostgreSQLCommandCompletePacket commandCompletePacket = captureCommandCompletePacket(2);
        assertThat(getCommandCompleteRowCount(commandCompletePacket), is(0L));
        verify(channelHandlerContext).write(PostgreSQLReadyForQueryPacket.IN_TRANSACTION);
    }
    
    private void mockProxyContextFlushThreshold(final int threshold) {
        ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
        when(proxyContext.getContextManager().getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD.getKey(), threshold))));
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
    }
    
    private PostgreSQLCommandCompletePacket captureCommandCompletePacket(final int expectedWrites) {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(channelHandlerContext, times(expectedWrites)).write(captor.capture());
        return (PostgreSQLCommandCompletePacket) captor.getAllValues().stream().filter(each -> each instanceof PostgreSQLCommandCompletePacket).findFirst()
                .orElseThrow(() -> new AssertionError("CommandComplete packet not written"));
    }
    
    private long getCommandCompleteRowCount(final PostgreSQLCommandCompletePacket packet) throws NoSuchFieldException, IllegalAccessException {
        MemberAccessor accessor = Plugins.getMemberAccessor();
        return (long) accessor.get(PostgreSQLCommandCompletePacket.class.getDeclaredField("rowCount"), packet);
    }
}

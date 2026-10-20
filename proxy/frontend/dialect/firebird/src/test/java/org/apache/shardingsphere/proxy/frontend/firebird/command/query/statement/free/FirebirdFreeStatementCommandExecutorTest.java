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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.free;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.firebird.FirebirdFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementResourceCleaner;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdFreeStatementCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 1;
    
    @Mock
    private FirebirdFreeStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ServerPreparedStatementRegistry registry;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Connection backendConnection;
    
    @BeforeEach
    void setUp() {
        FirebirdStatementIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, new FirebirdBatchStatement(STATEMENT_ID));
        when(packet.getStatementId()).thenReturn(STATEMENT_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, STATEMENT_ID);
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("preparedStatementFreeOptions")
    void assertExecuteWithPreparedStatementFreeOption(final String scenario, final int option) throws Exception {
        when(packet.getOption()).thenReturn(option);
        FirebirdFreeStatementCommandExecutor executor = new FirebirdFreeStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        verify(registry).removePreparedStatement(STATEMENT_ID);
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID));
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertExecuteWithClose() throws Exception {
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.CLOSE);
        new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        verify(connectionSession, never()).invalidatePreparedStatementCache(any());
        verify(registry, never()).removePreparedStatement(STATEMENT_ID);
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
        assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("preparedStatementFreeOptions")
    void assertExecuteWithPreparedStatementFreeOptionWithoutFetchHandler(final String scenario, final int option) throws Exception {
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, STATEMENT_ID);
        when(packet.getOption()).thenReturn(option);
        new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        verify(registry).removePreparedStatement(STATEMENT_ID);
        verify(connectionSession).invalidatePreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(STATEMENT_ID));
    }
    
    @Test
    void assertExecuteWithDropStatementHandle() throws Exception {
        when(packet.getOption()).thenReturn(FirebirdFreeStatementPacket.DROP);
        new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        assertFalse(FirebirdStatementIdGenerator.getInstance().isAllocated(CONNECTION_ID, STATEMENT_ID));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("nonDropOptions")
    void assertExecuteWithNonDropOption(final String scenario, final int option) throws Exception {
        when(packet.getOption()).thenReturn(option);
        new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        assertTrue(FirebirdStatementIdGenerator.getInstance().isAllocated(CONNECTION_ID, STATEMENT_ID));
    }
    
    @Test
    void assertExecuteWithNotAllocatedStatement() throws SQLException {
        when(packet.getStatementId()).thenReturn(STATEMENT_ID + 1);
        Collection<DatabasePacket> actual = new FirebirdFreeStatementCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getErrorCode(), is(ISCConstants.isc_bad_req_handle));
        verify(registry, never()).removePreparedStatement(STATEMENT_ID + 1);
    }
    
    @Test
    void assertExecuteWithNotAllocatedStatementInTransaction() throws SQLException {
        ConnectionSession transactionSession = createConnectionSessionInTransaction();
        int transactionHandle = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdFrontendEngine engine = new FirebirdFrontendEngine();
        new CommandExecutorTask(engine, transactionSession, channelHandlerContext,
                Unpooled.buffer().writeInt(FirebirdCommandPacketType.FREE_STATEMENT.getValue()).writeInt(STATEMENT_ID + 1).writeInt(FirebirdFreeStatementPacket.DROP)).run();
        new CommandExecutorTask(engine, transactionSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionHandle)).run();
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        ArgumentCaptor<FirebirdGenericResponsePacket> responseCaptor = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
        verify(channelHandlerContext, times(2)).write(responseCaptor.capture());
        assertThat(responseCaptor.getAllValues().get(0).getErrorCode(), is(ISCConstants.isc_bad_req_handle));
        verify(backendConnection).commit();
        verify(backendConnection, never()).rollback();
    }
    
    private ConnectionSession createConnectionSessionInTransaction() {
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(
                new ShardingSphereMetaData(Collections.emptyList(), mock(), new RuleMetaData(Collections.singleton(transactionRule)), new ConfigurationProperties(new Properties())));
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        ConnectionSession result = new ConnectionSession(TypedSPILoader.getService(DatabaseType.class, "Firebird"), new DefaultAttributeMap());
        result.setConnectionId(CONNECTION_ID);
        result.setGrantee(new Grantee("foo_user", "%"));
        result.setAutoCommit(false);
        result.getDatabaseConnectionManager().handleAutoCommit();
        result.getDatabaseConnectionManager().getCachedConnections().put("foo_db.foo_ds", backendConnection);
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        return result;
    }
    
    @Test
    void assertExecuteWithUnknownOption() {
        when(packet.getOption()).thenReturn(999);
        assertThrows(DatabaseProtocolException.class, new FirebirdFreeStatementCommandExecutor(packet, connectionSession)::execute);
    }
    
    private static Stream<Arguments> nonDropOptions() {
        return Stream.of(Arguments.of("unprepare", FirebirdFreeStatementPacket.UNPREPARE), Arguments.of("close", FirebirdFreeStatementPacket.CLOSE));
    }
    
    private static Stream<Arguments> preparedStatementFreeOptions() {
        return Stream.of(
                Arguments.of("execute_dropCleansPreparedStatementAndFetchResources", FirebirdFreeStatementPacket.DROP),
                Arguments.of("execute_unprepareCleansPreparedStatementAndFetchResources", FirebirdFreeStatementPacket.UNPREPARE));
    }
}

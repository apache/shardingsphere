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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.firebird.FirebirdFrontendEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdSQLInfoExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 1;
    
    @Mock
    private FirebirdInfoPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Connection backendConnection;
    
    @BeforeEach
    void setUp() {
        FirebirdStatementIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        FirebirdStatementIdGenerator.getInstance().nextStatementId(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() {
        when(packet.getInfoItems()).thenReturn(Collections.emptyList());
        when(packet.getHandle()).thenReturn(STATEMENT_ID);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        registry.addPreparedStatement(STATEMENT_ID, new FirebirdServerPreparedStatement("SELECT 1 FROM RDB$DATABASE", mock(SQLStatementContext.class), new HintValueContext()));
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        FirebirdSQLInfoExecutor executor = new FirebirdSQLInfoExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(responsePacket.getData(), isA(FirebirdSQLInfoReturnPacket.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sqlStatementProvider")
    void assertExecuteWithRecordsInfo(final String name, final SQLStatement sqlStatement, final long expectedInsertCount, final long expectedUpdateCount, final long expectedDeleteCount) {
        when(packet.getInfoItems()).thenReturn(Collections.emptyList());
        when(packet.getHandle()).thenReturn(STATEMENT_ID);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        FirebirdServerPreparedStatement preparedStatement = new FirebirdServerPreparedStatement("DML", sqlStatementContext, new HintValueContext());
        preparedStatement.setAffectedRows(3L);
        registry.addPreparedStatement(STATEMENT_ID, preparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        FirebirdSQLInfoExecutor executor = new FirebirdSQLInfoExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        FirebirdGenericResponsePacket responsePacket = (FirebirdGenericResponsePacket) actual.iterator().next();
        FirebirdSQLInfoReturnPacket returnPacket = (FirebirdSQLInfoReturnPacket) responsePacket.getData();
        assertThat(returnPacket.getRecordsInfo().getInsertCount(), is(expectedInsertCount));
        assertThat(returnPacket.getRecordsInfo().getUpdateCount(), is(expectedUpdateCount));
        assertThat(returnPacket.getRecordsInfo().getDeleteCount(), is(expectedDeleteCount));
    }
    
    @Test
    void assertExecuteWithNotAllocatedStatement() {
        when(packet.getHandle()).thenReturn(STATEMENT_ID + 1);
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getErrorCode(), is(ISCConstants.isc_bad_req_handle));
    }
    
    @Test
    void assertExecuteWithNotPreparedStatement() {
        when(packet.getHandle()).thenReturn(STATEMENT_ID);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        Collection<DatabasePacket> actual = new FirebirdSQLInfoExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getErrorCode(), is(ISCConstants.isc_unprepared_stmt));
    }
    
    @Test
    void assertExecuteWithNotAllocatedStatementInTransaction() throws SQLException {
        ConnectionSession transactionSession = createConnectionSessionInTransaction();
        int transactionHandle = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdFrontendEngine engine = new FirebirdFrontendEngine();
        ByteBuf infoPacket = Unpooled.buffer().writeInt(FirebirdCommandPacketType.INFO_SQL.getValue()).writeInt(STATEMENT_ID + 1).writeInt(0).writeInt(1).writeByte(ISCConstants.isc_info_sql_records)
                .writeZero(3).writeInt(64);
        new CommandExecutorTask(engine, transactionSession, channelHandlerContext, infoPacket).run();
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
    
    private static Stream<Arguments> sqlStatementProvider() {
        return Stream.of(
                Arguments.of("insert", mock(InsertStatement.class), 3L, 0L, 0L),
                Arguments.of("update", mock(UpdateStatement.class), 0L, 3L, 0L),
                Arguments.of("delete", mock(DeleteStatement.class), 0L, 0L, 3L));
    }
}

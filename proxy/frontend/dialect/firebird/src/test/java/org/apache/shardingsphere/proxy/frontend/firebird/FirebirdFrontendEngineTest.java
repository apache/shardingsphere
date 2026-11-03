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

package org.apache.shardingsphere.proxy.frontend.firebird;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxySQLComQueryParser;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.FirebirdAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.FirebirdCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionSessionRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.firebirdsql.gds.ISCConstants;
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
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings({ProxyContext.class, FirebirdConnectionResourceManager.class, ProxySQLComQueryParser.class, ProxyBackendHandlerFactory.class})
class FirebirdFrontendEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final DatabaseProtocolFrontendEngine engine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdConnectionResourceManager connectionResourceManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Connection backendConnection;
    
    @Test
    void assertGetAuthenticationEngine() {
        assertThat(engine.getAuthenticationEngine(), isA(FirebirdAuthenticationEngine.class));
    }
    
    @Test
    void assertGetCommandExecuteEngine() {
        assertThat(engine.getCommandExecuteEngine(), isA(FirebirdCommandExecuteEngine.class));
    }
    
    @Test
    void assertGetCodecEngine() {
        assertThat(engine.getCodecEngine(), isA(FirebirdPacketCodecEngine.class));
    }
    
    @Test
    void assertRelease() {
        int connectionId = 1;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(FirebirdConnectionResourceManager.getInstance()).thenReturn(connectionResourceManager);
        engine.release(connectionSession);
        verify(connectionResourceManager).unregisterConnection(connectionId);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideHandleExceptionParameters")
    void assertHandleException(final String testName, final boolean inTransaction, final boolean exceptionOccur, final Exception exception, final boolean expectedExceptionOccur) {
        ConnectionContext connectionContext = mockConnectionSession(inTransaction, exceptionOccur);
        engine.handleException(connectionSession, exception);
        assertThat(connectionContext.getTransactionContext().isExceptionOccur(), is(expectedExceptionOccur));
    }
    
    @Test
    void assertActiveTransactionSurvivesRepeatedSetTransaction() throws SQLException {
        int connectionId = 3;
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        try {
            ConnectionSession transactionSession = createConnectionSessionInTransaction(connectionId);
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionId);
            FirebirdTransactionSessionRegistry.getInstance().register(transactionSession, transactionId);
            String sql = "SET TRANSACTION";
            byte[] sqlBytes = sql.getBytes(StandardCharsets.US_ASCII);
            when(ProxySQLComQueryParser.parse(sql, databaseType, transactionSession)).thenReturn(new SetTransactionStatement(databaseType));
            DatabaseProtocolFrontendEngine engine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
            ByteBuf request = Unpooled.buffer().writeInt(FirebirdCommandPacketType.EXEC_IMMEDIATE.getValue()).writeInt(transactionId).writeInt(0).writeInt(3)
                    .writeInt(sqlBytes.length).writeBytes(sqlBytes).writeZero((4 - sqlBytes.length % 4) % 4).writeInt(0).writeInt(0);
            new CommandExecutorTask(engine, transactionSession, channelHandlerContext, request).run();
            new CommandExecutorTask(engine, transactionSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionId)).run();
            ArgumentCaptor<FirebirdGenericResponsePacket> responses = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
            verify(channelHandlerContext, times(2)).write(responses.capture());
            FirebirdGenericResponsePacket actualError = responses.getAllValues().get(0);
            assertThat(actualError.getHandle(), is(transactionId));
            assertThat(actualError.getStatusVector().getSegments().size(), is(1));
            assertThat(actualError.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_trans_handle));
            assertTrue(actualError.getStatusVector().getSegments().get(0).getArguments().isEmpty());
            assertNull(responses.getAllValues().get(1).getStatusVector());
            verify(backendConnection).commit();
            verify(backendConnection, never()).rollback();
        } finally {
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionId);
        }
    }
    
    @Test
    void assertActiveTransactionSurvivesDialectParsingError() throws SQLException {
        int connectionId = 3;
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        try {
            ConnectionSession transactionSession = createConnectionSessionInTransaction(connectionId);
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionId);
            FirebirdTransactionSessionRegistry.getInstance().register(transactionSession, transactionId);
            String sql = "SET TRANSACTION INVALID";
            byte[] sqlBytes = sql.getBytes(StandardCharsets.US_ASCII);
            when(ProxySQLComQueryParser.parse(sql, databaseType, transactionSession)).thenThrow(new DialectSQLParsingException("invalid", null, 1));
            DatabaseProtocolFrontendEngine engine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType);
            ByteBuf request = Unpooled.buffer().writeInt(FirebirdCommandPacketType.EXEC_IMMEDIATE.getValue()).writeInt(transactionId).writeInt(0).writeInt(3)
                    .writeInt(sqlBytes.length).writeBytes(sqlBytes).writeZero((4 - sqlBytes.length % 4) % 4).writeInt(0).writeInt(0);
            new CommandExecutorTask(engine, transactionSession, channelHandlerContext, request).run();
            new CommandExecutorTask(engine, transactionSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionId)).run();
            ArgumentCaptor<FirebirdGenericResponsePacket> responses = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
            verify(channelHandlerContext, times(2)).write(responses.capture());
            FirebirdGenericResponsePacket actualError = responses.getAllValues().get(0);
            assertThat(actualError.getHandle(), is(transactionId));
            assertTrue(0 != actualError.getStatusVector().getSegments().get(0).getGdsCode());
            assertNull(responses.getAllValues().get(1).getStatusVector());
            verify(backendConnection).commit();
            verify(backendConnection, never()).rollback();
        } finally {
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionId);
        }
    }
    
    @Test
    void assertExecuteWithBackendErrorInActiveTransaction() throws SQLException {
        int connectionId = 4;
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdStatementIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(connectionId, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        try {
            ConnectionSession actualSession = createConnectionSessionInTransaction(connectionId);
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionId);
            FirebirdTransactionSessionRegistry.getInstance().register(actualSession, transactionId);
            int statementId = FirebirdStatementIdGenerator.getInstance().nextStatementId(connectionId);
            UpdateStatementContext updateContext = mock(UpdateStatementContext.class, Answers.RETURNS_DEEP_STUBS);
            when(updateContext.getSqlStatement()).thenReturn(UpdateStatement.builder().databaseType(databaseType).build());
            actualSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new FirebirdServerPreparedStatement("UPDATE tbl SET col=1", updateContext, new HintValueContext()));
            ProxyBackendHandler backendHandler = mock(ProxyBackendHandler.class);
            when(backendHandler.execute()).thenThrow(new SQLException("violation of PRIMARY or UNIQUE KEY constraint \"INTEG_4\" on table \"T_PLAIN\"",
                    "23000", ISCConstants.isc_unique_key_violation));
            when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), any(QueryContext.class), eq(actualSession), eq(true))).thenReturn(backendHandler);
            ByteBuf execute = Unpooled.buffer().writeInt(FirebirdCommandPacketType.EXECUTE.getValue()).writeInt(statementId).writeInt(transactionId).writeInt(0).writeInt(0).writeInt(0);
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, execute).run();
            assertFalse(actualSession.getConnectionContext().getTransactionContext().isExceptionOccur());
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionId)).run();
            ArgumentCaptor<FirebirdGenericResponsePacket> responses = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
            verify(channelHandlerContext, times(2)).write(responses.capture());
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_unique_key_violation));
            assertNull(responses.getAllValues().get(1).getStatusVector());
            verify(backendConnection).commit();
            verify(backendConnection, never()).rollback();
            verify(channelHandlerContext.channel(), never()).close();
        } finally {
            FirebirdStatementIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(connectionId);
        }
    }
    
    @Test
    void assertOpenBatchErrorKeepsTransactionUsable() throws SQLException {
        int connectionId = 5;
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdStatementIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdBatchRegistry.getInstance().registerConnection(connectionId);
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(connectionId, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        try {
            ConnectionSession actualSession = createConnectionSessionInTransaction(connectionId);
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionId);
            FirebirdTransactionSessionRegistry.getInstance().register(actualSession, transactionId);
            int statementId = FirebirdStatementIdGenerator.getInstance().nextStatementId(connectionId);
            UpdateStatementContext updateContext = mock(UpdateStatementContext.class, Answers.RETURNS_DEEP_STUBS);
            when(updateContext.getSqlStatement()).thenReturn(UpdateStatement.builder().databaseType(databaseType).build());
            actualSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new FirebirdServerPreparedStatement("UPDATE tbl SET col=1", updateContext, new HintValueContext()));
            FirebirdBatchStatement batch = new FirebirdBatchStatement(statementId);
            FirebirdBatchRegistry.getInstance().registerBatchStatement(connectionId, statementId, batch);
            ByteBuf execute = Unpooled.buffer().writeInt(FirebirdCommandPacketType.EXECUTE.getValue()).writeInt(statementId).writeInt(transactionId).writeInt(0).writeInt(0).writeInt(0);
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, execute).run();
            assertFalse(actualSession.getConnectionContext().getTransactionContext().isExceptionOccur());
            assertThat(FirebirdBatchRegistry.getInstance().getBatchStatement(connectionId, statementId), is(batch));
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionId)).run();
            ArgumentCaptor<FirebirdGenericResponsePacket> responses = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
            verify(channelHandlerContext, times(2)).write(responses.capture());
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_sqlerr));
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(0).getTypedArguments().get(0).getNumber(), is(-502));
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_dsql_cursor_open_err));
            assertNull(responses.getAllValues().get(1).getStatusVector());
            verify(backendConnection).commit();
            verify(backendConnection, never()).rollback();
        } finally {
            FirebirdBatchRegistry.getInstance().unregisterConnection(connectionId);
            FirebirdStatementIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(connectionId);
        }
    }
    
    @Test
    void assertInvalidBatchVersionKeepsTransactionUsable() throws SQLException {
        int connectionId = 6;
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdStatementIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdBatchRegistry.getInstance().registerConnection(connectionId);
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(connectionId, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        try {
            ConnectionSession actualSession = createConnectionSessionInTransaction(connectionId);
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionId);
            FirebirdTransactionSessionRegistry.getInstance().register(actualSession, transactionId);
            int statementId = FirebirdStatementIdGenerator.getInstance().nextStatementId(connectionId);
            UpdateStatementContext updateContext = mock(UpdateStatementContext.class, Answers.RETURNS_DEEP_STUBS);
            actualSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new FirebirdServerPreparedStatement("UPDATE tbl SET col=1", updateContext, new HintValueContext()));
            byte[] blr = new byte[]{5, 2, 4, 0, 2, 0, 8, 0, 7, 0, (byte) 255, 76};
            ByteBuf create = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_CREATE.getValue()).writeInt(statementId)
                    .writeInt(blr.length).writeBytes(blr).writeInt(6).writeInt(1).writeByte(2).writeZero(3);
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, create).run();
            assertFalse(actualSession.getConnectionContext().getTransactionContext().isExceptionOccur());
            assertNull(FirebirdBatchRegistry.getInstance().getBatchStatement(connectionId, statementId));
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionId)).run();
            ArgumentCaptor<FirebirdGenericResponsePacket> responses = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
            verify(channelHandlerContext, times(2)).write(responses.capture());
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_batch_param_version));
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(0).getTypedArguments().get(0).getNumber(), is(2));
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(0).getTypedArguments().get(1).getNumber(), is(1));
            assertNull(responses.getAllValues().get(1).getStatusVector());
            verify(backendConnection).commit();
            verify(backendConnection, never()).rollback();
        } finally {
            FirebirdBatchRegistry.getInstance().unregisterConnection(connectionId);
            FirebirdStatementIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(connectionId);
        }
    }
    
    @Test
    void assertInvalidBlobIdKeepsTransactionUsable() throws SQLException {
        int connectionId = 7;
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdBlobIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdBlobWriteCache.getInstance().registerConnection(connectionId);
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(connectionId, FirebirdProtocolVersion.PROTOCOL_VERSION13);
        try {
            ConnectionSession actualSession = createConnectionSessionInTransaction(connectionId);
            int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionId);
            FirebirdTransactionSessionRegistry.getInstance().register(actualSession, transactionId);
            ByteBuf open = Unpooled.buffer().writeInt(FirebirdCommandPacketType.OPEN_BLOB2.getValue()).writeInt(0).writeInt(transactionId).writeLong(99L);
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, open).run();
            assertFalse(actualSession.getConnectionContext().getTransactionContext().isExceptionOccur());
            new CommandExecutorTask(engine, actualSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionId)).run();
            ArgumentCaptor<FirebirdGenericResponsePacket> responses = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
            verify(channelHandlerContext, times(2)).write(responses.capture());
            assertThat(responses.getAllValues().get(0).getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_segstr_id));
            assertNull(responses.getAllValues().get(1).getStatusVector());
            verify(backendConnection).commit();
            verify(backendConnection, never()).rollback();
        } finally {
            FirebirdBlobWriteCache.getInstance().unregisterConnection(connectionId);
            FirebirdBlobIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionId);
            FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(connectionId);
        }
    }
    
    private ConnectionSession createConnectionSessionInTransaction(final int connectionId) throws SQLException {
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(
                new ShardingSphereMetaData(Collections.emptyList(), mock(), new RuleMetaData(Collections.singleton(transactionRule)), new ConfigurationProperties(new Properties())));
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        ConnectionSession result = new ConnectionSession(databaseType, new DefaultAttributeMap());
        result.setConnectionId(connectionId);
        result.setGrantee(new Grantee("foo_user", "%"));
        result.setAutoCommit(false);
        result.getDatabaseConnectionManager().handleAutoCommit();
        result.getDatabaseConnectionManager().getCachedConnections().put("foo_db.foo_ds", backendConnection);
        return result;
    }
    
    private ConnectionContext mockConnectionSession(final boolean inTransaction, final boolean exceptionOccur) {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(inTransaction);
        ConnectionContext result = new ConnectionContext(Collections::emptyList);
        result.getTransactionContext().setExceptionOccur(exceptionOccur);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        if (inTransaction) {
            when(connectionSession.getConnectionContext()).thenReturn(result);
        }
        return result;
    }
    
    private static Stream<Arguments> provideHandleExceptionParameters() {
        return Stream.of(
                Arguments.of("mark when in transaction", true, false, new Exception("error"), true),
                Arguments.of("skip when in transaction exception", true, false, new InTransactionException(), false),
                Arguments.of("skip cancelled operation", true, false, new SQLException("operation was cancelled", "HY008", ISCConstants.isc_cancelled), false),
                Arguments.of("skip when not in transaction", false, false, new Exception("error"), false),
                Arguments.of("keep marked when already occurred", true, true, new Exception("error"), true));
    }
}

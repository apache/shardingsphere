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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdSQLResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementResourceCleaner;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionSessionRegistry;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyBackendHandlerFactory.class, ProxyContext.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class FirebirdExecuteStatementCommandExecutorTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 1;
    
    @Mock
    private FirebirdExecuteStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private ConnectionContext connectionContext;
    
    @Mock
    private QueryHeader queryHeader;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UpdateStatementContext updateContext;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SelectStatementContext selectContext;
    
    private FirebirdExecuteStatementCommandExecutor executor;
    
    private int transactionId;
    
    @BeforeEach
    void setUp() {
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().registerConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, transactionId);
        when(packet.getStatementId()).thenReturn(STATEMENT_ID);
        when(packet.getTransactionId()).thenReturn(transactionId);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(new ShardingSphereMetaData(Collections.emptyList(),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties())));
        when(selectContext.getSqlStatement()).thenReturn(SelectStatement.builder().databaseType(DATABASE_TYPE).build());
        registry.addPreparedStatement(1, new FirebirdServerPreparedStatement("SELECT * FROM tbl", selectContext, new HintValueContext()));
        when(updateContext.getSqlStatement()).thenReturn(UpdateStatement.builder().databaseType(DATABASE_TYPE).build());
        registry.addPreparedStatement(2, new FirebirdServerPreparedStatement("UPDATE tbl SET col=1", updateContext, new HintValueContext()));
    }
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobWriteCache.getInstance().unregisterConnection(CONNECTION_ID);
        FirebirdBlobBinaryProtocolValue.unregisterConnection(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithUnknownStatementId() {
        when(packet.getStatementId()).thenReturn(99);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        assertThrows(InvalidStatementHandleException.class, executor::execute);
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(booleans = {false, true})
    void assertExecuteWithOpenBatch(final boolean execute2) throws SQLException {
        FirebirdBatchRegistry actualRegistry = FirebirdBatchRegistry.getInstance();
        actualRegistry.registerConnection(CONNECTION_ID);
        FirebirdBatchStatement batch = new FirebirdBatchStatement(STATEMENT_ID);
        actualRegistry.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batch);
        try {
            when(packet.isStoredProcedure()).thenReturn(execute2);
            FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteStatementCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(actual.getStatusVector().getSegments().size(), is(2));
            assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_sqlerr));
            assertThat(actual.getStatusVector().getSegments().get(0).getTypedArguments().get(0).getNumber(), is(-502));
            assertThat(actual.getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_dsql_cursor_open_err));
            assertTrue(actual.getStatusVector().getSegments().get(1).getTypedArguments().isEmpty());
            assertThat(actualRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID), is(batch));
            verify(connectionSession, never()).beginPreparedStatementCache(any());
        } finally {
            actualRegistry.unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertExecute2WithNoOutputMessageKeepsOpenBatch() throws SQLException {
        FirebirdBatchRegistry actualRegistry = FirebirdBatchRegistry.getInstance();
        actualRegistry.registerConnection(CONNECTION_ID);
        FirebirdBatchStatement batch = new FirebirdBatchStatement(STATEMENT_ID);
        actualRegistry.registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batch);
        try {
            when(packet.isStoredProcedure()).thenReturn(true);
            when(packet.getOutputMessageNumber()).thenReturn(65535);
            FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteStatementCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertNull(actual.getStatusVector());
            assertThat(actual.getHandle(), is(transactionId));
            assertThat(actualRegistry.getBatchStatement(CONNECTION_ID, STATEMENT_ID), is(batch));
            verify(connectionSession, never()).beginPreparedStatementCache(any());
        } finally {
            actualRegistry.unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertExecuteWithClosedTransactionHandle() {
        when(packet.getStatementId()).thenReturn(2);
        FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, transactionId);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        assertThrows(InvalidTransactionHandleException.class, executor::execute);
        verify(connectionSession, never()).finishPreparedStatementCache();
    }
    
    @Test
    void assertIsQueryResponse() throws SQLException {
        when(packet.isStoredProcedure()).thenReturn(true);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(queryHeader)));
        when(proxyBackendHandler.next()).thenReturn(true);
        QueryResponseRow row = new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1)));
        when(proxyBackendHandler.getRowData()).thenReturn(row);
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> iterator = actual.iterator();
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
        assertThat(iterator.next(), isA(FirebirdSQLResponsePacket.class));
        assertThat(((FirebirdGenericResponsePacket) iterator.next()).getHandle(), is(transactionId));
        assertFalse(iterator.hasNext());
        assertThat(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID), is(proxyBackendHandler));
        verify(connectionSession).beginPreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(1));
        verify(connectionSession).finishPreparedStatementCache();
    }
    
    @Test
    void assertIsUpdateResponse() throws SQLException {
        when(packet.getStatementId()).thenReturn(2);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(UpdateStatement.builder().databaseType(DATABASE_TYPE).build(), Collections.singleton(new UpdateResult(7, 0L))));
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(executor.getResponseType(), is(ResponseType.UPDATE));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(transactionId));
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(2);
        assertThat(preparedStatement.getAffectedRows(), is(7L));
        assertNull(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID));
        verify(connectionSession).beginPreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(2));
        verify(connectionSession).finishPreparedStatementCache();
    }
    
    @Test
    void assertExecuteWithBackendErrorInFirstTransaction() throws SQLException {
        int otherTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        ConnectionSession otherSession = FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, otherTransactionId);
        assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, otherTransactionId), sameInstance(otherSession));
        when(packet.getStatementId()).thenReturn(2);
        when(proxyBackendHandler.execute()).thenThrow(new SQLException("violation of PRIMARY or UNIQUE KEY constraint \"INTEG_4\" on table \"T_PLAIN\"; "
                + "Problematic key value is (\"ID\" = 57201)", "23000", ISCConstants.isc_unique_key_violation));
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteStatementCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getStatusVector().getSegments().size(), is(2));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_unique_key_violation));
        assertThat(actual.getStatusVector().getSegments().get(0).getArguments(), contains("INTEG_4", "T_PLAIN"));
        assertThat(actual.getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_random));
        assertThat(actual.getStatusVector().getSegments().get(1).getArguments(), contains("Problematic key value is (\"ID\" = 57201)"));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, otherTransactionId));
        assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, otherTransactionId), sameInstance(otherSession));
        verify(connectionSession).finishPreparedStatementCache();
    }
    
    @Test
    void assertExecuteCommitWithConcurrentTransaction() throws SQLException {
        int otherTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        ConnectionSession otherSession = FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, otherTransactionId);
        Collection<DatabasePacket> actual = executeTransactionControlStatement(new CommitStatement(DATABASE_TYPE));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(0));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
        assertNull(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, transactionId));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, otherTransactionId));
        assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, otherTransactionId), sameInstance(otherSession));
    }
    
    @Test
    void assertExecuteRollbackClosesTransaction() throws SQLException {
        Collection<DatabasePacket> actual = executeTransactionControlStatement(new RollbackStatement(DATABASE_TYPE));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(0));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
        assertNull(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, transactionId));
    }
    
    @Test
    void assertExecuteRollbackToSavepointKeepsTransaction() throws SQLException {
        Collection<DatabasePacket> actual = executeTransactionControlStatement(new RollbackStatement(DATABASE_TYPE, "savepoint"));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(transactionId));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    private Collection<DatabasePacket> executeTransactionControlStatement(final SQLStatement sqlStatement) throws SQLException {
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(3,
                new FirebirdServerPreparedStatement("transaction control", new CommonSQLStatementContext(sqlStatement), new HintValueContext()));
        when(packet.getStatementId()).thenReturn(3);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(sqlStatement));
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        return new FirebirdExecuteStatementCommandExecutor(packet, connectionSession).execute();
    }
    
    @Test
    void assertRejectUnclosedBlobParameter() throws SQLException {
        int blobHandle = 7;
        long blobId = 11L;
        FirebirdBlobWriteCache.getInstance().registerBlob(CONNECTION_ID, transactionId, blobHandle, blobId, false);
        FirebirdBlobWriteCache.getInstance().appendSegment(CONNECTION_ID, blobHandle, new byte[]{1, 2});
        mockParameter(FirebirdBinaryColumnType.BLOB, blobId);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        assertInvalidBlobId(executor.execute());
    }
    
    private void mockParameter(final FirebirdBinaryColumnType parameterType, final Object parameterValue) {
        when(packet.getStatementId()).thenReturn(2);
        when(packet.getParameterTypes()).thenReturn(Collections.singletonList(parameterType));
        when(packet.getParameterValues()).thenReturn(new ArrayList<>(Collections.singletonList(parameterValue)));
    }
    
    @Test
    void assertRejectUnknownBlobParameter() throws SQLException {
        long blobId = 13L;
        mockParameter(FirebirdBinaryColumnType.BLOB, blobId);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        assertInvalidBlobId(executor.execute());
    }
    
    @Test
    void assertSkipNonBlobParameter() throws SQLException {
        mockParameter(FirebirdBinaryColumnType.LONG, 9);
        List<Object> actualParams = executeAndGetParameters();
        assertThat(actualParams.size(), is(1));
        assertThat(actualParams.get(0), is(9));
    }
    
    private List<Object> executeAndGetParameters() throws SQLException {
        ArgumentCaptor<QueryContext> queryContextCaptor = ArgumentCaptor.forClass(QueryContext.class);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(UpdateStatement.builder().databaseType(DATABASE_TYPE).build()));
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), queryContextCaptor.capture(), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        new FirebirdExecuteStatementCommandExecutor(packet, connectionSession).execute();
        return queryContextCaptor.getValue().getParameters();
    }
    
    @Test
    void assertSkipBlobParameterWhenNotLong() throws SQLException {
        mockParameter(FirebirdBinaryColumnType.BLOB, "text");
        List<Object> actualParams = executeAndGetParameters();
        assertThat(actualParams.size(), is(1));
        assertNull(actualParams.get(0));
    }
    
    @Test
    void assertBindEmptyBlobParameterWhenZeroBlobId() throws SQLException {
        mockParameter(FirebirdBinaryColumnType.BLOB, 0L);
        List<Object> actualParams = executeAndGetParameters();
        assertThat(actualParams.size(), is(1));
        assertThat(actualParams.get(0), is(new byte[0]));
    }
    
    @Test
    void assertRejectUnknownResultBlobParameter() throws SQLException {
        mockParameter(FirebirdBinaryColumnType.BLOB, -1L);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        assertInvalidBlobId(executor.execute());
    }
    
    private void assertInvalidBlobId(final Collection<DatabasePacket> packets) {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) packets.iterator().next();
        assertThat(actual.getStatusVector().getSegments().size(), is(1));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_segstr_id));
        assertTrue(actual.getStatusVector().getSegments().get(0).getArguments().isEmpty());
    }
    
    @Test
    void assertBindResultBlobParameter() throws SQLException {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        payload.setConnectionId(CONNECTION_ID);
        new FirebirdBlobBinaryProtocolValue().write(payload, new byte[]{5, 6});
        long resultBlobId = byteBuf.getLong(0);
        mockParameter(FirebirdBinaryColumnType.BLOB, resultBlobId);
        assertThat((byte[]) executeAndGetParameters().get(0), is(new byte[]{5, 6}));
    }
    
    @Test
    void assertBindBlobParameterAndClearUpload() throws SQLException {
        int blobHandle = 13;
        long blobId = 17L;
        byte[] expectedBytes = new byte[]{3, 4};
        FirebirdBlobWriteCache.getInstance().registerBlob(CONNECTION_ID, transactionId, blobHandle, blobId, false);
        FirebirdBlobWriteCache.getInstance().appendSegment(CONNECTION_ID, blobHandle, expectedBytes);
        FirebirdBlobWriteCache.getInstance().closeWrite(CONNECTION_ID, blobHandle);
        mockParameter(FirebirdBinaryColumnType.BLOB, blobId);
        List<Object> actualParams = executeAndGetParameters();
        assertThat(actualParams.size(), is(1));
        assertThat(actualParams.get(0), is(expectedBytes));
        assertFalse(FirebirdBlobWriteCache.getInstance().getBlobData(CONNECTION_ID, transactionId, blobId).isPresent());
    }
    
    @Test
    void assertStoredProcedureWithoutResult() throws SQLException {
        when(packet.isStoredProcedure()).thenReturn(true);
        executor = new FirebirdExecuteStatementCommandExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(queryHeader)));
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> iterator = actual.iterator();
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
        assertThat(iterator.next(), isA(FirebirdGenericResponsePacket.class));
        assertFalse(iterator.hasNext());
        assertThat(FirebirdFetchStatementCache.getInstance().getFetchBackendHandler(CONNECTION_ID, STATEMENT_ID), is(proxyBackendHandler));
    }
}

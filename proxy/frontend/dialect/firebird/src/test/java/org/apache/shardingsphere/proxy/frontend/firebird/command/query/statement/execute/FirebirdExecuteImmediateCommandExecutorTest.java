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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteImmediatePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({AutoMockExtension.class, MockitoExtension.class})
@StaticMockSettings({ProxyContext.class, ProxyBackendHandlerFactory.class})
class FirebirdExecuteImmediateCommandExecutorTest {
    
    private static final int CONNECTION_ID = 401;
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final FirebirdTransactionIdGenerator transactionIds = FirebirdTransactionIdGenerator.getInstance();
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdExecuteImmediatePacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLParserRule sqlParserRule;
    
    @Mock
    private ProxyBackendHandler backendHandler;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        transactionIds.registerConnection(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() {
        transactionIds.unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWithActiveTransaction() throws SQLException {
        int transactionId = mockActiveTransaction();
        InsertStatement sqlStatement = InsertStatement.builder().databaseType(DATABASE_TYPE).returning(mock(ReturningSegment.class)).build();
        mockBackendExecution("INSERT INTO foo_tbl VALUES (1) RETURNING id", sqlStatement);
        assertThat(executeAndGetResponseHandle(), is(transactionId));
        assertTrue(transactionIds.isTransactionActive(CONNECTION_ID, transactionId));
        verify(backendHandler).execute();
    }
    
    @Test
    void assertExecuteWithDefaultTransaction() throws SQLException {
        mockParsedStatement("SET TRANSACTION", new SetTransactionStatement(DATABASE_TYPE));
        int actualHandle = executeAndGetResponseHandle();
        assertTrue(transactionIds.isTransactionActive(CONNECTION_ID, actualHandle));
        verify(connectionSession).setAutoCommit(false);
        verify(connectionSession).setReadOnly(false);
        verify(connectionSession).setIsolationLevel(TransactionIsolationLevel.REPEATABLE_READ);
    }
    
    @Test
    void assertExecuteWithTransactionIsolationLevel() throws SQLException {
        mockParsedStatement("SET TRANSACTION ISOLATION LEVEL READ COMMITTED", new SetTransactionStatement(DATABASE_TYPE, null, TransactionIsolationLevel.READ_COMMITTED, null));
        int actualHandle = executeAndGetResponseHandle();
        assertTrue(transactionIds.isTransactionActive(CONNECTION_ID, actualHandle));
        verify(connectionSession).setIsolationLevel(TransactionIsolationLevel.READ_COMMITTED);
    }
    
    @Test
    void assertExecuteWhenTransactionAlreadyActiveExpectsNextHandle() throws SQLException {
        int firstTransactionId = transactionIds.nextTransactionId(CONNECTION_ID);
        mockParsedStatement("SET TRANSACTION", new SetTransactionStatement(DATABASE_TYPE));
        int actualHandle = executeAndGetResponseHandle();
        assertThat(actualHandle, is(firstTransactionId + 1));
        assertTrue(transactionIds.isTransactionActive(CONNECTION_ID, firstTransactionId));
        assertTrue(transactionIds.isTransactionActive(CONNECTION_ID, actualHandle));
    }
    
    @Test
    void assertExecuteWithTransactionSuppliedForStart() {
        int transactionId = mockActiveTransaction();
        mockParsedStatement("SET TRANSACTION", new SetTransactionStatement(DATABASE_TYPE));
        assertThrows(InvalidTransactionHandleException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
        assertTrue(transactionIds.isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertExecuteWithMissingTransaction() {
        mockParsedStatement("INSERT INTO foo_tbl VALUES (1)", mock(InsertStatement.class));
        assertThrows(InvalidTransactionHandleException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithUnknownTransaction() {
        when(packet.getTransactionId()).thenReturn(42);
        assertThrows(InvalidTransactionHandleException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithCommit() throws SQLException {
        int transactionId = mockActiveTransaction();
        mockBackendExecution("COMMIT", new CommitStatement(DATABASE_TYPE));
        assertThat(executeAndGetResponseHandle(), is(0));
        assertFalse(transactionIds.isTransactionActive(CONNECTION_ID, transactionId));
        verify(backendHandler).execute();
    }
    
    @Test
    void assertExecuteWithRollback() throws SQLException {
        int transactionId = mockActiveTransaction();
        mockBackendExecution("ROLLBACK", new RollbackStatement(DATABASE_TYPE));
        assertThat(executeAndGetResponseHandle(), is(0));
        assertFalse(transactionIds.isTransactionActive(CONNECTION_ID, transactionId));
        verify(backendHandler).execute();
    }
    
    @Test
    void assertExecuteWithCommitError() throws SQLException {
        int transactionId = mockActiveTransaction();
        mockBackendExecution("COMMIT", new CommitStatement(DATABASE_TYPE));
        SQLException expected = new SQLException("foo_error");
        when(backendHandler.execute()).thenThrow(expected);
        assertThat(assertThrows(SQLException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute()), sameInstance(expected));
        assertTrue(transactionIds.isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertExecuteWithInputParameter() {
        mockActiveTransaction();
        InsertStatement sqlStatement = mock(InsertStatement.class);
        when(sqlStatement.getParameterCount()).thenReturn(1);
        mockParsedStatement("INSERT INTO foo_tbl VALUES (?)", sqlStatement);
        SQLException actual = assertThrows(SQLException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_dsql_no_input_sqlda));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("statementsWithCursor")
    void assertExecuteWithCursor(final String sql, final SQLStatement sqlStatement) {
        mockActiveTransaction();
        mockParsedStatement(sql, sqlStatement);
        SQLException actual = assertThrows(SQLException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
        assertThat(actual.getErrorCode(), is(ISCConstants.isc_dsql_no_output_sqlda));
    }
    
    private static Stream<Arguments> statementsWithCursor() {
        ReturningSegment returning = mock(ReturningSegment.class);
        InsertStatement insert = mock(InsertStatement.class);
        when(insert.getInsertSelect()).thenReturn(Optional.of(mock(SubquerySegment.class)));
        when(insert.getReturning()).thenReturn(Optional.of(returning));
        UpdateStatement update = mock(UpdateStatement.class);
        when(update.getReturning()).thenReturn(Optional.of(returning));
        DeleteStatement delete = mock(DeleteStatement.class);
        when(delete.getReturning()).thenReturn(Optional.of(returning));
        return Stream.of(
                Arguments.of("SELECT id FROM foo_tbl", mock(SelectStatement.class)),
                Arguments.of("INSERT INTO foo_tbl SELECT id FROM bar_tbl RETURNING id", insert),
                Arguments.of("UPDATE foo_tbl SET id = 1 RETURNING id", update),
                Arguments.of("DELETE FROM foo_tbl RETURNING id", delete));
    }
    
    private int mockActiveTransaction() {
        int result = transactionIds.nextTransactionId(CONNECTION_ID);
        when(packet.getTransactionId()).thenReturn(result);
        return result;
    }
    
    private void mockBackendExecution(final String sql, final SQLStatement sqlStatement) throws SQLException {
        mockParsedStatement(sql, sqlStatement);
        HintValueContext hintValueContext = new HintValueContext();
        when(packet.getHintValueContext()).thenReturn(hintValueContext);
        when(ProxyBackendHandlerFactory.newInstance(DATABASE_TYPE, sql, sqlStatement, connectionSession, hintValueContext)).thenReturn(backendHandler);
    }
    
    private void mockParsedStatement(final String sql, final SQLStatement sqlStatement) {
        when(connectionSession.getProtocolType()).thenReturn(DATABASE_TYPE);
        when(packet.getSQL()).thenReturn(sql);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(sqlParserRule)));
        when(sqlParserRule.getSQLParserEngine(DATABASE_TYPE).parse(sql, true)).thenReturn(sqlStatement);
    }
    
    private int executeAndGetResponseHandle() throws SQLException {
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        return ((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle();
    }
}

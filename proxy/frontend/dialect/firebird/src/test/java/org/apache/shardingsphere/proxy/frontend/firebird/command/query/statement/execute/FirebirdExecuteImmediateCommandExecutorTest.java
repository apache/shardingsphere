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
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteImmediatePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.ConstructionMockSettings;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxyBackendHandlerFactory.class})
@ConstructionMockSettings(ProxyBackendTransactionManager.class)
class FirebirdExecuteImmediateCommandExecutorTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private static final int CONNECTION_ID = 1;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLParserRule sqlParserRule;
    
    @Mock
    private TransactionRule transactionRule;
    
    @Mock
    private ProxyBackendHandler backendHandler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLStatementContext statementContext;
    
    @Mock
    private FirebirdExecuteImmediatePacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        RuleMetaData ruleMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        doReturn(sqlParserRule).when(ruleMetaData).getSingleRule(SQLParserRule.class);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteStartsTransaction() throws SQLException {
        RuleMetaData ruleMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        doReturn(transactionRule).when(ruleMetaData).getSingleRule(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        when(connectionSession.getDatabaseConnectionManager().getCachedConnections().keySet()).thenReturn(Collections.emptySet());
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getProtocolType()).thenReturn(DATABASE_TYPE);
        when(connectionSession.getDatabaseConnectionManager().getCloseLock()).thenReturn(new Object());
        when(packet.getSQL()).thenReturn("SET TRANSACTION");
        when(sqlParserRule.getSQLParserEngine(DATABASE_TYPE).parse("SET TRANSACTION", true)).thenReturn(new SetTransactionStatement(DATABASE_TYPE));
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        int actualHandle = ((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle();
        assertThat(actualHandle, is(1));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, actualHandle));
        ConnectionSession actualSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, actualHandle);
        assertFalse(actualSession.isAutoCommit());
        assertThat(actualSession.getIsolationLevel().get(), is(TransactionIsolationLevel.REPEATABLE_READ));
    }
    
    @Test
    void assertExecuteWithExistingTransactionHandle() {
        when(packet.getSQL()).thenReturn("SET TRANSACTION");
        when(sqlParserRule.getSQLParserEngine(DATABASE_TYPE).parse("SET TRANSACTION", true)).thenReturn(new SetTransactionStatement(DATABASE_TYPE));
        when(packet.getTransactionId()).thenReturn(1);
        assertThrows(DatabaseProtocolException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithUnknownTransactionHandle() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getSQL()).thenReturn("COMMIT");
        when(packet.getTransactionId()).thenReturn(99);
        when(sqlParserRule.getSQLParserEngine(DATABASE_TYPE).parse("COMMIT", true)).thenReturn(new CommitStatement(DATABASE_TYPE));
        assertThrows(InvalidTransactionHandleException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWithUpdate() throws SQLException, BackendConnectionException {
        SQLStatement statement = mock(SQLStatement.class);
        prepareExecution(statement);
        when(backendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        try (MockedConstruction<SQLBindEngine> ignored = mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(statement)).thenReturn(statementContext))) {
            FirebirdExecuteImmediateCommandExecutor executor = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession);
            Collection<DatabasePacket> actual = executor.execute();
            assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(1));
            assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, 1));
            executor.close();
            verify(connectionSession).clearQueryContext();
            verify(connectionSession.getDatabaseConnectionManager()).closeExecutionResources();
        }
    }
    
    private void prepareExecution(final SQLStatement statement) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(Collections.emptyList());
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        when(packet.getTransactionId()).thenReturn(transactionId);
        when(packet.getSQL()).thenReturn("foo_sql");
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(sqlParserRule.getSQLParserEngine(DATABASE_TYPE).parse("foo_sql", true)).thenReturn(statement);
        when(statementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(connectionSession.getConnectionContext().getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(backendHandler);
    }
    
    @Test
    void assertExecuteWithCommit() throws SQLException {
        SQLStatement statement = new CommitStatement(DATABASE_TYPE);
        prepareExecution(statement);
        when(backendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        try (MockedConstruction<SQLBindEngine> ignored = mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(statement)).thenReturn(statementContext))) {
            Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
            assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(0));
            assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, 1));
        }
    }
    
    @Test
    void assertExecuteWithResultSet() throws SQLException, BackendConnectionException {
        SQLStatement statement = mock(SQLStatement.class);
        prepareExecution(statement);
        when(backendHandler.execute()).thenReturn(mock(QueryResponseHeader.class));
        try (MockedConstruction<SQLBindEngine> ignored = mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(statement)).thenReturn(statementContext))) {
            FirebirdExecuteImmediateCommandExecutor executor = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession);
            assertThrows(UnsupportedSQLOperationException.class, executor::execute);
            executor.close();
            verify(connectionSession.getDatabaseConnectionManager()).closeExecutionResources();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("resultSetStatementProvider")
    void assertExecuteRejectsResultSetBeforeExecution(final String name, final SQLStatement statement) {
        when(packet.getSQL()).thenReturn("foo_sql");
        when(sqlParserRule.getSQLParserEngine(DATABASE_TYPE).parse("foo_sql", true)).thenReturn(statement);
        assertThrows(UnsupportedSQLOperationException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
    }
    
    private static Stream<Arguments> resultSetStatementProvider() {
        ReturningSegment returning = mock(ReturningSegment.class);
        InsertStatement insert = mock(InsertStatement.class);
        when(insert.getReturning()).thenReturn(Optional.of(returning));
        UpdateStatement update = mock(UpdateStatement.class);
        when(update.getReturning()).thenReturn(Optional.of(returning));
        DeleteStatement delete = mock(DeleteStatement.class);
        when(delete.getReturning()).thenReturn(Optional.of(returning));
        return Stream.of(
                Arguments.of("select", SelectStatement.builder().databaseType(DATABASE_TYPE).build()),
                Arguments.of("insert returning", insert),
                Arguments.of("update returning", update),
                Arguments.of("delete returning", delete));
    }
}

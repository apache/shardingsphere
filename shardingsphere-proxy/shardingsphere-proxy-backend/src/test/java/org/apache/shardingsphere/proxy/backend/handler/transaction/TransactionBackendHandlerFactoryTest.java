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

package org.apache.shardingsphere.proxy.backend.handler.transaction;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.JDBCBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class TransactionBackendHandlerFactoryTest extends ProxyContextRestorer {
    
    @Before
    public void setTransactionContexts() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(mock(TransactionRule.class));
        ProxyContext.init(contextManager);
    }
    
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfCommitStatement() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, Answers.RETURNS_DEEP_STUBS);
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        SQLStatementContext<CommitStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(CommitStatement.class));
        ProxyBackendHandler proxyBackendHandler = TransactionBackendHandlerFactory.newInstance(context, null, connectionSession);
        assertThat(proxyBackendHandler, instanceOf(TransactionBackendHandler.class));
        TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) proxyBackendHandler;
        assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.COMMIT));
        assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
    }
    
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfRollbackStatement() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, Answers.RETURNS_DEEP_STUBS);
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        SQLStatementContext<RollbackStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(RollbackStatement.class));
        ProxyBackendHandler proxyBackendHandler = TransactionBackendHandlerFactory.newInstance(context, null, connectionSession);
        assertThat(proxyBackendHandler, instanceOf(TransactionBackendHandler.class));
        TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) proxyBackendHandler;
        assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.ROLLBACK));
        assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
    }
    
    @Test
    public void assertBroadcastBackendHandlerReturnedWhenTCLStatementNotHit() {
        SQLStatementContext<TCLStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(TCLStatement.class));
        try (MockedStatic<DatabaseCommunicationEngineFactory> mockedStatic = mockStatic(DatabaseCommunicationEngineFactory.class)) {
            DatabaseCommunicationEngineFactory mockFactory = mock(DatabaseCommunicationEngineFactory.class);
            mockedStatic.when(DatabaseCommunicationEngineFactory::getInstance).thenReturn(mockFactory);
            when(mockFactory.newDatabaseCommunicationEngine(any(QueryContext.class), nullable(BackendConnection.class), anyBoolean())).thenReturn(mock(DatabaseCommunicationEngine.class));
            assertThat(TransactionBackendHandlerFactory.newInstance(context, null, mock(ConnectionSession.class)), instanceOf(DatabaseCommunicationEngine.class));
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <S, T> void assertFieldOfInstance(final S classInstance, final String fieldName, final Matcher<T> matcher) {
        Field field = classInstance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        T value = (T) field.get(classInstance);
        assertThat(value, matcher);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private JDBCBackendTransactionManager getBackendTransactionManager(final TransactionBackendHandler transactionBackendHandler) {
        Field field = transactionBackendHandler.getClass().getDeclaredField("backendTransactionManager");
        field.setAccessible(true);
        return (JDBCBackendTransactionManager) field.get(transactionBackendHandler);
    }
}

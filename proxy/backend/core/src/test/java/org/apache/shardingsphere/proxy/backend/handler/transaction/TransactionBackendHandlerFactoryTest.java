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
import org.apache.shardingsphere.proxy.backend.connector.BackendConnection;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class TransactionBackendHandlerFactoryTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfCommitStatement() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, Answers.RETURNS_DEEP_STUBS);
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        SQLStatementContext<CommitStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(CommitStatement.class));
        ContextManager contextManager = mockContextManager();
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            ProxyBackendHandler proxyBackendHandler = TransactionBackendHandlerFactory.newInstance(context, null, connectionSession);
            assertThat(proxyBackendHandler, instanceOf(TransactionBackendHandler.class));
            TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) proxyBackendHandler;
            assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.COMMIT));
            assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
        }
    }
    
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfRollbackStatement() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, Answers.RETURNS_DEEP_STUBS);
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        SQLStatementContext<RollbackStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(RollbackStatement.class));
        ContextManager contextManager = mockContextManager();
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            ProxyBackendHandler proxyBackendHandler = TransactionBackendHandlerFactory.newInstance(context, null, connectionSession);
            assertThat(proxyBackendHandler, instanceOf(TransactionBackendHandler.class));
            TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) proxyBackendHandler;
            assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.ROLLBACK));
            assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
        }
    }
    
    private static ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(mock(TransactionRule.class))));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertBroadcastBackendHandlerReturnedWhenTCLStatementNotHit() {
        SQLStatementContext<TCLStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(TCLStatement.class));
        try (MockedStatic<DatabaseConnectorFactory> mockedStatic = mockStatic(DatabaseConnectorFactory.class)) {
            DatabaseConnectorFactory mockFactory = mock(DatabaseConnectorFactory.class);
            mockedStatic.when(DatabaseConnectorFactory::getInstance).thenReturn(mockFactory);
            when(mockFactory.newInstance(any(QueryContext.class), nullable(BackendConnection.class), anyBoolean())).thenReturn(mock(DatabaseConnector.class));
            assertThat(TransactionBackendHandlerFactory.newInstance(context, null, mock(ConnectionSession.class)), instanceOf(DatabaseConnector.class));
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <S, T> void assertFieldOfInstance(final S classInstance, final String fieldName, final Matcher<T> matcher) {
        T value = (T) Plugins.getMemberAccessor().get(classInstance.getClass().getDeclaredField(fieldName), classInstance);
        assertThat(value, matcher);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private BackendTransactionManager getBackendTransactionManager(final TransactionBackendHandler transactionBackendHandler) {
        return (BackendTransactionManager) Plugins.getMemberAccessor().get(transactionBackendHandler.getClass().getDeclaredField("backendTransactionManager"), transactionBackendHandler);
    }
}

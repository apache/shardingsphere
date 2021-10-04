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

package org.apache.shardingsphere.proxy.backend.text.transaction;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.BroadcastDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TransactionBackendHandlerFactoryTest {

    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setTransactionContexts() {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        TransactionContexts transactionContexts = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        when(contextManager.getTransactionContexts()).thenReturn(transactionContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfCommitStatement() {
        BackendConnection backendConnection = mock(BackendConnection.class, Answers.RETURNS_DEEP_STUBS);
        SQLStatementContext<CommitStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(CommitStatement.class));
        TextProtocolBackendHandler textProtocolBackendHandler = TransactionBackendHandlerFactory.newInstance(context, null, backendConnection);
        assertThat(textProtocolBackendHandler, instanceOf(TransactionBackendHandler.class));
        TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) textProtocolBackendHandler;
        assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.COMMIT));
        assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
    }
    
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfRollbackStatement() {
        BackendConnection backendConnection = mock(BackendConnection.class, Answers.RETURNS_DEEP_STUBS);
        SQLStatementContext<RollbackStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(RollbackStatement.class));
        TextProtocolBackendHandler textProtocolBackendHandler = TransactionBackendHandlerFactory.newInstance(context, null, backendConnection);
        assertThat(textProtocolBackendHandler, instanceOf(TransactionBackendHandler.class));
        TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) textProtocolBackendHandler;
        assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.ROLLBACK));
        assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
    }
    
    @Test
    public void assertBroadcastBackendHandlerReturnedWhenTCLStatementNotHit() {
        SQLStatementContext<TCLStatement> context = mock(SQLStatementContext.class);
        when(context.getSqlStatement()).thenReturn(mock(TCLStatement.class));
        assertThat(TransactionBackendHandlerFactory.newInstance(context, null, null), instanceOf(BroadcastDatabaseBackendHandler.class));
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
    private BackendTransactionManager getBackendTransactionManager(final TransactionBackendHandler transactionBackendHandler) {
        Field field = transactionBackendHandler.getClass().getDeclaredField("backendTransactionManager");
        field.setAccessible(true);
        return (BackendTransactionManager) field.get(transactionBackendHandler);
    }
}

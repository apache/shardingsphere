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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.metadata.environment.BroadcastBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Answers;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class TransactionBackendHandlerFactoryTest {
    
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfCommitStatement() {
        BackendConnection backendConnection = mock(BackendConnection.class, Answers.RETURNS_DEEP_STUBS);
        TextProtocolBackendHandler textProtocolBackendHandler = TransactionBackendHandlerFactory.newInstance(mock(CommitStatement.class), null, backendConnection);
        assertThat(textProtocolBackendHandler, instanceOf(TransactionBackendHandler.class));
        TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) textProtocolBackendHandler;
        assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.COMMIT));
        assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
    }
    
    @Test
    public void assertTransactionBackendHandlerReturnedWhenTCLStatementInstanceOfRollbackStatement() {
        BackendConnection backendConnection = mock(BackendConnection.class, Answers.RETURNS_DEEP_STUBS);
        TextProtocolBackendHandler textProtocolBackendHandler = TransactionBackendHandlerFactory.newInstance(mock(RollbackStatement.class), null, backendConnection);
        assertThat(textProtocolBackendHandler, instanceOf(TransactionBackendHandler.class));
        TransactionBackendHandler transactionBackendHandler = (TransactionBackendHandler) textProtocolBackendHandler;
        assertFieldOfInstance(transactionBackendHandler, "operationType", is(TransactionOperationType.ROLLBACK));
        assertFieldOfInstance(getBackendTransactionManager(transactionBackendHandler), "connection", is(backendConnection));
    }
    
    @Test
    public void assertBroadcastBackendHandlerReturnedWhenTCLStatementNotHit() {
        assertThat(TransactionBackendHandlerFactory.newInstance(mock(TCLStatement.class), null, null), instanceOf(BroadcastBackendHandler.class));
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

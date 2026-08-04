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

package org.apache.shardingsphere.proxy.backend.handler.tcl.local.type;

import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.transaction.exception.SwitchTypeInTransactionException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetTransactionProxyBackendHandlerTest {
    
    @Test
    void assertExecuteWhenSwitchTypeInTransaction() {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(false);
        assertThrows(SwitchTypeInTransactionException.class, () -> new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute());
    }
    
    @Test
    void assertExecuteWhenSetReadOnly() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(true);
        when(sqlStatement.isDesiredAccessMode(TransactionAccessType.READ_ONLY)).thenReturn(true);
        when(sqlStatement.getIsolationLevel()).thenReturn(Optional.empty());
        UpdateResponseHeader actual = (UpdateResponseHeader) new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute();
        verify(connectionSession).setReadOnly(true);
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
    
    @Test
    void assertExecuteWhenSetReadWrite() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(true);
        when(sqlStatement.isDesiredAccessMode(TransactionAccessType.READ_WRITE)).thenReturn(true);
        when(sqlStatement.getIsolationLevel()).thenReturn(Optional.empty());
        new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute();
        verify(connectionSession).setReadOnly(false);
    }
    
    @Test
    void assertExecuteWhenSetIsolationLevel() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(false);
        when(sqlStatement.isDesiredAccessMode(Mockito.any())).thenReturn(false);
        when(sqlStatement.getIsolationLevel()).thenReturn(Optional.of(TransactionIsolationLevel.SERIALIZABLE));
        new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute();
        verify(connectionSession).setIsolationLevel(TransactionIsolationLevel.SERIALIZABLE);
    }
}

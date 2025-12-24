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

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.transaction.exception.SwitchTypeInTransactionException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetTransactionProxyBackendHandlerTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Test
    void assertExecuteWhenSwitchTypeInTransaction() {
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(false);
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(Connection.TRANSACTION_READ_COMMITTED);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            assertThrows(SwitchTypeInTransactionException.class, () -> new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute());
        }
    }
    
    @Test
    void assertExecuteWhenSetReadOnly() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(Connection.TRANSACTION_READ_COMMITTED);
        TransactionStatus transactionStatus = new TransactionStatus();
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(true);
        when(sqlStatement.isDesiredAccessMode(TransactionAccessType.READ_ONLY)).thenReturn(true);
        when(sqlStatement.getIsolationLevel()).thenReturn(Optional.empty());
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            UpdateResponseHeader actual = (UpdateResponseHeader) new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute();
            verify(connectionSession).setReadOnly(true);
            assertThat(actual.getSqlStatement(), is(sqlStatement));
        }
    }
    
    @Test
    void assertExecuteWhenSetReadWrite() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(Connection.TRANSACTION_READ_COMMITTED);
        TransactionStatus transactionStatus = new TransactionStatus();
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(true);
        when(sqlStatement.isDesiredAccessMode(TransactionAccessType.READ_WRITE)).thenReturn(true);
        when(sqlStatement.getIsolationLevel()).thenReturn(Optional.empty());
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute();
            verify(connectionSession).setReadOnly(false);
        }
    }
    
    @Test
    void assertExecuteWhenSetIsolationLevel() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(Connection.TRANSACTION_SERIALIZABLE);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        SetTransactionStatement sqlStatement = mock(SetTransactionStatement.class);
        when(sqlStatement.containsScope()).thenReturn(false);
        when(sqlStatement.isDesiredAccessMode(Mockito.any())).thenReturn(false);
        when(sqlStatement.getIsolationLevel()).thenReturn(Optional.of(TransactionIsolationLevel.SERIALIZABLE));
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new SetTransactionProxyBackendHandler(sqlStatement, connectionSession).execute();
            verify(connectionSession).setDefaultIsolationLevel(TransactionIsolationLevel.SERIALIZABLE);
            verify(connectionSession).setIsolationLevel(TransactionIsolationLevel.SERIALIZABLE);
        }
    }
    
    private DialectDatabaseMetaData mockDialectDatabaseMetaData(final int defaultIsolationLevel) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        DialectTransactionOption transactionOption = new DialectTransactionOption(false, false, false, false, true, defaultIsolationLevel, false, false, Collections.emptyList());
        doReturn(transactionOption).when(result).getTransactionOption();
        return result;
    }
}

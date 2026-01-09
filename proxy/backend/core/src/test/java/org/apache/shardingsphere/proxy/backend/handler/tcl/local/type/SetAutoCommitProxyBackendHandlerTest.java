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
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetAutoCommitStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetAutoCommitProxyBackendHandlerTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Test
    void assertExecuteCommitWhenNestedTransactionSupported() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(true);
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        SetAutoCommitStatement sqlStatement = mock(SetAutoCommitStatement.class);
        when(sqlStatement.isAutoCommit()).thenReturn(true);
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ResponseHeader actual = new SetAutoCommitProxyBackendHandler(sqlStatement, connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager).commit();
            verify(connectionSession).setAutoCommit(true);
            assertThat(((UpdateResponseHeader) actual).getSqlStatement(), is(sqlStatement));
        }
    }
    
    @Test
    void assertExecuteWithoutCommitWhenNestedTransactionUnsupported() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(false);
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        SetAutoCommitStatement sqlStatement = mock(SetAutoCommitStatement.class);
        when(sqlStatement.isAutoCommit()).thenReturn(true);
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            assertDoesNotThrow(() -> new SetAutoCommitProxyBackendHandler(sqlStatement, connectionSession).execute());
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(connectionSession).setAutoCommit(true);
            verify(transactionManager, never()).commit();
        }
    }
    
    @Test
    void assertExecuteWhenAutoCommitDisabled() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(true);
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        SetAutoCommitStatement sqlStatement = mock(SetAutoCommitStatement.class);
        when(sqlStatement.isAutoCommit()).thenReturn(false);
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new SetAutoCommitProxyBackendHandler(sqlStatement, connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager, never()).commit();
            verify(connectionSession).setAutoCommit(false);
        }
    }
    
    @Test
    void assertExecuteWhenNotInTransaction() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(true);
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(false);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        SetAutoCommitStatement sqlStatement = mock(SetAutoCommitStatement.class);
        when(sqlStatement.isAutoCommit()).thenReturn(true);
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new SetAutoCommitProxyBackendHandler(sqlStatement, connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager, never()).commit();
            verify(connectionSession).setAutoCommit(true);
        }
    }
    
    private DialectDatabaseMetaData mockDialectDatabaseMetaData(final boolean supportAutoCommitInNestedTransaction) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        DialectTransactionOption transactionOption = new DialectTransactionOption(false, false, supportAutoCommitInNestedTransaction, false, true,
                Connection.TRANSACTION_READ_COMMITTED, false, false, Collections.emptyList());
        when(result.getTransactionOption()).thenReturn(transactionOption);
        return result;
    }
}

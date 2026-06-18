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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeginTransactionProxyBackendHandlerTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Test
    void assertBeginWhenNotInTransaction() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(false, "public");
        TransactionStatus transactionStatus = new TransactionStatus();
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new BeginTransactionProxyBackendHandler(mock(TCLStatement.class), connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager).begin();
            verify(transactionManager, never()).commit();
        }
    }
    
    @Test
    void assertBeginWithNestedTransactionCommit() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(true, "public");
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new BeginTransactionProxyBackendHandler(mock(TCLStatement.class), connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager).commit();
            verify(transactionManager).begin();
        }
    }
    
    @Test
    void assertThrowWhenNestedTransactionUnsupportedWithDefaultSchema() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(false, "public");
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> ignored = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            assertThrows(InTransactionException.class, () -> new BeginTransactionProxyBackendHandler(mock(TCLStatement.class), connectionSession).execute());
        }
    }
    
    @Test
    void assertBeginWhenDefaultSchemaNotPresent() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(false, null);
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new BeginTransactionProxyBackendHandler(mock(TCLStatement.class), connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager, never()).commit();
            verify(transactionManager).begin();
        }
    }
    
    private DialectDatabaseMetaData mockDialectDatabaseMetaData(final boolean supportAutoCommitInNestedTransaction, final String defaultSchema) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        DialectTransactionOption transactionOption = new DialectTransactionOption(false, false, supportAutoCommitInNestedTransaction, false, true,
                Connection.TRANSACTION_READ_COMMITTED, false, false, Collections.emptyList());
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.ofNullable(defaultSchema));
        when(result.getTransactionOption()).thenReturn(transactionOption);
        when(result.getSchemaOption()).thenReturn(schemaOption);
        return result;
    }
}

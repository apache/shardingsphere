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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReleaseSavepointProxyBackendHandlerTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Test
    void assertReleaseSavepoint() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData("public");
        TransactionStatus transactionStatus = new TransactionStatus();
        transactionStatus.setInTransaction(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        ReleaseSavepointStatement sqlStatement = mock(ReleaseSavepointStatement.class);
        when(sqlStatement.getSavepointName()).thenReturn("sp_release");
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new ReleaseSavepointProxyBackendHandler(sqlStatement, connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager).releaseSavepoint("sp_release");
        }
    }
    
    @Test
    void assertReleaseWithDefaultSchemaMissing() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(null);
        TransactionStatus transactionStatus = new TransactionStatus();
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        ReleaseSavepointStatement sqlStatement = mock(ReleaseSavepointStatement.class);
        when(sqlStatement.getSavepointName()).thenReturn("sp_release");
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            new ReleaseSavepointProxyBackendHandler(sqlStatement, connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager).releaseSavepoint("sp_release");
        }
    }
    
    @Test
    void assertReleaseWithInvalidStatus() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData("public");
        TransactionStatus transactionStatus = new TransactionStatus();
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getTransactionStatus()).thenReturn(transactionStatus);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        ReleaseSavepointStatement sqlStatement = mock(ReleaseSavepointStatement.class);
        when(sqlStatement.getSavepointName()).thenReturn("sp_release");
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> ignored = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            assertThrows(SQLFeatureNotSupportedException.class, () -> new ReleaseSavepointProxyBackendHandler(sqlStatement, connectionSession).execute());
        }
    }
    
    private DialectDatabaseMetaData mockDialectDatabaseMetaData(final String defaultSchema) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.ofNullable(defaultSchema));
        when(result.getSchemaOption()).thenReturn(schemaOption);
        return result;
    }
}

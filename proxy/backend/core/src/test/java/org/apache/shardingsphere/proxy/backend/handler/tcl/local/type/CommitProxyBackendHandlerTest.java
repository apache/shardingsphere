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
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommitProxyBackendHandlerTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Test
    void assertReturnRollbackStatementWhenCommitFailed() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(true);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptyList);
        connectionContext.getTransactionContext().setExceptionOccur(true);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        TCLStatement sqlStatement = mock(TCLStatement.class);
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ResponseHeader actual = new CommitProxyBackendHandler(sqlStatement, connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager).commit();
            assertThat(((UpdateResponseHeader) actual).getSqlStatement(), instanceOf(RollbackStatement.class));
        }
    }
    
    @Test
    void assertReturnOriginalStatementWhenNoException() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mockDialectDatabaseMetaData(false);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getProtocolType()).thenReturn(databaseType);
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptyList);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(mock(ProxyDatabaseConnectionManager.class));
        TCLStatement sqlStatement = mock(TCLStatement.class);
        try (
                MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class);
                MockedConstruction<ProxyBackendTransactionManager> mockedConstruction = mockConstruction(ProxyBackendTransactionManager.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ResponseHeader actual = new CommitProxyBackendHandler(sqlStatement, connectionSession).execute();
            ProxyBackendTransactionManager transactionManager = mockedConstruction.constructed().get(0);
            verify(transactionManager).commit();
            assertThat(((UpdateResponseHeader) actual).getSqlStatement(), is(sqlStatement));
        }
    }
    
    private DialectDatabaseMetaData mockDialectDatabaseMetaData(final boolean returnRollbackWhenFailed) {
        DialectDatabaseMetaData result = mock(DialectDatabaseMetaData.class);
        when(result.getTransactionOption()).thenReturn(new DialectTransactionOption(
                false, false, false, false, true, Connection.TRANSACTION_READ_COMMITTED, returnRollbackWhenFailed, false, Collections.emptyList()));
        return result;
    }
}

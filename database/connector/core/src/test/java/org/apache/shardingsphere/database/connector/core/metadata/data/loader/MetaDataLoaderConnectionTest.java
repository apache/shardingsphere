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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDataLoaderConnectionTest {
    
    @Mock
    private Connection connection;
    
    private MetaDataLoaderConnection metaDataLoaderConnection;
    
    @BeforeEach
    void setUp() {
        metaDataLoaderConnection = new MetaDataLoaderConnection(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), connection);
    }
    
    @Test
    void assertGetCatalog() throws SQLException {
        when(connection.getCatalog()).thenReturn("foo_catalog");
        assertThat(metaDataLoaderConnection.getCatalog(), is("foo_catalog"));
    }
    
    @Test
    void assertGetCatalogWhenThrowsSQLException() throws SQLException {
        when(connection.getCatalog()).thenThrow(SQLException.class);
        assertNull(metaDataLoaderConnection.getCatalog());
    }
    
    @Test
    void assertSetCatalog() throws SQLException {
        metaDataLoaderConnection.setCatalog("foo_catalog");
        verify(connection).setCatalog("foo_catalog");
    }
    
    @Test
    void assertGetSchema() throws SQLException {
        when(connection.getSchema()).thenReturn("foo_schema");
        assertThat(metaDataLoaderConnection.getSchema(), is("foo_schema"));
    }
    
    @Test
    void assertGetSchemaWhenThrowsSQLException() throws SQLException {
        when(connection.getSchema()).thenThrow(SQLException.class);
        assertNull(metaDataLoaderConnection.getSchema());
    }
    
    @Test
    void assertSetSchema() throws SQLException {
        metaDataLoaderConnection.setSchema("foo_schema");
        verify(connection).setSchema("foo_schema");
    }
    
    @Test
    void assertCreateStatement() throws SQLException {
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        assertThat(metaDataLoaderConnection.createStatement(), is(statement));
    }
    
    @Test
    void assertCreateStatementWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        Statement statement = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenReturn(statement);
        assertThat(metaDataLoaderConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY), is(statement));
    }
    
    @Test
    void assertCreateStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        Statement statement = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT)).thenReturn(statement);
        assertThat(metaDataLoaderConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT), is(statement));
    }
    
    @Test
    void assertPrepareStatement() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("sql")).thenReturn(preparedStatement);
        assertThat(metaDataLoaderConnection.prepareStatement("sql"), is(preparedStatement));
    }
    
    @Test
    void assertPrepareStatementWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenReturn(preparedStatement);
        assertThat(metaDataLoaderConnection.prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY), is(preparedStatement));
    }
    
    @Test
    void assertPrepareStatementWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT)).thenReturn(preparedStatement);
        assertThat(metaDataLoaderConnection.prepareStatement("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT), is(preparedStatement));
    }
    
    @Test
    void assertPrepareStatementWithAutoGeneratedKeys() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("sql", Statement.RETURN_GENERATED_KEYS)).thenReturn(preparedStatement);
        assertThat(metaDataLoaderConnection.prepareStatement("sql", Statement.RETURN_GENERATED_KEYS), is(preparedStatement));
    }
    
    @Test
    void assertPrepareStatementWithColumnIndexes() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("sql", new int[0])).thenReturn(preparedStatement);
        assertThat(metaDataLoaderConnection.prepareStatement("sql", new int[0]), is(preparedStatement));
    }
    
    @Test
    void assertPrepareStatementWithColumnNames() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement("sql", new String[0])).thenReturn(preparedStatement);
        assertThat(metaDataLoaderConnection.prepareStatement("sql", new String[0]), is(preparedStatement));
    }
    
    @Test
    void assertPrepareCall() throws SQLException {
        CallableStatement callableStatement = mock(CallableStatement.class);
        when(connection.prepareCall("sql")).thenReturn(callableStatement);
        assertThat(metaDataLoaderConnection.prepareCall("sql"), is(callableStatement));
    }
    
    @Test
    void assertPrepareCallWithResultSetTypeAndResultSetConcurrency() throws SQLException {
        CallableStatement callableStatement = mock(CallableStatement.class);
        when(connection.prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenReturn(callableStatement);
        assertThat(metaDataLoaderConnection.prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY), is(callableStatement));
    }
    
    @Test
    void assertPrepareCallWithResultSetTypeAndResultSetConcurrencyAndResultSetHoldability() throws SQLException {
        CallableStatement callableStatement = mock(CallableStatement.class);
        when(connection.prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT)).thenReturn(callableStatement);
        assertThat(metaDataLoaderConnection.prepareCall("sql", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT), is(callableStatement));
    }
    
    @Test
    void assertNativeSQL() throws SQLException {
        when(connection.nativeSQL("sql")).thenReturn("native_sql");
        assertThat(metaDataLoaderConnection.nativeSQL("sql"), is("native_sql"));
    }
    
    @Test
    void assertSetAutoCommit() throws SQLException {
        metaDataLoaderConnection.setAutoCommit(true);
        verify(connection).setAutoCommit(true);
    }
    
    @Test
    void assertGetAutoCommit() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(true);
        assertTrue(metaDataLoaderConnection.getAutoCommit());
    }
    
    @Test
    void assertCommit() throws SQLException {
        metaDataLoaderConnection.commit();
        verify(connection).commit();
    }
    
    @Test
    void assertRollback() throws SQLException {
        metaDataLoaderConnection.rollback();
        verify(connection).rollback();
    }
    
    @Test
    void assertRollbackWithSavepoint() throws SQLException {
        Savepoint savepoint = mock(Savepoint.class);
        metaDataLoaderConnection.rollback(savepoint);
        verify(connection).rollback(savepoint);
    }
    
    @Test
    void assertSetSavepoint() throws SQLException {
        metaDataLoaderConnection.setSavepoint();
        verify(connection).setSavepoint();
    }
    
    @Test
    void assertSetSavepointWithName() throws SQLException {
        metaDataLoaderConnection.setSavepoint("foo");
        verify(connection).setSavepoint("foo");
    }
    
    @Test
    void assertReleaseSavepointWithName() throws SQLException {
        Savepoint savepoint = mock(Savepoint.class);
        metaDataLoaderConnection.releaseSavepoint(savepoint);
        verify(connection).releaseSavepoint(savepoint);
    }
    
    @Test
    void assertClose() throws SQLException {
        metaDataLoaderConnection.close();
        verify(connection).close();
    }
    
    @Test
    void assertIsClosed() throws SQLException {
        when(connection.isClosed()).thenReturn(true);
        assertTrue(metaDataLoaderConnection.isClosed());
    }
    
    @Test
    void assertGetMetaData() throws SQLException {
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        assertThat(metaDataLoaderConnection.getMetaData(), is(metaData));
    }
    
    @Test
    void assertSetReadOnly() throws SQLException {
        metaDataLoaderConnection.setReadOnly(true);
        verify(connection).setReadOnly(true);
    }
    
    @Test
    void assertIsReadOnly() throws SQLException {
        when(connection.isReadOnly()).thenReturn(true);
        assertTrue(metaDataLoaderConnection.isReadOnly());
    }
    
    @Test
    void assertSetTransactionIsolation() throws SQLException {
        metaDataLoaderConnection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        verify(connection).setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }
    
    @Test
    void assertGetTransactionIsolation() throws SQLException {
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_READ_UNCOMMITTED);
        assertThat(metaDataLoaderConnection.getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
    }
    
    @Test
    void assertGetWarnings() throws SQLException {
        SQLWarning sqlWarning = mock(SQLWarning.class);
        when(connection.getWarnings()).thenReturn(sqlWarning);
        assertThat(metaDataLoaderConnection.getWarnings(), is(sqlWarning));
    }
    
    @Test
    void assertClearWarnings() throws SQLException {
        metaDataLoaderConnection.clearWarnings();
        verify(connection).clearWarnings();
    }
    
    @Test
    void assertGetTypeMap() throws SQLException {
        when(connection.getTypeMap()).thenReturn(Collections.singletonMap("foo", Object.class));
        assertThat(metaDataLoaderConnection.getTypeMap(), is(Collections.singletonMap("foo", Object.class)));
    }
    
    @Test
    void assertSetTypeMap() throws SQLException {
        metaDataLoaderConnection.setTypeMap(Collections.singletonMap("foo", Object.class));
        verify(connection).setTypeMap(Collections.singletonMap("foo", Object.class));
    }
    
    @Test
    void assertSetHoldability() throws SQLException {
        metaDataLoaderConnection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
        verify(connection).setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
    }
    
    @Test
    void assertGetHoldability() throws SQLException {
        when(connection.getHoldability()).thenReturn(ResultSet.CLOSE_CURSORS_AT_COMMIT);
        assertThat(metaDataLoaderConnection.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
    }
    
    @Test
    void assertCreateBlob() throws SQLException {
        Blob blob = mock(Blob.class);
        when(connection.createBlob()).thenReturn(blob);
        assertThat(metaDataLoaderConnection.createBlob(), is(blob));
    }
    
    @Test
    void assertCreateClob() throws SQLException {
        Clob clob = mock(Clob.class);
        when(connection.createClob()).thenReturn(clob);
        assertThat(metaDataLoaderConnection.createClob(), is(clob));
    }
    
    @Test
    void assertCreateNClob() throws SQLException {
        NClob nClob = mock(NClob.class);
        when(connection.createNClob()).thenReturn(nClob);
        assertThat(metaDataLoaderConnection.createNClob(), is(nClob));
    }
    
    @Test
    void assertCreateSQLXML() throws SQLException {
        SQLXML sqlxml = mock(SQLXML.class);
        when(connection.createSQLXML()).thenReturn(sqlxml);
        assertThat(metaDataLoaderConnection.createSQLXML(), is(sqlxml));
    }
    
    @Test
    void assertCreateArrayOf() throws SQLException {
        Array array = mock(Array.class);
        when(connection.createArrayOf("type", new Object[0])).thenReturn(array);
        assertThat(metaDataLoaderConnection.createArrayOf("type", new Object[0]), is(array));
    }
    
    @Test
    void assertCreateStruct() throws SQLException {
        Struct struct = mock(Struct.class);
        when(connection.createStruct("type", new Object[0])).thenReturn(struct);
        assertThat(metaDataLoaderConnection.createStruct("type", new Object[0]), is(struct));
    }
    
    @Test
    void assertIsValid() throws SQLException {
        when(connection.isValid(1)).thenReturn(true);
        assertTrue(metaDataLoaderConnection.isValid(1));
    }
    
    @Test
    void assertSetClientInfoWithNameAndValue() throws SQLException {
        metaDataLoaderConnection.setClientInfo("name", "value");
        verify(connection).setClientInfo("name", "value");
    }
    
    @Test
    void assertSetClientInfoWithProperties() throws SQLException {
        metaDataLoaderConnection.setClientInfo(new Properties());
        verify(connection).setClientInfo(new Properties());
    }
    
    @Test
    void assertGetClientInfoWithName() throws SQLException {
        when(connection.getClientInfo("name")).thenReturn("value");
        assertThat(metaDataLoaderConnection.getClientInfo("name"), is("value"));
    }
    
    @Test
    void assertGetClientInfoWithProperties() throws SQLException {
        when(connection.getClientInfo()).thenReturn(new Properties());
        assertThat(metaDataLoaderConnection.getClientInfo(), is(new Properties()));
    }
    
    @Test
    void assertAbort() throws SQLException {
        Executor executor = mock(Executor.class);
        metaDataLoaderConnection.abort(executor);
        verify(connection).abort(executor);
    }
    
    @Test
    void assertSetNetworkTimeout() throws SQLException {
        Executor executor = mock(Executor.class);
        metaDataLoaderConnection.setNetworkTimeout(executor, 1);
        verify(connection).setNetworkTimeout(executor, 1);
    }
    
    @Test
    void assertGetNetworkTimeout() throws SQLException {
        when(connection.getNetworkTimeout()).thenReturn(1);
        assertThat(metaDataLoaderConnection.getNetworkTimeout(), is(1));
    }
    
    @Test
    void assertUnwrap() throws SQLException {
        metaDataLoaderConnection.unwrap(Object.class);
        verify(connection).unwrap(Object.class);
    }
    
    @Test
    void assertIsWrapperFor() throws SQLException {
        when(connection.isWrapperFor(Object.class)).thenReturn(true);
        assertTrue(metaDataLoaderConnection.isWrapperFor(Object.class));
    }
}

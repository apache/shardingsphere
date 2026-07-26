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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query;

import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.junit.jupiter.api.Test;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Oid;
import org.postgresql.core.TypeInfo;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLColumnTypeOIDLoaderTest {
    
    @Test
    void assertLoadWithSameDataSource() throws SQLException {
        BaseConnection connection = mock(BaseConnection.class);
        mockTypeOID(connection, "record_type", 2249);
        ConnectionSession connectionSession = createConnectionSession(connection);
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(
                Collections.singletonList(createQueryHeader(Types.STRUCT, "record_type")), "ds_0");
        Map<Integer, Integer> actual = PostgreSQLColumnTypeOIDLoader.load(connectionSession, queryResponseHeader);
        assertThat(actual.get(1), is(2249));
        verify(connectionSession.getDatabaseConnectionManager()).getConnections("postgres", "ds_0", 0, 1, ConnectionMode.CONNECTION_STRICTLY);
    }
    
    @Test
    void assertLoadWithoutCompositeColumn() throws SQLException {
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(
                Collections.singletonList(createQueryHeader(Types.VARCHAR, "varchar")), "ds_0");
        assertTrue(PostgreSQLColumnTypeOIDLoader.load(mock(ConnectionSession.class), queryResponseHeader).isEmpty());
    }
    
    @Test
    void assertLoadWithoutDataSource() throws SQLException {
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(Collections.singletonList(createQueryHeader(Types.STRUCT, "record_type")));
        assertTrue(PostgreSQLColumnTypeOIDLoader.load(mock(ConnectionSession.class), queryResponseHeader).isEmpty());
    }
    
    @Test
    void assertLoadWithoutPgjdbcConnection() throws SQLException {
        Connection connection = mock(Connection.class);
        ConnectionSession connectionSession = createConnectionSession(connection);
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(
                Collections.singletonList(createQueryHeader(Types.STRUCT, "record_type")), "ds_0");
        assertTrue(PostgreSQLColumnTypeOIDLoader.load(connectionSession, queryResponseHeader).isEmpty());
        verify(connection, never()).unwrap(BaseConnection.class);
    }
    
    @Test
    void assertLoadFromResultSetMetaData() throws SQLException {
        BaseConnection connection = mock(BaseConnection.class);
        mockTypeOID(connection, "record_type", 2249);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnType(1)).thenReturn(Types.STRUCT);
        when(metaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnTypeName(1)).thenReturn("record_type");
        Map<Integer, Integer> actual = PostgreSQLColumnTypeOIDLoader.load(connection, metaData);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(1), is(2249));
        verify(metaData, never()).getColumnTypeName(2);
    }
    
    @Test
    void assertLoadUnspecifiedTypeOID() throws SQLException {
        BaseConnection connection = mock(BaseConnection.class);
        mockTypeOID(connection, "unknown_type", Oid.UNSPECIFIED);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnType(1)).thenReturn(Types.STRUCT);
        when(metaData.getColumnTypeName(1)).thenReturn("unknown_type");
        assertTrue(PostgreSQLColumnTypeOIDLoader.load(connection, metaData).isEmpty());
    }
    
    private QueryHeader createQueryHeader(final int columnType, final String columnTypeName) {
        return new QueryHeader("", "", "record_value", "record_value", columnType, columnTypeName, -1, 0, false, false, false, false);
    }
    
    private ConnectionSession createConnectionSession(final Connection connection) throws SQLException {
        ConnectionSession result = mock(ConnectionSession.class);
        ProxyDatabaseConnectionManager connectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionManager.getConnections(any(), anyString(), anyInt(), anyInt(), any())).thenReturn(Collections.singletonList(connection));
        when(result.getDatabaseConnectionManager()).thenReturn(connectionManager);
        when(result.getUsedDatabaseName()).thenReturn("postgres");
        return result;
    }
    
    private void mockTypeOID(final BaseConnection connection, final String columnTypeName, final int typeOID) throws SQLException {
        TypeInfo typeInfo = mock(TypeInfo.class);
        when(typeInfo.getPGType(columnTypeName)).thenReturn(typeOID);
        when(connection.getTypeInfo()).thenReturn(typeInfo);
        when(connection.isWrapperFor(BaseConnection.class)).thenReturn(true);
        when(connection.unwrap(BaseConnection.class)).thenReturn(connection);
    }
}

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

import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.StandardDatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Oid;
import org.postgresql.core.TypeInfo;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLColumnTypeOIDLoaderTest {
    
    @Test
    void assertLoadFromExecutedRoute() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        StandardDatabaseProxyConnector backendHandler = mock(StandardDatabaseProxyConnector.class);
        Collection<ExecutionUnit> executionUnits = mockExecutionUnits(1);
        when(backendHandler.getExecutionUnits()).thenReturn(executionUnits);
        BaseConnection connection = mockConnection("record_type", 2249);
        ProxyDatabaseConnectionManager connectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(connectionManager);
        when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        when(connectionManager.getConnections("postgres", "ds_0", 0, 1, ConnectionMode.CONNECTION_STRICTLY)).thenReturn(Collections.singletonList(connection));
        Map<Integer, Integer> actual = PostgreSQLColumnTypeOIDLoader.load(connectionSession, backendHandler, Collections.singletonList(createQueryHeader(Types.STRUCT, "record_type")));
        assertThat(actual, is(Collections.singletonMap(1, 2249)));
    }
    
    @Test
    void assertLoadFromMultipleExecutionUnits() {
        StandardDatabaseProxyConnector backendHandler = mock(StandardDatabaseProxyConnector.class);
        Collection<ExecutionUnit> executionUnits = mockExecutionUnits(2);
        when(backendHandler.getExecutionUnits()).thenReturn(executionUnits);
        assertThrows(UnsupportedSQLOperationException.class, () -> PostgreSQLColumnTypeOIDLoader.load(
                mock(ConnectionSession.class), backendHandler, Collections.singletonList(createQueryHeader(Types.STRUCT, "record_type"))));
    }
    
    @Test
    void assertLoadWithoutCompositeType() throws SQLException {
        StandardDatabaseProxyConnector backendHandler = mock(StandardDatabaseProxyConnector.class);
        assertTrue(PostgreSQLColumnTypeOIDLoader.load(
                mock(ConnectionSession.class), backendHandler, Collections.singletonList(createQueryHeader(Types.VARCHAR, "varchar"))).isEmpty());
        verify(backendHandler, never()).getExecutionUnits();
    }
    
    @Test
    void assertLoadFromResultSetMetaData() throws SQLException {
        BaseConnection connection = mockConnection("record_type", 2249);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnType(1)).thenReturn(Types.STRUCT);
        when(metaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(metaData.getColumnTypeName(1)).thenReturn("record_type");
        Map<Integer, Integer> actual = PostgreSQLColumnTypeOIDLoader.load(connection, metaData);
        assertThat(actual, is(Collections.singletonMap(1, 2249)));
        verify(metaData, never()).getColumnTypeName(2);
    }
    
    @Test
    void assertLoadUnspecifiedTypeOID() throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnType(1)).thenReturn(Types.STRUCT);
        when(metaData.getColumnTypeName(1)).thenReturn("unknown_type");
        BaseConnection connection = mockConnection("unknown_type", Oid.UNSPECIFIED);
        assertTrue(PostgreSQLColumnTypeOIDLoader.load(connection, metaData).isEmpty());
    }
    
    private Collection<ExecutionUnit> mockExecutionUnits(final int executionUnitCount) {
        ExecutionUnit executionUnit = mock(ExecutionUnit.class);
        when(executionUnit.getDataSourceName()).thenReturn("ds_0");
        return Collections.nCopies(executionUnitCount, executionUnit);
    }
    
    private BaseConnection mockConnection(final String columnTypeName, final int typeOID) throws SQLException {
        TypeInfo typeInfo = mock(TypeInfo.class);
        when(typeInfo.getPGType(columnTypeName)).thenReturn(typeOID);
        BaseConnection result = mock(BaseConnection.class);
        when(result.getTypeInfo()).thenReturn(typeInfo);
        when(result.isWrapperFor(BaseConnection.class)).thenReturn(true);
        when(result.unwrap(BaseConnection.class)).thenReturn(result);
        return result;
    }
    
    private QueryHeader createQueryHeader(final int columnType, final String columnTypeName) {
        return new QueryHeader("", "", "", "", columnType, columnTypeName, 0, 0, false, false, false, false);
    }
}

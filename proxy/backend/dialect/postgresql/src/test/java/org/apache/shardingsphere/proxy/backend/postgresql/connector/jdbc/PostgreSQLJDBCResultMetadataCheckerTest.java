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

package org.apache.shardingsphere.proxy.backend.postgresql.connector.jdbc;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.DialectJDBCResultMetadataChecker;
import org.apache.shardingsphere.proxy.backend.postgresql.exception.PostgreSQLCompositeTypeAcrossDataSourcesException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLJDBCResultMetadataCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DialectJDBCResultMetadataChecker checker = DatabaseTypedSPILoader.getService(DialectJDBCResultMetadataChecker.class, databaseType);
    
    @Test
    void assertCheckWithSingleDataSource() throws SQLException {
        Statement statement = mock(Statement.class);
        checker.check(createExecutionContext("ds_0", "ds_0"), statement, "SELECT record_value");
        verify(statement, never()).getConnection();
    }
    
    @Test
    void assertCheckPreparedStatement() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData metaData = createMetaData(Types.INTEGER);
        when(preparedStatement.getMetaData()).thenReturn(metaData);
        assertDoesNotThrow(() -> checker.check(createExecutionContext("ds_0", "ds_1"), preparedStatement, "SELECT record_value"));
        verify(preparedStatement).getMetaData();
    }
    
    @Test
    void assertCheckCompositeType() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData metaData = createMetaData(Types.STRUCT);
        when(preparedStatement.getMetaData()).thenReturn(metaData);
        assertThrows(PostgreSQLCompositeTypeAcrossDataSourcesException.class,
                () -> checker.check(createExecutionContext("ds_0", "ds_1"), preparedStatement, "SELECT record_value"));
    }
    
    @Test
    void assertCheckStatement() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData metaData = createMetaData(Types.INTEGER);
        when(preparedStatement.getMetaData()).thenReturn(metaData);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement("SELECT record_value")).thenReturn(preparedStatement);
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(connection);
        checker.check(createExecutionContext("ds_0", "ds_1"), statement, "SELECT record_value");
        verify(preparedStatement).close();
    }
    
    @Test
    void assertCheckCompositeTypeWithoutMetaData() {
        assertDoesNotThrow(() -> checker.check(createExecutionContext("ds_0", "ds_1"), mock(PreparedStatement.class), "SELECT record_value"));
    }
    
    @Test
    void assertCheckCompositeTypeWithDerivedProjection() throws SQLException {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(metaData.getColumnType(2)).thenReturn(Types.STRUCT);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.containsDerivedProjections()).thenReturn(true);
        when(sqlStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Collections.singletonList(mock(Projection.class)));
        ExecutionContext executionContext = createExecutionContext("ds_0", "ds_1");
        when(executionContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getMetaData()).thenReturn(metaData);
        assertDoesNotThrow(() -> checker.check(executionContext, preparedStatement, "SELECT record_value"));
    }
    
    private ExecutionContext createExecutionContext(final String... dataSourceNames) {
        ExecutionContext result = mock(ExecutionContext.class);
        ExecutionUnit[] executionUnits = new ExecutionUnit[dataSourceNames.length];
        for (int i = 0; i < dataSourceNames.length; i++) {
            executionUnits[i] = new ExecutionUnit(dataSourceNames[i], new SQLUnit("SELECT record_value", Collections.emptyList()));
        }
        when(result.getExecutionUnits()).thenReturn(Arrays.asList(executionUnits));
        return result;
    }
    
    private ResultSetMetaData createMetaData(final int columnType) throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnType(1)).thenReturn(columnType);
        return result;
    }
}

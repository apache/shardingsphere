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

import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLJDBCResultMetadataCheckerTest {
    
    private final PostgreSQLJDBCResultMetadataChecker checker = new PostgreSQLJDBCResultMetadataChecker();
    
    @Test
    void assertCheckSingleExecutionUnit() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        checker.check(createExecutionUnits("ds_0"), statement, "SELECT record_value");
        verify(statement, never()).getMetaData();
    }
    
    @Test
    void assertCheckMultipleExecutionUnits() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSetMetaData metaData = createMetaData(Types.INTEGER);
        when(statement.getMetaData()).thenReturn(metaData);
        assertDoesNotThrow(() -> checker.check(createExecutionUnits("ds_0", "ds_1"), statement, "SELECT value"));
    }
    
    @Test
    void assertCheckCompositeTypeAcrossExecutionUnits() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSetMetaData metaData = createMetaData(Types.STRUCT);
        when(statement.getMetaData()).thenReturn(metaData);
        UnsupportedSQLOperationException actual =
                assertThrows(UnsupportedSQLOperationException.class, () -> checker.check(createExecutionUnits("ds_0", "ds_0"), statement, "SELECT record_value"));
        assertThat(actual.getMessage(), is(
                "Unsupported SQL operation: PostgreSQL composite result columns cannot be returned when routed to multiple execution units."));
    }
    
    @Test
    void assertCheckStatement() throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData metaData = createMetaData(Types.INTEGER);
        when(preparedStatement.getMetaData()).thenReturn(metaData);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement("SELECT value")).thenReturn(preparedStatement);
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(connection);
        checker.check(createExecutionUnits("ds_0", "ds_1"), statement, "SELECT value");
        verify(preparedStatement).close();
    }
    
    @Test
    void assertCheckWithoutResultMetadata() {
        assertDoesNotThrow(() -> checker.check(createExecutionUnits("ds_0", "ds_1"), mock(PreparedStatement.class), "UPDATE foo SET value = 1"));
    }
    
    private Collection<ExecutionUnit> createExecutionUnits(final String... storageUnitNames) {
        ExecutionUnit[] result = new ExecutionUnit[storageUnitNames.length];
        for (int i = 0; i < storageUnitNames.length; i++) {
            result[i] = new ExecutionUnit(storageUnitNames[i], new SQLUnit("SELECT value", Collections.emptyList()));
        }
        return Arrays.asList(result);
    }
    
    private ResultSetMetaData createMetaData(final int columnType) throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnType(1)).thenReturn(columnType);
        return result;
    }
}

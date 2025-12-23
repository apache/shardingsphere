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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.statement;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.parameter.TypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class JDBCBackendStatementTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreateStorageResourceWithStatementSetsFetchSizeOnlyInMemoryStrictly() throws SQLException {
        JDBCBackendStatement backendStatement = new JDBCBackendStatement();
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        StatementMemoryStrictlyFetchSizeSetter fetchSizeSetter = mock(StatementMemoryStrictlyFetchSizeSetter.class);
        when(DatabaseTypedSPILoader.findService(StatementMemoryStrictlyFetchSizeSetter.class, databaseType)).thenReturn(Optional.of(fetchSizeSetter));
        StatementOption statementOption = new StatementOption(false);
        Statement actualMemoryStrict = backendStatement.createStorageResource(connection, ConnectionMode.MEMORY_STRICTLY, statementOption, databaseType);
        assertThat(actualMemoryStrict, is(statement));
        verify(fetchSizeSetter).setFetchSize(statement);
        assertThat(backendStatement.createStorageResource(connection, ConnectionMode.CONNECTION_STRICTLY, statementOption, databaseType), is(statement));
        verifyNoMoreInteractions(fetchSizeSetter);
    }
    
    @Test
    void assertCreateStorageResourceWithPreparedStatement() throws SQLException {
        JDBCBackendStatement backendStatement = new JDBCBackendStatement();
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatementWithGeneratedKeys = mock(PreparedStatement.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        String sql = "SELECT * FROM foo WHERE id = ? AND name = ?";
        when(connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(preparedStatementWithGeneratedKeys);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(DatabaseTypedSPILoader.findService(StatementMemoryStrictlyFetchSizeSetter.class, databaseType)).thenReturn(Optional.empty());
        Object typeUnspecifiedParam = mock(TypeUnspecifiedSQLParameter.class);
        SQLUnit sqlUnitWithParams = new SQLUnit(sql, Arrays.asList(typeUnspecifiedParam, "foo_param"));
        ExecutionUnit executionUnitWithParams = new ExecutionUnit("ds", sqlUnitWithParams);
        StatementOption statementOption = new StatementOption(true);
        Statement actualWithGeneratedKeys = backendStatement.createStorageResource(executionUnitWithParams, connection, 0, ConnectionMode.MEMORY_STRICTLY, statementOption, databaseType);
        assertThat(actualWithGeneratedKeys, is(preparedStatementWithGeneratedKeys));
        verify(preparedStatementWithGeneratedKeys).setObject(1, typeUnspecifiedParam, Types.OTHER);
        verify(preparedStatementWithGeneratedKeys).setObject(2, "foo_param");
        Statement actualWithoutGeneratedKeys = backendStatement.createStorageResource(
                new ExecutionUnit("ds", new SQLUnit(sql, Collections.emptyList())), connection, 0, ConnectionMode.CONNECTION_STRICTLY, new StatementOption(false), databaseType);
        assertThat(actualWithoutGeneratedKeys, is(preparedStatement));
    }
}

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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DistSQLStatementExecutorTest {
    
    @Test
    void assertExecuteQuery() throws SQLException {
        QueryContext queryContext = createQueryContext(new ParseStatement("SELECT 1"));
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getProcessId()).thenReturn("foo_process_id");
        Statement statement = mock(Statement.class);
        when(statement.getResultSetType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
        when(statement.getResultSetConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
        AtomicReference<DistSQLConnectionContext> actualConnectionContext = new AtomicReference<>();
        try (MockedConstruction<DistSQLQueryExecuteEngine> mockedEngines = mockConstruction(DistSQLQueryExecuteEngine.class, (mock, context) -> {
            actualConnectionContext.set((DistSQLConnectionContext) context.arguments().get(3));
            when(mock.getColumnNames()).thenReturn(Collections.singleton("foo_column"));
            when(mock.getRows()).thenReturn(Collections.singleton(new LocalDataQueryResultRow("foo_value")));
        })) {
            try (ResultSet actual = new DistSQLStatementExecutor(connection, mock(StatementManager.class), statement).executeQuery(queryContext)) {
                verify(mockedEngines.constructed().get(0)).executeQuery();
                assertThat(actualConnectionContext.get().getQueryContext(), sameInstance(queryContext));
                assertThat(actualConnectionContext.get().getProcessId(), is("foo_process_id"));
                assertTrue(actual.next());
                assertThat(actual.getString(1), is("foo_value"));
            }
        }
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        QueryContext queryContext = createQueryContext(new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false));
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        try (MockedConstruction<DistSQLUpdateExecuteEngine> mockedEngines = mockConstruction(DistSQLUpdateExecuteEngine.class)) {
            new DistSQLStatementExecutor(connection, mock(StatementManager.class), mock(Statement.class)).executeUpdate(queryContext);
            verify(mockedEngines.constructed().get(0)).executeUpdate();
        }
    }
    
    @Test
    void assertIsQuery() {
        QueryContext queryContext = createQueryContext(mock(DistSQLStatement.class));
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        DistSQLStatementExecutor executor = new DistSQLStatementExecutor(connection, mock(StatementManager.class), mock(Statement.class));
        assertThrows(UnsupportedSQLOperationException.class, () -> executor.isQuery(queryContext));
    }
    
    private QueryContext createQueryContext(final DistSQLStatement sqlStatement) {
        QueryContext result = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
}

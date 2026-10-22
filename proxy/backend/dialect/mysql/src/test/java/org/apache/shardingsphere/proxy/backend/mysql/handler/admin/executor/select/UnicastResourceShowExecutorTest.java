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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select;

import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseProxyConnectorFactory.class)
class UnicastResourceShowExecutorTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private DatabaseProxyConnector databaseProxyConnector;
    
    private final SelectStatement sqlStatement = mock(SelectStatement.class);
    
    @Test
    void assertExecuteWithUnknownDatabase() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        UnknownDatabaseException actual = assertThrows(UnknownDatabaseException.class,
                () -> new UnicastResourceShowExecutor(sqlStatement, "SELECT @@max_allowed_packet").execute(connectionSession, metaData));
        assertThat(actual.getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertExecuteWithEmptyStorageUnits() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(metaData.getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class));
        EmptyStorageUnitException actual = assertThrows(EmptyStorageUnitException.class,
                () -> new UnicastResourceShowExecutor(sqlStatement, "SELECT @@max_allowed_packet").execute(connectionSession, metaData));
        assertThat(actual.getMessage(), is("There is no storage unit in database 'foo_db'."));
    }
    
    @Test
    void assertExecuteWithoutDatabases() {
        assertThrows(NoDatabaseSelectedException.class, () -> new UnicastResourceShowExecutor(sqlStatement, "SELECT @@max_allowed_packet").execute(connectionSession, metaData));
    }
    
    @Test
    void assertExecuteWithoutAvailableStorageUnits() {
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(mock(ShardingSphereDatabase.class)));
        EmptyStorageUnitException actual = assertThrows(EmptyStorageUnitException.class,
                () -> new UnicastResourceShowExecutor(sqlStatement, "SELECT @@max_allowed_packet").execute(connectionSession, metaData));
        assertThat(actual.getMessage(), is("There is no storage unit in any database."));
    }
    
    @Test
    void assertExecuteWithFirstAvailableDatabase() throws SQLException {
        ShardingSphereDatabase firstDatabase = mock(ShardingSphereDatabase.class);
        when(firstDatabase.containsDataSource()).thenReturn(true);
        when(firstDatabase.getName()).thenReturn("foo_db");
        when(metaData.getAllDatabases()).thenReturn(Arrays.asList(mock(ShardingSphereDatabase.class), firstDatabase));
        when(metaData.getDatabase("foo_db")).thenReturn(firstDatabase);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(DatabaseProxyConnectorFactory.newInstance(any(QueryContext.class), any(), eq(false))).thenReturn(databaseProxyConnector);
        QueryHeader queryHeader = new QueryHeader("", "", "foo_label", "foo_column", Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false);
        when(databaseProxyConnector.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(queryHeader)));
        when(databaseProxyConnector.next()).thenReturn(true, false);
        when(databaseProxyConnector.getRowData()).thenReturn(new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.VARCHAR, "foo_value"))));
        UnicastResourceShowExecutor executor = new UnicastResourceShowExecutor(sqlStatement, "SELECT @@max_allowed_packet");
        try (MockedConstruction<SQLBindEngine> ignored = mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(sqlStatement)).thenReturn(sqlStatementContext))) {
            executor.execute(connectionSession, metaData);
        }
        InOrder actualOrder = inOrder(connectionSession, databaseProxyConnector);
        actualOrder.verify(connectionSession).setCurrentDatabaseName("foo_db");
        actualOrder.verify(databaseProxyConnector).execute();
        actualOrder.verify(connectionSession).setCurrentDatabaseName(null);
        actualOrder.verify(databaseProxyConnector).close();
        MergedResult actual = executor.getMergedResult();
        assertTrue(actual.next());
        assertThat(actual.getValue(1, Object.class), is("foo_value"));
        assertFalse(actual.next());
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnLabel(1), is("foo_label"));
        assertThat(actualMetaData.getColumnName(1), is("foo_label"));
        assertThat(actualMetaData.getColumnType(1), is(Types.VARCHAR));
    }
    
    @Test
    void assertExecuteWithConnectorFailure() throws SQLException {
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsDataSource()).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.singletonList("foo_db"));
        when(DatabaseProxyConnectorFactory.newInstance(any(QueryContext.class), any(), eq(false))).thenReturn(databaseProxyConnector);
        SQLException expected = new SQLException("foo_failure");
        when(databaseProxyConnector.execute()).thenThrow(expected);
        try (MockedConstruction<SQLBindEngine> ignored = mockConstruction(SQLBindEngine.class, (mock, context) -> when(mock.bind(sqlStatement)).thenReturn(sqlStatementContext))) {
            SQLException actual = assertThrows(SQLException.class, () -> new UnicastResourceShowExecutor(sqlStatement, "SELECT @@max_allowed_packet").execute(connectionSession, metaData));
            assertThat(actual, sameInstance(expected));
        }
        InOrder actualOrder = inOrder(connectionSession, databaseProxyConnector);
        actualOrder.verify(connectionSession).setCurrentDatabaseName("foo_db");
        actualOrder.verify(databaseProxyConnector).execute();
        actualOrder.verify(connectionSession).setCurrentDatabaseName("foo_db");
        actualOrder.verify(databaseProxyConnector).close();
    }
}

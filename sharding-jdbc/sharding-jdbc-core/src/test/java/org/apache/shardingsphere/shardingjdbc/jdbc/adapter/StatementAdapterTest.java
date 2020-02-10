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

package org.apache.shardingsphere.shardingjdbc.jdbc.adapter;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.apache.shardingsphere.shardingjdbc.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShardingPreparedStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShardingStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.util.JDBCTestSQL;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public final class StatementAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private final List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    private final Map<DatabaseType, Statement> statements = new HashMap<>();
    
    private String sql = JDBCTestSQL.SELECT_GROUP_BY_USER_ID_SQL;
    
    @Before
    public void init() {
        ShardingConnection shardingConnection = getShardingDataSource().getConnection();
        shardingConnections.add(shardingConnection);
        statements.put(DatabaseTypes.getActualDatabaseType("H2"), shardingConnection.createStatement());
    }
    
    @After
    public void close() throws SQLException {
        for (Statement each : statements.values()) {
            each.close();
        }
        for (ShardingConnection each : shardingConnections) {
            each.close();
        }
    }
    
    @Test
    public void assertClose() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery(sql);
            each.close();
            assertTrue(each.isClosed());
            assertTrue(((ShardingStatement) each).getRoutedStatements().isEmpty());
        }
    }
    
    @Test
    public void assertSetPoolable() throws SQLException {
        for (Entry<DatabaseType, Statement> each : statements.entrySet()) {
            each.getValue().setPoolable(true);
            each.getValue().executeQuery(sql);
            assertPoolable((ShardingStatement) each.getValue(), true);
            each.getValue().setPoolable(false);
            assertPoolable((ShardingStatement) each.getValue(), false);
        }
    }
    
    private void assertPoolable(final ShardingStatement actual, final boolean poolable) throws SQLException {
        assertThat(actual.isPoolable(), is(poolable));
        assertThat(actual.getRoutedStatements().size(), is(4));
        for (Statement each : actual.getRoutedStatements()) {
            // H2 do not implements method `setPoolable()`
            assertFalse(each.isPoolable());
        }
    }
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        for (Statement each : statements.values()) {
            each.setFetchSize(4);
            each.executeQuery(sql);
            assertFetchSize((ShardingStatement) each, 4);
            each.setFetchSize(100);
            assertFetchSize((ShardingStatement) each, 100);
        }
    }
    
    private void assertFetchSize(final ShardingStatement actual, final int fetchSize) throws SQLException {
        assertThat(actual.getFetchSize(), is(fetchSize));
        assertThat(actual.getRoutedStatements().size(), is(4));
        for (Statement each : actual.getRoutedStatements()) {
            assertThat(each.getFetchSize(), is(fetchSize));
        }
    }
    
    @Test
    public void assertSetEscapeProcessing() throws SQLException {
        for (Statement each : statements.values()) {
            each.setEscapeProcessing(true);
            each.executeQuery(sql);
            each.setEscapeProcessing(false);
        }
    }
    
    @Test
    public void assertCancel() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery(sql);
            each.cancel();
        }
    }
    
    @Test
    public void assertGetUpdateCount() throws SQLException {
        String sql = "DELETE FROM t_order WHERE status = 'init'";
        for (Entry<DatabaseType, Statement> each : statements.entrySet()) {
            each.getValue().execute(sql);
            assertThat(each.getValue().getUpdateCount(), is(4));
        }
    }
    
    @Test
    public void assertGetUpdateCountNoData() throws SQLException {
        String sql = "DELETE FROM t_order WHERE status = 'none'";
        for (Entry<DatabaseType, Statement> each : statements.entrySet()) {
            each.getValue().execute(sql);
            assertThat(each.getValue().getUpdateCount(), is(0));
        }
    }
    
    @Test
    public void assertGetUpdateCountSelect() throws SQLException {
        for (Statement each : statements.values()) {
            each.execute(sql);
            assertThat(each.getUpdateCount(), is(-1));
        }
    }
    
    @Test
    public void assertOverMaxUpdateRow() throws SQLException {
        final Statement statement1 = Mockito.mock(Statement.class);
        when(statement1.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        final Statement statement2 = Mockito.mock(Statement.class);
        when(statement2.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        ShardingStatement shardingStatement1 = spy(new ShardingStatement(getShardingDataSource().getConnection()));
        doReturn(true).when(shardingStatement1).isAccumulate();
        doReturn(Lists.newArrayList(statement1, statement2)).when(shardingStatement1).getRoutedStatements();
        assertThat(shardingStatement1.getUpdateCount(), is(Integer.MAX_VALUE));
        ShardingPreparedStatement shardingStatement2 = spy(new ShardingPreparedStatement(getShardingDataSource().getConnection(), sql));
        doReturn(true).when(shardingStatement2).isAccumulate();
        doReturn(Lists.newArrayList(statement1, statement2)).when(shardingStatement2).getRoutedStatements();
        assertThat(shardingStatement2.getUpdateCount(), is(Integer.MAX_VALUE));
    }
    
    @Test
    public void assertNotAccumulateUpdateRow() throws SQLException {
        final Statement statement1 = Mockito.mock(Statement.class);
        when(statement1.getUpdateCount()).thenReturn(10);
        final Statement statement2 = Mockito.mock(Statement.class);
        when(statement2.getUpdateCount()).thenReturn(10);
        ShardingStatement shardingStatement1 = spy(new ShardingStatement(getShardingDataSource().getConnection()));
        doReturn(false).when(shardingStatement1).isAccumulate();
        doReturn(Lists.newArrayList(statement1, statement2)).when(shardingStatement1).getRoutedStatements();
        assertThat(shardingStatement1.getUpdateCount(), is(10));
        ShardingPreparedStatement shardingStatement2 = spy(new ShardingPreparedStatement(getShardingDataSource().getConnection(), sql));
        doReturn(false).when(shardingStatement2).isAccumulate();
        doReturn(Lists.newArrayList(statement1, statement2)).when(shardingStatement2).getRoutedStatements();
        assertThat(shardingStatement2.getUpdateCount(), is(10));
    }
    
    @Test
    public void assertGetWarnings() throws SQLException {
        for (Statement each : statements.values()) {
            assertNull(each.getWarnings());
        }
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        for (Statement each : statements.values()) {
            each.clearWarnings();
        }
    }
    
    @Test
    public void assertGetMoreResults() throws SQLException {
        for (Statement each : statements.values()) {
            assertFalse(each.getMoreResults());
        }
    }
    
    @Test
    public void assertGetMoreResultsWithCurrent() throws SQLException {
        for (Statement each : statements.values()) {
            assertFalse(each.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        }
    }
    
    @Test
    public void assertGetMaxFieldSizeWithoutRoutedStatements() throws SQLException {
        for (Statement each : statements.values()) {
            assertThat(each.getMaxFieldSize(), is(0));
        }
    }
    
    @Test
    public void assertGetMaxFieldSizeWithRoutedStatements() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery(sql);
            assertTrue(each.getMaxFieldSize() > -1);
        }
    }
    
    @Test
    public void assertSetMaxFieldSize() throws SQLException {
        for (Entry<DatabaseType, Statement> each : statements.entrySet()) {
            each.getValue().executeQuery(sql);
            each.getValue().setMaxFieldSize(10);
            assertThat(each.getValue().getMaxFieldSize(), is(DatabaseTypes.getActualDatabaseType("H2") == each.getKey() ? 0 : 10));
        }
    }
    
    @Test
    public void assertGetMaxRowsWitRoutedStatements() throws SQLException {
        for (Statement each : statements.values()) {
            assertThat(each.getMaxRows(), is(-1));
        }
    }
    
    @Test
    public void assertGetMaxRowsWithoutRoutedStatements() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery(sql);
            assertThat(each.getMaxRows(), is(0));
        }
    }
    
    @Test
    public void assertSetMaxRows() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery(sql);
            each.setMaxRows(10);
            assertThat(each.getMaxRows(), is(10));
        }
    }
    
    @Test
    public void assertGetQueryTimeoutWithoutRoutedStatements() throws SQLException {
        for (Statement each : statements.values()) {
            assertThat(each.getQueryTimeout(), is(0));
        }
    }

    @Test
    public void assertGetQueryTimeoutWithRoutedStatements() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery(sql);
            assertThat(each.getQueryTimeout(), is(0));
        }
    }
    
    @Test
    public void assertSetQueryTimeout() throws SQLException {
        for (Entry<DatabaseType, Statement> each : statements.entrySet()) {
            each.getValue().executeQuery(sql);
            each.getValue().setQueryTimeout(10);
            assertThat(each.getValue().getQueryTimeout(), is(10));
        }
    }
    
    @Test
    public void assertGetGeneratedKeysForSingleRoutedStatement() throws SQLException {
        for (Statement each : statements.values()) {
            each.execute("INSERT INTO t_order_item (user_id, order_id, status) VALUES (1, 1, 'init')", Statement.RETURN_GENERATED_KEYS);
            ResultSet generatedKeysResult = each.getGeneratedKeys();
            assertTrue(generatedKeysResult.next());
            assertTrue(generatedKeysResult.getInt(1) > 0);
        }
    }
    
    @Test
    public void assertGetGeneratedKeysForMultipleRoutedStatement() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery("SELECT user_id AS usr_id FROM t_order WHERE order_id IN (1, 2)");
            assertFalse(each.getGeneratedKeys().next());
        }
    }
}

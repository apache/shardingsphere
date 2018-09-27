/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.adapter;

import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingjdbc.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.shardingjdbc.jdbc.core.statement.ShardingStatement;
import io.shardingsphere.shardingjdbc.jdbc.util.JDBCTestSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class StatementAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private final List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    private final Map<DatabaseType, Statement> statements = new HashMap<>();
    
    private String sql = JDBCTestSQL.SELECT_GROUP_BY_USER_ID_SQL;
    
    @Before
    public void init() {
        ShardingConnection shardingConnection = getShardingDataSource().getConnection();
        shardingConnections.add(shardingConnection);
        statements.put(DatabaseType.H2, shardingConnection.createStatement());
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
            // H2数据库未实现setPoolable方法
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
        AbstractStatementAdapter statement = new AbstractStatementAdapter(Statement.class) {
            
            @Override
            protected Collection<Statement> getRoutedStatements() {
                return Lists.newArrayList(statement1, statement2);
            }
            
            @Override
            public ResultSet executeQuery(final String sql) {
                return null;
            }
            
            @Override
            public ResultSet getResultSet() {
                return null;
            }
            
            @Override
            public int getResultSetConcurrency() {
                return ResultSet.CONCUR_READ_ONLY;
            }
            
            @Override
            public int getResultSetType() {
                return ResultSet.TYPE_FORWARD_ONLY;
            }
            
            @Override
            public Connection getConnection() {
                return null;
            }
            
            @Override
            public ResultSet getGeneratedKeys() {
                return null;
            }
            
            @Override
            public int executeUpdate(final String sql) {
                return 0;
            }
            
            @Override
            public int executeUpdate(final String sql, final int autoGeneratedKeys) {
                return 0;
            }
            
            @Override
            public int executeUpdate(final String sql, final int[] columnIndexes) {
                return 0;
            }
            
            @Override
            public int executeUpdate(final String sql, final String[] columnNames) {
                return 0;
            }
            
            @Override
            public boolean execute(final String sql) {
                return false;
            }
            
            @Override
            public boolean execute(final String sql, final int autoGeneratedKeys) {
                return false;
            }
            
            @Override
            public boolean execute(final String sql, final int[] columnIndexes) {
                return false;
            }
            
            @Override
            public boolean execute(final String sql, final String[] columnNames) {
                return false;
            }
            
            @Override
            public int getResultSetHoldability() {
                return 0;
            }
        };
        assertThat(statement.getUpdateCount(), is(Integer.MAX_VALUE));
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
            assertThat(each.getValue().getMaxFieldSize(), is(DatabaseType.H2 == each.getKey() ? 0 : 10));
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

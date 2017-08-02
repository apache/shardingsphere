/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.common.sql.base.AbstractShardingJDBCDatabaseAndTableTest;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingStatement;
import com.dangdang.ddframe.rdb.sharding.jdbc.util.JDBCTestSQL;
import com.google.common.collect.Lists;
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class StatementAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private List<ShardingConnection> shardingConnections = new ArrayList<>();
    
    private Map<DatabaseType, Statement> statements = new HashMap<>();
    
    private String sql = JDBCTestSQL.SELECT_GROUP_BY_USER_ID_SQL;
    
    @Before
    public void init() throws SQLException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : getShardingDataSources().entrySet()) {
            ShardingConnection shardingConnection = each.getValue().getConnection();
            shardingConnections.add(shardingConnection);
            statements.put(each.getKey(), shardingConnection.createStatement());
        }
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
        for (Map.Entry<DatabaseType, Statement> each : statements.entrySet()) {
            if (DatabaseType.Oracle != each.getKey()) {
                each.getValue().setPoolable(true);
                each.getValue().executeQuery(sql);
                assertPoolable((ShardingStatement) each.getValue(), true, each.getKey());
                each.getValue().setPoolable(false);
                assertPoolable((ShardingStatement) each.getValue(), false, each.getKey());
            }
        }
    }
    
    private void assertPoolable(final ShardingStatement actual, final boolean poolable, final DatabaseType type) throws SQLException {
        assertThat(actual.isPoolable(), is(poolable));
        assertThat(actual.getRoutedStatements().size(), is(4));
        for (Statement each : actual.getRoutedStatements()) {
            // H2数据库未实现setPoolable方法
            if (DatabaseType.H2 == type) {
                assertFalse(each.isPoolable());
            } else {
                assertThat(each.isPoolable(), is(poolable));
            }
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
    public void assertSetCursorName() throws SQLException {
        for (Map.Entry<DatabaseType, Statement> each : statements.entrySet()) {
            if (DatabaseType.Oracle != each.getKey()) {
                each.getValue().setCursorName("cursorName");
                each.getValue().executeQuery(sql);
                each.getValue().setCursorName("cursorName");
            }
        }
    }
    
    @Test
    public void assertGetUpdateCount() throws SQLException {
        String sql = "DELETE FROM t_order WHERE status = 'init'";
        for (Map.Entry<DatabaseType, Statement> each : statements.entrySet()) {
            each.getValue().execute(sql);
            if (DatabaseType.Oracle == each.getKey()) {
                assertThat(each.getValue().getUpdateCount(), is(-10));
            } else {
                assertThat(each.getValue().getUpdateCount(), is(4));
            }
        }
    }
    
    @Test
    public void assertGetUpdateCountNoData() throws SQLException {
        String sql = "DELETE FROM t_order WHERE status = 'none'";
        for (Map.Entry<DatabaseType, Statement> each : statements.entrySet()) {
            each.getValue().execute(sql);
            if (DatabaseType.Oracle == each.getKey()) {
                assertThat(each.getValue().getUpdateCount(), is(-10));
            } else {
                assertThat(each.getValue().getUpdateCount(), is(0));
            }
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
        final Statement st1 = Mockito.mock(Statement.class);
        when(st1.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        final Statement st2 = Mockito.mock(Statement.class);
        when(st2.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        AbstractStatementAdapter statement = new AbstractStatementAdapter(Statement.class) {
            
            @Override
            protected Collection<? extends Statement> getRoutedStatements() {
                return Lists.newArrayList(st1, st2);
            }
            
            @Override
            public ResultSet executeQuery(final String sql) throws SQLException {
                return null;
            }
            
            @Override
            public ResultSet getResultSet() throws SQLException {
                return null;
            }
            
            @Override
            public int getResultSetConcurrency() throws SQLException {
                return ResultSet.CONCUR_READ_ONLY;
            }
            
            @Override
            public int getResultSetType() throws SQLException {
                return ResultSet.TYPE_FORWARD_ONLY;
            }
            
            @Override
            public Connection getConnection() throws SQLException {
                return null;
            }
            
            @Override
            public ResultSet getGeneratedKeys() throws SQLException {
                return null;
            }
            
            @Override
            public int executeUpdate(final String sql) throws SQLException {
                return 0;
            }
            
            @Override
            public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
                return 0;
            }
            
            @Override
            public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
                return 0;
            }
            
            @Override
            public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
                return 0;
            }
            
            @Override
            public boolean execute(final String sql) throws SQLException {
                return false;
            }
            
            @Override
            public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
                return false;
            }
            
            @Override
            public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
                return false;
            }
            
            @Override
            public boolean execute(final String sql, final String[] columnNames) throws SQLException {
                return false;
            }
            
            @Override
            public int getResultSetHoldability() throws SQLException {
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
        for (Map.Entry<DatabaseType, Statement> each : statements.entrySet()) {
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
        for (Map.Entry<DatabaseType, Statement> each : statements.entrySet()) {
            if (DatabaseType.PostgreSQL != each.getKey()) {
                each.getValue().executeQuery(sql);
                each.getValue().setQueryTimeout(10);
                assertThat(each.getValue().getQueryTimeout(), is(10));
            }
        }
    }
    
    @Test
    public void assertGetGeneratedKeysForSingleRoutedStatement() throws SQLException {
        for (Statement each : statements.values()) {
            each.execute("INSERT INTO t_order (user_id, order_id, status) VALUES (1, 1, 'init')", Statement.RETURN_GENERATED_KEYS);
//            ResultSet generatedKeysResult = each.getGeneratedKeys();
//            assertTrue(generatedKeysResult.next());
//            assertTrue(generatedKeysResult.getInt(1) > 0);
        }
    }
    
    @Test
    public void assertGetGeneratedKeysForMultipleRoutedStatement() throws SQLException {
        for (Statement each : statements.values()) {
            each.executeQuery("SELECT user_id AS uid FROM t_order WHERE order_id IN (1, 2)");
            assertFalse(each.getGeneratedKeys().next());
        }
    }
}

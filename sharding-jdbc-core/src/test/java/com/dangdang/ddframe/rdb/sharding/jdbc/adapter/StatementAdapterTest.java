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

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingStatement;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class StatementAdapterTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingConnection shardingConnection;
    
    private Statement actual;
    
    @Before
    public void init() throws SQLException {
        shardingConnection = getShardingDataSource().getConnection();
        shardingConnection.setReadOnly(false);
        actual = shardingConnection.createStatement();
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        shardingConnection.close();
    }
    
    @Test
    public void assertClose() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        actual.close();
        assertTrue(actual.isClosed());
        assertTrue(((ShardingStatement) actual).getRoutedStatements().isEmpty());
    }
    
    @Test
    public void assertSetPoolable() throws SQLException {
        actual.setPoolable(true);
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        assertPoolable((ShardingStatement) actual, true);
        actual.setPoolable(false);
        assertPoolable((ShardingStatement) actual, false);
    }
    
    private void assertPoolable(final ShardingStatement actual, final boolean poolable) throws SQLException {
        assertThat(actual.isPoolable(), is(poolable));
        assertThat(actual.getRoutedStatements().size(), is(10));
        for (Statement each : actual.getRoutedStatements()) {
            // H2数据库未实现setPoolable方法
            if (DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE) {
                assertFalse(each.isPoolable());
            } else {
                assertThat(each.isPoolable(), is(poolable));
            }
        }
    }
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        actual.setFetchSize(10);
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        assertFetchSize((ShardingStatement) actual, 10);
        actual.setFetchSize(100);
        assertFetchSize((ShardingStatement) actual, 100);
    }
    
    private void assertFetchSize(final ShardingStatement actual, final int fetchSize) throws SQLException {
        assertThat(actual.getFetchSize(), is(fetchSize));
        assertThat(actual.getRoutedStatements().size(), is(10));
        for (Statement each : actual.getRoutedStatements()) {
            assertThat(each.getFetchSize(), is(fetchSize));
        }
    }
    
    @Test
    public void assertSetEscapeProcessing() throws SQLException {
        actual.setEscapeProcessing(true);
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        actual.setEscapeProcessing(false);
    }
    
    @Test
    public void assertCancel() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        actual.cancel();
    }
    
    @Test
    public void assertSetCursorName() throws SQLException {
        actual.setCursorName("cursorName");
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        actual.setCursorName("cursorName");
    }
    
    @Test
    public void assertGetUpdateCount() throws SQLException {
        actual.execute("DELETE FROM `t_order` WHERE `status` = 'init'");
        assertThat(actual.getUpdateCount(), is(40));
    }
    
    @Test
    public void assertGetUpdateCountNoData() throws SQLException {
        actual.execute("DELETE FROM `t_order` WHERE `status` = 'none'");
        assertThat(actual.getUpdateCount(), is(0));
    }
    
    @Test
    public void assertGetUpdateCountSelect() throws SQLException {
        actual.execute("SELECT * FROM `t_order`");
        assertThat(actual.getUpdateCount(), is(-1));
    }
    
    @Test
    public void assertOverMaxUpdateRow() throws SQLException {
        final Statement st1 = Mockito.mock(Statement.class);
        when(st1.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        final Statement st2 = Mockito.mock(Statement.class);
        when(st2.getUpdateCount()).thenReturn(Integer.MAX_VALUE);
        AbstractStatementAdapter statement = new AbstractStatementAdapter(Statement.class) {
            
            @Override
            protected void clearRouteStatements() {
            }
            
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
                return 0;
            }
            
            @Override
            public int getResultSetType() throws SQLException {
                return 0;
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
        assertNull(actual.getWarnings());
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        actual.clearWarnings();
    }
    
    @Test
    public void assertGetMoreResults() throws SQLException {
        assertFalse(actual.getMoreResults());
    }
    
    @Test
    public void assertGetMoreResultsWithCurrent() throws SQLException {
        assertFalse(actual.getMoreResults(Statement.KEEP_CURRENT_RESULT));
    }
    
    @Test
    public void assertGetMaxFieldSizeWithoutRoutedStatements() throws SQLException {
        assertThat(actual.getMaxFieldSize(), is(0));
    }
    
    @Test
    public void assertGetMaxFieldSizeWithRoutedStatements() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        assertTrue(actual.getMaxFieldSize() > -1);
    }
    
    @Test
    public void assertSetMaxFieldSize() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        actual.setMaxFieldSize(10);
        assertThat(actual.getMaxFieldSize(), is(DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE ? 0 : 10));
    }
    
    @Test
    public void assertGetMaxRowsWitRoutedStatements() throws SQLException {
        assertThat(actual.getMaxRows(), is(-1));
    }
    
    @Test
    public void assertGetMaxRowsWithoutRoutedStatements() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        assertThat(actual.getMaxRows(), is(0));
    }
    
    @Test
    public void assertSetMaxRows() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        actual.setMaxRows(10);
        assertThat(actual.getMaxRows(), is(10));
    }
    
    @Test
    public void assertGetQueryTimeoutWithoutRoutedStatements() throws SQLException {
        assertThat(actual.getQueryTimeout(), is(0));
    }
    
    @Test
    public void assertGetQueryTimeoutWithRoutedStatements() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        assertThat(actual.getQueryTimeout(), is(0));
    }
    
    @Test
    public void assertSetQueryTimeout() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
        actual.setQueryTimeout(10);
        assertThat(actual.getQueryTimeout(), is(10));
    }
    
    @Test
    public void assertGetGeneratedKeysForSingleRoutedStatement() throws SQLException {
        actual.executeUpdate("INSERT INTO `t_order` (`user_id`, `status`) VALUES (1, 'init')", Statement.RETURN_GENERATED_KEYS);
        ResultSet generatedKeysResult = actual.getGeneratedKeys();
        assertTrue(generatedKeysResult.next());
        assertTrue(generatedKeysResult.getInt(1) > 0);
    }
    
    @Test
    public void assertGetGeneratedKeysForMultipleRoutedStatement() throws SQLException {
        actual.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `order_id` IN (1, 2)");
        assertFalse(actual.getGeneratedKeys().next());
    }
}

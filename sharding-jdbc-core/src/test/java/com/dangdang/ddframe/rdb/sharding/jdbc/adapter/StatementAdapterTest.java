/**
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingStatement;

public final class StatementAdapterTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    private ShardingConnection shardingConnection;
    
    private Statement actual;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
        shardingConnection = shardingDataSource.getConnection();
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
}

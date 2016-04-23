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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.AbstractShardingResultSet;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingConnection;

public final class ResultSetAdapterTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    private ShardingConnection shardingConnection;
    
    private Statement statement;
    
    private ResultSet actual;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
        shardingConnection = shardingDataSource.getConnection();
        statement = shardingConnection.createStatement();
        actual = statement.executeQuery("SELECT user_id AS `uid` FROM `t_order` WHERE `status` = 'init'");
    }
    
    @After
    public void close() throws SQLException {
        actual.close();
        statement.close();
        shardingConnection.close();
    }
    
    @Test
    public void assertClose() throws SQLException {
        actual.close();
        assertClose((AbstractShardingResultSet) actual);
    }
    
    private void assertClose(final AbstractShardingResultSet actual) throws SQLException {
        assertTrue(actual.isClosed());
        assertThat(actual.getResultSets().size(), is(10));
        for (ResultSet each : actual.getResultSets()) {
            assertTrue(each.isClosed());
        }
    }
    
    @Test
    public void assertWasNull() throws SQLException {
        assertFalse(actual.wasNull());
    }
    
    @Test
    public void assertSetFetchDirection() throws SQLException {
        assertThat(actual.getFetchDirection(), is(ResultSet.FETCH_FORWARD));
        try {
            actual.setFetchDirection(ResultSet.FETCH_REVERSE);
        } catch (final SQLException ignore) {
        }
        assertFetchDirection((AbstractShardingResultSet) actual, ResultSet.FETCH_REVERSE);
    }
    
    private void assertFetchDirection(final AbstractShardingResultSet actual, final int fetchDirection) throws SQLException {
        // H2数据库未实现getFetchDirection方法
        assertThat(actual.getFetchDirection(), is(DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE ? ResultSet.FETCH_FORWARD : fetchDirection));
        assertThat(actual.getResultSets().size(), is(10));
        for (ResultSet each : actual.getResultSets()) {
            assertThat(each.getFetchDirection(), is(DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE ? ResultSet.FETCH_FORWARD : fetchDirection));
        }
    }
    
    @Test
    public void assertSetFetchSize() throws SQLException {
        assertThat(actual.getFetchSize(), is(0));
        actual.setFetchSize(100);
        assertFetchSize((AbstractShardingResultSet) actual, 100);
    }
    
    private void assertFetchSize(final AbstractShardingResultSet actual, final int fetchSize) throws SQLException {
        // H2数据库未实现getFetchSize方法
        assertThat(actual.getFetchSize(), is(DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE ? 0 : fetchSize));
        assertThat(actual.getResultSets().size(), is(10));
        for (ResultSet each : actual.getResultSets()) {
            assertThat(each.getFetchSize(), is(DatabaseType.H2 == AbstractDBUnitTest.CURRENT_DB_TYPE ? 0 : fetchSize));
        }
    }
    
    @Test
    public void assertGetType() throws SQLException {
        assertThat(actual.getType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertGetConcurrency() throws SQLException {
        assertThat(actual.getConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void assertGetStatement() throws SQLException {
        assertNotNull(actual.getStatement());
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        assertNull(actual.getWarnings());
        actual.clearWarnings();
        assertNull(actual.getWarnings());
    }
    
    @Test
    public void assertGetMetaData() throws SQLException {
        assertNotNull(actual.getMetaData());
    }
    
    @Test
    public void assertFindColumn() throws SQLException {
        assertThat(actual.findColumn("uid"), is(1));
    }
}

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
import com.dangdang.ddframe.rdb.sharding.jdbc.util.JDBCTestSQL;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConnectionAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private Map<DatabaseType, ShardingDataSource> shardingDataSources;
    
    private String sql = JDBCTestSQL.SELECT_GROUP_BY_USER_ID_SQL;
    
    @Before
    public void init() throws SQLException {
        shardingDataSources = getShardingDataSources();
    }
    
    @Test
    public void assertSetAutoCommit() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                assertTrue(actual.getAutoCommit());
                actual.setAutoCommit(false);
                actual.createStatement().executeQuery(sql);
                assertAutoCommit(actual, false);
                actual.setAutoCommit(true);
                assertAutoCommit(actual, true);
            }
        }
    }
    
    private void assertAutoCommit(final ShardingConnection actual, final boolean autoCommit) throws SQLException {
        assertThat(actual.getAutoCommit(), is(autoCommit));
        assertThat(actual.getConnections().size(), is(2));
        for (Connection each : actual.getConnections()) {
            assertThat(each.getAutoCommit(), is(autoCommit));
        }
    }
    
    @Test
    // TODO 缺少断言，做柔性事务时补充
    public void assertCommit() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                actual.setAutoCommit(false);
                actual.createStatement().executeQuery(sql);
                actual.commit();
            }
        }
    }
    
    @Test
    // TODO 缺少断言，做柔性事务时补充
    public void assertRollback() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                actual.setAutoCommit(false);
                actual.createStatement().executeQuery(sql);
                actual.rollback();
            }
        }
    }
    
    @Test
    public void assertClose() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                actual.createStatement().executeQuery(sql);
                assertClose(actual, false);
                actual.close();
                assertClose(actual, true);
            }
        }
    }
    
    private void assertClose(final ShardingConnection actual, final boolean closed) throws SQLException {
        assertThat(actual.isClosed(), is(closed));
        assertThat(actual.getConnections().size(), is(2));
        for (Connection each : actual.getConnections()) {
            assertThat(each.isClosed(), is(closed));
        }
    }
    
    @Test
    public void assertSetReadOnly() throws SQLException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            try (ShardingConnection actual = each.getValue().getConnection()) {
                assertTrue(actual.isReadOnly());
                actual.setReadOnly(false);
                actual.createStatement().executeQuery(sql);
                assertReadOnly(actual, false, each.getKey());
                if (DatabaseType.SQLServer != each.getKey()) {
                    actual.setReadOnly(true);
                    assertReadOnly(actual, true, each.getKey());
                }
            }
        }
    }
    
    private void assertReadOnly(final ShardingConnection actual, final boolean readOnly, final DatabaseType type) throws SQLException {
        assertThat(actual.isReadOnly(), is(readOnly));
        assertThat(actual.getConnections().size(), is(2));
        for (Connection each : actual.getConnections()) {
            // H2数据库未实现setReadOnly方法
            if (DatabaseType.H2 == type) {
                assertFalse(each.isReadOnly());
            } else {
                assertThat(each.isReadOnly(), is(readOnly));
            }
        }
    }
    
    @Test
    public void assertSetTransactionIsolation() throws SQLException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            try (ShardingConnection actual = each.getValue().getConnection()) {
                assertThat(actual.getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
                actual.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                actual.createStatement().executeQuery(sql);
                assertTransactionIsolation(actual, Connection.TRANSACTION_SERIALIZABLE);
                if (DatabaseType.Oracle != each.getKey()) {
                    actual.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    assertTransactionIsolation(actual, Connection.TRANSACTION_READ_COMMITTED);
                }
            }
        }
    }
    
    private void assertTransactionIsolation(final ShardingConnection actual, final int transactionIsolation) throws SQLException {
        assertThat(actual.getTransactionIsolation(), is(transactionIsolation));
        assertThat(actual.getConnections().size(), is(2));
        for (Connection each : actual.getConnections()) {
            assertThat(each.getTransactionIsolation(), is(transactionIsolation));
        }
    }
    
    @Test
    public void assertGetWarnings() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                assertNull(actual.getWarnings());
            }
        }
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                actual.clearWarnings();
            }
        }
    }
    
    @Test
    public void assertGetHoldability() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
            }
        }
    }
    
    @Test
    public void assertSetHoldability() throws SQLException {
        for (ShardingDataSource each : shardingDataSources.values()) {
            try (ShardingConnection actual = each.getConnection()) {
                actual.setHoldability(ResultSet.CONCUR_READ_ONLY);
                assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
            }
        }
    }
}

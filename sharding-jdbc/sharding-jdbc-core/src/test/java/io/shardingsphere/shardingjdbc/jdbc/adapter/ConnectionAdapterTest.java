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

import com.google.common.collect.Multimap;
import io.shardingsphere.shardingjdbc.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.shardingjdbc.jdbc.util.JDBCTestSQL;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConnectionAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private String sql = JDBCTestSQL.SELECT_GROUP_BY_USER_ID_SQL;
    
    @Test
    public void assertSetAutoCommit() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            assertTrue(actual.getAutoCommit());
            actual.setAutoCommit(false);
            actual.createStatement().executeQuery(sql);
            assertAutoCommit(actual, false);
        }
    }
    
    private void assertAutoCommit(final ShardingConnection actual, final boolean autoCommit) throws SQLException {
        assertThat(actual.getAutoCommit(), is(autoCommit));
        Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
        assertThat(cachedConnections.size(), is(2));
        for (Connection each : cachedConnections.values()) {
            assertThat(each.getAutoCommit(), is(autoCommit));
        }
    }
    
    @Test
    // TODO 缺少断言，做柔性事务时补充
    public void assertCommit() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            actual.setAutoCommit(false);
            actual.createStatement().executeQuery(sql);
            actual.commit();
        }
    }
    
    @Test
    // TODO 缺少断言，做柔性事务时补充
    public void assertRollback() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            actual.setAutoCommit(false);
            actual.createStatement().executeQuery(sql);
            actual.rollback();
        }
    }
    
    @Test
    public void assertClose() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            actual.createStatement().executeQuery(sql);
            assertClose(actual, false);
            actual.close();
            assertClose(actual, true);
        }
    }
    
    private void assertClose(final ShardingConnection actual, final boolean closed) throws SQLException {
        assertThat(actual.isClosed(), is(closed));
        Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
        assertThat(cachedConnections.size(), is(2));
        for (Connection each : cachedConnections.values()) {
            assertThat(each.isClosed(), is(closed));
        }
    }
    
    @Test
    public void assertSetReadOnly() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            assertTrue(actual.isReadOnly());
            actual.setReadOnly(false);
            actual.createStatement().executeQuery(sql);
            assertReadOnly(actual, false);
            actual.setReadOnly(true);
            assertReadOnly(actual, true);
        }
    }
    
    private void assertReadOnly(final ShardingConnection actual, final boolean readOnly) throws SQLException {
        assertThat(actual.isReadOnly(), is(readOnly));
        Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
        assertThat(cachedConnections.size(), is(2));
        for (Connection each : cachedConnections.values()) {
            assertThat(each.isReadOnly(), is(readOnly));
        }
    }
    
    @Test
    public void assertGetTransactionIsolation() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            actual.createStatement().executeQuery(sql);
            assertThat(actual.getTransactionIsolation(), is(Connection.TRANSACTION_READ_COMMITTED));
        }
    }
    
    @Test
    public void assertSetTransactionIsolation() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            assertThat(actual.getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
            actual.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            actual.createStatement().executeQuery(sql);
            assertTransactionIsolation(actual, Connection.TRANSACTION_SERIALIZABLE);
            actual.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            assertTransactionIsolation(actual, Connection.TRANSACTION_READ_COMMITTED);
        }
    }
    
    private void assertTransactionIsolation(final ShardingConnection actual, final int transactionIsolation) throws SQLException {
        assertThat(actual.getTransactionIsolation(), is(transactionIsolation));
        Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
        assertThat(cachedConnections.size(), is(2));
        for (Connection each : cachedConnections.values()) {
            assertThat(each.getTransactionIsolation(), is(transactionIsolation));
        }
    }
    
    @Test
    public void assertGetWarnings() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            assertNull(actual.getWarnings());
        }
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            actual.clearWarnings();
        }
    }
    
    @Test
    public void assertGetHoldability() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
        }
    }
    
    @Test
    public void assertSetHoldability() throws SQLException {
        try (ShardingConnection actual = getShardingDataSource().getConnection()) {
            actual.setHoldability(ResultSet.CONCUR_READ_ONLY);
            assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private Multimap<String, Connection> getCachedConnections(final AbstractConnectionAdapter connectionAdapter) {
        Field field = AbstractConnectionAdapter.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        return (Multimap<String, Connection>) field.get(connectionAdapter);
    }
}

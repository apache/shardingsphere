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

package org.apache.shardingsphere.driver.jdbc.adapter;

import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForShardingTest;
import org.apache.shardingsphere.driver.jdbc.core.fixture.BASEShardingTransactionManagerFixture;
import org.apache.shardingsphere.driver.jdbc.core.fixture.XAShardingTransactionManagerFixture;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConnectionAdapterTest extends AbstractShardingSphereDataSourceForShardingTest {
    
    private static final String SQL = "SELECT 1";
    
    @After
    public void tearDown() {
        TransactionTypeHolder.clear();
        XAShardingTransactionManagerFixture.getINVOCATIONS().clear();
        BASEShardingTransactionManagerFixture.getINVOCATIONS().clear();
    }
    
    @Test
    public void assertLocalTransactionAutoCommit() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            assertTrue(actual.getAutoCommit());
            actual.createStatement().executeQuery(SQL);
            actual.setAutoCommit(false);
            assertFalse(actual.getAutoCommit());
            Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
            assertThat(cachedConnections.size(), is(1));
            for (Connection each : cachedConnections.values()) {
                assertFalse(each.getAutoCommit());
            }
        }
    }
    
    @Test
    public void assertShardingTransactionAutoCommit() throws SQLException {
        TransactionTypeHolder.set(TransactionType.XA);
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.createStatement().executeQuery(SQL);
            actual.setAutoCommit(false);
            actual.createStatement().executeQuery(SQL);
            assertTrue(actual.getShardingTransactionManager().isInTransaction());
            Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
            assertThat(cachedConnections.size(), is(1));
            for (Connection each : cachedConnections.values()) {
                assertTrue(each.getAutoCommit());
            }
        } finally {
            TransactionTypeHolder.clear();
        }
    }
    
    @Test
    public void assertShardingTransactionSkipAutoCommit() throws SQLException {
        TransactionTypeHolder.set(TransactionType.XA);
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.setAutoCommit(true);
            assertFalse(actual.getShardingTransactionManager().isInTransaction());
        } finally {
            TransactionTypeHolder.clear();
        }
        TransactionTypeHolder.set(TransactionType.XA);
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.setAutoCommit(false);
            assertTrue(actual.getShardingTransactionManager().isInTransaction());
            assertThat(XAShardingTransactionManagerFixture.getINVOCATIONS().size(), is(1));
            actual.setAutoCommit(false);
            assertThat(XAShardingTransactionManagerFixture.getINVOCATIONS().size(), is(1));
        } finally {
            TransactionTypeHolder.clear();
        }
    }
    
    @Test
    public void assertLocalTransactionCommit() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.setAutoCommit(false);
            actual.createStatement().executeQuery(SQL);
            actual.commit();
        }
    }
    
    @Test
    public void assertShardingTransactionCommit() throws SQLException {
        TransactionTypeHolder.set(TransactionType.XA);
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.commit();
            assertTrue(XAShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.COMMIT));
        } finally {
            TransactionTypeHolder.clear();
        }
    }
    
    @Test
    public void assertShardingTransactionForceCommit() throws SQLException {
        TransactionTypeHolder.set(TransactionType.XA);
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.setAutoCommit(false);
            actual.setAutoCommit(true);
            assertTrue(XAShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.COMMIT));
        } finally {
            TransactionTypeHolder.clear();
        }
    }
    
    @Test
    public void assertLocalTransactionRollback() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.setAutoCommit(false);
            actual.createStatement().executeQuery(SQL);
            actual.rollback();
        }
    }
    
    @Test
    public void assertShardingTransactionRollback() throws SQLException {
        TransactionTypeHolder.set(TransactionType.XA);
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.rollback();
            assertTrue(XAShardingTransactionManagerFixture.getINVOCATIONS().contains(TransactionOperationType.ROLLBACK));
        } finally {
            TransactionTypeHolder.clear();
        }
    }
    
    @Test
    public void assertClose() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.createStatement().executeQuery(SQL);
            actual.close();
            assertClose(actual);
        }
    }
    
    private void assertClose(final ShardingSphereConnection actual) {
        assertTrue(actual.isClosed());
        Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
        assertTrue(cachedConnections.isEmpty());
    }
    
    @Test
    public void assertCloseShouldNotClearTransactionType() throws SQLException {
        TransactionTypeHolder.set(TransactionType.XA);
        TransactionType transactionType = TransactionTypeHolder.get();
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.createStatement().executeQuery(SQL);
        }
        assertThat(TransactionTypeHolder.get(), is(transactionType));
    }
    
    @Test
    public void assertSetReadOnly() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            assertFalse(actual.isReadOnly());
            actual.setReadOnly(true);
            actual.createStatement().executeQuery(SQL);
            assertReadOnly(actual, true);
            actual.setReadOnly(false);
            assertReadOnly(actual, false);
        }
    }
    
    private void assertReadOnly(final ShardingSphereConnection actual, final boolean readOnly) throws SQLException {
        assertThat(actual.isReadOnly(), is(readOnly));
        Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
        assertThat(cachedConnections.size(), is(1));
        for (Connection each : cachedConnections.values()) {
            assertThat(each.isReadOnly(), is(readOnly));
        }
    }
    
    @Test
    public void assertGetTransactionIsolation() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.createStatement().executeQuery(SQL);
            assertThat(actual.getTransactionIsolation(), is(Connection.TRANSACTION_READ_COMMITTED));
        }
    }
    
    @Test
    public void assertSetTransactionIsolation() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            assertThat(actual.getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
            actual.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            actual.createStatement().executeQuery(SQL);
            assertTransactionIsolation(actual, Connection.TRANSACTION_SERIALIZABLE);
            actual.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            assertTransactionIsolation(actual, Connection.TRANSACTION_READ_COMMITTED);
        }
    }
    
    private void assertTransactionIsolation(final ShardingSphereConnection actual, final int transactionIsolation) throws SQLException {
        assertThat(actual.getTransactionIsolation(), is(transactionIsolation));
        Multimap<String, Connection> cachedConnections = getCachedConnections(actual);
        assertThat(cachedConnections.size(), is(1));
        for (Connection each : cachedConnections.values()) {
            assertThat(each.getTransactionIsolation(), is(transactionIsolation));
        }
    }
    
    @Test
    public void assertGetWarnings() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            assertNull(actual.getWarnings());
        }
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.clearWarnings();
        }
    }
    
    @Test
    public void assertGetHoldability() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
        }
    }
    
    @Test
    public void assertSetHoldability() throws SQLException {
        try (ShardingSphereConnection actual = getShardingSphereDataSource().getConnection()) {
            actual.setHoldability(ResultSet.CONCUR_READ_ONLY);
            assertThat(actual.getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
        }
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Multimap<String, Connection> getCachedConnections(final AbstractConnectionAdapter connectionAdapter) {
        Field field = AbstractConnectionAdapter.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        return (Multimap<String, Connection>) field.get(connectionAdapter);
    }
}

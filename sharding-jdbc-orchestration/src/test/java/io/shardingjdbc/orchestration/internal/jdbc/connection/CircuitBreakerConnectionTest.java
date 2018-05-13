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

package io.shardingjdbc.orchestration.internal.jdbc.connection;

import io.shardingjdbc.orchestration.internal.jdbc.metadata.CircuitBreakerDatabaseMetaData;
import io.shardingjdbc.orchestration.internal.jdbc.statement.CircuitBreakerPreparedStatement;
import io.shardingjdbc.orchestration.internal.jdbc.statement.CircuitBreakerStatement;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CircuitBreakerConnectionTest {
    
    private CircuitBreakerConnection connection = new CircuitBreakerConnection();
    
    private final String sql = "select 1";
    
    @Test
    public void assertGetMetaData() throws SQLException {
        assertTrue(connection.getMetaData() instanceof CircuitBreakerDatabaseMetaData);
    }
    
    @Test
    public void setReadOnly() throws SQLException {
        connection.setReadOnly(true);
        assertFalse(connection.isReadOnly());
    }
    
    @Test
    public void assertIsReadOnly() throws SQLException {
        assertFalse(connection.isReadOnly());
    }
    
    @Test
    public void assertSetTransactionIsolation() throws SQLException {
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetTransactionIsolation() throws SQLException {
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetWarnings() throws SQLException {
        assertNull(connection.getWarnings());
    }
    
    @Test
    public void assertClearWarnings() throws SQLException {
        connection.clearWarnings();
    }
    
    @Test
    public void assertSetAutoCommit() throws SQLException {
        connection.setAutoCommit(true);
        assertFalse(connection.getAutoCommit());
    }
    
    @Test
    public void assertGetAutoCommit() throws SQLException {
        assertFalse(connection.getAutoCommit());
    }
    
    @Test
    public void assertCommit() throws SQLException {
        connection.commit();
    }
    
    @Test
    public void assertRollback() throws SQLException {
        connection.rollback();
    }
    
    @Test
    public void assertSetHoldability() throws SQLException {
        connection.setHoldability(-1);
        assertThat(connection.getHoldability(), is(0));
    }
    
    @Test
    public void assertGetHoldability() throws SQLException {
        assertThat(connection.getHoldability(), is(0));
    }
    
    @Test
    public void assertPrepareStatement() throws SQLException {
        assertTrue(connection.prepareStatement(sql) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, 0, 0) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, 0, 0, 0) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, 0) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, new int[]{0}) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, new String[]{""}) instanceof CircuitBreakerPreparedStatement);
    }
    
    @Test
    public void assertCreateStatement() throws SQLException {
        assertTrue(connection.createStatement() instanceof CircuitBreakerStatement);
        assertTrue(connection.createStatement(0, 0) instanceof CircuitBreakerStatement);
        assertTrue(connection.createStatement(0, 0, 0) instanceof CircuitBreakerStatement);
    }
    
    @Test
    public void assertClose() throws SQLException {
        connection.close();
    }
    
    @Test
    public void assertIsClosed() throws SQLException {
        assertFalse(connection.isClosed());
    }
}

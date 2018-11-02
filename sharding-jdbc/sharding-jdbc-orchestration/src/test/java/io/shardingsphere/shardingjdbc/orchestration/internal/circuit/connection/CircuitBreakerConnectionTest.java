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

package io.shardingsphere.shardingjdbc.orchestration.internal.circuit.connection;

import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.metadata.CircuitBreakerDatabaseMetaData;
import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.statement.CircuitBreakerPreparedStatement;
import io.shardingsphere.shardingjdbc.orchestration.internal.circuit.statement.CircuitBreakerStatement;
import org.junit.Test;

import java.sql.Connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CircuitBreakerConnectionTest {
    
    private final CircuitBreakerConnection connection = new CircuitBreakerConnection();
    
    private final String sql = "select 1";
    
    @Test
    public void assertGetMetaData() {
        assertTrue(connection.getMetaData() instanceof CircuitBreakerDatabaseMetaData);
    }
    
    @Test
    public void setReadOnly() {
        connection.setReadOnly(true);
        assertFalse(connection.isReadOnly());
    }
    
    @Test
    public void assertIsReadOnly() {
        assertFalse(connection.isReadOnly());
    }
    
    @Test
    public void assertSetTransactionIsolation() {
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetTransactionIsolation() {
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetWarnings() {
        assertNull(connection.getWarnings());
    }
    
    @Test
    public void assertClearWarnings() {
        connection.clearWarnings();
    }
    
    @Test
    public void assertSetAutoCommit() {
        connection.setAutoCommit(true);
        assertFalse(connection.getAutoCommit());
    }
    
    @Test
    public void assertGetAutoCommit() {
        assertFalse(connection.getAutoCommit());
    }
    
    @Test
    public void assertCommit() {
        connection.commit();
    }
    
    @Test
    public void assertRollback() {
        connection.rollback();
    }
    
    @Test
    public void assertSetHoldability() {
        connection.setHoldability(-1);
        assertThat(connection.getHoldability(), is(0));
    }
    
    @Test
    public void assertGetHoldability() {
        assertThat(connection.getHoldability(), is(0));
    }
    
    @Test
    public void assertPrepareStatement() {
        assertTrue(connection.prepareStatement(sql) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, 0, 0) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, 0, 0, 0) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, 0) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, new int[]{0}) instanceof CircuitBreakerPreparedStatement);
        assertTrue(connection.prepareStatement(sql, new String[]{""}) instanceof CircuitBreakerPreparedStatement);
    }
    
    @Test
    public void assertCreateStatement() {
        assertTrue(connection.createStatement() instanceof CircuitBreakerStatement);
        assertTrue(connection.createStatement(0, 0) instanceof CircuitBreakerStatement);
        assertTrue(connection.createStatement(0, 0, 0) instanceof CircuitBreakerStatement);
    }
    
    @Test
    public void assertClose() {
        connection.close();
    }
    
    @Test
    public void assertIsClosed() {
        assertFalse(connection.isClosed());
    }
}

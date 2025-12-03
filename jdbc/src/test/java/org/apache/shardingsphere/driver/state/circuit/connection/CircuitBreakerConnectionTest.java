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

package org.apache.shardingsphere.driver.state.circuit.connection;

import org.apache.shardingsphere.driver.state.circuit.metadata.CircuitBreakerDatabaseMetaData;
import org.apache.shardingsphere.driver.state.circuit.statement.CircuitBreakerPreparedStatement;
import org.apache.shardingsphere.driver.state.circuit.statement.CircuitBreakerStatement;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CircuitBreakerConnectionTest {
    
    private final CircuitBreakerConnection connection = new CircuitBreakerConnection();
    
    @Test
    void assertGetMetaData() {
        assertThat(connection.getMetaData(), isA(CircuitBreakerDatabaseMetaData.class));
    }
    
    @Test
    void assertSetReadOnly() {
        connection.setReadOnly(true);
        assertFalse(connection.isReadOnly());
    }
    
    @Test
    void assertIsReadOnly() {
        assertFalse(connection.isReadOnly());
    }
    
    @Test
    void assertSetCatalog() {
        connection.setCatalog("foo_catalog");
        assertThat(connection.getCatalog(), is(""));
    }
    
    @Test
    void assertSetSchema() {
        connection.setSchema("foo_schema");
        assertThat(connection.getSchema(), is(""));
    }
    
    @Test
    void assertSetTransactionIsolation() {
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_NONE));
    }
    
    @Test
    void assertGetTransactionIsolation() {
        assertThat(connection.getTransactionIsolation(), is(Connection.TRANSACTION_NONE));
    }
    
    @Test
    void assertGetWarnings() {
        assertNull(connection.getWarnings());
    }
    
    @Test
    void assertClearWarnings() {
        assertDoesNotThrow(connection::clearWarnings);
    }
    
    @Test
    void assertSetAutoCommit() {
        connection.setAutoCommit(true);
        assertFalse(connection.getAutoCommit());
    }
    
    @Test
    void assertGetAutoCommit() {
        assertFalse(connection.getAutoCommit());
    }
    
    @Test
    void assertCommit() {
        assertDoesNotThrow(connection::commit);
    }
    
    @Test
    void assertRollback() {
        assertDoesNotThrow(() -> connection.rollback());
    }
    
    @Test
    void assertRollbackWithSavepoint() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> connection.rollback(mock(Savepoint.class)));
    }
    
    @Test
    void assertSetSavepoint() {
        assertThrows(SQLFeatureNotSupportedException.class, connection::setSavepoint);
    }
    
    @Test
    void assertSetSavepointWithName() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> connection.setSavepoint("savepoint_name"));
    }
    
    @Test
    void assertReleaseSavepoint() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> connection.releaseSavepoint(mock(Savepoint.class)));
    }
    
    @Test
    void assertSetHoldability() {
        connection.setHoldability(-1);
        assertThat(connection.getHoldability(), is(0));
    }
    
    @Test
    void assertGetHoldability() {
        assertThat(connection.getHoldability(), is(0));
    }
    
    @Test
    void assertPrepareStatement() {
        String sql = "SELECT 1";
        assertThat(connection.prepareStatement(sql), isA(CircuitBreakerPreparedStatement.class));
        assertThat(connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY), isA(CircuitBreakerPreparedStatement.class));
        assertThat(connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT), isA(CircuitBreakerPreparedStatement.class));
        assertThat(connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS), isA(CircuitBreakerPreparedStatement.class));
        assertThat(connection.prepareStatement(sql, new int[]{0}), isA(CircuitBreakerPreparedStatement.class));
        assertThat(connection.prepareStatement(sql, new String[]{""}), isA(CircuitBreakerPreparedStatement.class));
    }
    
    @Test
    void assertCreateStatement() {
        assertThat(connection.createStatement(), isA(CircuitBreakerStatement.class));
        assertThat(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY), isA(CircuitBreakerStatement.class));
        assertThat(connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT), isA(CircuitBreakerStatement.class));
    }
    
    @Test
    void assertIsValid() {
        assertTrue(connection.isValid(1));
    }
    
    @Test
    void assertCreateClob() {
        assertNull(connection.createClob());
    }
    
    @Test
    void assertCreateArrayOf() {
        assertNull(connection.createArrayOf("", new Object[]{}));
    }
    
    @Test
    void assertIsClosed() {
        assertFalse(connection.isClosed());
    }
    
    @Test
    void assertClose() {
        assertDoesNotThrow(connection::close);
    }
}

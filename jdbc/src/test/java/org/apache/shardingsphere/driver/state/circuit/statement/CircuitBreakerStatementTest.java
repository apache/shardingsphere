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

package org.apache.shardingsphere.driver.state.circuit.statement;

import org.apache.shardingsphere.driver.state.circuit.connection.CircuitBreakerConnection;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("resource")
class CircuitBreakerStatementTest {
    
    @Test
    void assertGetMaxFieldSize() {
        assertThat(new CircuitBreakerStatement().getMaxFieldSize(), is(0));
    }
    
    @Test
    void assertSetMaxFieldSize() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setMaxFieldSize(1));
    }
    
    @Test
    void assertGetMaxRows() {
        assertThat(new CircuitBreakerStatement().getMaxRows(), is(0));
    }
    
    @Test
    void assertSetMaxRows() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setMaxRows(1));
    }
    
    @Test
    void assertSetEscapeProcessing() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setEscapeProcessing(true));
    }
    
    @Test
    void assertGetQueryTimeout() {
        assertThat(new CircuitBreakerStatement().getQueryTimeout(), is(0));
    }
    
    @Test
    void assertSetQueryTimeout() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setQueryTimeout(1));
    }
    
    @Test
    void assertCancel() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().cancel());
    }
    
    @Test
    void assertGetWarnings() {
        assertThat(new CircuitBreakerStatement().getWarnings(), is(nullValue()));
    }
    
    @Test
    void assertClearWarnings() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().clearWarnings());
    }
    
    @Test
    void assertSetCursorName() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setCursorName("c"));
    }
    
    @Test
    void assertGetResultSet() {
        assertThat(new CircuitBreakerStatement().getResultSet(), is(nullValue()));
    }
    
    @Test
    void assertGetUpdateCount() {
        assertThat(new CircuitBreakerStatement().getUpdateCount(), is(0));
    }
    
    @Test
    void assertSetFetchSize() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setFetchSize(10));
    }
    
    @Test
    void assertGetFetchSize() {
        assertThat(new CircuitBreakerStatement().getFetchSize(), is(0));
    }
    
    @Test
    void assertGetFetchDirection() {
        assertThat(new CircuitBreakerStatement().getFetchDirection(), is(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    void assertSetFetchDirection() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setFetchDirection(ResultSet.FETCH_FORWARD));
    }
    
    @Test
    void assertGetResultSetConcurrency() {
        assertThat(new CircuitBreakerStatement().getResultSetConcurrency(), is(ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    void assertGetResultSetType() {
        assertThat(new CircuitBreakerStatement().getResultSetType(), is(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertAddBatch() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().addBatch("sql"));
    }
    
    @Test
    void assertClearBatch() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().clearBatch());
    }
    
    @Test
    void assertExecuteBatch() {
        assertThat(new CircuitBreakerStatement().executeBatch(), is(new int[0]));
    }
    
    @Test
    void assertGetConnection() {
        assertThat(new CircuitBreakerStatement().getConnection(), instanceOf(CircuitBreakerConnection.class));
    }
    
    @Test
    void assertGetMoreResultsWithoutParam() {
        assertThat(new CircuitBreakerStatement().getMoreResults(), is(false));
    }
    
    @Test
    void assertGetMoreResultsWithParam() {
        assertThat(new CircuitBreakerStatement().getMoreResults(Statement.CLOSE_CURRENT_RESULT), is(false));
    }
    
    @Test
    void assertGetGeneratedKeys() {
        assertThat(new CircuitBreakerStatement().getGeneratedKeys(), is(nullValue()));
    }
    
    @Test
    void assertExecuteQuery() {
        assertThat(new CircuitBreakerStatement().executeQuery("sql"), is(nullValue()));
    }
    
    @Test
    void assertExecuteUpdate() {
        assertThat(new CircuitBreakerStatement().executeUpdate("sql"), is(0));
    }
    
    @Test
    void assertExecuteUpdateWithAutoGeneratedKeys() {
        assertThat(new CircuitBreakerStatement().executeUpdate("sql", Statement.NO_GENERATED_KEYS), is(0));
    }
    
    @Test
    void assertExecuteUpdateWithColumnIndexes() {
        assertThat(new CircuitBreakerStatement().executeUpdate("sql", new int[]{0}), is(0));
    }
    
    @Test
    void assertExecuteUpdateWithColumnNames() {
        assertThat(new CircuitBreakerStatement().executeUpdate("sql", new String[]{""}), is(0));
    }
    
    @Test
    void assertExecute() {
        assertThat(new CircuitBreakerStatement().execute("sql"), is(false));
    }
    
    @Test
    void assertExecuteWithAutoGeneratedKeys() {
        assertThat(new CircuitBreakerStatement().execute("sql", Statement.NO_GENERATED_KEYS), is(false));
    }
    
    @Test
    void assertExecuteWithColumnIndexes() {
        assertThat(new CircuitBreakerStatement().execute("sql", new int[]{0}), is(false));
    }
    
    @Test
    void assertExecuteWithColumnNames() {
        assertThat(new CircuitBreakerStatement().execute("sql", new String[]{""}), is(false));
    }
    
    @Test
    void assertGetResultSetHoldability() {
        assertThat(new CircuitBreakerStatement().getResultSetHoldability(), is(0));
    }
    
    @Test
    void assertIsClosed() {
        assertThat(new CircuitBreakerStatement().isClosed(), is(false));
    }
    
    @Test
    void assertSetPoolable() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().setPoolable(true));
    }
    
    @Test
    void assertIsPoolable() {
        assertThat(new CircuitBreakerStatement().isPoolable(), is(false));
    }
    
    @Test
    void assertCloseOnCompletion() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().closeOnCompletion());
    }
    
    @Test
    void assertIsCloseOnCompletion() {
        assertThat(new CircuitBreakerStatement().isCloseOnCompletion(), is(false));
    }
    
    @Test
    void assertClose() {
        assertDoesNotThrow(() -> new CircuitBreakerStatement().close());
    }
}

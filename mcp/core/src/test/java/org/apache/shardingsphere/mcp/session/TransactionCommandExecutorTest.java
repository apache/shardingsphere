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

package org.apache.shardingsphere.mcp.session;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityBuilder;
import org.apache.shardingsphere.mcp.execute.MCPJdbcExecutionAdapter;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TransactionCommandExecutorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteCases")
    void assertExecute(final String name, final String sql, final String expectedStatementType, final String expectedMessage) {
        MCPJdbcExecutionAdapter jdbcExecutionAdapter = mock(MCPJdbcExecutionAdapter.class);
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        prepareTransactionState(sessionManager, sql);
        TransactionCommandExecutor executor = new TransactionCommandExecutor(sessionManager, jdbcExecutionAdapter);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify(sql));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getMessage(), is(expectedMessage));
        assertDatabaseExecution(sql, jdbcExecutionAdapter);
    }
    
    static Stream<Arguments> assertExecuteCases() {
        return Stream.of(
                Arguments.of("begin", "BEGIN", "BEGIN", "Transaction started."),
                Arguments.of("commit", "COMMIT", "COMMIT", "Transaction committed."),
                Arguments.of("rollback", "ROLLBACK", "ROLLBACK", "Transaction rolled back."),
                Arguments.of("savepoint", "SAVEPOINT sp_1", "SAVEPOINT", "Savepoint created."),
                Arguments.of("rollback to savepoint", "ROLLBACK TO SAVEPOINT sp_1", "ROLLBACK TO SAVEPOINT", "Savepoint rolled back."),
                Arguments.of("release savepoint", "RELEASE SAVEPOINT sp_1", "RELEASE SAVEPOINT", "Savepoint released."));
    }
    
    @Test
    void assertExecuteWithUnsupportedSavepoint() {
        MCPJdbcExecutionAdapter jdbcExecutionAdapter = mock(MCPJdbcExecutionAdapter.class);
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "warehouse");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(sessionManager, jdbcExecutionAdapter);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "warehouse", createCapability("warehouse"), new StatementClassifier().classify("SAVEPOINT sp_1"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.UNSUPPORTED));
        verifyNoInteractions(jdbcExecutionAdapter);
    }
    
    @Test
    void assertExecuteWithClassificationResult() {
        MCPJdbcExecutionAdapter jdbcExecutionAdapter = mock(MCPJdbcExecutionAdapter.class);
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(sessionManager, jdbcExecutionAdapter);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("BEGIN"));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getStatementType(), is("BEGIN"));
        assertThat(actual.getMessage(), is("Transaction started."));
        verify(jdbcExecutionAdapter).beginTransaction("session-1", "logic_db");
    }
    
    @Test
    void assertExecuteWithInvalidCommand() {
        MCPJdbcExecutionAdapter jdbcExecutionAdapter = mock(MCPJdbcExecutionAdapter.class);
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(sessionManager, jdbcExecutionAdapter);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("SELECT 1"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.INVALID_REQUEST));
        verifyNoInteractions(jdbcExecutionAdapter);
    }
    
    @Test
    void assertExecuteWithNoActiveTransaction() {
        MCPJdbcExecutionAdapter jdbcExecutionAdapter = mock(MCPJdbcExecutionAdapter.class);
        doThrow(new IllegalStateException("Transaction is not active.")).when(jdbcExecutionAdapter).commitTransaction("session-1");
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(sessionManager, jdbcExecutionAdapter);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("COMMIT"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.TRANSACTION_STATE_ERROR));
        verify(jdbcExecutionAdapter).commitTransaction("session-1");
    }
    
    private void prepareTransactionState(final MCPSessionManager sessionManager, final String sql) {
        if ("BEGIN".equals(sql)) {
            return;
        }
        sessionManager.beginTransaction("session-1", "logic_db");
        if (sql.startsWith("ROLLBACK TO SAVEPOINT") || sql.startsWith("RELEASE SAVEPOINT")) {
            sessionManager.rememberSavepoint("session-1", "SP_1");
        }
    }
    
    private DatabaseCapabilityBuilder createCapabilityAssembler() {
        return new DatabaseCapabilityBuilder(new DatabaseMetadataSnapshots(Map.of(
                "logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()),
                "warehouse", new DatabaseMetadataSnapshot("Hive", "", Collections.emptyList()))));
    }
    
    private DatabaseCapability createCapability(final String databaseName) {
        return createCapabilityAssembler().assembleDatabaseCapability(databaseName).orElseThrow(IllegalStateException::new);
    }
    
    private void assertDatabaseExecution(final String sql, final MCPJdbcExecutionAdapter jdbcExecutionAdapter) {
        if ("BEGIN".equals(sql)) {
            verify(jdbcExecutionAdapter).beginTransaction("session-1", "logic_db");
            return;
        }
        if ("COMMIT".equals(sql)) {
            verify(jdbcExecutionAdapter).commitTransaction("session-1");
            return;
        }
        if ("ROLLBACK".equals(sql)) {
            verify(jdbcExecutionAdapter).rollbackTransaction("session-1");
            return;
        }
        if (sql.startsWith("SAVEPOINT")) {
            verify(jdbcExecutionAdapter).createSavepoint("session-1", "SP_1");
            return;
        }
        if (sql.startsWith("ROLLBACK TO SAVEPOINT")) {
            verify(jdbcExecutionAdapter).rollbackToSavepoint("session-1", "SP_1");
            return;
        }
        verify(jdbcExecutionAdapter).releaseSavepoint("session-1", "SP_1");
    }
}

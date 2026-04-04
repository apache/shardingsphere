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
import org.apache.shardingsphere.mcp.capability.MCPCapabilityBuilder;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionStatementExecutor;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.MCPErrorPayload.MCPErrorCode;
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

class MCPJdbcTransactionStatementExecutorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteCases")
    void assertExecute(final String name, final String sql, final String expectedStatementType, final String expectedMessage) {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify(sql));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getMessage(), is(expectedMessage));
        assertDatabaseExecution(sql, jdbcTransactionResourceManager);
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
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "warehouse", createCapability("warehouse"), new StatementClassifier().classify("SAVEPOINT sp_1"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getCode(), is(MCPErrorCode.UNSUPPORTED));
        verifyNoInteractions(jdbcTransactionResourceManager);
    }
    
    @Test
    void assertExecuteWithClassificationResult() {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("BEGIN"));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getStatementType(), is("BEGIN"));
        assertThat(actual.getMessage(), is("Transaction started."));
        verify(jdbcTransactionResourceManager).beginTransaction("session-1", "logic_db");
    }
    
    @Test
    void assertExecuteWithInvalidCommand() {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("SELECT 1"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getCode(), is(MCPErrorCode.INVALID_REQUEST));
        verifyNoInteractions(jdbcTransactionResourceManager);
    }
    
    @Test
    void assertExecuteWithMissingSession() {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("BEGIN"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getCode(), is(MCPErrorCode.TRANSACTION_STATE_ERROR));
        assertThat(actual.getError().get().getMessage(), is("Session does not exist."));
        verifyNoInteractions(jdbcTransactionResourceManager);
    }
    
    @Test
    void assertExecuteWithNoActiveTransaction() {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        doThrow(new IllegalStateException("Transaction is not active.")).when(jdbcTransactionResourceManager).commitTransaction("session-1");
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("COMMIT"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getCode(), is(MCPErrorCode.TRANSACTION_STATE_ERROR));
        verify(jdbcTransactionResourceManager).commitTransaction("session-1");
    }
    
    private MCPCapabilityBuilder createCapabilityBuilder() {
        return new MCPCapabilityBuilder(new DatabaseMetadataSnapshots(Map.of(
                "logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()),
                "warehouse", new DatabaseMetadataSnapshot("Hive", "", Collections.emptyList()))));
    }
    
    private DatabaseCapability createCapability(final String databaseName) {
        return createCapabilityBuilder().buildDatabaseCapability(databaseName).orElseThrow(IllegalStateException::new);
    }
    
    private void assertDatabaseExecution(final String sql, final MCPJdbcTransactionResourceManager jdbcTransactionResourceManager) {
        if ("BEGIN".equals(sql)) {
            verify(jdbcTransactionResourceManager).beginTransaction("session-1", "logic_db");
            return;
        }
        if ("COMMIT".equals(sql)) {
            verify(jdbcTransactionResourceManager).commitTransaction("session-1");
            return;
        }
        if ("ROLLBACK".equals(sql)) {
            verify(jdbcTransactionResourceManager).rollbackTransaction("session-1");
            return;
        }
        if (sql.startsWith("SAVEPOINT")) {
            verify(jdbcTransactionResourceManager).createSavepoint("session-1", "sp_1");
            return;
        }
        if (sql.startsWith("ROLLBACK TO SAVEPOINT")) {
            verify(jdbcTransactionResourceManager).rollbackToSavepoint("session-1", "sp_1");
            return;
        }
        verify(jdbcTransactionResourceManager).releaseSavepoint("session-1", "sp_1");
    }
}

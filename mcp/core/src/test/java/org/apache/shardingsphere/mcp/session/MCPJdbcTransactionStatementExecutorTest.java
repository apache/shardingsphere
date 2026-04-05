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

import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionStatementExecutor;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class,
                () -> executor.execute("session-1", "warehouse", createCapability("warehouse"), new StatementClassifier().classify("SAVEPOINT sp_1")));
        assertThat(actual.getMessage(), is("Savepoint is not supported."));
        verifyNoInteractions(jdbcTransactionResourceManager);
    }
    
    @Test
    void assertExecuteWithClassificationResult() {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("BEGIN"));
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
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("SELECT 1")));
        assertThat(actual.getMessage(), is("Statement is not a transaction command."));
        verifyNoInteractions(jdbcTransactionResourceManager);
    }
    
    @Test
    void assertExecuteWithMissingSession() {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        MCPSessionNotExistedException actual = assertThrows(MCPSessionNotExistedException.class,
                () -> executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("BEGIN")));
        assertThat(actual.getMessage(), is("Session does not exist."));
        verifyNoInteractions(jdbcTransactionResourceManager);
    }
    
    @Test
    void assertExecuteWithNoActiveTransaction() {
        MCPJdbcTransactionResourceManager jdbcTransactionResourceManager = mock(MCPJdbcTransactionResourceManager.class);
        doThrow(new IllegalStateException("Transaction is not active.")).when(jdbcTransactionResourceManager).commitTransaction("session-1");
        MCPSessionManager sessionManager = new MCPSessionManager(mock());
        sessionManager.createSession("session-1");
        MCPJdbcTransactionStatementExecutor executor = new MCPJdbcTransactionStatementExecutor(sessionManager, jdbcTransactionResourceManager);
        MCPTransactionStateException actual = assertThrows(MCPTransactionStateException.class,
                () -> executor.execute("session-1", "logic_db", createCapability("logic_db"), new StatementClassifier().classify("COMMIT")));
        assertThat(actual.getMessage(), is("Transaction is not active."));
        verify(jdbcTransactionResourceManager).commitTransaction("session-1");
    }
    
    private MCPDatabaseCapabilityProvider createDatabaseCapabilityBuilder() {
        return new MCPDatabaseCapabilityProvider(new DatabaseMetadataSnapshots(Map.of(
                "logic_db", new DatabaseMetadataSnapshot("MySQL", "", Collections.emptyList()),
                "warehouse", new DatabaseMetadataSnapshot("Hive", "", Collections.emptyList()))));
    }
    
    private MCPDatabaseCapability createCapability(final String databaseName) {
        return createDatabaseCapabilityBuilder().provide(databaseName).orElseThrow(IllegalStateException::new);
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

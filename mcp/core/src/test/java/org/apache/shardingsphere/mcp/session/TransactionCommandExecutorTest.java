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

import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.execute.DatabaseExecutionBackend;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionCommandExecutorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteCases")
    void assertExecute(final String name, final String sql, final String expectedStatementType, final String expectedMessage) {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        prepareTransactionState(sessionManager, sql);
        TransactionCommandExecutor executor = new TransactionCommandExecutor(createCapabilityAssembler(), sessionManager, createDatabaseRuntime());
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", "MySQL", new StatementClassifier().classify(sql));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getMessage(), is(expectedMessage));
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
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        sessionManager.beginTransaction("session-1", "warehouse");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(createCapabilityAssembler(), sessionManager, createDatabaseRuntime());
        
        ExecuteQueryResponse actual = executor.execute("session-1", "warehouse", "Hive", new StatementClassifier().classify("SAVEPOINT sp_1"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.UNSUPPORTED));
    }
    
    @Test
    void assertExecuteWithClassificationResult() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(createCapabilityAssembler(), sessionManager, createDatabaseRuntime());
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", "MySQL", new StatementClassifier().classify("BEGIN"));
        
        assertTrue(actual.isSuccessful());
        assertThat(actual.getStatementType(), is("BEGIN"));
        assertThat(actual.getMessage(), is("Transaction started."));
    }
    
    @Test
    void assertExecuteWithInvalidCommand() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(createCapabilityAssembler(), sessionManager, createDatabaseRuntime());
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", "MySQL", new StatementClassifier().classify("SELECT 1"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.INVALID_REQUEST));
    }
    
    @Test
    void assertExecuteWithNoActiveTransaction() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        sessionManager.createSession("session-1");
        TransactionCommandExecutor executor = new TransactionCommandExecutor(createCapabilityAssembler(), sessionManager, createDatabaseRuntime());
        
        ExecuteQueryResponse actual = executor.execute("session-1", "logic_db", "MySQL", new StatementClassifier().classify("COMMIT"));
        
        assertFalse(actual.isSuccessful());
        assertTrue(actual.getError().isPresent());
        assertThat(actual.getError().get().getErrorCode(), is(MCPErrorCode.TRANSACTION_STATE_ERROR));
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
    
    private DatabaseCapabilityAssembler createCapabilityAssembler() {
        return new DatabaseCapabilityAssembler(new DatabaseMetadataSnapshots(Collections.emptyMap()));
    }
    
    private DatabaseExecutionBackend createDatabaseRuntime() {
        return new DatabaseExecutionBackend(Collections.emptyMap(), Collections.emptyMap());
    }
}

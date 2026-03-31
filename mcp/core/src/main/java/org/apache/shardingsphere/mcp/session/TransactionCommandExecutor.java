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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.execute.MCPJdbcTransactionResourceManager;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

/**
 * Execute MCP transaction-control and savepoint commands.
 */
@RequiredArgsConstructor
public final class TransactionCommandExecutor {
    
    private final MCPSessionManager sessionManager;
    
    private final MCPJdbcTransactionResourceManager jdbcTransactionResourceManager;
    
    /**
     * Execute one transaction-control or savepoint command with resolved database capability.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @param databaseCapability resolved database capability
     * @param classificationResult statement classification result
     * @return execution response
     */
    public ExecuteQueryResponse execute(final String sessionId, final String databaseName, final DatabaseCapability databaseCapability, final ClassificationResult classificationResult) {
        String statementType = classificationResult.getStatementType();
        try {
            sessionManager.getSession(sessionId);
            if ("BEGIN".equals(statementType) || "START TRANSACTION".equals(statementType)) {
                return executeBeginTransaction(sessionId, databaseName, databaseCapability, statementType);
            }
            if ("COMMIT".equals(statementType)) {
                return executeCommit(sessionId, databaseCapability);
            }
            if ("ROLLBACK".equals(statementType)) {
                return executeRollback(sessionId, databaseCapability);
            }
            String savepointName = classificationResult.getSavepointName().orElse("");
            if ("SAVEPOINT".equals(statementType)) {
                return executeSavepoint(sessionId, databaseCapability, savepointName);
            }
            if ("ROLLBACK TO SAVEPOINT".equals(statementType)) {
                return executeRollbackSavepoint(sessionId, databaseCapability, savepointName);
            }
            if ("RELEASE SAVEPOINT".equals(statementType)) {
                return executeReleaseSavepoint(sessionId, databaseCapability, savepointName);
            }
            return ExecuteQueryResponse.error(MCPErrorCode.INVALID_REQUEST, "Statement is not a transaction command.");
        } catch (final IllegalStateException ex) {
            return ExecuteQueryResponse.error(MCPErrorCode.TRANSACTION_STATE_ERROR, ex.getMessage());
        }
    }
    
    private ExecuteQueryResponse executeBeginTransaction(final String sessionId, final String databaseName, final DatabaseCapability databaseCapability, final String statementType) {
        if (!databaseCapability.isSupportsTransactionControl()) {
            return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Transaction control is not supported.");
        }
        jdbcTransactionResourceManager.beginTransaction(sessionId, databaseName);
        return ExecuteQueryResponse.statementAck(statementType, "Transaction started.");
    }
    
    private ExecuteQueryResponse executeCommit(final String sessionId, final DatabaseCapability databaseCapability) {
        if (!databaseCapability.isSupportsTransactionControl()) {
            return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Transaction control is not supported.");
        }
        jdbcTransactionResourceManager.commitTransaction(sessionId);
        return ExecuteQueryResponse.statementAck("COMMIT", "Transaction committed.");
    }
    
    private ExecuteQueryResponse executeRollback(final String sessionId, final DatabaseCapability databaseCapability) {
        if (!databaseCapability.isSupportsTransactionControl()) {
            return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Transaction control is not supported.");
        }
        jdbcTransactionResourceManager.rollbackTransaction(sessionId);
        return ExecuteQueryResponse.statementAck("ROLLBACK", "Transaction rolled back.");
    }
    
    private ExecuteQueryResponse executeSavepoint(final String sessionId, final DatabaseCapability databaseCapability, final String savepointName) {
        if (!databaseCapability.isSupportsSavepoint()) {
            return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Savepoint is not supported.");
        }
        jdbcTransactionResourceManager.createSavepoint(sessionId, savepointName);
        return ExecuteQueryResponse.statementAck("SAVEPOINT", "Savepoint created.");
    }
    
    private ExecuteQueryResponse executeRollbackSavepoint(final String sessionId, final DatabaseCapability databaseCapability, final String savepointName) {
        if (!databaseCapability.isSupportsSavepoint()) {
            return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Savepoint is not supported.");
        }
        jdbcTransactionResourceManager.rollbackToSavepoint(sessionId, savepointName);
        return ExecuteQueryResponse.statementAck("ROLLBACK TO SAVEPOINT", "Savepoint rolled back.");
    }
    
    private ExecuteQueryResponse executeReleaseSavepoint(final String sessionId, final DatabaseCapability databaseCapability, final String savepointName) {
        if (!databaseCapability.isSupportsSavepoint()) {
            return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Savepoint is not supported.");
        }
        jdbcTransactionResourceManager.releaseSavepoint(sessionId, savepointName);
        return ExecuteQueryResponse.statementAck("RELEASE SAVEPOINT", "Savepoint released.");
    }
}

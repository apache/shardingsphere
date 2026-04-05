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

package org.apache.shardingsphere.mcp.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.MCPSessionNotExistedException;

/**
 * MCP JDBC transaction statement executor.
 */
@RequiredArgsConstructor
public final class MCPJdbcTransactionStatementExecutor {
    
    private final MCPSessionManager sessionManager;
    
    private final MCPJdbcTransactionResourceManager transactionResourceManager;
    
    /**
     * Execute one transaction-control or savepoint command with resolved database capability.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @param databaseCapability resolved database capability
     * @param classificationResult statement classification result
     * @return execution response
     * @throws MCPSessionNotExistedException when the session does not exist
     * @throws MCPInvalidRequestException when the statement is not a transaction command
     * @throws MCPTransactionStateException when the transaction state does not allow the operation
     * @throws MCPUnsupportedException when the database does not support the requested transaction feature
     */
    public ExecuteQueryResponse execute(final String sessionId, final String databaseName, final MCPDatabaseCapability databaseCapability, final ClassificationResult classificationResult) {
        String statementType = classificationResult.getStatementType();
        try {
            ShardingSpherePreconditions.checkState(sessionManager.hasSession(sessionId), MCPSessionNotExistedException::new);
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
            throw new MCPInvalidRequestException("Statement is not a transaction command.");
        } catch (final IllegalStateException ex) {
            throw new MCPTransactionStateException(ex.getMessage(), ex);
        }
    }
    
    private ExecuteQueryResponse executeBeginTransaction(final String sessionId, final String databaseName, final MCPDatabaseCapability databaseCapability, final String statementType) {
        if (!databaseCapability.isSupportsTransactionControl()) {
            throw new MCPUnsupportedException("Transaction control is not supported.");
        }
        transactionResourceManager.beginTransaction(sessionId, databaseName);
        return ExecuteQueryResponse.statementAck(statementType, "Transaction started.");
    }
    
    private ExecuteQueryResponse executeCommit(final String sessionId, final MCPDatabaseCapability databaseCapability) {
        if (!databaseCapability.isSupportsTransactionControl()) {
            throw new MCPUnsupportedException("Transaction control is not supported.");
        }
        transactionResourceManager.commitTransaction(sessionId);
        return ExecuteQueryResponse.statementAck("COMMIT", "Transaction committed.");
    }
    
    private ExecuteQueryResponse executeRollback(final String sessionId, final MCPDatabaseCapability databaseCapability) {
        if (!databaseCapability.isSupportsTransactionControl()) {
            throw new MCPUnsupportedException("Transaction control is not supported.");
        }
        transactionResourceManager.rollbackTransaction(sessionId);
        return ExecuteQueryResponse.statementAck("ROLLBACK", "Transaction rolled back.");
    }
    
    private ExecuteQueryResponse executeSavepoint(final String sessionId, final MCPDatabaseCapability databaseCapability, final String savepointName) {
        if (!databaseCapability.isSupportsSavepoint()) {
            throw new MCPUnsupportedException("Savepoint is not supported.");
        }
        transactionResourceManager.createSavepoint(sessionId, savepointName);
        return ExecuteQueryResponse.statementAck("SAVEPOINT", "Savepoint created.");
    }
    
    private ExecuteQueryResponse executeRollbackSavepoint(final String sessionId, final MCPDatabaseCapability databaseCapability, final String savepointName) {
        if (!databaseCapability.isSupportsSavepoint()) {
            throw new MCPUnsupportedException("Savepoint is not supported.");
        }
        transactionResourceManager.rollbackToSavepoint(sessionId, savepointName);
        return ExecuteQueryResponse.statementAck("ROLLBACK TO SAVEPOINT", "Savepoint rolled back.");
    }
    
    private ExecuteQueryResponse executeReleaseSavepoint(final String sessionId, final MCPDatabaseCapability databaseCapability, final String savepointName) {
        if (!databaseCapability.isSupportsSavepoint()) {
            throw new MCPUnsupportedException("Savepoint is not supported.");
        }
        transactionResourceManager.releaseSavepoint(sessionId, savepointName);
        return ExecuteQueryResponse.statementAck("RELEASE SAVEPOINT", "Savepoint released.");
    }
}

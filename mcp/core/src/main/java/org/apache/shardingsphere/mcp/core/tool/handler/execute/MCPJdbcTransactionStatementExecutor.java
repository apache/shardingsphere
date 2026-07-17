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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.core.session.MCPSessionNotExistedException;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;

/**
 * MCP JDBC transaction statement executor.
 */
@RequiredArgsConstructor
public final class MCPJdbcTransactionStatementExecutor {
    
    private final MCPSessionManager sessionManager;
    
    /**
     * Execute one transaction-control or savepoint command with resolved database capability.
     *
     * @param sessionId session identifier
     * @param databaseName logical database name
     * @param databaseCapability resolved database capability
     * @param classificationResult statement classification result
     * @return execution result
     * @throws MCPSessionNotExistedException when the session does not exist
     * @throws MCPInvalidRequestException when the statement is not a transaction command
     * @throws MCPTransactionStateException when the transaction state does not allow the operation
     * @throws MCPUnsupportedException when the database does not support the requested transaction feature
     */
    public SQLExecutionResult execute(final String sessionId, final String databaseName, final MCPDatabaseCapability databaseCapability,
                                      final ClassificationResult classificationResult) {
        String statementType = classificationResult.getStatementType();
        try {
            ShardingSpherePreconditions.checkState(sessionManager.hasSession(sessionId), MCPSessionNotExistedException::new);
            if ("BEGIN".equals(statementType) || "START TRANSACTION".equals(statementType)) {
                executeBeginTransaction(sessionId, databaseName, databaseCapability);
            } else if ("COMMIT".equals(statementType)) {
                executeCommit(sessionId, databaseCapability);
            } else if ("ROLLBACK".equals(statementType)) {
                executeRollback(sessionId, databaseCapability);
            } else {
                executeSavepointStatement(sessionId, databaseCapability, classificationResult);
            }
            return SQLExecutionResult.statementAck(classificationResult.getStatementClass(), statementType,
                    0, 0, classificationResult.getNormalizedSql());
        } catch (final IllegalArgumentException ex) {
            throw new MCPInvalidRequestException(ex.getMessage(), ex);
        } catch (final IllegalStateException ex) {
            throw new MCPTransactionStateException(ex.getMessage(), ex);
        }
    }
    
    private void executeSavepointStatement(final String sessionId, final MCPDatabaseCapability databaseCapability, final ClassificationResult classificationResult) {
        String statementType = classificationResult.getStatementType();
        String savepointName = classificationResult.getSavepointName().orElse("");
        if ("SAVEPOINT".equals(statementType)) {
            executeSavepoint(sessionId, databaseCapability, getRequiredSavepointName(savepointName));
        } else if ("ROLLBACK TO SAVEPOINT".equals(statementType)) {
            executeRollbackSavepoint(sessionId, databaseCapability, getRequiredSavepointName(savepointName));
        } else if ("RELEASE SAVEPOINT".equals(statementType)) {
            executeReleaseSavepoint(sessionId, databaseCapability, getRequiredSavepointName(savepointName));
        } else {
            throw new MCPInvalidRequestException("Statement is not a transaction command.");
        }
    }
    
    private String getRequiredSavepointName(final String savepointName) {
        String result = savepointName.trim();
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException("Savepoint name is required."));
        return result;
    }
    
    private void executeBeginTransaction(final String sessionId, final String databaseName, final MCPDatabaseCapability databaseCapability) {
        ShardingSpherePreconditions.checkState(databaseCapability.supportsTransactionControl(), () -> new MCPUnsupportedException("Transaction control is not supported."));
        sessionManager.getTransactionResourceManager().beginTransaction(sessionId, databaseName);
    }
    
    private void executeCommit(final String sessionId, final MCPDatabaseCapability databaseCapability) {
        ShardingSpherePreconditions.checkState(databaseCapability.supportsTransactionControl(), () -> new MCPUnsupportedException("Transaction control is not supported."));
        sessionManager.getTransactionResourceManager().commitTransaction(sessionId);
    }
    
    private void executeRollback(final String sessionId, final MCPDatabaseCapability databaseCapability) {
        ShardingSpherePreconditions.checkState(databaseCapability.supportsTransactionControl(), () -> new MCPUnsupportedException("Transaction control is not supported."));
        sessionManager.getTransactionResourceManager().rollbackTransaction(sessionId);
    }
    
    private void executeSavepoint(final String sessionId, final MCPDatabaseCapability databaseCapability, final String savepointName) {
        ShardingSpherePreconditions.checkState(databaseCapability.supportsSavepoint(), () -> new MCPUnsupportedException("Savepoint is not supported."));
        sessionManager.getTransactionResourceManager().createSavepoint(sessionId, savepointName);
    }
    
    private void executeRollbackSavepoint(final String sessionId, final MCPDatabaseCapability databaseCapability, final String savepointName) {
        ShardingSpherePreconditions.checkState(databaseCapability.supportsSavepoint(), () -> new MCPUnsupportedException("Savepoint is not supported."));
        sessionManager.getTransactionResourceManager().rollbackToSavepoint(sessionId, savepointName);
    }
    
    private void executeReleaseSavepoint(final String sessionId, final MCPDatabaseCapability databaseCapability, final String savepointName) {
        ShardingSpherePreconditions.checkState(databaseCapability.supportsSavepoint(), () -> new MCPUnsupportedException("Savepoint is not supported."));
        sessionManager.getTransactionResourceManager().releaseSavepoint(sessionId, savepointName);
    }
}

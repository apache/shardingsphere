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
import org.apache.shardingsphere.mcp.execute.MCPJdbcExecutionAdapter;
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

import java.util.Locale;

/**
 * Execute MCP transaction-control and savepoint commands.
 */
@RequiredArgsConstructor
public final class TransactionCommandExecutor {
    
    private final MCPSessionManager sessionManager;
    
    private final MCPJdbcExecutionAdapter jdbcExecutionAdapter;
    
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
        return execute(sessionId, databaseName, databaseCapability, classificationResult.getStatementType(),
                classificationResult.getSavepointName().map(each -> each.toUpperCase(Locale.ENGLISH)).orElse(""));
    }
    
    private ExecuteQueryResponse execute(final String sessionId, final String databaseName, final DatabaseCapability databaseCapability, final String statementType, final String savepointName) {
        try {
            if ("BEGIN".equals(statementType) || "START TRANSACTION".equals(statementType)) {
                if (!databaseCapability.isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                sessionManager.beginTransaction(sessionId, databaseName);
                jdbcExecutionAdapter.beginTransaction(sessionId, databaseName);
                return ExecuteQueryResponse.statementAck(statementType, "Transaction started.");
            }
            if ("COMMIT".equals(statementType)) {
                if (!databaseCapability.isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                jdbcExecutionAdapter.commitTransaction(sessionId);
                sessionManager.commitTransaction(sessionId);
                return ExecuteQueryResponse.statementAck("COMMIT", "Transaction committed.");
            }
            if ("ROLLBACK".equals(statementType)) {
                if (!databaseCapability.isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                jdbcExecutionAdapter.rollbackTransaction(sessionId);
                sessionManager.rollbackTransaction(sessionId);
                return ExecuteQueryResponse.statementAck("ROLLBACK", "Transaction rolled back.");
            }
            if ("SAVEPOINT".equals(statementType)) {
                if (!databaseCapability.isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                jdbcExecutionAdapter.createSavepoint(sessionId, savepointName);
                sessionManager.rememberSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("SAVEPOINT", "Savepoint created.");
            }
            if ("ROLLBACK TO SAVEPOINT".equals(statementType)) {
                if (!databaseCapability.isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                sessionManager.rollbackToSavepoint(sessionId, savepointName);
                jdbcExecutionAdapter.rollbackToSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("ROLLBACK TO SAVEPOINT", "Savepoint rolled back.");
            }
            if ("RELEASE SAVEPOINT".equals(statementType)) {
                if (!databaseCapability.isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(MCPErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                sessionManager.releaseSavepoint(sessionId, savepointName);
                jdbcExecutionAdapter.releaseSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("RELEASE SAVEPOINT", "Savepoint released.");
            }
            return ExecuteQueryResponse.error(MCPErrorCode.INVALID_REQUEST, "Statement is not a transaction command.");
        } catch (final IllegalStateException ex) {
            return ExecuteQueryResponse.error(MCPErrorCode.TRANSACTION_STATE_ERROR, ex.getMessage());
        }
    }
}

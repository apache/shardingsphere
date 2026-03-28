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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

import java.util.Locale;
import java.util.Optional;

/**
 * Execute MCP transaction-control and savepoint commands.
 */
@RequiredArgsConstructor
public final class TransactionCommandExecutor {
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    private final MCPSessionManager sessionManager;
    
    @Getter
    private final DatabaseRuntime databaseRuntime;
    
    /**
     * Execute one transaction-control or savepoint command with pre-classified SQL metadata.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param databaseType database type
     * @param classificationResult statement classification result
     * @return execution response
     */
    public ExecuteQueryResponse execute(final String sessionId, final String database, final String databaseType, final ClassificationResult classificationResult) {
        Optional<DatabaseCapability> databaseCapability = capabilityAssembler.assembleDatabaseCapability(database, databaseType);
        if (databaseCapability.isEmpty()) {
            return ExecuteQueryResponse.error(ErrorCode.NOT_FOUND, "Database capability does not exist.");
        }
        return execute(sessionId, database, databaseCapability.get(), classificationResult);
    }
    
    /**
     * Execute one transaction-control or savepoint command with resolved database capability.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param databaseCapability resolved database capability
     * @param classificationResult statement classification result
     * @return execution response
     */
    public ExecuteQueryResponse execute(final String sessionId, final String database, final DatabaseCapability databaseCapability, final ClassificationResult classificationResult) {
        return execute(sessionId, database, databaseCapability, classificationResult.getStatementType(),
                classificationResult.getSavepointName().map(each -> each.toUpperCase(Locale.ENGLISH)).orElse(""));
    }
    
    private ExecuteQueryResponse execute(final String sessionId, final String database, final DatabaseCapability databaseCapability, final String statementType, final String savepointName) {
        try {
            if ("BEGIN".equals(statementType) || "START TRANSACTION".equals(statementType)) {
                if (!databaseCapability.isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                sessionManager.beginTransaction(sessionId, database);
                databaseRuntime.beginTransaction(sessionId, database);
                return ExecuteQueryResponse.statementAck(statementType, "Transaction started.");
            }
            if ("COMMIT".equals(statementType)) {
                if (!databaseCapability.isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                databaseRuntime.commitTransaction(sessionId);
                sessionManager.commitTransaction(sessionId);
                return ExecuteQueryResponse.statementAck("COMMIT", "Transaction committed.");
            }
            if ("ROLLBACK".equals(statementType)) {
                if (!databaseCapability.isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                databaseRuntime.rollbackTransaction(sessionId);
                sessionManager.rollbackTransaction(sessionId);
                return ExecuteQueryResponse.statementAck("ROLLBACK", "Transaction rolled back.");
            }
            if ("SAVEPOINT".equals(statementType)) {
                if (!databaseCapability.isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                databaseRuntime.createSavepoint(sessionId, savepointName);
                sessionManager.rememberSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("SAVEPOINT", "Savepoint created.");
            }
            if ("ROLLBACK TO SAVEPOINT".equals(statementType)) {
                if (!databaseCapability.isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                sessionManager.rollbackToSavepoint(sessionId, savepointName);
                databaseRuntime.rollbackToSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("ROLLBACK TO SAVEPOINT", "Savepoint rolled back.");
            }
            if ("RELEASE SAVEPOINT".equals(statementType)) {
                if (!databaseCapability.isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                sessionManager.releaseSavepoint(sessionId, savepointName);
                databaseRuntime.releaseSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("RELEASE SAVEPOINT", "Savepoint released.");
            }
            return ExecuteQueryResponse.error(ErrorCode.INVALID_REQUEST, "Statement is not a transaction command.");
        } catch (final IllegalStateException ex) {
            return ExecuteQueryResponse.error(ErrorCode.TRANSACTION_STATE_ERROR, ex.getMessage());
        }
    }
}

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
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Execute MCP transaction-control and savepoint commands.
 */
public final class TransactionCommandExecutor {
    
    private final DatabaseCapabilityAssembler capabilityAssembler;
    
    private final MCPSessionManager sessionManager;
    
    @Getter
    private final DatabaseRuntime databaseRuntime;
    
    /**
     * Construct a transaction command executor.
     *
     * @param capabilityAssembler database capability assembler
     * @param sessionManager session manager
     * @param databaseRuntime database runtime
     */
    public TransactionCommandExecutor(final DatabaseCapabilityAssembler capabilityAssembler, final MCPSessionManager sessionManager, final DatabaseRuntime databaseRuntime) {
        this.capabilityAssembler = Objects.requireNonNull(capabilityAssembler, "capabilityAssembler cannot be null");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
        this.databaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
    }
    
    /**
     * Execute one transaction-control or savepoint SQL command.
     *
     * @param sessionId session identifier
     * @param database logical database name
     * @param databaseType database type
     * @param sql SQL text
     * @return execution response
     */
    public ExecuteQueryResponse execute(final String sessionId, final String database, final String databaseType, final String sql) {
        Optional<DatabaseCapabilityView> databaseCapability = capabilityAssembler.assembleDatabaseCapability(database, databaseType);
        if (databaseCapability.isEmpty()) {
            return ExecuteQueryResponse.error(ErrorCode.NOT_FOUND, "Database capability does not exist.");
        }
        String normalizedSql = Objects.requireNonNull(sql, "sql cannot be null").trim().toUpperCase(Locale.ENGLISH);
        try {
            if ("BEGIN".equals(normalizedSql) || "START TRANSACTION".equals(normalizedSql)) {
                if (!databaseCapability.get().isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                sessionManager.beginTransaction(sessionId, database);
                databaseRuntime.beginTransaction(sessionId, database);
                return ExecuteQueryResponse.statementAck(normalizedSql, "Transaction started.");
            }
            if ("COMMIT".equals(normalizedSql)) {
                if (!databaseCapability.get().isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                databaseRuntime.commitTransaction(sessionId);
                sessionManager.commitTransaction(sessionId);
                return ExecuteQueryResponse.statementAck("COMMIT", "Transaction committed.");
            }
            if ("ROLLBACK".equals(normalizedSql)) {
                if (!databaseCapability.get().isSupportsTransactionControl()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Transaction control is not supported.");
                }
                databaseRuntime.rollbackTransaction(sessionId);
                sessionManager.rollbackTransaction(sessionId);
                return ExecuteQueryResponse.statementAck("ROLLBACK", "Transaction rolled back.");
            }
            if (normalizedSql.startsWith("SAVEPOINT ")) {
                if (!databaseCapability.get().isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                String savepointName = normalizedSql.substring("SAVEPOINT ".length()).trim();
                databaseRuntime.createSavepoint(sessionId, savepointName);
                sessionManager.rememberSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("SAVEPOINT", "Savepoint created.");
            }
            if (normalizedSql.startsWith("ROLLBACK TO SAVEPOINT ")) {
                if (!databaseCapability.get().isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                String savepointName = normalizedSql.substring("ROLLBACK TO SAVEPOINT ".length()).trim();
                sessionManager.rollbackToSavepoint(sessionId, savepointName);
                databaseRuntime.rollbackToSavepoint(sessionId, savepointName);
                return ExecuteQueryResponse.statementAck("ROLLBACK TO SAVEPOINT", "Savepoint rolled back.");
            }
            if (normalizedSql.startsWith("RELEASE SAVEPOINT ")) {
                if (!databaseCapability.get().isSupportsSavepoint()) {
                    return ExecuteQueryResponse.error(ErrorCode.UNSUPPORTED, "Savepoint is not supported.");
                }
                String savepointName = normalizedSql.substring("RELEASE SAVEPOINT ".length()).trim();
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

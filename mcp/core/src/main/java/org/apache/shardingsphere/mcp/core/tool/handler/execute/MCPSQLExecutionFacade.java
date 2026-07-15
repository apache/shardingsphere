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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.core.session.MCPSessionNotExistedException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.trace.SQLExecutionTraceFactory;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCExceptionClassifier;
import org.apache.shardingsphere.mcp.support.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;

import java.util.Optional;

/**
 * MCP SQL execution facade.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class MCPSQLExecutionFacade implements MCPFeatureExecutionFacade {
    
    private final MCPDatabaseCapabilityProvider databaseCapabilityProvider;
    
    private final MCPSessionExecutionCoordinator sessionExecutionCoordinator;
    
    private final MCPJdbcTransactionStatementExecutor transactionStatementExecutor;
    
    private final MCPJdbcStatementExecutor statementExecutor;
    
    private final MCPStatementAnalyzer statementAnalyzer;
    
    private final SQLExecutionTraceFactory sqlExecutionTraceFactory;
    
    public MCPSQLExecutionFacade(final MCPDatabaseCapabilityProvider databaseCapabilityProvider, final MCPSessionManager sessionManager) {
        this(databaseCapabilityProvider, new MCPSessionExecutionCoordinator(sessionManager),
                new MCPJdbcTransactionStatementExecutor(sessionManager),
                new MCPJdbcStatementExecutor(sessionManager.getTransactionResourceManager().getRuntimeDatabases(), sessionManager.getTransactionResourceManager()),
                new MCPStatementAnalyzer(), new SQLExecutionTraceFactory());
    }
    
    @Override
    public SQLExecutionResult execute(final SQLExecutionRequest executionRequest) {
        try {
            return sessionExecutionCoordinator.executeWithSessionLock(executionRequest.getSessionId(), () -> {
                MCPDatabaseCapability databaseCapability = getDatabaseCapability(executionRequest);
                return executeInternal(executionRequest, classify(executionRequest, databaseCapability), databaseCapability);
            });
        } catch (final MCPSessionNotExistedException ex) {
            throw recordFailure(executionRequest, SupportedMCPStatement.QUERY.name(), ex);
        }
    }
    
    private SQLExecutionResult execute(final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult, final MCPDatabaseCapability databaseCapability) {
        try {
            return sessionExecutionCoordinator.executeWithSessionLock(executionRequest.getSessionId(), () -> executeInternal(executionRequest, classificationResult, databaseCapability));
        } catch (final MCPSessionNotExistedException ex) {
            throw recordFailure(executionRequest, classificationResult.getTraceStatementMarker(), ex);
        }
    }
    
    @Override
    public SQLExecutionResult executeExplain(final SQLExecutionRequest executionRequest, final String sql) {
        MCPDatabaseCapability databaseCapability = getDatabaseCapability(executionRequest);
        ClassificationResult classificationResult = new ExplainSQLCandidateValidator(statementAnalyzer).validate(sql, executionRequest.getSql(), databaseCapability);
        try {
            return execute(executionRequest, classificationResult, databaseCapability);
        } catch (final MCPInvalidRequestException | MCPQueryFailedException ex) {
            if (MCPJDBCErrorCategory.SYNTAX == MCPJDBCExceptionClassifier.classify(databaseCapability.getDatabaseType(), ex)) {
                throw new ExplainSQLSyntaxException(executionRequest.getDatabase(), executionRequest.getSchema(), sql, executionRequest.getSql(), ex);
            }
            throw ex;
        }
    }
    
    private ClassificationResult classify(final SQLExecutionRequest executionRequest, final MCPDatabaseCapability databaseCapability) {
        try {
            return statementAnalyzer.analyze(executionRequest.getSql(), databaseCapability);
        } catch (final MCPUnsupportedException | MCPInvalidRequestException ex) {
            throw recordFailure(executionRequest, SupportedMCPStatement.QUERY.name(), ex);
        } catch (final IllegalArgumentException ex) {
            throw recordFailure(executionRequest, SupportedMCPStatement.QUERY.name(), ex);
        }
    }
    
    private MCPDatabaseCapability getDatabaseCapability(final SQLExecutionRequest executionRequest) {
        Optional<MCPDatabaseCapability> databaseCapability = databaseCapabilityProvider.provide(executionRequest.getDatabase());
        ShardingSpherePreconditions.checkState(databaseCapability.isPresent(), () -> recordFailure(executionRequest, "QUERY", new DatabaseCapabilityNotFoundException()));
        return databaseCapability.orElseThrow();
    }
    
    private SQLExecutionResult executeInternal(final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult,
                                               final MCPDatabaseCapability databaseCapability) {
        ShardingSpherePreconditions.checkContains(databaseCapability.getSupportedStatementClasses(), classificationResult.getStatementClass(),
                () -> recordFailure(executionRequest, classificationResult.getTraceStatementMarker(), new StatementClassNotSupportedException()));
        checkCrossSchemaSql(executionRequest, databaseCapability, classificationResult);
        try {
            switch (classificationResult.getStatementClass()) {
                case TRANSACTION_CONTROL:
                case SAVEPOINT:
                    return recordSuccess(executionRequest, transactionStatementExecutor.execute(
                            executionRequest.getSessionId(), executionRequest.getDatabase(), databaseCapability, classificationResult), classificationResult.getTraceStatementMarker());
                case QUERY:
                case EXPLAIN:
                case DML:
                case DDL:
                case DCL:
                    return recordSuccess(executionRequest, statementExecutor.execute(executionRequest, classificationResult, databaseCapability), classificationResult.getTraceStatementMarker());
                default:
                    throw new StatementClassNotSupportedException();
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            throw recordFailure(executionRequest, classificationResult.getTraceStatementMarker(), ex);
        }
    }
    
    private SQLExecutionResult recordSuccess(final SQLExecutionRequest executionRequest, final SQLExecutionResult result, final String statementMarker) {
        sqlExecutionTraceFactory.create(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), true, statementMarker);
        return result;
    }
    
    private void checkCrossSchemaSql(final SQLExecutionRequest executionRequest, final MCPDatabaseCapability databaseCapability, final ClassificationResult classificationResult) {
        if (SchemaExecutionSemantics.BEST_EFFORT == databaseCapability.getSchemaExecutionSemantics()) {
            return;
        }
        IdentifierCasePolicy identifierCasePolicy = databaseCapability.getIdentifierCasePolicySet().getPolicy(IdentifierScope.SCHEMA);
        for (SQLStatementObjectName each : classificationResult.getReferencedObjects()) {
            if (isCrossSchemaReference(each, executionRequest.getDatabase(), identifierCasePolicy)) {
                throw recordFailure(executionRequest, classificationResult.getTraceStatementMarker(), new MCPInvalidRequestException(
                        String.format("Cross-schema SQL is not supported for database `%s`: `%s`.", executionRequest.getDatabase(), each.getObjectName())));
            }
        }
    }
    
    private boolean isCrossSchemaReference(final SQLStatementObjectName objectName, final String databaseName, final IdentifierCasePolicy identifierCasePolicy) {
        return (objectName.isQualified() || objectName.isNamespaceTarget())
                && !identifierCasePolicy.matches(databaseName, objectName.getFirstIdentifier(), objectName.getFirstIdentifierQuoteCharacter());
    }
    
    private <T extends RuntimeException> T recordFailure(final SQLExecutionRequest executionRequest, final String statementMarker, final T ex) {
        sqlExecutionTraceFactory.create(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, statementMarker);
        return ex;
    }
}

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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCExceptionClassifier;
import org.apache.shardingsphere.mcp.support.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

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
    
    public MCPSQLExecutionFacade(final MCPDatabaseCapabilityProvider databaseCapabilityProvider, final MCPSessionManager sessionManager) {
        this(databaseCapabilityProvider, new MCPSessionExecutionCoordinator(sessionManager),
                new MCPJdbcTransactionStatementExecutor(sessionManager),
                new MCPJdbcStatementExecutor(sessionManager.getTransactionResourceManager().getRuntimeDatabases(), sessionManager.getTransactionResourceManager()),
                new MCPStatementAnalyzer());
    }
    
    @Override
    public SQLExecutionResult execute(final SQLExecutionRequest executionRequest) {
        return sessionExecutionCoordinator.executeWithSessionLock(executionRequest.getSessionId(), () -> {
            MCPDatabaseCapability databaseCapability = getDatabaseCapability(executionRequest);
            return executeInternal(executionRequest, statementAnalyzer.analyze(executionRequest.getSql(), databaseCapability), databaseCapability);
        });
    }
    
    private SQLExecutionResult execute(final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult, final MCPDatabaseCapability databaseCapability) {
        return sessionExecutionCoordinator.executeWithSessionLock(executionRequest.getSessionId(), () -> executeInternal(executionRequest, classificationResult, databaseCapability));
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
    
    private MCPDatabaseCapability getDatabaseCapability(final SQLExecutionRequest executionRequest) {
        Optional<MCPDatabaseCapability> databaseCapability = databaseCapabilityProvider.provide(executionRequest.getDatabase());
        ShardingSpherePreconditions.checkState(databaseCapability.isPresent(), DatabaseCapabilityNotFoundException::new);
        return databaseCapability.orElseThrow();
    }
    
    private SQLExecutionResult executeInternal(final SQLExecutionRequest executionRequest, final ClassificationResult classificationResult,
                                               final MCPDatabaseCapability databaseCapability) {
        ShardingSpherePreconditions.checkContains(databaseCapability.getSupportedStatementClasses(), classificationResult.getStatementClass(),
                StatementClassNotSupportedException::new);
        checkCrossSchemaSql(executionRequest, databaseCapability, classificationResult);
        return switch (classificationResult.getStatementClass()) {
            case TRANSACTION_CONTROL, SAVEPOINT -> transactionStatementExecutor.execute(
                    executionRequest.getSessionId(), executionRequest.getDatabase(), databaseCapability, classificationResult);
            case QUERY, EXPLAIN, DML, DDL, DCL -> statementExecutor.execute(executionRequest, classificationResult, databaseCapability);
        };
    }
    
    private void checkCrossSchemaSql(final SQLExecutionRequest executionRequest, final MCPDatabaseCapability databaseCapability, final ClassificationResult classificationResult) {
        if (SchemaExecutionSemantics.BEST_EFFORT == databaseCapability.getSchemaExecutionSemantics()) {
            return;
        }
        for (SQLStatementObjectName each : classificationResult.getReferencedObjects()) {
            if (isCrossSchemaReference(each, executionRequest.getDatabase(), databaseCapability.getIdentifierContext())) {
                throw new MCPInvalidRequestException(String.format("Cross-schema SQL is not supported for database `%s`: `%s`.", executionRequest.getDatabase(), each.getObjectName()));
            }
        }
    }
    
    private boolean isCrossSchemaReference(final SQLStatementObjectName objectName, final String databaseName, final DatabaseIdentifierContext identifierContext) {
        return (objectName.isQualified() || objectName.isNamespaceTarget())
                && !identifierContext.matchesMetaData(IdentifierScope.SCHEMA, databaseName,
                        new IdentifierValue(objectName.getFirstIdentifier(), objectName.getFirstIdentifierQuoteCharacter()));
    }
}

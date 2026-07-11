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
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
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
import org.apache.shardingsphere.mcp.support.database.exception.StatementClassNotSupportedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.database.tool.response.SQLExecutionResponse;

import java.util.Locale;
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
    
    private final StatementClassifier statementClassifier;
    
    private final SQLStatementScanner scanner;
    
    private final SQLExecutionTraceFactory sqlExecutionTraceFactory;
    
    public MCPSQLExecutionFacade(final MCPDatabaseCapabilityProvider databaseCapabilityProvider, final MCPSessionManager sessionManager) {
        this(databaseCapabilityProvider, new MCPSessionExecutionCoordinator(sessionManager),
                new MCPJdbcTransactionStatementExecutor(sessionManager),
                new MCPJdbcStatementExecutor(sessionManager.getTransactionResourceManager().getRuntimeDatabases(), sessionManager.getTransactionResourceManager()),
                new StatementClassifier(), new SQLStatementScanner(), new SQLExecutionTraceFactory());
    }
    
    @Override
    public SQLExecutionResponse execute(final SQLExecutionRequest executionRequest) {
        try {
            return sessionExecutionCoordinator.executeWithSessionLock(executionRequest.getSessionId(), () -> executeInternal(executionRequest));
        } catch (final MCPSessionNotExistedException ex) {
            throw recordFailure(executionRequest, SupportedMCPStatement.QUERY.name(), ex);
        }
    }
    
    private SQLExecutionResponse executeInternal(final SQLExecutionRequest executionRequest) {
        Optional<MCPDatabaseCapability> databaseCapability = databaseCapabilityProvider.provide(executionRequest.getDatabase());
        ShardingSpherePreconditions.checkState(databaseCapability.isPresent(), () -> recordFailure(executionRequest, "QUERY", new DatabaseCapabilityNotFoundException()));
        MCPDatabaseCapability actualDatabaseCapability = databaseCapability.orElseThrow();
        ClassificationResult classificationResult;
        try {
            classificationResult = statementClassifier.classify(executionRequest.getSql());
        } catch (final MCPUnsupportedException | IllegalArgumentException ex) {
            throw recordFailure(executionRequest, SupportedMCPStatement.QUERY.name(), ex);
        }
        ShardingSpherePreconditions.checkContains(actualDatabaseCapability.getSupportedStatementClasses(), classificationResult.getStatementClass(),
                () -> recordFailure(executionRequest, classificationResult.getTraceStatementMarker(), new StatementClassNotSupportedException()));
        checkCrossSchemaSql(executionRequest, actualDatabaseCapability, classificationResult);
        try {
            switch (classificationResult.getStatementClass()) {
                case TRANSACTION_CONTROL:
                case SAVEPOINT:
                    return recordSuccess(executionRequest, transactionStatementExecutor.execute(
                            executionRequest.getSessionId(), executionRequest.getDatabase(), actualDatabaseCapability, classificationResult), classificationResult.getTraceStatementMarker());
                case QUERY:
                case EXPLAIN:
                case DML:
                case DDL:
                case DCL:
                    return recordSuccess(executionRequest, statementExecutor.execute(executionRequest, classificationResult, actualDatabaseCapability), classificationResult.getTraceStatementMarker());
                default:
                    throw new StatementClassNotSupportedException();
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            throw recordFailure(executionRequest, classificationResult.getTraceStatementMarker(), ex);
        }
    }
    
    private SQLExecutionResponse recordSuccess(final SQLExecutionRequest executionRequest, final SQLExecutionResponse response, final String statementMarker) {
        sqlExecutionTraceFactory.create(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), true, statementMarker);
        return response;
    }
    
    private void checkCrossSchemaSql(final SQLExecutionRequest executionRequest, final MCPDatabaseCapability databaseCapability, final ClassificationResult classificationResult) {
        if (SchemaExecutionSemantics.BEST_EFFORT == databaseCapability.getSchemaExecutionSemantics()) {
            return;
        }
        for (String each : classificationResult.getReferencedObjectNames()) {
            if (isCrossSchemaReference(each, executionRequest.getDatabase(), classificationResult)) {
                throw recordFailure(executionRequest, classificationResult.getTraceStatementMarker(), new MCPInvalidRequestException(
                        String.format("Cross-schema SQL is not supported for database `%s`: `%s`.", executionRequest.getDatabase(), each)));
            }
        }
    }
    
    private boolean isCrossSchemaReference(final String objectName, final String databaseName, final ClassificationResult classificationResult) {
        int qualifierSeparatorIndex = objectName.indexOf('.');
        if (-1 != qualifierSeparatorIndex) {
            return !objectName.substring(0, qualifierSeparatorIndex).equalsIgnoreCase(databaseName);
        }
        return isDatabaseOrSchemaBoundaryReference(objectName, databaseName, classificationResult);
    }
    
    private boolean isDatabaseOrSchemaBoundaryReference(final String objectName, final String databaseName, final ClassificationResult classificationResult) {
        if (objectName.equalsIgnoreCase(databaseName)) {
            return false;
        }
        String actualSql = classificationResult.getNormalizedSql();
        String upperSql = actualSql.substring(scanner.skipInsignificant(actualSql, 0)).toUpperCase(Locale.ENGLISH);
        if (SupportedMCPStatement.DDL == classificationResult.getStatementClass()) {
            return isDatabaseOrSchemaStatement(upperSql, classificationResult.getStatementType()) || containsSetSchemaClause(upperSql);
        }
        return SupportedMCPStatement.DCL == classificationResult.getStatementClass() && containsOnDatabaseOrSchemaClause(upperSql);
    }
    
    private boolean isDatabaseOrSchemaStatement(final String upperSql, final String statementType) {
        if (!"CREATE".equals(statementType) && !"ALTER".equals(statementType) && !"DROP".equals(statementType)) {
            return false;
        }
        return upperSql.startsWith(statementType + " DATABASE ") || upperSql.startsWith(statementType + " SCHEMA ");
    }
    
    private boolean containsSetSchemaClause(final String upperSql) {
        return upperSql.matches(".*\\bSET\\s+SCHEMA\\b.*");
    }
    
    private boolean containsOnDatabaseOrSchemaClause(final String upperSql) {
        return upperSql.matches(".*\\bON\\s+(DATABASE|SCHEMA)\\b.*");
    }
    
    private <T extends RuntimeException> T recordFailure(final SQLExecutionRequest executionRequest, final String statementMarker, final T ex) {
        sqlExecutionTraceFactory.create(executionRequest.getSessionId(), executionRequest.getDatabase(), executionRequest.getSql(), false, statementMarker);
        return ex;
    }
}

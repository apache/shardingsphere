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

package org.apache.shardingsphere.mcp.capability.provider;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.capability.ResultBehavior;
import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.capability.TransactionBoundaryBehavior;
import org.apache.shardingsphere.mcp.capability.TransactionCapability;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * MCP database capability provider.
 */
@RequiredArgsConstructor
public final class MCPDatabaseCapabilityProvider {
    
    private final DatabaseMetadataSnapshots databaseMetadataSnapshots;
    
    /**
     * Provide the database-level capability.
     *
     * @param databaseName logical database name
     * @return database-level capability when the database type is supported
     */
    public Optional<DatabaseCapability> provide(final String databaseName) {
        return databaseMetadataSnapshots.findDatabaseType(databaseName).flatMap(optional -> find(databaseName, optional, getDatabaseVersion(databaseName)));
    }
    
    private Optional<DatabaseCapability> find(final String databaseName, final String databaseType, final String databaseVersion) {
        Optional<DatabaseCapabilityOption> databaseCapabilityOption = TypedSPILoader.findService(DatabaseCapabilityOption.class, databaseType);
        return databaseCapabilityOption.map(optional -> createDefaultCapability(databaseName, databaseType, databaseVersion, optional));
    }
    
    private DatabaseCapability createDefaultCapability(final String databaseName, final String databaseType, final String databaseVersion, final DatabaseCapabilityOption option) {
        boolean supportsExplainAnalyze = option.isExplainAnalyzeSupported(databaseVersion);
        TransactionCapability transactionCapability = option.getTransactionCapability();
        return new DatabaseCapability(databaseName, databaseType, "BASELINE", createSupportedMetadataObjectTypes(option.isIndexSupported()),
                createSupportedStatementClasses(transactionCapability, supportsExplainAnalyze), TransactionCapability.NONE != transactionCapability,
                TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability, createSupportedTransactionStatements(transactionCapability),
                true, 1000, 30000, option.getDefaultSchemaSemantics(), option.isCrossSchemaQuerySupported(), supportsExplainAnalyze,
                TransactionBoundaryBehavior.NATIVE, TransactionBoundaryBehavior.NATIVE,
                supportsExplainAnalyze ? ResultBehavior.RESULT_SET : ResultBehavior.UNSUPPORTED,
                supportsExplainAnalyze ? TransactionBoundaryBehavior.NATIVE : TransactionBoundaryBehavior.UNSUPPORTED);
    }
    
    private Set<MetadataObjectType> createSupportedMetadataObjectTypes(final boolean indexSupported) {
        Set<MetadataObjectType> result = new LinkedHashSet<>(16, 1F);
        result.add(MetadataObjectType.SCHEMA);
        result.add(MetadataObjectType.TABLE);
        result.add(MetadataObjectType.VIEW);
        result.add(MetadataObjectType.COLUMN);
        if (indexSupported) {
            result.add(MetadataObjectType.INDEX);
        }
        return result;
    }
    
    private Set<SupportedMCPStatement> createSupportedStatementClasses(final TransactionCapability transactionCapability, final boolean supportsExplainAnalyze) {
        Set<SupportedMCPStatement> result = new LinkedHashSet<>(16, 1F);
        result.add(SupportedMCPStatement.QUERY);
        result.add(SupportedMCPStatement.DML);
        result.add(SupportedMCPStatement.DDL);
        result.add(SupportedMCPStatement.DCL);
        if (TransactionCapability.NONE != transactionCapability) {
            result.add(SupportedMCPStatement.TRANSACTION_CONTROL);
        }
        if (TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability) {
            result.add(SupportedMCPStatement.SAVEPOINT);
        }
        if (supportsExplainAnalyze) {
            result.add(SupportedMCPStatement.EXPLAIN_ANALYZE);
        }
        return result;
    }
    
    private Set<String> createSupportedTransactionStatements(final TransactionCapability transactionCapability) {
        Set<String> result = new LinkedHashSet<>(16, 1F);
        if (TransactionCapability.NONE != transactionCapability) {
            result.add("BEGIN");
            result.add("START TRANSACTION");
            result.add("COMMIT");
            result.add("ROLLBACK");
        }
        if (TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability) {
            result.add("SAVEPOINT");
            result.add("ROLLBACK TO SAVEPOINT");
            result.add("RELEASE SAVEPOINT");
        }
        return result;
    }
    
    private String getDatabaseVersion(final String databaseName) {
        return databaseMetadataSnapshots.findSnapshot(databaseName).map(DatabaseMetadataSnapshot::getDatabaseVersion).orElse("");
    }
}

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

package org.apache.shardingsphere.mcp.capability.database;

import lombok.Getter;
import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * MCP database capability.
 */
@Getter
public final class MCPDatabaseCapability {
    
    private final String databaseName;
    
    private final String databaseType;
    
    private final Set<SupportedMCPMetadataObjectType> supportedMetadataObjectTypes;
    
    private final Set<SupportedMCPStatement> supportedStatementClasses;
    
    private final TransactionCapability transactionCapability;
    
    private final SchemaSemantics defaultSchemaSemantics;
    
    private final boolean supportsCrossSchemaSql;
    
    private final boolean supportsExplainAnalyze;
    
    public MCPDatabaseCapability(final String databaseName, final String databaseVersion, final MCPDatabaseCapabilityOption option) {
        this.databaseName = databaseName;
        databaseType = option.getType();
        supportedMetadataObjectTypes = createSupportedMetadataObjectTypes(option);
        supportedStatementClasses = createSupportedStatementClasses(databaseVersion, option);
        transactionCapability = option.getTransactionCapability();
        defaultSchemaSemantics = option.getDefaultSchemaSemantics();
        supportsCrossSchemaSql = option.isCrossSchemaQuerySupported();
        supportsExplainAnalyze = option.isExplainAnalyzeSupported(databaseVersion);
    }
    
    private Set<SupportedMCPMetadataObjectType> createSupportedMetadataObjectTypes(final MCPDatabaseCapabilityOption option) {
        Set<SupportedMCPMetadataObjectType> result = new LinkedHashSet<>(16, 1F);
        result.add(SupportedMCPMetadataObjectType.SCHEMA);
        result.add(SupportedMCPMetadataObjectType.TABLE);
        result.add(SupportedMCPMetadataObjectType.VIEW);
        result.add(SupportedMCPMetadataObjectType.COLUMN);
        if (option.isIndexSupported()) {
            result.add(SupportedMCPMetadataObjectType.INDEX);
        }
        if (option.isSequenceSupported()) {
            result.add(SupportedMCPMetadataObjectType.SEQUENCE);
        }
        return result;
    }
    
    private Set<SupportedMCPStatement> createSupportedStatementClasses(final String databaseVersion, final MCPDatabaseCapabilityOption option) {
        Set<SupportedMCPStatement> result = new LinkedHashSet<>(16, 1F);
        result.add(SupportedMCPStatement.QUERY);
        result.add(SupportedMCPStatement.DML);
        result.add(SupportedMCPStatement.DDL);
        result.add(SupportedMCPStatement.DCL);
        if (TransactionCapability.NONE != option.getTransactionCapability()) {
            result.add(SupportedMCPStatement.TRANSACTION_CONTROL);
        }
        if (TransactionCapability.LOCAL_WITH_SAVEPOINT == option.getTransactionCapability()) {
            result.add(SupportedMCPStatement.SAVEPOINT);
        }
        if (option.isExplainAnalyzeSupported(databaseVersion)) {
            result.add(SupportedMCPStatement.EXPLAIN_ANALYZE);
        }
        return result;
    }
    
    /**
     * Judge whether transaction control is supported.
     *
     * @return whether transaction control is supported
     */
    public boolean isSupportsTransactionControl() {
        return TransactionCapability.NONE != transactionCapability;
    }
    
    /**
     * Judge whether savepoint is supported.
     *
     * @return whether savepoint is supported
     */
    public boolean isSupportsSavepoint() {
        return TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability;
    }
}

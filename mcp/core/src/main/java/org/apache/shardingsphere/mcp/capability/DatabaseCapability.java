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

package org.apache.shardingsphere.mcp.capability;

import lombok.Getter;

import java.util.Set;

/**
 * Database-level capability definition.
 */
@Getter
public final class DatabaseCapability {
    
    private final String databaseType;
    
    private final String minSupportedVersion;
    
    private final Set<SupportedObjectType> supportedObjectTypes;
    
    private final Set<StatementClass> supportedStatementClasses;
    
    private final boolean supportsTransactionControl;
    
    private final boolean supportsSavepoint;
    
    private final Set<String> supportedTransactionStatements;
    
    private final TransactionCapability transactionCapability;
    
    private final boolean defaultAutocommit;
    
    private final boolean crossSchemaQuerySupported;
    
    private final boolean supportsExplainAnalyze;
    
    private final int maxRowsDefault;
    
    private final int maxTimeoutMsDefault;
    
    private final SchemaSemantics defaultSchemaSemantics;
    
    private final TransactionBoundaryBehavior ddlTransactionBehavior;
    
    private final TransactionBoundaryBehavior dclTransactionBehavior;
    
    private final ResultBehavior explainAnalyzeResultBehavior;
    
    private final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior;
    
    /**
     * Construct a database capability definition.
     *
     * @param databaseType database type
     * @param supportedObjectTypes supported object types
     * @param supportedStatementClasses supported statement classes
     * @param transactionCapability transaction capability
     * @param defaultAutocommit default autocommit flag
     * @param crossSchemaQuerySupported cross-schema query support flag
     */
    public DatabaseCapability(final String databaseType, final Set<SupportedObjectType> supportedObjectTypes, final Set<StatementClass> supportedStatementClasses,
                              final TransactionCapability transactionCapability, final boolean defaultAutocommit, final boolean crossSchemaQuerySupported) {
        this(databaseType, "BASELINE", supportedObjectTypes, supportedStatementClasses, transactionCapability,
                DatabaseCapabilityRegistry.createSupportedTransactionStatements(transactionCapability), defaultAutocommit, 1000, 30000,
                SchemaSemantics.NATIVE_SCHEMA, crossSchemaQuerySupported,
                null != supportedStatementClasses && supportedStatementClasses.contains(StatementClass.EXPLAIN_ANALYZE),
                TransactionBoundaryBehavior.NATIVE, TransactionBoundaryBehavior.NATIVE, ResultBehavior.RESULT_SET, TransactionBoundaryBehavior.NATIVE);
    }
    
    /**
     * Construct a database capability definition with the full capability matrix fields.
     *
     * @param databaseType database type
     * @param minSupportedVersion minimum supported version label
     * @param supportedObjectTypes supported object types
     * @param supportedStatementClasses supported statement classes
     * @param transactionCapability transaction capability
     * @param supportedTransactionStatements supported transaction statements
     * @param defaultAutocommit default autocommit flag
     * @param maxRowsDefault default row limit
     * @param maxTimeoutMsDefault default timeout limit
     * @param defaultSchemaSemantics default schema semantics
     * @param crossSchemaQuerySupported cross-schema query support flag
     * @param supportsExplainAnalyze explain-analyze support flag
     * @param ddlTransactionBehavior DDL transaction behavior
     * @param dclTransactionBehavior DCL transaction behavior
     * @param explainAnalyzeResultBehavior explain-analyze result behavior
     * @param explainAnalyzeTransactionBehavior explain-analyze transaction behavior
     */
    public DatabaseCapability(final String databaseType, final String minSupportedVersion, final Set<SupportedObjectType> supportedObjectTypes,
                              final Set<StatementClass> supportedStatementClasses, final TransactionCapability transactionCapability,
                              final Set<String> supportedTransactionStatements, final boolean defaultAutocommit, final int maxRowsDefault,
                              final int maxTimeoutMsDefault, final SchemaSemantics defaultSchemaSemantics, final boolean crossSchemaQuerySupported,
                              final boolean supportsExplainAnalyze, final TransactionBoundaryBehavior ddlTransactionBehavior,
                              final TransactionBoundaryBehavior dclTransactionBehavior, final ResultBehavior explainAnalyzeResultBehavior,
                              final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior) {
        this.databaseType = DatabaseCapabilityRegistry.normalizeDatabaseType(databaseType);
        this.minSupportedVersion = minSupportedVersion;
        this.supportedObjectTypes = supportedObjectTypes;
        this.supportedStatementClasses = supportedStatementClasses;
        this.transactionCapability = transactionCapability;
        supportsTransactionControl = TransactionCapability.NONE != transactionCapability;
        supportsSavepoint = TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability;
        this.supportedTransactionStatements = supportedTransactionStatements;
        this.defaultAutocommit = defaultAutocommit;
        this.maxRowsDefault = maxRowsDefault;
        this.maxTimeoutMsDefault = maxTimeoutMsDefault;
        this.defaultSchemaSemantics = defaultSchemaSemantics;
        this.crossSchemaQuerySupported = crossSchemaQuerySupported;
        this.supportsExplainAnalyze = supportsExplainAnalyze;
        this.ddlTransactionBehavior = ddlTransactionBehavior;
        this.dclTransactionBehavior = dclTransactionBehavior;
        this.explainAnalyzeResultBehavior = explainAnalyzeResultBehavior;
        this.explainAnalyzeTransactionBehavior = explainAnalyzeTransactionBehavior;
    }
}

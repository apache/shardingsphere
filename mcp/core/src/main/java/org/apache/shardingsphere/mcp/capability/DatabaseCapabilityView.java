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

import java.util.Objects;
import java.util.Set;

/**
 * Database-level capability view.
 */
@Getter
public final class DatabaseCapabilityView {
    
    private final String database;
    
    private final String databaseType;
    
    private final String minSupportedVersion;
    
    private final Set<SupportedObjectType> supportedObjectTypes;
    
    private final Set<StatementClass> supportedStatementClasses;
    
    private final boolean supportsTransactionControl;
    
    private final boolean supportsSavepoint;
    
    private final Set<String> supportedTransactionStatements;
    
    private final boolean defaultAutocommit;
    
    private final int maxRowsDefault;
    
    private final int maxTimeoutMsDefault;
    
    private final SchemaSemantics defaultSchemaSemantics;
    
    private final boolean supportsCrossSchemaSql;
    
    private final boolean supportsExplainAnalyze;
    
    private final TransactionBoundaryBehavior ddlTransactionBehavior;
    
    private final TransactionBoundaryBehavior dclTransactionBehavior;
    
    private final ResultBehavior explainAnalyzeResultBehavior;
    
    private final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior;
    
    DatabaseCapabilityView(final String database, final String databaseType, final String minSupportedVersion, final Set<SupportedObjectType> supportedObjectTypes,
                           final Set<StatementClass> supportedStatementClasses, final boolean supportsTransactionControl, final boolean supportsSavepoint,
                           final Set<String> supportedTransactionStatements, final boolean defaultAutocommit, final int maxRowsDefault,
                           final int maxTimeoutMsDefault, final SchemaSemantics defaultSchemaSemantics, final boolean supportsCrossSchemaSql,
                           final boolean supportsExplainAnalyze, final TransactionBoundaryBehavior ddlTransactionBehavior,
                           final TransactionBoundaryBehavior dclTransactionBehavior, final ResultBehavior explainAnalyzeResultBehavior,
                           final TransactionBoundaryBehavior explainAnalyzeTransactionBehavior) {
        this.database = Objects.requireNonNull(database, "database cannot be null");
        this.databaseType = Objects.requireNonNull(databaseType, "databaseType cannot be null");
        this.minSupportedVersion = Objects.requireNonNull(minSupportedVersion, "minSupportedVersion cannot be null");
        this.supportedObjectTypes = DatabaseCapabilityAssembler.toImmutableEnumSet(supportedObjectTypes, SupportedObjectType.class);
        this.supportedStatementClasses = DatabaseCapabilityAssembler.toImmutableEnumSet(supportedStatementClasses, StatementClass.class);
        this.supportsTransactionControl = supportsTransactionControl;
        this.supportsSavepoint = supportsSavepoint;
        this.supportedTransactionStatements = DatabaseCapabilityAssembler.toImmutableStrings(supportedTransactionStatements);
        this.defaultAutocommit = defaultAutocommit;
        this.maxRowsDefault = maxRowsDefault;
        this.maxTimeoutMsDefault = maxTimeoutMsDefault;
        this.defaultSchemaSemantics = Objects.requireNonNull(defaultSchemaSemantics, "defaultSchemaSemantics cannot be null");
        this.supportsCrossSchemaSql = supportsCrossSchemaSql;
        this.supportsExplainAnalyze = supportsExplainAnalyze;
        this.ddlTransactionBehavior = Objects.requireNonNull(ddlTransactionBehavior, "ddlTransactionBehavior cannot be null");
        this.dclTransactionBehavior = Objects.requireNonNull(dclTransactionBehavior, "dclTransactionBehavior cannot be null");
        this.explainAnalyzeResultBehavior = Objects.requireNonNull(explainAnalyzeResultBehavior, "explainAnalyzeResultBehavior cannot be null");
        this.explainAnalyzeTransactionBehavior = Objects.requireNonNull(explainAnalyzeTransactionBehavior, "explainAnalyzeTransactionBehavior cannot be null");
    }
}

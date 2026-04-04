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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.MCPPayload;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Database-level 
 */
@RequiredArgsConstructor
@Getter
public final class DatabaseCapability implements MCPPayload {
    
    private final String database;
    
    private final String databaseType;
    
    private final String minSupportedVersion;
    
    private final Set<MetadataObjectType> supportedMetadataObjectTypes;
    
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
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(32, 1F);
        result.put("database", database);
        result.put("databaseType", databaseType);
        result.put("minSupportedVersion", minSupportedVersion);
        result.put("supportedObjectTypes", supportedMetadataObjectTypes);
        result.put("supportedStatementClasses", supportedStatementClasses);
        result.put("supportsTransactionControl", supportsTransactionControl);
        result.put("supportsSavepoint", supportsSavepoint);
        result.put("supportedTransactionStatements", supportedTransactionStatements);
        result.put("defaultAutocommit", defaultAutocommit);
        result.put("maxRowsDefault", maxRowsDefault);
        result.put("maxTimeoutMsDefault", maxTimeoutMsDefault);
        result.put("defaultSchemaSemantics", defaultSchemaSemantics);
        result.put("supportsCrossSchemaSql", supportsCrossSchemaSql);
        result.put("supportsExplainAnalyze", supportsExplainAnalyze);
        result.put("ddlTransactionBehavior", ddlTransactionBehavior);
        result.put("dclTransactionBehavior", dclTransactionBehavior);
        result.put("explainAnalyzeResultBehavior", explainAnalyzeResultBehavior);
        result.put("explainAnalyzeTransactionBehavior", explainAnalyzeTransactionBehavior);
        return result;
    }
}

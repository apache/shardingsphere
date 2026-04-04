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

package org.apache.shardingsphere.mcp.resource.response;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response for database capability resources.
 */
@RequiredArgsConstructor
public final class MCPDatabaseCapabilityResponse implements MCPResourceResponse {
    
    private final DatabaseCapability databaseCapability;
    
    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> result = new LinkedHashMap<>(32, 1F);
        result.put("database", databaseCapability.getDatabase());
        result.put("databaseType", databaseCapability.getDatabaseType());
        result.put("minSupportedVersion", databaseCapability.getMinSupportedVersion());
        result.put("supportedObjectTypes", databaseCapability.getSupportedMetadataObjectTypes());
        result.put("supportedStatementClasses", databaseCapability.getSupportedStatementClasses());
        result.put("supportsTransactionControl", databaseCapability.isSupportsTransactionControl());
        result.put("supportsSavepoint", databaseCapability.isSupportsSavepoint());
        result.put("supportedTransactionStatements", databaseCapability.getSupportedTransactionStatements());
        result.put("defaultAutocommit", databaseCapability.isDefaultAutocommit());
        result.put("maxRowsDefault", databaseCapability.getMaxRowsDefault());
        result.put("maxTimeoutMsDefault", databaseCapability.getMaxTimeoutMsDefault());
        result.put("defaultSchemaSemantics", databaseCapability.getDefaultSchemaSemantics());
        result.put("supportsCrossSchemaSql", databaseCapability.isSupportsCrossSchemaSql());
        result.put("supportsExplainAnalyze", databaseCapability.isSupportsExplainAnalyze());
        result.put("ddlTransactionBehavior", databaseCapability.getDdlTransactionBehavior());
        result.put("dclTransactionBehavior", databaseCapability.getDclTransactionBehavior());
        result.put("explainAnalyzeResultBehavior", databaseCapability.getExplainAnalyzeResultBehavior());
        result.put("explainAnalyzeTransactionBehavior", databaseCapability.getExplainAnalyzeTransactionBehavior());
        return result;
    }
}

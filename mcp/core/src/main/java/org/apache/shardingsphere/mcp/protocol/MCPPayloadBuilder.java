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

package org.apache.shardingsphere.mcp.protocol;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Build transport-neutral MCP payloads.
 */
public final class MCPPayloadBuilder {
    
    /**
     * Create service capability payload.
     *
     * @param capability service capability
     * @return payload
     */
    public Map<String, Object> createServiceCapabilityPayload(final ServiceCapability capability) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("supportedResources", capability.getSupportedResources());
        result.put("supportedTools", capability.getSupportedTools());
        result.put("supportedStatementClasses", capability.getSupportedStatementClasses());
        return result;
    }
    
    /**
     * Create database capability payload.
     *
     * @param capability database capability
     * @return payload
     */
    public Map<String, Object> createDatabaseCapabilityPayload(final DatabaseCapability capability) {
        Map<String, Object> result = new LinkedHashMap<>(32, 1F);
        result.put("database", capability.getDatabase());
        result.put("databaseType", capability.getDatabaseType());
        result.put("minSupportedVersion", capability.getMinSupportedVersion());
        result.put("supportedObjectTypes", capability.getSupportedMetadataObjectTypes());
        result.put("supportedStatementClasses", capability.getSupportedStatementClasses());
        result.put("supportsTransactionControl", capability.isSupportsTransactionControl());
        result.put("supportsSavepoint", capability.isSupportsSavepoint());
        result.put("supportedTransactionStatements", capability.getSupportedTransactionStatements());
        result.put("defaultAutocommit", capability.isDefaultAutocommit());
        result.put("maxRowsDefault", capability.getMaxRowsDefault());
        result.put("maxTimeoutMsDefault", capability.getMaxTimeoutMsDefault());
        result.put("defaultSchemaSemantics", capability.getDefaultSchemaSemantics());
        result.put("supportsCrossSchemaSql", capability.isSupportsCrossSchemaSql());
        result.put("supportsExplainAnalyze", capability.isSupportsExplainAnalyze());
        result.put("ddlTransactionBehavior", capability.getDdlTransactionBehavior());
        result.put("dclTransactionBehavior", capability.getDclTransactionBehavior());
        result.put("explainAnalyzeResultBehavior", capability.getExplainAnalyzeResultBehavior());
        result.put("explainAnalyzeTransactionBehavior", capability.getExplainAnalyzeTransactionBehavior());
        return result;
    }
    
    /**
     * Create metadata items payload.
     *
     * @param metadataObjects metadata objects
     * @param nextPageToken next page token
     * @return payload
     */
    public Map<String, Object> createMetadataItemsPayload(final List<MetadataObject> metadataObjects, final String nextPageToken) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", metadataObjects);
        if (null != nextPageToken && !nextPageToken.isEmpty()) {
            result.put("next_page_token", nextPageToken);
        }
        return result;
    }
    
    /**
     * Create execute-query payload.
     *
     * @param response execute-query response
     * @return payload
     */
    public Map<String, Object> createExecuteQueryPayload(final ExecuteQueryResponse response) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("result_kind", response.getResultKind().name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", response.getStatementType());
        result.put("status", response.getStatus());
        if (!response.getColumns().isEmpty()) {
            result.put("columns", response.getColumns());
        }
        if (!response.getRows().isEmpty()) {
            result.put("rows", response.getRows());
        }
        if (0 != response.getAffectedRows()) {
            result.put("affected_rows", response.getAffectedRows());
        }
        if (!response.getMessage().isEmpty()) {
            result.put("message", response.getMessage());
        }
        result.put("truncated", response.isTruncated());
        response.getError().ifPresent(error -> result.put("error", createErrorPayload(toDomainErrorCode(error.getErrorCode()), error.getMessage())));
        return result;
    }
    
    /**
     * Create error payload.
     *
     * @param errorCode error code
     * @param message error message
     * @return payload
     */
    public Map<String, Object> createErrorPayload(final String errorCode, final String message) {
        return Map.of("error_code", errorCode, "message", message);
    }
    
    /**
     * Convert protocol error code to transport domain error code.
     *
     * @param errorCode protocol error code
     * @return domain error code
     */
    public String toDomainErrorCode(final MCPErrorCode errorCode) {
        return errorCode.name().toLowerCase(Locale.ENGLISH);
    }
}

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

package org.apache.shardingsphere.mcp.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.capability.service.MCPServiceCapability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for MCP response payloads.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPResponsePayloadFactory {
    
    /**
     * Create metadata payload.
     *
     * @param metadataItems metadata items
     * @param nextPageToken next page token
     * @return payload
     */
    public static Map<String, Object> createMetadataPayload(final List<?> metadataItems, final String nextPageToken) {
        Map<String, Object> result = new LinkedHashMap<>(metadataItems.size() + 1, 1F);
        result.put("items", metadataItems);
        if (null != nextPageToken && !nextPageToken.isEmpty()) {
            result.put("next_page_token", nextPageToken);
        }
        return result;
    }
    
    /**
     * Create service capability payload.
     *
     * @param serviceCapability service capability
     * @return payload
     */
    public static Map<String, Object> createServiceCapabilityPayload(final MCPServiceCapability serviceCapability) {
        return Map.of("supportedResources", serviceCapability.getSupportedResources(),
                "supportedTools", serviceCapability.getSupportedTools(),
                "supportedStatementClasses", serviceCapability.getSupportedStatementClasses());
    }
    
    /**
     * Create database capability payload.
     *
     * @param databaseCapability database capability
     * @return payload
     */
    public static Map<String, Object> createDatabaseCapabilityPayload(final MCPDatabaseCapability databaseCapability) {
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

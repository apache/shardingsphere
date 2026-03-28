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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class MCPTransportPayloadBuilder {
    
    Map<String, Object> createDatabaseCapabilityPayload(final DatabaseCapability capability) {
        Map<String, Object> result = new LinkedHashMap<>();
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
    
    Map<String, Object> createErrorPayload(final String errorCode, final String message) {
        return Map.of("error_code", errorCode, "message", message);
    }
    
    String toDomainErrorCode(final ErrorCode errorCode) {
        return errorCode.name().toLowerCase(Locale.ENGLISH);
    }
}

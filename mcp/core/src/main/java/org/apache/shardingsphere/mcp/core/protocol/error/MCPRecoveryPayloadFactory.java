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

package org.apache.shardingsphere.mcp.core.protocol.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidApprovedStepsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidMetadataObjectTypesException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMissingToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMultipleSQLStatementsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolArgumentContractViolationException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolCallLimitExceededException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArgumentConflictException;

import java.util.Map;

/**
 * MCP recovery payload factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPRecoveryPayloadFactory {
    
    static Map<String, Object> create(final Throwable cause) {
        if (cause instanceof SQLToolMismatchException) {
            return MCPSQLRecoveryPayloadFactory.createSQLToolMismatchRecovery((SQLToolMismatchException) cause);
        }
        if (cause instanceof MetadataIntrospectionSQLStatementException) {
            return MCPSQLRecoveryPayloadFactory.createMetadataIntrospectionSQLRecovery((MetadataIntrospectionSQLStatementException) cause);
        }
        if (cause instanceof MCPMultipleSQLStatementsException) {
            return MCPSQLRecoveryPayloadFactory.createMultipleStatementsRecovery();
        }
        if (cause instanceof MCPUnsupportedSQLStatementException) {
            return MCPSQLRecoveryPayloadFactory.createUnsupportedStatementRecovery();
        }
        if (cause instanceof MCPBannedSQLStatementException) {
            return MCPSQLRecoveryPayloadFactory.createBannedStatementRecovery();
        }
        if (cause instanceof MCPExecutionModeRequiredException) {
            return MCPWorkflowRecoveryPayloadFactory.createMissingExecutionModeRecovery((MCPExecutionModeRequiredException) cause);
        }
        if (cause instanceof MCPInvalidExecutionModeException) {
            return MCPWorkflowRecoveryPayloadFactory.createInvalidExecutionModeRecovery((MCPInvalidExecutionModeException) cause);
        }
        if (cause instanceof MCPInvalidApprovedStepsException) {
            return MCPWorkflowRecoveryPayloadFactory.createInvalidApprovedStepsRecovery((MCPInvalidApprovedStepsException) cause);
        }
        if (cause instanceof WorkflowArgumentConflictException) {
            return MCPWorkflowRecoveryPayloadFactory.createWorkflowArgumentConflictRecovery((WorkflowArgumentConflictException) cause);
        }
        if (cause instanceof MCPWorkflowStateException) {
            return MCPWorkflowRecoveryPayloadFactory.createWorkflowStateRecovery((MCPWorkflowStateException) cause);
        }
        if (cause instanceof UnsupportedToolException) {
            return MCPBasicRecoveryPayloadFactory.createUnsupportedToolRecovery(((UnsupportedToolException) cause).getToolName());
        }
        if (cause instanceof UnsupportedResourceUriException) {
            return MCPBasicRecoveryPayloadFactory.createUnsupportedResourceRecovery(((UnsupportedResourceUriException) cause).getResourceUri());
        }
        if (cause instanceof RuntimeDatabaseConnectionException) {
            return MCPBasicRecoveryPayloadFactory.createRuntimeDatabaseConnectionRecovery((RuntimeDatabaseConnectionException) cause);
        }
        if (cause instanceof MCPToolCallLimitExceededException) {
            return MCPBasicRecoveryPayloadFactory.createToolCallLimitRecovery((MCPToolCallLimitExceededException) cause);
        }
        if (cause instanceof MCPInvalidToolArgumentException) {
            return MCPBasicRecoveryPayloadFactory.createInvalidToolArgumentRecovery((MCPInvalidToolArgumentException) cause);
        }
        if (cause instanceof MCPToolArgumentContractViolationException) {
            return MCPBasicRecoveryPayloadFactory.createToolArgumentContractViolationRecovery((MCPToolArgumentContractViolationException) cause);
        }
        if (cause instanceof MCPMissingToolArgumentException) {
            return MCPBasicRecoveryPayloadFactory.createMissingArgumentRecovery(((MCPMissingToolArgumentException) cause).getArgumentName());
        }
        if (cause instanceof MCPInvalidMetadataObjectTypesException) {
            return MCPBasicRecoveryPayloadFactory.createInvalidObjectTypesRecovery((MCPInvalidMetadataObjectTypesException) cause);
        }
        if (MCPQueryRecoveryPayloadFactory.isQueryFailure(cause)) {
            return MCPQueryRecoveryPayloadFactory.create(cause);
        }
        return Map.of();
    }
}

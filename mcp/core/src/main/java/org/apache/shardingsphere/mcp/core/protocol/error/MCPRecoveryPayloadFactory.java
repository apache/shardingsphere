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
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ExplainSQLSyntaxException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.RuleDistSQLExecutionException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * MCP recovery payload factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPRecoveryPayloadFactory {
    
    private static final List<RecoveryMapping> RECOVERY_MAPPINGS = List.of(
            new RecoveryMapping(SQLToolMismatchException.class, cause -> MCPSQLRecoveryPayloadFactory.createSQLToolMismatchRecovery((SQLToolMismatchException) cause)),
            new RecoveryMapping(ExplainSQLSyntaxException.class, cause -> MCPSQLRecoveryPayloadFactory.createExplainSQLSyntaxRecovery((ExplainSQLSyntaxException) cause)),
            new RecoveryMapping(RuleDistSQLExecutionException.class, cause -> MCPSQLRecoveryPayloadFactory.createRuleDistSQLExecutionRecovery((RuleDistSQLExecutionException) cause)),
            new RecoveryMapping(MetadataIntrospectionSQLStatementException.class,
                    cause -> MCPSQLRecoveryPayloadFactory.createMetadataIntrospectionSQLRecovery((MetadataIntrospectionSQLStatementException) cause)),
            new RecoveryMapping(MCPMultipleSQLStatementsException.class, cause -> MCPSQLRecoveryPayloadFactory.createMultipleStatementsRecovery()),
            new RecoveryMapping(MCPUnsupportedSQLStatementException.class, cause -> MCPSQLRecoveryPayloadFactory.createUnsupportedStatementRecovery()),
            new RecoveryMapping(MCPBannedSQLStatementException.class, cause -> MCPSQLRecoveryPayloadFactory.createBannedStatementRecovery()),
            new RecoveryMapping(MCPExecutionModeRequiredException.class,
                    cause -> MCPWorkflowRecoveryPayloadFactory.createMissingExecutionModeRecovery((MCPExecutionModeRequiredException) cause)),
            new RecoveryMapping(MCPInvalidExecutionModeException.class,
                    cause -> MCPWorkflowRecoveryPayloadFactory.createInvalidExecutionModeRecovery((MCPInvalidExecutionModeException) cause)),
            new RecoveryMapping(MCPInvalidApprovedStepsException.class,
                    cause -> MCPWorkflowRecoveryPayloadFactory.createInvalidApprovedStepsRecovery((MCPInvalidApprovedStepsException) cause)),
            new RecoveryMapping(MCPWorkflowStateException.class, cause -> MCPWorkflowRecoveryPayloadFactory.createWorkflowStateRecovery((MCPWorkflowStateException) cause)),
            new RecoveryMapping(UnsupportedToolException.class, cause -> MCPBasicRecoveryPayloadFactory.createUnsupportedToolRecovery(((UnsupportedToolException) cause).getToolName())),
            new RecoveryMapping(UnsupportedResourceUriException.class,
                    cause -> MCPBasicRecoveryPayloadFactory.createUnsupportedResourceRecovery(((UnsupportedResourceUriException) cause).getResourceUri())),
            new RecoveryMapping(RuntimeDatabaseConnectionException.class,
                    cause -> MCPBasicRecoveryPayloadFactory.createRuntimeDatabaseConnectionRecovery((RuntimeDatabaseConnectionException) cause)),
            new RecoveryMapping(MCPToolCallLimitExceededException.class,
                    cause -> MCPBasicRecoveryPayloadFactory.createToolCallLimitRecovery((MCPToolCallLimitExceededException) cause)),
            new RecoveryMapping(MCPInvalidToolArgumentException.class, cause -> MCPBasicRecoveryPayloadFactory.createInvalidToolArgumentRecovery((MCPInvalidToolArgumentException) cause)),
            new RecoveryMapping(MCPToolArgumentContractViolationException.class,
                    cause -> MCPBasicRecoveryPayloadFactory.createToolArgumentContractViolationRecovery((MCPToolArgumentContractViolationException) cause)),
            new RecoveryMapping(MCPMissingToolArgumentException.class,
                    cause -> MCPBasicRecoveryPayloadFactory.createMissingArgumentRecovery(((MCPMissingToolArgumentException) cause).getArgumentName())),
            new RecoveryMapping(MCPInvalidMetadataObjectTypesException.class,
                    cause -> MCPBasicRecoveryPayloadFactory.createInvalidObjectTypesRecovery((MCPInvalidMetadataObjectTypesException) cause)));
    
    static Map<String, Object> create(final Throwable cause) {
        for (RecoveryMapping each : RECOVERY_MAPPINGS) {
            if (each.matches(cause)) {
                return each.create(cause);
            }
        }
        if (MCPQueryRecoveryPayloadFactory.isQueryFailure(cause)) {
            return MCPQueryRecoveryPayloadFactory.create(cause);
        }
        return Map.of();
    }
    
    private record RecoveryMapping(Class<? extends Throwable> causeType, Function<Throwable, Map<String, Object>> payloadFactory) {
        
        private boolean matches(final Throwable cause) {
            return causeType.isInstance(cause);
        }
        
        private Map<String, Object> create(final Throwable cause) {
            return payloadFactory.apply(cause);
        }
    }
}

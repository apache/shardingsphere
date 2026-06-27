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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.Locale;

/**
 * Readwrite-splitting status DistSQL planning service.
 */
public final class ReadwriteSplittingStatusDistSQLPlanningService {
    
    /**
     * Plan readwrite-splitting storage-unit status artifact.
     *
     * @param request status workflow request
     * @return rule artifact
     * @throws MCPInvalidRequestException when no status operation can be resolved
     */
    public RuleArtifact planStatus(final ReadwriteSplittingStatusWorkflowRequest request) {
        String operation = resolveStatusOperation(request);
        if (operation.isEmpty()) {
            throw new MCPInvalidRequestException("target_status is required for readwrite-splitting status.");
        }
        return new RuleArtifact(operation.toLowerCase(Locale.ENGLISH), String.format("ALTER READWRITE_SPLITTING RULE %s %s %s FROM %s",
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getRuleName()), operation,
                WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getStorageUnit()), WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getDatabase())));
    }
    
    /**
     * Resolve ENABLE or DISABLE operation from request fields.
     *
     * @param request status workflow request
     * @return DistSQL status operation
     */
    public String resolveStatusOperation(final ReadwriteSplittingStatusWorkflowRequest request) {
        return normalizeStatusOperation(request.getTargetStatus());
    }
    
    private String normalizeStatusOperation(final String status) {
        String actualStatus = status.trim().toLowerCase(Locale.ENGLISH);
        if ("enable".equals(actualStatus) || "enabled".equals(actualStatus)) {
            return "ENABLE";
        }
        if ("disable".equals(actualStatus) || "disabled".equals(actualStatus)) {
            return "DISABLE";
        }
        return "";
    }
}

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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.List;

/**
 * Mask rule DistSQL planning service.
 */
public final class MaskRuleDistSQLPlanningService {
    
    /**
     * Plan mask rule artifact.
     *
     * @param request workflow request
     * @return rule artifact
     */
    public RuleArtifact planMaskRule(final WorkflowRequest request) {
        validateMaskIdentifiers(request);
        return new RuleArtifact(WorkflowLifecycle.OPERATION_CREATE, createMaskRuleSql("CREATE MASK RULE", request.getTable(), List.of(createTargetMaskColumnSegment(request))));
    }
    
    /**
     * Plan mask drop artifact.
     *
     * @param request workflow request
     * @return rule artifact
     */
    public RuleArtifact planMaskDropRule(final WorkflowRequest request) {
        validateMaskIdentifiers(request);
        return new RuleArtifact(WorkflowLifecycle.OPERATION_DROP, String.format("DROP MASK RULE %s", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getTable())));
    }
    
    private void validateMaskIdentifiers(final WorkflowRequest request) {
        WorkflowSQLUtils.checkSupportedIdentifier("table", request.getTable());
        WorkflowSQLUtils.checkSupportedIdentifier("column", request.getColumn());
    }
    
    private String createTargetMaskColumnSegment(final WorkflowRequest request) {
        return String.format("(NAME=%s, %s)", WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(request.getColumn()),
                WorkflowSQLUtils.createAlgorithmFragment(request.getAlgorithmType(), request.getPrimaryAlgorithmProperties()));
    }
    
    private String createMaskRuleSql(final String prefix, final String tableName, final List<String> columnSegments) {
        WorkflowSQLUtils.checkSupportedIdentifier("table", tableName);
        return String.format("%s %s (%sCOLUMNS(%s%s%s))", prefix, WorkflowSQLUtils.formatGeneratedRuleDistSQLIdentifier(tableName), System.lineSeparator(), System.lineSeparator(),
                String.join(", " + System.lineSeparator(), columnSegments), System.lineSeparator());
    }
}

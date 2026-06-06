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

import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule DistSQL planning service.
 */
public final class ReadwriteSplittingRuleDistSQLPlanningService {
    
    /**
     * Plan create rule artifact.
     *
     * @param request rule workflow request
     * @return rule artifact
     */
    public RuleArtifact planCreateRule(final ReadwriteSplittingRuleWorkflowRequest request) {
        return new RuleArtifact("create", String.format("CREATE READWRITE_SPLITTING RULE %s", createRuleDefinition(request)));
    }
    
    /**
     * Plan alter rule artifact.
     *
     * @param request rule workflow request
     * @return rule artifact
     */
    public RuleArtifact planAlterRule(final ReadwriteSplittingRuleWorkflowRequest request) {
        return new RuleArtifact("alter", String.format("ALTER READWRITE_SPLITTING RULE %s", createRuleDefinition(request)));
    }
    
    /**
     * Plan drop rule artifact.
     *
     * @param ruleName rule name
     * @return rule artifact
     */
    public RuleArtifact planDropRule(final String ruleName) {
        return new RuleArtifact("drop", String.format("DROP READWRITE_SPLITTING RULE %s", WorkflowSQLUtils.formatDistSQLIdentifier(ruleName)));
    }
    
    private String createRuleDefinition(final ReadwriteSplittingRuleWorkflowRequest request) {
        return String.format("%s (WRITE_STORAGE_UNIT=%s, READ_STORAGE_UNITS(%s), TRANSACTIONAL_READ_QUERY_STRATEGY='%s'%s)",
                WorkflowSQLUtils.formatDistSQLIdentifier(request.getRuleName()),
                WorkflowSQLUtils.formatDistSQLIdentifier(request.getWriteStorageUnit()),
                request.getReadStorageUnits().stream().map(WorkflowSQLUtils::formatDistSQLIdentifier).collect(Collectors.joining(", ")),
                WorkflowSQLUtils.escapeLiteral(request.getTransactionalReadQueryStrategy().toUpperCase(Locale.ENGLISH)),
                createLoadBalancerFragment(request));
    }
    
    private String createLoadBalancerFragment(final ReadwriteSplittingRuleWorkflowRequest request) {
        String algorithmFragment = WorkflowSQLUtils.createAlgorithmFragment(request.getLoadBalancerType(), request.getLoadBalancerProperties());
        return algorithmFragment.isEmpty() ? "" : ", " + algorithmFragment;
    }
}

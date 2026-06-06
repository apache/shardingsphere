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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Broadcast rule DistSQL planning service.
 */
public final class BroadcastRuleDistSQLPlanningService {
    
    /**
     * Plan create broadcast rule artifact.
     *
     * @param tableNames broadcast table names
     * @return rule artifact
     */
    public RuleArtifact planCreateRule(final List<String> tableNames) {
        return new RuleArtifact("create", String.format("CREATE BROADCAST TABLE RULE %s", formatTableNames(tableNames)));
    }
    
    /**
     * Plan drop broadcast rule artifact.
     *
     * @param tableNames broadcast table names
     * @return rule artifact
     */
    public RuleArtifact planDropRule(final List<String> tableNames) {
        return new RuleArtifact("drop", String.format("DROP BROADCAST TABLE RULE %s", formatTableNames(tableNames)));
    }
    
    private String formatTableNames(final List<String> tableNames) {
        return tableNames.stream().peek(each -> WorkflowSQLUtils.checkSupportedIdentifier("table", each))
                .map(WorkflowSQLUtils::formatDistSQLIdentifier).collect(Collectors.joining(", "));
    }
}

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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Physical DDL planning service.
 */
public final class PhysicalDDLPlanningService {
    
    private static final String DEFAULT_DERIVED_COLUMN_DATA_TYPE = "VARCHAR(4000)";
    
    /**
     * Plan physical DDL artifacts.
     *
     * @param tableName table name
     * @param derivedColumnPlan derived column plan
     * @param existingPhysicalNames existing physical names
     * @param derivedColumnDefinition derived column definition
     * @return DDL artifacts
     */
    public List<DDLArtifact> planAddColumnArtifacts(final String tableName, final DerivedColumnPlan derivedColumnPlan,
                                                    final Set<String> existingPhysicalNames, final String derivedColumnDefinition) {
        WorkflowSqlUtils.checkSafeIdentifier("table", tableName);
        validateDerivedColumnIdentifiers(derivedColumnPlan);
        String actualDerivedColumnDefinition = WorkflowSqlUtils.trimToEmpty(derivedColumnDefinition).isEmpty() ? DEFAULT_DERIVED_COLUMN_DATA_TYPE : derivedColumnDefinition;
        List<String> clauses = new LinkedList<>();
        if (derivedColumnPlan.isCipherColumnRequired() && !existingPhysicalNames.contains(derivedColumnPlan.getCipherColumnName())) {
            clauses.add(String.format("ADD COLUMN %s %s", derivedColumnPlan.getCipherColumnName(), actualDerivedColumnDefinition));
        }
        if (derivedColumnPlan.isAssistedQueryColumnRequired() && !WorkflowSqlUtils.trimToEmpty(derivedColumnPlan.getAssistedQueryColumnName()).isEmpty()
                && !existingPhysicalNames.contains(derivedColumnPlan.getAssistedQueryColumnName())) {
            clauses.add(String.format("ADD COLUMN %s %s", derivedColumnPlan.getAssistedQueryColumnName(), actualDerivedColumnDefinition));
        }
        if (derivedColumnPlan.isLikeQueryColumnRequired() && !WorkflowSqlUtils.trimToEmpty(derivedColumnPlan.getLikeQueryColumnName()).isEmpty()
                && !existingPhysicalNames.contains(derivedColumnPlan.getLikeQueryColumnName())) {
            clauses.add(String.format("ADD COLUMN %s %s", derivedColumnPlan.getLikeQueryColumnName(), actualDerivedColumnDefinition));
        }
        if (clauses.isEmpty()) {
            return List.of();
        }
        return List.of(new DDLArtifact("add-column", String.format("ALTER TABLE %s %s", tableName, String.join(", ", clauses)), 10));
    }
    
    private void validateDerivedColumnIdentifiers(final DerivedColumnPlan derivedColumnPlan) {
        WorkflowSqlUtils.checkSafeIdentifier("cipher_column", derivedColumnPlan.getCipherColumnName());
        WorkflowSqlUtils.checkSafeIdentifier("assisted_query_column", derivedColumnPlan.getAssistedQueryColumnName());
        WorkflowSqlUtils.checkSafeIdentifier("like_query_column", derivedColumnPlan.getLikeQueryColumnName());
    }
}

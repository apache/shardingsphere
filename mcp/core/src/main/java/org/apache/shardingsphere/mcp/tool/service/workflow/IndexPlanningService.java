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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Index planning service.
 */
public final class IndexPlanningService {
    
    /**
     * Plan index artifacts.
     *
     * @param tableName table name
     * @param derivedColumnPlan derived column plan
     * @param existingIndexes existing indexes
     * @return index plans
     */
    public List<IndexPlan> planIndexes(final String tableName, final DerivedColumnPlan derivedColumnPlan, final Set<String> existingIndexes) {
        WorkflowSqlUtils.checkSafeIdentifier("table", tableName);
        List<IndexPlan> result = new LinkedList<>();
        if (derivedColumnPlan.isAssistedQueryColumnRequired() && !WorkflowSqlUtils.trimToEmpty(derivedColumnPlan.getAssistedQueryColumnName()).isEmpty()) {
            result.add(createIndexPlan(tableName, derivedColumnPlan.getAssistedQueryColumnName(), "Recommended for assisted query performance.", existingIndexes));
        }
        if (derivedColumnPlan.isLikeQueryColumnRequired() && !WorkflowSqlUtils.trimToEmpty(derivedColumnPlan.getLikeQueryColumnName()).isEmpty()) {
            result.add(createIndexPlan(tableName, derivedColumnPlan.getLikeQueryColumnName(), "Recommended for like-query performance.", existingIndexes));
        }
        return result;
    }
    
    private IndexPlan createIndexPlan(final String tableName, final String columnName, final String reason, final Set<String> existingIndexes) {
        WorkflowSqlUtils.checkSafeIdentifier("column", columnName);
        String baseIndexName = "idx_" + tableName + "_" + columnName;
        String actualIndexName = baseIndexName;
        int suffix = 1;
        while (existingIndexes.contains(actualIndexName)) {
            actualIndexName = baseIndexName + "_" + suffix;
            suffix++;
        }
        existingIndexes.add(actualIndexName);
        return new IndexPlan(actualIndexName, columnName, reason, String.format("CREATE INDEX %s ON %s (%s)", actualIndexName, tableName, columnName));
    }
}

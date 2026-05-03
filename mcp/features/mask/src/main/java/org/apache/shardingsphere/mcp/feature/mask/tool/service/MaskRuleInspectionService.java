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

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Mask rule inspection service.
 */
public final class MaskRuleInspectionService {
    
    /**
     * Query mask rules.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @param tableName table name
     * @return mask rules
     */
    public List<Map<String, Object>> queryMaskRules(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : queryFacade.query(databaseName, "", buildShowMaskRulesSQL(databaseName, tableName))) {
            Map<String, Object> actualRow = new LinkedHashMap<>(each);
            putAliasIfAbsent(actualRow, "column", "logic_column");
            putAliasIfAbsent(actualRow, "algorithm_type", "mask_algorithm");
            putAliasIfAbsent(actualRow, "algorithm_props", "props");
            result.add(actualRow);
        }
        return result;
    }
    
    private String buildShowMaskRulesSQL(final String databaseName, final String tableName) {
        WorkflowSQLUtils.checkSafeIdentifier("database", databaseName);
        WorkflowSQLUtils.checkSafeIdentifier("table", tableName);
        return tableName.isEmpty() ? String.format("SHOW MASK RULES FROM %s", databaseName) : String.format("SHOW MASK RULE %s FROM %s", tableName, databaseName);
    }
    
    private void putAliasIfAbsent(final Map<String, Object> row, final String targetKey, final String sourceKey) {
        if (!row.containsKey(targetKey) && row.containsKey(sourceKey)) {
            row.put(targetKey, row.get(sourceKey));
        }
    }
    
    /**
     * Enrich mask algorithm plugins with MCP-specific metadata.
     *
     * @param queryFacade query facade
     * @return enriched plugin rows
     */
    public List<Map<String, Object>> enrichMaskAlgorithms(final MCPFeatureQueryFacade queryFacade) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS")) {
            String type = Objects.toString(each.get("type"), "").trim().toUpperCase(Locale.ENGLISH);
            Map<String, Object> row = new LinkedHashMap<>(each);
            row.put("source", MaskAlgorithmRecommendationService.isKnownMaskAlgorithm(type) ? "builtin" : "custom-spi");
            result.add(row);
        }
        return result;
    }
}

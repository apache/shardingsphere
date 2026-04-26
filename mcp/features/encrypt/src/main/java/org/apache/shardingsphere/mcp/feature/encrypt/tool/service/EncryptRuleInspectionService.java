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

import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Encrypt rule inspection service.
 */
public final class EncryptRuleInspectionService {
    
    /**
     * Query encrypt rules.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @param tableName table name
     * @return encrypt rules
     */
    public List<Map<String, Object>> queryEncryptRules(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName) {
        return normalizeEncryptRuleRows(queryFacade.query(databaseName, "", buildShowEncryptRulesSql(databaseName, tableName)));
    }
    
    /**
     * Query encrypt algorithms.
     *
     * @param queryFacade query facade
     * @return encrypt algorithm plugins
     */
    public List<Map<String, Object>> queryEncryptAlgorithms(final MCPFeatureQueryFacade queryFacade) {
        return queryFacade.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS");
    }
    
    /**
     * Enrich encrypt algorithm plugins with MCP-specific metadata.
     *
     * @param rawRows raw plugin rows
     * @return enriched plugin rows
     */
    public List<Map<String, Object>> enrichEncryptAlgorithms(final List<Map<String, Object>> rawRows) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rawRows) {
            String type = WorkflowSqlUtils.trimToEmpty(String.valueOf(each.get("type"))).toUpperCase(Locale.ENGLISH);
            Map<String, Boolean> capability = EncryptAlgorithmRecommendationService.findEncryptCapability(type);
            Map<String, Object> row = new LinkedHashMap<>(each);
            row.put("source", EncryptAlgorithmRecommendationService.isKnownEncryptAlgorithm(type) ? "builtin" : "custom-spi");
            row.put("supports_decrypt", capability.get("supports_decrypt"));
            row.put("supports_equivalent_filter", capability.get("supports_equivalent_filter"));
            row.put("supports_like", capability.get("supports_like"));
            result.add(row);
        }
        return result;
    }
    
    private String buildShowEncryptRulesSql(final String databaseName, final String tableName) {
        String actualDatabaseName = WorkflowSqlUtils.trimToEmpty(databaseName);
        String actualTableName = WorkflowSqlUtils.trimToEmpty(tableName);
        WorkflowSqlUtils.checkSafeIdentifier("database", actualDatabaseName);
        WorkflowSqlUtils.checkSafeIdentifier("table", actualTableName);
        return actualTableName.isEmpty()
                ? String.format("SHOW ENCRYPT RULES FROM %s", actualDatabaseName)
                : String.format("SHOW ENCRYPT TABLE RULE %s FROM %s", actualTableName, actualDatabaseName);
    }
    
    private List<Map<String, Object>> normalizeEncryptRuleRows(final List<Map<String, Object>> rawRows) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rawRows) {
            Map<String, Object> actualRow = new LinkedHashMap<>(each);
            putAliasIfAbsent(actualRow, "assisted_query_column", "assisted_query");
            putAliasIfAbsent(actualRow, "like_query_column", "like_query");
            result.add(actualRow);
        }
        return result;
    }
    
    private void putAliasIfAbsent(final Map<String, Object> row, final String targetKey, final String sourceKey) {
        if (!row.containsKey(targetKey) && row.containsKey(sourceKey)) {
            row.put(targetKey, row.get(sourceKey));
        }
    }
}

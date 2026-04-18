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

import org.apache.shardingsphere.mcp.context.MCPRequestContext;

import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Rule inspection service.
 */
public final class RuleInspectionService {
    
    private final WorkflowProxyQueryService queryService;
    
    public RuleInspectionService() {
        this(new WorkflowProxyQueryService());
    }
    
    RuleInspectionService(final WorkflowProxyQueryService queryService) {
        this.queryService = queryService;
    }
    
    /**
     * Query encrypt rules.
     *
     * @param requestContext request context
     * @param databaseName database name
     * @param tableName table name
     * @return encrypt rules
     */
    public List<Map<String, Object>> queryEncryptRules(final MCPRequestContext requestContext, final String databaseName, final String tableName) {
        return normalizeEncryptRuleRows(queryService.query(requestContext, databaseName, "", buildShowEncryptRulesSql(databaseName, tableName)));
    }
    
    /**
     * Query mask rules.
     *
     * @param requestContext request context
     * @param databaseName database name
     * @param tableName table name
     * @return mask rules
     */
    public List<Map<String, Object>> queryMaskRules(final MCPRequestContext requestContext, final String databaseName, final String tableName) {
        return normalizeMaskRuleRows(queryService.query(requestContext, databaseName, "", buildShowMaskRulesSql(databaseName, tableName)));
    }
    
    /**
     * Query encrypt algorithms.
     *
     * @param requestContext request context
     * @return encrypt algorithm plugins
     */
    public List<Map<String, Object>> queryEncryptAlgorithms(final MCPRequestContext requestContext) {
        return queryService.queryWithAnyDatabase(requestContext, "SHOW ENCRYPT ALGORITHM PLUGINS");
    }
    
    /**
     * Query mask algorithms.
     *
     * @param requestContext request context
     * @return mask algorithm plugins
     */
    public List<Map<String, Object>> queryMaskAlgorithms(final MCPRequestContext requestContext) {
        return queryService.queryWithAnyDatabase(requestContext, "SHOW MASK ALGORITHM PLUGINS");
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
            Map<String, Boolean> capability = AlgorithmRecommendationService.findEncryptCapability(type);
            Map<String, Object> row = new LinkedHashMap<>(each);
            row.put("source", AlgorithmRecommendationService.isKnownEncryptAlgorithm(type) ? "builtin" : "custom-spi");
            row.put("supports_decrypt", capability.get("supports_decrypt"));
            row.put("supports_equivalent_filter", capability.get("supports_equivalent_filter"));
            row.put("supports_like", capability.get("supports_like"));
            result.add(row);
        }
        return result;
    }
    
    /**
     * Enrich mask algorithm plugins with MCP-specific metadata.
     *
     * @param rawRows raw plugin rows
     * @return enriched plugin rows
     */
    public List<Map<String, Object>> enrichMaskAlgorithms(final List<Map<String, Object>> rawRows) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rawRows) {
            String type = WorkflowSqlUtils.trimToEmpty(String.valueOf(each.get("type"))).toUpperCase(Locale.ENGLISH);
            Map<String, Object> row = new LinkedHashMap<>(each);
            row.put("source", AlgorithmRecommendationService.isKnownMaskAlgorithm(type) ? "builtin" : "custom-spi");
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
    
    private String buildShowMaskRulesSql(final String databaseName, final String tableName) {
        String actualDatabaseName = WorkflowSqlUtils.trimToEmpty(databaseName);
        String actualTableName = WorkflowSqlUtils.trimToEmpty(tableName);
        WorkflowSqlUtils.checkSafeIdentifier("database", actualDatabaseName);
        WorkflowSqlUtils.checkSafeIdentifier("table", actualTableName);
        return actualTableName.isEmpty()
                ? String.format("SHOW MASK RULES FROM %s", actualDatabaseName)
                : String.format("SHOW MASK RULE %s FROM %s", actualTableName, actualDatabaseName);
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
    
    private List<Map<String, Object>> normalizeMaskRuleRows(final List<Map<String, Object>> rawRows) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : rawRows) {
            Map<String, Object> actualRow = new LinkedHashMap<>(each);
            putAliasIfAbsent(actualRow, "column", "logic_column");
            putAliasIfAbsent(actualRow, "algorithm_type", "mask_algorithm");
            putAliasIfAbsent(actualRow, "algorithm_props", "props");
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

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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowDistSQLQueryUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sharding inspection service.
 */
public final class ShardingInspectionService {
    
    /**
     * Query DistSQL-visible sharding algorithm plugins.
     *
     * @param queryFacade feature query facade
     * @return sharding algorithm plugin rows
     */
    public List<Map<String, Object>> queryAlgorithmPlugins(final MCPFeatureQueryFacade queryFacade) {
        return queryPluginRows(queryFacade, "SHOW SHARDING ALGORITHM PLUGINS", getShardingAlgorithmPluginRows()).stream().map(this::appendShardingAlgorithmGuidance).toList();
    }
    
    /**
     * Query DistSQL-visible key generate algorithm plugins.
     *
     * @param queryFacade feature query facade
     * @return key generate algorithm plugin rows
     */
    public List<Map<String, Object>> queryKeyGenerateAlgorithmPlugins(final MCPFeatureQueryFacade queryFacade) {
        return queryPluginRows(queryFacade, "SHOW KEY GENERATE ALGORITHM PLUGINS", getKeyGenerateAlgorithmPluginRows()).stream().map(this::appendKeyGeneratorGuidance).toList();
    }
    
    private List<Map<String, Object>> queryPluginRows(final MCPFeatureQueryFacade queryFacade, final String sql, final List<Map<String, Object>> fallbackRows) {
        try {
            List<Map<String, Object>> result = queryFacade.queryWithAnyDatabase(sql);
            return null == result ? fallbackRows : result;
        } catch (final MCPQueryFailedException ex) {
            if (WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(ex)) {
                return fallbackRows;
            }
            throw ex;
        }
    }
    
    private List<Map<String, Object>> getShardingAlgorithmPluginRows() {
        return List.of(
                Map.of("type", "MOD"),
                Map.of("type", "HASH_MOD"),
                Map.of("type", "VOLUME_RANGE"),
                Map.of("type", "BOUNDARY_RANGE"),
                Map.of("type", "AUTO_INTERVAL"),
                Map.of("type", "INTERVAL"),
                Map.of("type", "CLASS_BASED"),
                Map.of("type", "INLINE"),
                Map.of("type", "COMPLEX_INLINE"),
                Map.of("type", "HINT_INLINE"));
    }
    
    private List<Map<String, Object>> getKeyGenerateAlgorithmPluginRows() {
        return List.of(Map.of("type", "SNOWFLAKE"), Map.of("type", "UUID"));
    }
    
    /**
     * Query DistSQL-visible sharding algorithms.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return sharding algorithm rows
     */
    public List<Map<String, Object>> queryAlgorithms(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING ALGORITHMS FROM %s", format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding table rules.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return sharding table rule rows
     */
    public List<Map<String, Object>> queryTableRules(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE RULES FROM %s", format(databaseName)));
    }
    
    /**
     * Query a DistSQL-visible sharding table rule.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param tableName table name
     * @return sharding table rule rows
     */
    public List<Map<String, Object>> queryTableRule(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE RULE %s FROM %s", format(tableName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding table nodes.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return sharding table node rows
     */
    public List<Map<String, Object>> queryTableNodes(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE NODES FROM %s", format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding table nodes for a table.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param tableName table name
     * @return sharding table node rows
     */
    public List<Map<String, Object>> queryTableNode(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE NODES %s FROM %s", format(tableName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding table reference rules.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return table reference rule rows
     */
    public List<Map<String, Object>> queryTableReferenceRules(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE REFERENCE RULES FROM %s", format(databaseName)));
    }
    
    /**
     * Query a DistSQL-visible sharding table reference rule.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param ruleName rule name
     * @return table reference rule rows
     */
    public List<Map<String, Object>> queryTableReferenceRule(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String ruleName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE REFERENCE RULE %s FROM %s", format(ruleName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible default sharding strategy.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return default sharding strategy rows
     */
    public List<Map<String, Object>> queryDefaultStrategy(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW DEFAULT SHARDING STRATEGY FROM %s", format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding key generators.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return key generator rows
     */
    public List<Map<String, Object>> queryKeyGenerators(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING KEY GENERATORS FROM %s", format(databaseName)));
    }
    
    /**
     * Query a DistSQL-visible sharding key generator.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param keyGeneratorName key generator name
     * @return key generator rows
     */
    public List<Map<String, Object>> queryKeyGenerator(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String keyGeneratorName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING KEY GENERATOR %s FROM %s", format(keyGeneratorName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding key generate strategies.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return key generate strategy rows
     */
    public List<Map<String, Object>> queryKeyGenerateStrategies(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING KEY GENERATE STRATEGIES FROM %s", format(databaseName)));
    }
    
    /**
     * Query a DistSQL-visible sharding key generate strategy.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param strategyName strategy name
     * @return key generate strategy rows
     */
    public List<Map<String, Object>> queryKeyGenerateStrategy(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String strategyName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING KEY GENERATE STRATEGY %s FROM %s", format(strategyName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding auditors.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return auditor rows
     */
    public List<Map<String, Object>> queryAuditors(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING AUDITORS FROM %s", format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible unused sharding algorithms.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return unused sharding algorithm rows
     */
    public List<Map<String, Object>> queryUnusedAlgorithms(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW UNUSED SHARDING ALGORITHMS FROM %s", format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible unused sharding key generators.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return unused sharding key generator rows
     */
    public List<Map<String, Object>> queryUnusedKeyGenerators(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW UNUSED SHARDING KEY GENERATORS FROM %s", format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible unused sharding auditors.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return unused sharding auditor rows
     */
    public List<Map<String, Object>> queryUnusedAuditors(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW UNUSED SHARDING AUDITORS FROM %s", format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible table rules that use a sharding algorithm.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param algorithmName algorithm name
     * @return table rule rows
     */
    public List<Map<String, Object>> queryTableRulesUsedAlgorithm(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String algorithmName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE RULES USED ALGORITHM %s FROM %s", format(algorithmName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible table rules that use a sharding key generator.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param keyGeneratorName key generator name
     * @return table rule rows
     */
    public List<Map<String, Object>> queryTableRulesUsedKeyGenerator(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String keyGeneratorName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE RULES USED KEY GENERATOR %s FROM %s", format(keyGeneratorName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible table rules that use a sharding auditor.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @param auditorName auditor name
     * @return table rule rows
     */
    public List<Map<String, Object>> queryTableRulesUsedAuditor(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String auditorName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SHARDING TABLE RULES USED AUDITOR %s FROM %s", format(auditorName), format(databaseName)));
    }
    
    /**
     * Query DistSQL-visible sharding rule count.
     *
     * @param queryFacade feature query facade
     * @param databaseName database name
     * @return rule count rows
     */
    public List<Map<String, Object>> queryRuleCount(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("COUNT SHARDING RULE FROM %s", format(databaseName)));
    }
    
    private String format(final String value) {
        return WorkflowSQLUtils.formatDistSQLIdentifier(value);
    }
    
    private Map<String, Object> appendShardingAlgorithmGuidance(final Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.putIfAbsent("property_guidance", resolveShardingAlgorithmGuidance(String.valueOf(row.getOrDefault("type", row.getOrDefault("name", "")))));
        return result;
    }
    
    private Map<String, Object> appendKeyGeneratorGuidance(final Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.putIfAbsent("property_guidance", resolveKeyGeneratorGuidance(String.valueOf(row.getOrDefault("type", row.getOrDefault("name", "")))));
        return result;
    }
    
    private String resolveShardingAlgorithmGuidance(final String algorithmType) {
        String actualType = algorithmType.trim().toUpperCase(Locale.ENGLISH);
        if (actualType.contains("INLINE")) {
            return "Usually requires algorithm-expression.";
        }
        if ("MOD".equals(actualType)) {
            return "Usually requires sharding-count.";
        }
        return "Read the plugin documentation for required properties before planning.";
    }
    
    private String resolveKeyGeneratorGuidance(final String algorithmType) {
        String actualType = algorithmType.trim().toUpperCase(Locale.ENGLISH);
        if ("SNOWFLAKE".equals(actualType)) {
            return "Optional worker-id properties may be supplied when required by deployment.";
        }
        if ("UUID".equals(actualType)) {
            return "No required properties.";
        }
        return "Read the plugin documentation for required properties before planning.";
    }
}

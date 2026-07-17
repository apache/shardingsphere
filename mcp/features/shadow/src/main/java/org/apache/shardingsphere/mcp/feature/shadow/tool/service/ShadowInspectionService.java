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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowDistSQLQueryUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shadow rule inspection service.
 */
public final class ShadowInspectionService {
    
    /**
     * Query shadow rules.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return shadow rule rows
     */
    public List<Map<String, Object>> queryRules(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("SHOW SHADOW RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query one shadow rule.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @param ruleName rule name
     * @return shadow rule rows
     */
    public List<Map<String, Object>> queryRule(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String ruleName) {
        return queryFacade.query(databaseName, String.format("SHOW SHADOW RULE %s FROM %s",
                WorkflowSQLUtils.formatDistSQLIdentifier(ruleName), WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query shadow table rules.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return shadow table rule rows
     */
    public List<Map<String, Object>> queryTableRules(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("SHOW SHADOW TABLE RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query one table's shadow table rules.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @param tableName table name
     * @return shadow table rule rows
     */
    public List<Map<String, Object>> queryTableRule(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName) {
        return queryFacade.query(databaseName, String.format("SHOW SHADOW TABLE RULE %s FROM %s",
                WorkflowSQLUtils.formatDistSQLIdentifier(tableName), WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query configured shadow algorithms.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return configured shadow algorithm rows
     */
    public List<Map<String, Object>> queryAlgorithms(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("SHOW SHADOW ALGORITHMS FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query default shadow algorithm.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return default shadow algorithm rows
     */
    public List<Map<String, Object>> queryDefaultAlgorithm(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("SHOW DEFAULT SHADOW ALGORITHM FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query shadow rule count.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return count rows
     */
    public List<Map<String, Object>> queryRuleCount(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("COUNT SHADOW RULE FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query shadow algorithm plugin catalog.
     *
     * @param queryFacade query facade
     * @return shadow algorithm plugin rows with built-in property guidance
     */
    public List<Map<String, Object>> queryAlgorithmPlugins(final MCPFeatureQueryFacade queryFacade) {
        return queryAlgorithmRows(queryFacade).stream().map(this::appendPropertyGuidance).toList();
    }
    
    private List<Map<String, Object>> queryAlgorithmRows(final MCPFeatureQueryFacade queryFacade) {
        try {
            List<Map<String, Object>> result = queryFacade.queryWithAnyDatabase("SHOW SHADOW ALGORITHM PLUGINS");
            return null == result ? List.of(Map.of("type", "SQL_HINT"), Map.of("type", "REGEX_MATCH"), Map.of("type", "VALUE_MATCH")) : result;
        } catch (final MCPQueryFailedException ex) {
            if (WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(ex)) {
                return List.of(Map.of("type", "SQL_HINT"), Map.of("type", "REGEX_MATCH"), Map.of("type", "VALUE_MATCH"));
            }
            throw ex;
        }
    }
    
    private Map<String, Object> appendPropertyGuidance(final Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row);
        result.putIfAbsent("property_guidance", resolvePropertyGuidance(String.valueOf(row.getOrDefault("type", row.getOrDefault("name", "")))));
        return result;
    }
    
    private String resolvePropertyGuidance(final String algorithmType) {
        String actualType = algorithmType.trim().toUpperCase(Locale.ENGLISH);
        if ("VALUE_MATCH".equals(actualType)) {
            return "Requires operation, column and value properties.";
        }
        if ("REGEX_MATCH".equals(actualType)) {
            return "Requires operation, column and regex properties.";
        }
        if ("SQL_HINT".equals(actualType)) {
            return "No required properties. This is the recommended default shadow algorithm type.";
        }
        return "Read the plugin documentation for required properties before planning.";
    }
}

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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowDistSQLQueryUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Readwrite-splitting rule inspection service.
 */
public final class ReadwriteSplittingInspectionService {
    
    /**
     * Query readwrite-splitting rules.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return readwrite-splitting rules
     */
    public List<Map<String, Object>> queryRules(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("SHOW READWRITE_SPLITTING RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query one readwrite-splitting rule.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @param ruleName rule name
     * @return readwrite-splitting rule rows
     */
    public List<Map<String, Object>> queryRule(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String ruleName) {
        return queryFacade.query(databaseName, String.format("SHOW READWRITE_SPLITTING RULE %s FROM %s",
                WorkflowSQLUtils.formatDistSQLIdentifier(ruleName), WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query readwrite-splitting rule count.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return count rows
     */
    public List<Map<String, Object>> queryRuleCount(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("COUNT READWRITE_SPLITTING RULE FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query all readwrite-splitting storage-unit statuses.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @return status rows
     */
    public List<Map<String, Object>> queryStatuses(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, String.format("SHOW STATUS FROM READWRITE_SPLITTING RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query one readwrite-splitting rule's storage-unit statuses.
     *
     * @param queryFacade query facade
     * @param databaseName logical database name
     * @param ruleName rule name
     * @return status rows
     */
    public List<Map<String, Object>> queryRuleStatus(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String ruleName) {
        return queryFacade.query(databaseName, String.format("SHOW STATUS FROM READWRITE_SPLITTING RULE %s FROM %s",
                WorkflowSQLUtils.formatDistSQLIdentifier(ruleName), WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query load-balance algorithm plugin catalog.
     *
     * @param queryFacade query facade
     * @return algorithm plugin rows with built-in property hints
     */
    public List<Map<String, Object>> queryLoadBalanceAlgorithmPlugins(final MCPFeatureQueryFacade queryFacade) {
        return queryAlgorithmRows(queryFacade).stream().map(this::appendPropertyGuidance).toList();
    }
    
    private List<Map<String, Object>> queryAlgorithmRows(final MCPFeatureQueryFacade queryFacade) {
        try {
            List<Map<String, Object>> result = queryFacade.queryWithAnyDatabase("SHOW LOAD BALANCE ALGORITHM PLUGINS");
            return null == result ? List.of(Map.of("type", "RANDOM"), Map.of("type", "ROUND_ROBIN"), Map.of("type", "WEIGHT")) : result;
        } catch (final MCPQueryFailedException ex) {
            if (WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(ex)) {
                return List.of(Map.of("type", "RANDOM"), Map.of("type", "ROUND_ROBIN"), Map.of("type", "WEIGHT"));
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
        if ("WEIGHT".equals(actualType)) {
            return "Provide one numeric property per read storage unit, for example read_ds_0=2 and read_ds_1=1.";
        }
        if ("RANDOM".equals(actualType) || "ROUND_ROBIN".equals(actualType)) {
            return "No required properties.";
        }
        return "Read the plugin documentation for required properties before planning.";
    }
}

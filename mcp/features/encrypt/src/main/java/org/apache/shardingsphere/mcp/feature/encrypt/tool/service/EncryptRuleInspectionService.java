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

import org.apache.shardingsphere.mcp.api.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowDistSQLQueryUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
     * @return encrypt rules
     */
    public List<Map<String, Object>> queryEncryptRules(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return WorkflowDistSQLQueryUtils.queryRuleRows(queryFacade, databaseName, String.format("SHOW ENCRYPT RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query encrypt rules.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @param tableName table name
     * @return encrypt rules
     */
    public List<Map<String, Object>> queryEncryptRules(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName) {
        return WorkflowDistSQLQueryUtils.queryRuleRows(
                queryFacade, databaseName,
                String.format("SHOW ENCRYPT TABLE RULE %s FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(tableName), WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query encrypt algorithms.
     *
     * @param queryFacade query facade
     * @return encrypt algorithms
     */
    public List<Map<String, Object>> queryEncryptAlgorithms(final MCPFeatureQueryFacade queryFacade) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : queryAlgorithmRows(queryFacade)) {
            String type = WorkflowAlgorithmUtils.getAlgorithmType(each);
            Map<String, Boolean> capability = EncryptAlgorithmCatalog.findCapability(type);
            List<AlgorithmPropertyRequirement> propertyTemplates = EncryptAlgorithmCatalog.findRequirements(EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY, type);
            Map<String, Object> row = new LinkedHashMap<>(each);
            row.put(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_DECRYPT, capability.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_DECRYPT));
            row.put(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_EQUIVALENT_FILTER, capability.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_EQUIVALENT_FILTER));
            row.put(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_LIKE, capability.get(EncryptFeatureDefinition.ALGORITHM_CAPABILITY_LIKE));
            row.put("capability_confirmed", EncryptAlgorithmCatalog.isCapabilityConfirmed(type));
            row.put("property_templates", propertyTemplates.stream().map(AlgorithmPropertyRequirement::toMap).toList());
            row.put("required_properties", propertyTemplates.stream().filter(AlgorithmPropertyRequirement::isRequired).map(AlgorithmPropertyRequirement::getPropertyKey).toList());
            row.put("optional_properties", propertyTemplates.stream().filter(eachRequirement -> !eachRequirement.isRequired()).map(AlgorithmPropertyRequirement::getPropertyKey).toList());
            row.put("secret_properties", propertyTemplates.stream().filter(AlgorithmPropertyRequirement::isSecret).map(AlgorithmPropertyRequirement::getPropertyKey).toList());
            result.add(row);
        }
        return result;
    }
    
    private List<Map<String, Object>> queryAlgorithmRows(final MCPFeatureQueryFacade queryFacade) {
        try {
            return queryFacade.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS");
        } catch (final MCPQueryFailedException ex) {
            if (WorkflowDistSQLQueryUtils.isUnsupportedDistSQLQueryFailure(ex)) {
                return EncryptAlgorithmCatalog.getSupportedAlgorithmTypes().stream().map(each -> Map.<String, Object>of("type", each)).toList();
            }
            throw ex;
        }
    }
}

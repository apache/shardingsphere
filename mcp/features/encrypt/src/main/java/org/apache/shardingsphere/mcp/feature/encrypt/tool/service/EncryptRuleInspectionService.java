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

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
        String sql = tableName.isEmpty()
                ? String.format("SHOW ENCRYPT RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName))
                : String.format("SHOW ENCRYPT TABLE RULE %s FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(tableName), WorkflowSQLUtils.formatDistSQLIdentifier(databaseName));
        return queryFacade.query(databaseName, "", sql);
    }
    
    /**
     * Query encrypt algorithms.
     *
     * @param queryFacade query facade
     * @return encrypt algorithms
     */
    public List<Map<String, Object>> queryEncryptAlgorithms(final MCPFeatureQueryFacade queryFacade) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : queryFacade.queryWithAnyDatabase("SHOW ENCRYPT ALGORITHM PLUGINS")) {
            String type = Objects.toString(each.get("type"), "").trim().toUpperCase(Locale.ENGLISH);
            Map<String, Boolean> capability = EncryptAlgorithmRecommendationService.findEncryptCapability(type);
            Map<String, Object> row = new LinkedHashMap<>(each);
            row.put("supports_decrypt", capability.get("supports_decrypt"));
            row.put("supports_equivalent_filter", capability.get("supports_equivalent_filter"));
            row.put("supports_like", capability.get("supports_like"));
            result.add(row);
        }
        return result;
    }
}

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

import java.util.List;
import java.util.Map;

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
        String sql = tableName.isEmpty()
                ? String.format("SHOW MASK RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName))
                : String.format("SHOW MASK RULE %s FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(tableName), WorkflowSQLUtils.formatDistSQLIdentifier(databaseName));
        return queryFacade.query(databaseName, "", sql);
    }
    
    /**
     * Query mask algorithms.
     *
     * @param queryFacade query facade
     * @return mask algorithms
     */
    public List<Map<String, Object>> queryMaskAlgorithms(final MCPFeatureQueryFacade queryFacade) {
        return queryFacade.queryWithAnyDatabase("SHOW MASK ALGORITHM PLUGINS");
    }
}

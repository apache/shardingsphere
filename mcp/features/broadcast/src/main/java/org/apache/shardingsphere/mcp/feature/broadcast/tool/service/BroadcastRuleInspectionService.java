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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.service;

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.List;
import java.util.Map;

/**
 * Broadcast rule inspection service.
 */
public final class BroadcastRuleInspectionService {
    
    /**
     * Query broadcast rules.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @return broadcast rule rows
     */
    public List<Map<String, Object>> queryBroadcastRules(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW BROADCAST TABLE RULES FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
    
    /**
     * Query broadcast rule count.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @return broadcast rule count rows
     */
    public List<Map<String, Object>> queryBroadcastRuleCount(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("COUNT BROADCAST RULE FROM %s", WorkflowSQLUtils.formatDistSQLIdentifier(databaseName)));
    }
}

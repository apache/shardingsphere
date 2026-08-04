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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCExceptionClassifier;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;

import java.util.List;
import java.util.Map;

/**
 * DistSQL query utilities for workflow planning.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowDistSQLQueryUtils {
    
    /**
     * Judge whether a query failed because the current backend cannot parse ShardingSphere DistSQL.
     *
     * @param ex query failure
     * @return whether the backend does not support DistSQL syntax
     */
    public static boolean isUnsupportedDistSQLQueryFailure(final MCPQueryFailedException ex) {
        return MCPJDBCErrorCategory.SYNTAX == MCPJDBCExceptionClassifier.classify(ex);
    }
    
    /**
     * Query DistSQL rule rows, returning an empty list when the current backend does not support the rule DistSQL syntax.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @param sql DistSQL to execute
     * @return queried rows
     */
    public static List<Map<String, Object>> queryRuleRows(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String sql) {
        try {
            return queryFacade.query(databaseName, sql);
        } catch (final MCPQueryFailedException ex) {
            if (isUnsupportedDistSQLQueryFailure(ex)) {
                return List.of();
            }
            throw ex;
        }
    }
    
}

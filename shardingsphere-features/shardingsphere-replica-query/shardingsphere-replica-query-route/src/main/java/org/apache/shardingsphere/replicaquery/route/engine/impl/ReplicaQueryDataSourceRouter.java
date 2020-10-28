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

package org.apache.shardingsphere.replicaquery.route.engine.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryDataSourceRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

/**
 * Data source router for replica query.
 */
@RequiredArgsConstructor
public final class ReplicaQueryDataSourceRouter {
    
    private final ReplicaQueryDataSourceRule rule;
    
    /**
     * Route.
     * 
     * @param sqlStatement SQL statement
     * @return data source name
     */
    public String route(final SQLStatement sqlStatement) {
        if (isPrimaryRoute(sqlStatement)) {
            PrimaryVisitedManager.setPrimaryVisited();
            return rule.getPrimaryDataSourceName();
        }
        return rule.getLoadBalancer().getDataSource(rule.getName(), rule.getPrimaryDataSourceName(), rule.getReplicaDataSourceNames());
    }
    
    private boolean isPrimaryRoute(final SQLStatement sqlStatement) {
        return containsLockSegment(sqlStatement) || !(sqlStatement instanceof SelectStatement) || PrimaryVisitedManager.getPrimaryVisited() || HintManager.isPrimaryRouteOnly();
    }
    
    private boolean containsLockSegment(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && SelectStatementHandler.getLockSegment((SelectStatement) sqlStatement).isPresent();
    }
}

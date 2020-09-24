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

package org.apache.shardingsphere.replication.primaryreplica.route.engine.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationDataSourceRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.helper.dml.SelectStatementHelper;

import java.util.ArrayList;

/**
 * Data source router for primary-replica replication.
 */
@RequiredArgsConstructor
public final class PrimaryReplicaReplicationDataSourceRouter {
    
    private final PrimaryReplicaReplicationDataSourceRule rule;
    
    /**
     * Route.
     * 
     * @param sqlStatement SQL statement
     * @return data source name
     */
    public String route(final SQLStatement sqlStatement) {
        if (isMasterRoute(sqlStatement)) {
            PrimaryVisitedManager.setPrimaryVisited();
            return rule.getPrimaryDataSourceName();
        }
        return rule.getLoadBalancer().getDataSource(rule.getName(), rule.getPrimaryDataSourceName(), new ArrayList<>(rule.getReplicaDataSourceNames()));
    }
    
    private boolean isMasterRoute(final SQLStatement sqlStatement) {
        return containsLockSegment(sqlStatement) || !(sqlStatement instanceof SelectStatement) || PrimaryVisitedManager.getPrimaryVisited() || HintManager.isMasterRouteOnly();
    }
    
    private boolean containsLockSegment(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && SelectStatementHelper.getLockSegment((SelectStatement) sqlStatement).isPresent();
    }
}

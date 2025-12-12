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

package org.apache.shardingsphere.sharding.route.engine.checker.dml;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.syntax.DMLMultipleDataNodesWithLimitException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.route.engine.checker.ShardingRouteContextChecker;
import org.apache.shardingsphere.sharding.route.engine.checker.util.ShardingRouteContextCheckUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Optional;

/**
 * Sharding update route context checker.
 */
public final class ShardingUpdateRouteContextChecker implements ShardingRouteContextChecker {
    
    @Override
    public void check(final ShardingRule shardingRule, final QueryContext queryContext, final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        UpdateStatementContext updateStatementContext = (UpdateStatementContext) sqlStatementContext;
        String tableName = updateStatementContext.getTablesContext().getTableNames().iterator().next();
        UpdateStatement updateStatement = updateStatementContext.getSqlStatement();
        Optional<ShardingConditions> shardingConditions =
                ShardingRouteContextCheckUtils.createShardingConditions(sqlStatementContext, shardingRule, updateStatement.getSetAssignment().getAssignments(), queryContext.getParameters());
        Optional<RouteContext> setAssignmentRouteContext =
                shardingConditions.map(optional -> new ShardingStandardRouteEngine(tableName, optional, sqlStatementContext, queryContext.getHintValueContext(), props).route(shardingRule));
        if (setAssignmentRouteContext.isPresent() && !ShardingRouteContextCheckUtils.isSameRouteContext(routeContext, setAssignmentRouteContext.get())) {
            throw new UnsupportedUpdatingShardingValueException(tableName);
        }
        ShardingSpherePreconditions.checkState(!updateStatement.getLimit().isPresent()
                || routeContext.getRouteUnits().size() <= 1, () -> new DMLMultipleDataNodesWithLimitException("UPDATE"));
    }
}

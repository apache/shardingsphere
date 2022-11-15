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

package org.apache.shardingsphere.sharding.route.engine.validator.dml.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.syntax.DMLMultipleDataNodesWithLimitException;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedUpdatingShardingValueException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.validator.dml.ShardingDMLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.UpdateStatementHandler;

import java.util.List;
import java.util.Optional;

/**
 * Sharding update statement validator.
 */
public final class ShardingUpdateStatementValidator extends ShardingDMLStatementValidator<UpdateStatement> {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext<UpdateStatement> sqlStatementContext, final List<Object> params, final ShardingSphereDatabase database,
                            final ConfigurationProperties props) {
        validateMultipleTable(shardingRule, sqlStatementContext);
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext<UpdateStatement> sqlStatementContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
        Optional<ShardingConditions> shardingConditions = createShardingConditions(sqlStatementContext, shardingRule,
                sqlStatementContext.getSqlStatement().getSetAssignment().getAssignments(), params);
        Optional<RouteContext> setAssignmentRouteContext = shardingConditions.map(optional -> new ShardingStandardRoutingEngine(tableName, optional, sqlStatementContext, props).route(shardingRule));
        if (setAssignmentRouteContext.isPresent() && !isSameRouteContext(routeContext, setAssignmentRouteContext.get())) {
            throw new UnsupportedUpdatingShardingValueException(tableName);
        }
        if (!shardingRule.isBroadcastTable(tableName) && UpdateStatementHandler.getLimitSegment(sqlStatementContext.getSqlStatement()).isPresent() && routeContext.getRouteUnits().size() > 1) {
            throw new DMLMultipleDataNodesWithLimitException("UPDATE");
        }
    }
}

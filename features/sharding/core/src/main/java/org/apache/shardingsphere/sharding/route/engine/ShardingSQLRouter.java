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

package org.apache.shardingsphere.sharding.route.engine;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.lifecycle.EntranceSQLRouter;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.cache.route.CachedShardingSQLRouter;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.checker.ShardingRouteContextCheckerFactory;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngineFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding SQL router.
 */
@HighFrequencyInvocation
public final class ShardingSQLRouter implements EntranceSQLRouter<ShardingRule> {
    
    @Override
    public RouteContext createRouteContext(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database,
                                           final ShardingRule rule, final Collection<String> tableNames, final ConfigurationProperties props) {
        if (rule.isShardingCacheEnabled()) {
            Optional<RouteContext> result = new CachedShardingSQLRouter()
                    .loadRouteContext(this::createRouteContext0, queryContext, globalRuleMetaData, database, rule.getShardingCache(), tableNames, props);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return createRouteContext0(queryContext, globalRuleMetaData, database, rule, tableNames, props);
    }
    
    private RouteContext createRouteContext0(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final ShardingRule rule,
                                             final Collection<String> tableNames, final ConfigurationProperties props) {
        Collection<String> logicTableNames = rule.getShardingLogicTableNames(tableNames);
        if (logicTableNames.isEmpty()) {
            return new RouteContext();
        }
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        ShardingConditions shardingConditions = createShardingConditions(queryContext, globalRuleMetaData, database, rule);
        if (sqlStatement instanceof DMLStatement && shardingConditions.isNeedMerge()) {
            shardingConditions.merge();
        }
        RouteContext result = ShardingRouteEngineFactory.newInstance(rule, database, queryContext, shardingConditions, logicTableNames, props).route(rule);
        checkRouteContext(queryContext, database, rule, props, sqlStatement, shardingConditions, result);
        return result;
    }
    
    private ShardingConditions createShardingConditions(final QueryContext queryContext,
                                                        final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final ShardingRule rule) {
        List<ShardingCondition> shardingConditions;
        if (queryContext.getSqlStatementContext().getSqlStatement() instanceof DMLStatement
                || queryContext.getSqlStatementContext().getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent()) {
            shardingConditions = new ShardingConditionEngine(globalRuleMetaData, database, rule).createShardingConditions(queryContext.getSqlStatementContext(), queryContext.getParameters());
        } else {
            shardingConditions = Collections.emptyList();
        }
        return new ShardingConditions(shardingConditions, queryContext.getSqlStatementContext(), rule);
    }
    
    private void checkRouteContext(final QueryContext queryContext, final ShardingSphereDatabase database, final ShardingRule rule, final ConfigurationProperties props,
                                   final SQLStatement sqlStatement, final ShardingConditions shardingConditions, final RouteContext routeContext) {
        ShardingRouteContextCheckerFactory.newInstance(sqlStatement, shardingConditions).ifPresent(optional -> optional.check(rule, queryContext, database, props, routeContext));
    }
    
    @Override
    public Type getType() {
        return Type.DATA_NODE;
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}

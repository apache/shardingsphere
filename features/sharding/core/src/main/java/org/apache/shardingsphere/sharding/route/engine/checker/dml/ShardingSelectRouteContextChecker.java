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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.syntax.SelectMultipleDataSourcesWithCombineException;
import org.apache.shardingsphere.sharding.route.engine.checker.ShardingRouteContextChecker;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Sharding select route context checker.
 */
public final class ShardingSelectRouteContextChecker implements ShardingRouteContextChecker {
    
    @Override
    public void check(final ShardingRule shardingRule, final QueryContext queryContext, final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        SelectStatement selectStatement = (SelectStatement) queryContext.getSqlStatementContext().getSqlStatement();
        if (selectStatement.getCombine().isPresent() && !isRouteToSingleDataSource(routeContext)) {
            throw new SelectMultipleDataSourcesWithCombineException(selectStatement.getCombine().get().getCombineType().name());
        }
        if (!"openGauss".equals(selectStatement.getDatabaseType().getType()) || routeContext.getRouteUnits().size() < 2) {
            return;
        }
        ShardingSpherePreconditions.checkState(!containsCube(selectStatement),
                () -> new UnsupportedSQLOperationException("openGauss CUBE query across multiple data nodes"));
        ShardingSpherePreconditions.checkState(!containsAggregationWithNamedWindow(selectStatement),
                () -> new UnsupportedSQLOperationException("openGauss aggregate query with a named window across multiple data nodes"));
        ShardingSpherePreconditions.checkState(!selectStatement.getHaving().isPresent(),
                () -> new UnsupportedSQLOperationException("openGauss HAVING query across multiple data nodes without SQL Federation"));
    }
    
    private boolean isRouteToSingleDataSource(final RouteContext routeContext) {
        if (routeContext.getRouteUnits().isEmpty()) {
            return true;
        }
        boolean result = true;
        String sampleDataSourceName = routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getLogicName();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            if (!each.getDataSourceMapper().getLogicName().equals(sampleDataSourceName)) {
                result = false;
                break;
            }
        }
        return result;
    }
    
    private boolean containsCube(final SelectStatement selectStatement) {
        if (!selectStatement.getGroupBy().isPresent() || !selectStatement.getGroupBy().get().isContainsGroupingExtension()) {
            return false;
        }
        GroupBySegment groupBy = selectStatement.getGroupBy().get();
        for (OrderByItemSegment each : groupBy.getGroupByItems()) {
            if (each instanceof ExpressionOrderByItemSegment && isCubeExpression(((ExpressionOrderByItemSegment) each).getExpression())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isCubeExpression(final String expression) {
        int leftParenthesisIndex = expression.indexOf('(');
        return leftParenthesisIndex > 0 && "CUBE".equalsIgnoreCase(expression.substring(0, leftParenthesisIndex).trim());
    }
    
    private boolean containsAggregationWithNamedWindow(final SelectStatement selectStatement) {
        for (ProjectionSegment each : selectStatement.getProjections().getProjections()) {
            if (each instanceof AggregationProjectionSegment && ((AggregationProjectionSegment) each).getWindow().isPresent()
                    && null != ((AggregationProjectionSegment) each).getWindow().get().getWindowName()) {
                return true;
            }
        }
        return false;
    }
}

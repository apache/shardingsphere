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

package org.apache.shardingsphere.sharding.route.engine.checker.ddl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedCreateViewException;
import org.apache.shardingsphere.sharding.route.engine.checker.ShardingRouteContextChecker;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Sharding create view route context checker.
 */
@RequiredArgsConstructor
public final class ShardingCreateViewRouteContextChecker implements ShardingRouteContextChecker {
    
    @Override
    public void check(final ShardingRule shardingRule, final QueryContext queryContext, final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
        SelectStatement selectStatement = ((CreateViewStatement) queryContext.getSqlStatementContext().getSqlStatement()).getSelect();
        if (isContainsNotSupportedViewStatement(selectStatement, routeContext)) {
            throw new UnsupportedCreateViewException();
        }
    }
    
    private boolean isContainsNotSupportedViewStatement(final SelectStatement selectStatement, final RouteContext routeContext) {
        if (routeContext.getRouteUnits().size() <= 1) {
            return false;
        }
        return hasGroupBy(selectStatement) || hasAggregation(selectStatement) || hasDistinct(selectStatement) || hasLimit(selectStatement);
    }
    
    private boolean hasGroupBy(final SelectStatement selectStatement) {
        return selectStatement.getGroupBy().map(groupBySegment -> !groupBySegment.getGroupByItems().isEmpty()).orElse(false);
    }
    
    private boolean hasAggregation(final SelectStatement selectStatement) {
        for (ProjectionSegment each : selectStatement.getProjections().getProjections()) {
            if (each instanceof AggregationProjectionSegment) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasDistinct(final SelectStatement selectStatement) {
        return selectStatement.getProjections().isDistinctRow();
    }
    
    private boolean hasLimit(final SelectStatement selectStatement) {
        return selectStatement.getLimit().isPresent();
    }
}

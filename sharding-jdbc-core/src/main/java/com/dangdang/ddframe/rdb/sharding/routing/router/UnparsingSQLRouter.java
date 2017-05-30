/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.routing.router;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLJudgeEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.routing.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.dangdang.ddframe.rdb.sharding.routing.SQLRouteResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.database.DatabaseRouter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 无需解析的SQL路由器.
 * 
 * @author zhangiang
 */
@Slf4j
public final class UnparsingSQLRouter implements SQLRouter {
    
    private final ShardingRule shardingRule;
    
    public UnparsingSQLRouter(final ShardingContext shardingContext) {
        shardingRule = shardingContext.getShardingRule();
    }
    
    @Override
    public SQLStatement parse(final String logicSQL, final int parametersSize) {
        return new SQLJudgeEngine(logicSQL).judge();
    }
    
    @Override
    // TODO insert的SQL仍然需要解析自增主键
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        RoutingResult routingResult = new DatabaseRouter(shardingRule.getDataSourceRule(), shardingRule.getDatabaseShardingStrategy(), sqlStatement.getType()).route();
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.append(logicSQL);
        result.getExecutionUnits().addAll(routingResult.getSQLExecutionUnits(sqlBuilder));
        MetricsContext.stop(context);
        logSQLRouteResult(result, parameters);
        return result;
    }
    
    private void logSQLRouteResult(final SQLRouteResult routeResult, final List<Object> parameters) {
        log.debug("final route result is {} target", routeResult.getExecutionUnits().size());
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            log.debug("{}:{} {}", each.getDataSource(), each.getSQL(), parameters);
        }
    }
}

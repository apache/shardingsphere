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
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GeneratedKeyContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;
import com.dangdang.ddframe.rdb.sharding.routing.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.dangdang.ddframe.rdb.sharding.routing.SQLRouteResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.routing.type.mixed.MixedTablesRouter;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleTableRouter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 需要解析的SQL路由器.
 * 
 * @author zhangiang
 */
@Slf4j
public final class ParsingSQLRouter implements SQLRouter {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final List<Number> generatedKeys = new LinkedList<>();
    
    public ParsingSQLRouter(final ShardingContext shardingContext) {
        shardingRule = shardingContext.getShardingRule();
        databaseType = shardingContext.getDatabaseType();
    }
    
    @Override
    public SQLContext parse(final String logicSQL, final int parametersSize) {
        SQLParsingEngine parsingEngine = new SQLParsingEngine(databaseType, logicSQL, shardingRule);
        Context context = MetricsContext.start("Parse SQL");
        log.debug("Logic SQL: {}", logicSQL);
        SQLContext result = parsingEngine.parse();
        if (result instanceof InsertSQLContext) {
            ((InsertSQLContext) result).appendGenerateKeyToken(shardingRule, parametersSize);
        }
        MetricsContext.stop(context);
        return result;
    }
    
    @Override
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLContext sqlContext) {
        final Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(sqlContext);
        if (sqlContext instanceof InsertSQLContext && null != ((InsertSQLContext) sqlContext).getGeneratedKeyContext()) {
            GeneratedKeyContext generatedKeyContext = ((InsertSQLContext) sqlContext).getGeneratedKeyContext();
            if (parameters.isEmpty()) {
                result.getGeneratedKeys().add(generatedKeyContext.getValue());
            } else if (parameters.size() == generatedKeyContext.getIndex()) {
                Number generatedKey = shardingRule.generateKey(sqlContext.getTables().get(0).getName());
                parameters.add(generatedKey);
                setGeneratedKeys(result, generatedKey);
            } else if (-1 != generatedKeyContext.getIndex()) {
                setGeneratedKeys(result, (Number) parameters.get(generatedKeyContext.getIndex()));
            }
        }
        if (null != sqlContext.getLimitContext()) {
            sqlContext.getLimitContext().processParameters(parameters);
        }
        RoutingResult routingResult = route(parameters, sqlContext);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(logicSQL, sqlContext);
        SQLBuilder sqlBuilder = rewriteEngine.rewrite();
        result.getExecutionUnits().addAll(routingResult.getSQLExecutionUnits(sqlBuilder));
        if (null != sqlContext.getLimitContext() && 1 == result.getExecutionUnits().size()) {
            rewriteEngine.amend(sqlBuilder, parameters);
        }
        MetricsContext.stop(context);
        logSQLRouteResult(result, parameters);
        return result;
    }
    
    private RoutingResult route(final List<Object> parameters, final SQLContext sqlContext) {
        Set<String> logicTables = Sets.newLinkedHashSet(Collections2.transform(sqlContext.getTables(), new Function<TableContext, String>() {
            
            @Override
            public String apply(final TableContext input) {
                return input.getName();
            }
        }));
        if (1 == logicTables.size()) {
            return new SingleTableRouter(shardingRule, parameters, logicTables.iterator().next(), sqlContext.getConditionContext(), sqlContext.getType()).route();
        }
        if (shardingRule.isAllBindingTables(logicTables)) {
            return new BindingTablesRouter(shardingRule, parameters, logicTables, sqlContext.getConditionContext(), sqlContext.getType()).route();
        }
        // TODO 可配置是否执行笛卡尔积
        return new MixedTablesRouter(shardingRule, parameters, logicTables, sqlContext.getConditionContext(), sqlContext.getType()).route();
    }
    
    private void logSQLRouteResult(final SQLRouteResult routeResult, final List<Object> parameters) {
        log.debug("final route result is {} target", routeResult.getExecutionUnits().size());
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            log.debug("{}:{} {}", each.getDataSource(), each.getSQL(), parameters);
        }
    }
    
    private void setGeneratedKeys(final SQLRouteResult sqlRouteResult, final Number generatedKey) {
        generatedKeys.add(generatedKey);
        sqlRouteResult.getGeneratedKeys().clear();
        sqlRouteResult.getGeneratedKeys().addAll(generatedKeys);
    }
}

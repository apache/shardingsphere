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

package com.dangdang.ddframe.rdb.sharding.router;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.rewrite.DerivedColumnUtils;
import com.dangdang.ddframe.rdb.sharding.rewrite.GenerateKeysUtils;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;
import com.dangdang.ddframe.rdb.sharding.router.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.database.DatabaseRouter;
import com.dangdang.ddframe.rdb.sharding.router.mixed.MixedTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleTableRouter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * 路由引擎.
 * 
 * @author zhangiang
 */
@RequiredArgsConstructor
@Slf4j
public final class RouteEngine {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    SQLContext parse(final String logicSQL, final List<Object> parameters) {
        SQLParsingEngine parsingEngine = new SQLParsingEngine(databaseType, logicSQL, shardingRule);
        if (HintManagerHolder.isDatabaseShardingOnly()) {
            return parsingEngine.prepareParse();
        }
        Context context = MetricsContext.start("Parse SQL");
        log.debug("Logic SQL: {}, {}", logicSQL, parameters);
        SQLContext result = parsingEngine.parse();
        MetricsContext.stop(context);
        if (result instanceof InsertSQLContext) {
            GenerateKeysUtils.appendGenerateKeys(shardingRule, parameters, (InsertSQLContext) result);
        }
        if (result instanceof SelectSQLContext) {
            DerivedColumnUtils.appendDerivedColumns((SelectSQLContext) result);
        }
        return result;
    }
    
    SQLRouteResult route(final String logicSQL, final SQLContext sqlContext, final List<Object> parameters) {
        final Context context = MetricsContext.start("Route SQL");
        if (null != sqlContext.getLimitContext()) {
            sqlContext.getLimitContext().processParameters(parameters);
        }
        SQLRouteResult result = new SQLRouteResult(sqlContext);
        RoutingResult routingResult = route(sqlContext.getConditionContext(), sqlContext, parameters);
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(logicSQL, sqlContext);
        SQLBuilder sqlBuilder = rewriteEngine.rewrite();
        result.getExecutionUnits().addAll(routingResult.getSQLExecutionUnits(sqlBuilder));
        if (null != sqlContext.getLimitContext() && 1 == result.getExecutionUnits().size()) {
            rewriteEngine.amend(sqlBuilder, parameters);
        }
        MetricsContext.stop(context);
        log.debug("final route result is {} target", result.getExecutionUnits().size());
        for (SQLExecutionUnit each : result.getExecutionUnits()) {
            log.debug("{}:{} {}", each.getDataSource(), each.getSQL(), parameters);
        }
        return result;
    }
    
    private RoutingResult route(final ConditionContext conditionContext, final SQLContext sqlContext, final List<Object> parameters) {
        if (HintManagerHolder.isDatabaseShardingOnly()) {
            return new DatabaseRouter(shardingRule.getDataSourceRule(), shardingRule.getDatabaseShardingStrategy(), sqlContext.getType()).route();
        }
        Set<String> logicTables = Sets.newLinkedHashSet(Collections2.transform(sqlContext.getTables(), new Function<TableContext, String>() {
            
            @Override
            public String apply(final TableContext input) {
                return input.getName();
            }
        }));
        if (1 == logicTables.size()) {
            return new SingleTableRouter(shardingRule, parameters, logicTables.iterator().next(), conditionContext, sqlContext.getType()).route();
        }
        if (shardingRule.isAllBindingTables(logicTables)) {
            return new BindingTablesRouter(shardingRule, parameters, logicTables, conditionContext, sqlContext.getType()).route();
        }
        // TODO 可配置是否执行笛卡尔积
        return new MixedTablesRouter(shardingRule, parameters, logicTables, conditionContext, sqlContext.getType()).route();
    }
}

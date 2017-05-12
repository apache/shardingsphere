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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.DeleteSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.LimitContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountLimitToken;
import com.dangdang.ddframe.rdb.sharding.rewrite.DerivedUtils;
import com.dangdang.ddframe.rdb.sharding.rewrite.GenerateKeysUtils;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;
import com.dangdang.ddframe.rdb.sharding.router.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.database.DatabaseRouter;
import com.dangdang.ddframe.rdb.sharding.router.mixed.MixedTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleTableRouter;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * SQL路由引擎.
 * 
 * @author gaohongtao
 * @author zhangiang
 */
@RequiredArgsConstructor
@Slf4j
public final class SQLRouteEngine {
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    /**
     * SQL路由.
     *
     * @param logicSQL 逻辑SQL
     * @return 路由结果
     * @throws SQLParsingException SQL解析失败异常
     */
    public SQLRouteResult route(final String logicSQL) throws SQLParsingException {
        return routeSQL(logicSQL, parseSQL(logicSQL, Collections.emptyList()), Collections.emptyList());
    }
    
    /**
     * 预解析SQL路由.
     * 
     * @param logicSql 逻辑SQL
     * @return 预解析SQL路由器
     */
    public PreparedSQLRouter prepareSQL(final String logicSql) {
        return new PreparedSQLRouter(logicSql, this, shardingRule);
    }
    
    SQLContext parseSQL(final String logicSql, final List<Object> parameters) {
        if (HintManagerHolder.isDatabaseShardingOnly()) {
            return buildHintParsedResult(logicSql);
        }
        Context context = MetricsContext.start("Parse SQL");
        log.debug("Logic SQL: {}, {}", logicSql, parameters);
        SQLContext result = new SQLParsingEngine(databaseType, logicSql, shardingRule).parseStatement();
        MetricsContext.stop(context);
        if (result instanceof InsertSQLContext) {
            GenerateKeysUtils.appendGenerateKeys(shardingRule, parameters, (InsertSQLContext) result);
        }
        if (result instanceof SelectSQLContext) {
            DerivedUtils.appendDerivedColumns((SelectSQLContext) result);
        }
        return result;
    }
    
    private SQLContext buildHintParsedResult(final String logicSql) {
        SQLContext result;
        switch (SQLUtil.getTypeByStart(logicSql)) {
            case SELECT:
                result = new SelectSQLContext();
                break;
            case INSERT:
                result = new InsertSQLContext();
                break;
            case UPDATE:
                result = new UpdateSQLContext();
                break;
            case DELETE:
                result = new DeleteSQLContext();
                break;
            default:
                throw new UnsupportedOperationException("");
        }
        return result;
    }
    
    SQLRouteResult routeSQL(final String logicSQL, final SQLContext sqlContext, final List<Object> parameters) {
        Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(sqlContext);
        RoutingResult routingResult = routeSQL(sqlContext.getConditionContext(), sqlContext, parameters);
        SQLRewriteEngine sqlRewriteEngine = new SQLRewriteEngine(logicSQL, sqlContext);
        result.getExecutionUnits().addAll(routingResult.getSQLExecutionUnits(sqlRewriteEngine.rewrite()));
        amendSQLAccordingToRouteResult(parameters, result, sqlRewriteEngine);
        MetricsContext.stop(context);
        log.debug("final route result is {} target", result.getExecutionUnits().size());
        for (SQLExecutionUnit each : result.getExecutionUnits()) {
            log.debug("{}:{} {}", each.getDataSource(), each.getSQL(), parameters);
        }
        return result;
    }
    
    private RoutingResult routeSQL(final ConditionContext conditionContext, final SQLContext sqlContext, final List<Object> parameters) {
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
    
    private void amendSQLAccordingToRouteResult(final List<Object> parameters, final SQLRouteResult sqlRouteResult, final SQLRewriteEngine sqlRewriteEngine) {
        LimitContext limit = sqlRouteResult.getSqlContext().getLimitContext();
        SQLBuilder sqlBuilder = sqlRewriteEngine.rewrite();
        if (null != limit) {
            if (1 == sqlRouteResult.getExecutionUnits().size()) {
                if (limit.getOffsetParameterIndex() > -1) {
                    parameters.set(limit.getOffsetParameterIndex(), limit.getOffset());
                }
                if (limit.getRowCountParameterIndex() > -1) {
                    parameters.set(limit.getRowCountParameterIndex(), limit.getRowCount());
                }
            } else {
                int offset = 0;
                int rowCount = limit.getOffset() + limit.getRowCount();
                if (limit.getOffsetParameterIndex() > -1) {
                    parameters.set(limit.getOffsetParameterIndex(), offset);
                }
                if (limit.getRowCountParameterIndex() > -1) {
                    parameters.set(limit.getRowCountParameterIndex(), rowCount);
                }
                sqlBuilder.buildSQL(OffsetLimitToken.OFFSET_NAME, String.valueOf(offset));
                sqlBuilder.buildSQL(RowCountLimitToken.COUNT_NAME, String.valueOf(rowCount));
            }
        }
    }
}

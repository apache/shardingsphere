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
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parser.SQLParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.router.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.mixed.MixedTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleTableRouter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
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
     * @param logicSql 逻辑SQL
     * @return 路由结果
     * @throws SQLParserException SQL解析失败异常
     */
    public SQLRouteResult route(final String logicSql) throws SQLParserException {
        return route(logicSql, Collections.emptyList());
    }
    
    /**
     * SQL路由.
     * 
     * @param logicSql 逻辑SQL
     * @param parameters 参数列表
     * @return 路由结果
     * @throws SQLParserException SQL解析失败异常
     */
    public SQLRouteResult route(final String logicSql, final List<Object> parameters) throws SQLParserException {
        return routeSQL(parseSQL(logicSql, parameters), parameters);
    }
    
    /**
     * 预解析SQL路由.
     * 
     * @param logicSql 逻辑SQL
     * @return 预解析SQL路由器
     */
    public PreparedSQLRouter prepareSQL(final String logicSql) {
        return new PreparedSQLRouter(logicSql, this);
    }
    
    SQLParsedResult parseSQL(final String logicSql, final List<Object> parameters) {
        Context context = MetricsContext.start("Parse SQL");
        SQLParsedResult result = SQLParserFactory.create(databaseType, logicSql, parameters, shardingRule.getAllShardingColumns()).parse();
        MetricsContext.stop(context);
        return result;
    }
    
    SQLRouteResult routeSQL(final SQLParsedResult parsedResult, final List<Object> parameters) {
        Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(parsedResult.getRouteContext().getSqlStatementType(), parsedResult.getMergeContext());
        for (ConditionContext each : parsedResult.getConditionContexts()) {
            result.getExecutionUnits().addAll(routeSQL(each, Sets.newLinkedHashSet(Collections2.transform(parsedResult.getRouteContext().getTables(), new Function<Table, String>() {
                
                @Override
                public String apply(final Table input) {
                    return input.getName();
                }
            })), parsedResult.getRouteContext().getSqlBuilder(), parsedResult.getRouteContext().getSqlStatementType()));
        }
        processLimit(result.getExecutionUnits(), parsedResult, parameters);
        MetricsContext.stop(context);
        if (result.getExecutionUnits().isEmpty()) {
            throw new ShardingJdbcException("Sharding-JDBC: cannot route any result, please check your sharding rule.");
        }
        log.debug("final route result:{}", result.getExecutionUnits());
        log.debug("merge context:{}", result.getMergeContext());
        return result;
    }
    
    private Collection<SQLExecutionUnit> routeSQL(final ConditionContext conditionContext, final Set<String> logicTables, final SQLBuilder sqlBuilder, final SQLStatementType type) {
        RoutingResult result;
        if (1 == logicTables.size()) {
            result = new SingleTableRouter(shardingRule, logicTables.iterator().next(), conditionContext, type).route();
        } else if (shardingRule.isAllBindingTables(logicTables)) {
            result = new BindingTablesRouter(shardingRule, logicTables, conditionContext, type).route();
        } else {
            // TODO 可配置是否执行笛卡尔积
            result = new MixedTablesRouter(shardingRule, logicTables, conditionContext, type).route();
        }
        return result.getSQLExecutionUnits(sqlBuilder);
    }
    
    private void processLimit(final Set<SQLExecutionUnit> sqlExecutionUnits, final SQLParsedResult parsedResult, final List<Object> parameters) {
        if (!parsedResult.getMergeContext().hasLimit()) {
            return;
        }
        int offset;
        int rowCount;
        Limit limit = parsedResult.getMergeContext().getLimit();
        if (sqlExecutionUnits.size() > 1) {
            offset = 0;
            rowCount = limit.getOffset() + limit.getRowCount();
        } else {
            offset = limit.getOffset();
            rowCount = limit.getRowCount();
        }
        if (parsedResult.getRouteContext().getSqlBuilder().containsToken(Limit.OFFSET_NAME) || parsedResult.getRouteContext().getSqlBuilder().containsToken(Limit.COUNT_NAME)) {
            for (SQLExecutionUnit each : sqlExecutionUnits) {
                SQLBuilder sqlBuilder = each.getSqlBuilder();
                sqlBuilder.buildSQL(Limit.OFFSET_NAME, String.valueOf(offset));
                sqlBuilder.buildSQL(Limit.COUNT_NAME, String.valueOf(rowCount));
                each.setSql(sqlBuilder.toSQL());
            }
        }
        if (limit.getOffsetParameterIndex().isPresent()) {
            parameters.set(limit.getOffsetParameterIndex().get(), offset);
        }
        if (limit.getRowCountParameterIndex().isPresent()) {
            parameters.set(limit.getRowCountParameterIndex().get(), rowCount);
        }
    }
}

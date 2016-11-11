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
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parser.SQLParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.router.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.mixed.MixedTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleTableRouter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
    
    @Setter
    private List<Object> parameters;
    
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
    
    SQLRouteResult route(final String logicSql, final List<Object> parameters) throws SQLParserException {
        return routeSQL(parseSQL(logicSql, parameters));
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
    
    SQLParsedResult parseSQL(final String logicSql, final List<Object> parameters) {
        this.parameters = parameters;
        Context context = MetricsContext.start("Parse SQL");
        SQLParsedResult result = SQLParserFactory.create(databaseType, logicSql, parameters, shardingRule).parse();
        MetricsContext.stop(context);
        return result;
    }
    
    SQLRouteResult routeSQL(final SQLParsedResult parsedResult) {
        Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(parsedResult.getRouteContext().getSqlStatementType(), parsedResult.getMergeContext(), parsedResult.getGeneratedKeyContext());
        for (ConditionContext each : parsedResult.getConditionContexts()) {
            RoutingResult routingResult = routeSQL(each, parsedResult);
            result.getExecutionUnits().addAll(routingResult.getSQLExecutionUnits(parsedResult.getRouteContext().getSqlBuilder()));
        }
        MetricsContext.stop(context);
        Limit limit = result.getMergeContext().getLimit();
        if (null != limit) {
            limit.replaceSQL(parsedResult.getRouteContext().getSqlBuilder(), result.getExecutionUnits().size() > 1);
            limit.replaceParameters(parameters, result.getExecutionUnits().size() > 1);
        }
        log.debug("final route result is {} target", result.getExecutionUnits().size());
        for (SQLExecutionUnit each : result.getExecutionUnits()) {
            log.debug("{}:{} {}", each.getDataSource(), each.getSql(), parameters);
        }
        log.debug("merge context:{}", result.getMergeContext());
        return result;
    }
    
    private RoutingResult routeSQL(final ConditionContext conditionContext, final SQLParsedResult parsedResult) {
        Set<String> logicTables = Sets.newLinkedHashSet(Collections2.transform(parsedResult.getRouteContext().getTables(), new Function<Table, String>() {
        
            @Override
            public String apply(final Table input) {
                return input.getName();
            }
        }));
        if (1 == logicTables.size()) {
            return new SingleTableRouter(shardingRule, logicTables.iterator().next(), conditionContext, parsedResult.getRouteContext().getSqlStatementType()).route();
        } 
        if (shardingRule.isAllBindingTables(logicTables)) {
            return new BindingTablesRouter(shardingRule, logicTables, conditionContext, parsedResult.getRouteContext().getSqlStatementType()).route();
        } 
        // TODO 可配置是否执行笛卡尔积
        return new MixedTablesRouter(shardingRule, logicTables, conditionContext, parsedResult.getRouteContext().getSqlStatementType()).route();
    }
}

/**
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

import java.util.Collection;
import java.util.List;

import com.codahale.metrics.Timer.Context;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import com.dangdang.ddframe.rdb.sharding.parser.SQLParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.router.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.mixed.MixedTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleTableRouter;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SQL路由引擎.
 * 
 * @author gaohongtao, zhangiang
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
    public SQLRouteResult route(final String logicSql, final List<Object> parameters) throws SQLParserException {
        return routeSQL(parseSQL(logicSql, parameters));
    }
    
    private SQLParsedResult parseSQL(final String logicSql, final List<Object> parameters) {
        Context context = MetricsContext.start("Parse SQL");
        SQLParsedResult result = SQLParserFactory.create(databaseType, logicSql, parameters, shardingRule.getAllShardingColumns()).parse();
        MetricsContext.stop(context);
        return result;
    }
    
    private SQLRouteResult routeSQL(final SQLParsedResult parsedResult) {
        Context context = MetricsContext.start("Route SQL");
        SQLRouteResult result = new SQLRouteResult(parsedResult.getMergeContext());
        for (ConditionContext each : parsedResult.getConditionContexts()) {
            result.getExecutionUnits().addAll(routeSQL(each, Collections2.transform(parsedResult.getRouteContext().getTables(), new Function<Table, String>() {
                
                @Override
                public String apply(final Table input) {
                    return input.getName();
                }
            }), parsedResult.getRouteContext().getSqlBuilder(), parsedResult.getRouteContext().getSqlStatementType()));
        }
        MetricsContext.stop(context);
        log.debug("final route result:{}", result.getExecutionUnits());
        log.debug("merge context:{}", result.getMergeContext());
        return result;
    }
    
    private Collection<SQLExecutionUnit> routeSQL(final ConditionContext conditionContext, final Collection<String> logicTables, final SQLBuilder sqlBuilder, final SQLStatementType type) {
        RoutingResult result;
        if (1 == logicTables.size()) {
            result = new SingleTableRouter(shardingRule, logicTables.iterator().next(), conditionContext, type).route();
        } else if (shardingRule.isAllBindingTable(logicTables)) {
            result = new BindingTablesRouter(shardingRule, logicTables, conditionContext, type).route();
        } else {
            // TODO 可配置是否执行笛卡尔积
            result = new MixedTablesRouter(shardingRule, logicTables, conditionContext, type).route();
        }
        if (null == result) {
            throw new ShardingJdbcException("Sharding-JDBC: cannot route any result, please check your sharding rule.");
        }
        return result.getSQLExecutionUnits(sqlBuilder);
    }
}

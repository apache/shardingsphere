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

package io.shardingjdbc.core.routing.router;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.parsing.SQLJudgeEngine;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
import io.shardingjdbc.core.routing.strategy.hint.HintShardingStrategy;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.routing.type.hint.DatabaseHintRoutingEngine;
import io.shardingjdbc.core.util.SQLLogger;

import java.util.List;

/**
 * SQL router for hint database only.
 * 
 * @author zhangiang
 */
public final class DatabaseHintSQLRouter implements SQLRouter {
    
    private final ShardingRule shardingRule;
    
    private final boolean showSQL;
    
    public DatabaseHintSQLRouter(final ShardingContext shardingContext) {
        shardingRule = shardingContext.getShardingRule();
        showSQL = shardingContext.isShowSQL();
    }
    
    @Override
    public SQLStatement parse(final String logicSQL, final int parametersSize) {
        return new SQLJudgeEngine(logicSQL).judge();
    }
    
    @Override
    // TODO insert SQL need parse gen key
    public SQLRouteResult route(final String logicSQL, final List<Object> parameters, final SQLStatement sqlStatement) {
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        RoutingResult routingResult = new DatabaseHintRoutingEngine(shardingRule.getDataSourceMap(), (HintShardingStrategy) shardingRule.getDefaultDatabaseShardingStrategy()).route();
        for (TableUnit each : routingResult.getTableUnits().getTableUnits()) {
            result.getExecutionUnits().add(new SQLExecutionUnit(each.getDataSourceName(), logicSQL));
        }
        if (showSQL) {
            SQLLogger.logSQL(logicSQL, sqlStatement, result.getExecutionUnits(), parameters);
        }
        return result;
    }
}

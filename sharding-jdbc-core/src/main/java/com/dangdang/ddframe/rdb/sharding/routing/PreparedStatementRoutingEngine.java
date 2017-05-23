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

package com.dangdang.ddframe.rdb.sharding.routing;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.rewrite.GenerateKeysUtils;
import com.dangdang.ddframe.rdb.sharding.routing.router.SQLRouter;
import com.dangdang.ddframe.rdb.sharding.routing.router.SQLRouterFactory;

import java.util.List;

/**
 * 预解析的SQL路由器.
 * 
 * @author gaohongtao
 */
public final class PreparedStatementRoutingEngine {
    
    private final String logicSQL;
    
    private final SQLRouter sqlRouter;
    
    private final ShardingRule shardingRule;
    
    private SQLContext sqlContext;
    
    public PreparedStatementRoutingEngine(final String logicSQL, final ShardingContext shardingContext) {
        this.logicSQL = logicSQL;
        sqlRouter = SQLRouterFactory.createSQLRouter(shardingContext);
        shardingRule = shardingContext.getShardingRule();
    }
    
    /**
     * SQL路由.
     * 当第一次路由时进行SQL解析,之后的路由复用第一次的解析结果.
     * 
     * @param parameters SQL中的参数
     * @return 路由结果
     */
    public SQLRouteResult route(final List<Object> parameters) {
        if (null == sqlContext) {
            sqlContext = sqlRouter.parse(logicSQL, parameters);
        } else if (sqlContext instanceof InsertSQLContext) {
            parameters.addAll(GenerateKeysUtils.generateKeys(shardingRule, (InsertSQLContext) sqlContext));
        }
        return sqlRouter.route(logicSQL, parameters, sqlContext);
    }
}

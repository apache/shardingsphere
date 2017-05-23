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

import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.routing.router.SQLRouter;
import com.dangdang.ddframe.rdb.sharding.routing.router.SQLRouterFactory;

import java.util.Collections;

/**
 * SQL路由器.
 * 
 * @author zhangiang
 */
public final class StatementRoutingEngine {
    
    private final SQLRouter sqlRouter;
    
    public StatementRoutingEngine(final ShardingContext shardingContext) {
        sqlRouter = SQLRouterFactory.createSQLRouter(shardingContext);
    }
    
    /**
     * SQL路由.
     *
     * @param logicSQL 逻辑SQL
     * @return 路由结果
     */
    public SQLRouteResult route(final String logicSQL) {
        SQLContext sqlContext = sqlRouter.parse(logicSQL, Collections.emptyList());
        return sqlRouter.route(logicSQL, Collections.emptyList(), sqlContext);
    }
}

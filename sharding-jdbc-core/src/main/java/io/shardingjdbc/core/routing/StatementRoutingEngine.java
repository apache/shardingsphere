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

package io.shardingjdbc.core.routing;

import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.routing.router.SQLRouter;
import io.shardingjdbc.core.routing.router.SQLRouterFactory;

import java.util.Collections;

/**
 * Statement routing engine.
 * 
 * @author zhangiang
 */
public final class StatementRoutingEngine {
    
    private final SQLRouter sqlRouter;
    
    public StatementRoutingEngine(final ShardingContext shardingContext) {
        sqlRouter = SQLRouterFactory.createSQLRouter(shardingContext);
    }
    
    /**
     * SQL route.
     *
     * @param logicSQL logic SQL
     * @return route result
     */
    public SQLRouteResult route(final String logicSQL) {
        SQLStatement sqlStatement = sqlRouter.parse(logicSQL, 0);
        return sqlRouter.route(logicSQL, Collections.emptyList(), sqlStatement);
    }
}

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

import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 预解析功能的SQL路由器.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class PreparedSQLRouter {
    
    private final String logicSql;
    
    private final SQLRouteEngine engine;
    
    private SQLParsedResult sqlParsedResult;
    
    /**
     * 使用参数进行SQL路由.
     * 当第一次路由时进行SQL解析,之后的路由复用第一次的解析结果.
     * 
     * @param parameters SQL中的参数
     * @return 路由结果
     */
    public SQLRouteResult route(final List<Object> parameters) {
        if (null == sqlParsedResult) {
            sqlParsedResult = engine.parseSQL(logicSql, parameters);
        } else {
            for (ConditionContext each : sqlParsedResult.getConditionContexts()) {
                each.setNewConditionValue(parameters);
            }
        }
        return engine.routeSQL(sqlParsedResult, parameters);
    }
}


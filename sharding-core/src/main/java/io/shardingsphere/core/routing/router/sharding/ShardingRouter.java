/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.router.sharding;

import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.SQLRouteResult;

import java.util.List;

/**
 * Sharding router.
 * 
 * @author zhangliang
 */
public interface ShardingRouter {
    
    /**
     * Parse SQL.
     * 
     * @param logicSQL logic SQL
     * @param useCache use cache to save SQL parse result or not
     * @return parse result
     */
    SQLStatement parse(String logicSQL, boolean useCache);
    
    /**
     * Route SQL.
     * 
     * @param logicSQL logic SQL
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @return parse result
     */
    SQLRouteResult route(String logicSQL, List<Object> parameters, SQLStatement sqlStatement);
}
